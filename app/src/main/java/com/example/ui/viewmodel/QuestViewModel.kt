package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.QuestRepository
import com.example.util.NotificationHelper
import com.example.util.QuestAlarmScheduler
import com.example.util.SoundEffectsHelper
import com.example.util.SystemAlarmHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class QuestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuestRepository
    private val vibrator: Vibrator?
    val soundHelper: SoundEffectsHelper

    init {
        val db = AppDatabase.getInstance(application)
        repository = QuestRepository(
            taskDao = db.taskDao(),
            questLevelDao = db.questLevelDao(),
            userProfileDao = db.userProfileDao(),
            customRewardDao = db.customRewardDao(),
            badgeDao = db.badgeDao()
        )
        soundHelper = SoundEffectsHelper(application)
        NotificationHelper.createNotificationChannels(application)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            refreshQuote()
            QuestAlarmScheduler.scheduleDailyReminder(application, 9, 0)
        }
    }

    val currentQuestTasks = repository.currentQuestTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val backlogTasks = repository.backlogTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val completedTasks = repository.completedTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val activeLevel = repository.activeLevel.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val userProfile = repository.userProfile.map { it ?: UserProfile() }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile()
    )

    val customRewards = repository.customRewards.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val badges = repository.badges.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val urgentTasks = repository.urgentDeadlineTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val staleTasks = repository.getStaleTasks().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // UI State for Celebrations and Modals
    private val _showLevelClearDialog = MutableStateFlow(false)
    val showLevelClearDialog: StateFlow<Boolean> = _showLevelClearDialog.asStateFlow()

    private val _clearedLevel = MutableStateFlow<QuestLevel?>(null)
    val clearedLevel: StateFlow<QuestLevel?> = _clearedLevel.asStateFlow()

    private val _showConfetti = MutableStateFlow(false)
    val showConfetti: StateFlow<Boolean> = _showConfetti.asStateFlow()

    private val _showAntiProcrastinationDialog = MutableStateFlow(false)
    val showAntiProcrastinationDialog: StateFlow<Boolean> = _showAntiProcrastinationDialog.asStateFlow()

    private val _showBreathingExerciseDialog = MutableStateFlow(false)
    val showBreathingExerciseDialog: StateFlow<Boolean> = _showBreathingExerciseDialog.asStateFlow()

    private val _currentQuote = MutableStateFlow(
        MotivationalEngine.getContextualQuote(MoodType.FOCUS)
    )
    val currentQuote: StateFlow<MotivationalMessage> = _currentQuote.asStateFlow()

    val searchQuery = MutableStateFlow("")
    val selectedPriorityFilter = MutableStateFlow<Priority?>(null)

    // Persistent Pomodoro Focus Timer State
    private val _timerState = MutableStateFlow(FocusTimerUiState())
    val timerState: StateFlow<FocusTimerUiState> = _timerState.asStateFlow()
    private var timerJob: Job? = null

    // Demon Mentor Mode & Devil's Pact States
    val showDevilsPactDialog = MutableStateFlow(false)
    val showCauldronDialog = MutableStateFlow(false)
    val pactRemainingSeconds = MutableStateFlow(0)
    val demonMascotRoast = MutableStateFlow("")
    private var pactJob: Job? = null

    init {
        // Monitor profile for active pact countdown
        viewModelScope.launch {
            userProfile.collect { profile ->
                if (profile.devilPactActive && profile.devilPactEndTime > System.currentTimeMillis()) {
                    val remaining = ((profile.devilPactEndTime - System.currentTimeMillis()) / 1000).toInt()
                    pactRemainingSeconds.value = remaining.coerceAtLeast(0)
                    startPactTimerLoop(profile.devilPactEndTime)
                }
            }
        }
    }

    fun refreshQuote() {
        val mood = userProfile.value.activeMood
        _currentQuote.value = MotivationalEngine.getContextualQuote(mood)
    }

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch {
            triggerHaptic(HapticType.LIGHT)
            if (!task.isCompleted) {
                QuestAlarmScheduler.cancelTaskAlarm(getApplication(), task.id)
            }
            val allCleared = repository.toggleTaskCompleted(task)

            // Recurrence support: if task was completed and has recurrence, schedule next occurrence
            if (!task.isCompleted && task.recurrence != RecurrenceRule.NONE && task.dueDate != null) {
                val nextDue = calculateNextDueDate(task.dueDate, task.recurrence)
                val recurringTask = task.copy(
                    id = 0,
                    isCompleted = false,
                    completedAt = null,
                    dueDate = nextDue,
                    createdAt = System.currentTimeMillis()
                )
                val newId = repository.insertTask(recurringTask)
                QuestAlarmScheduler.scheduleTaskAlarm(getApplication(), recurringTask.copy(id = newId))
            }

            if (allCleared) {
                val currentLevel = activeLevel.value
                if (currentLevel != null && !currentLevel.isCompleted) {
                    triggerHaptic(HapticType.VICTORY)
                    if (userProfile.value.soundEffectsEnabled) {
                        soundHelper.playVictoryChime()
                    }
                    _clearedLevel.value = currentLevel
                    _showConfetti.value = true
                    _showLevelClearDialog.value = true
                }
            }
        }
    }

    private fun calculateNextDueDate(currentDueDate: Long, recurrence: RecurrenceRule): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = currentDueDate }
        when (recurrence) {
            RecurrenceRule.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            RecurrenceRule.WEEKDAYS -> {
                do {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                } while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            }
            RecurrenceRule.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            RecurrenceRule.NONE -> {}
        }
        return cal.timeInMillis
    }

    fun claimLevelReward(chosenReward: String) {
        viewModelScope.launch {
            val level = _clearedLevel.value ?: activeLevel.value
            if (level != null) {
                repository.completeActiveQuest(level, chosenReward)
                _showLevelClearDialog.value = false
                _showConfetti.value = false
                repository.startNextQuest()
            }
        }
    }

    fun dismissLevelClearDialog() {
        _showLevelClearDialog.value = false
        _showConfetti.value = false
        viewModelScope.launch {
            repository.startNextQuest()
        }
    }

    fun shuffleCurrentQuest() {
        viewModelScope.launch {
            triggerHaptic(HapticType.MEDIUM)
            repository.shuffleCurrentQuest()
        }
    }

    fun toggleSubtask(task: TaskItem, index: Int) {
        viewModelScope.launch {
            triggerHaptic(HapticType.LIGHT)
            repository.toggleSubtask(task, index)
        }
    }

    fun addSubtask(task: TaskItem, title: String) {
        viewModelScope.launch {
            repository.addSubtask(task, title)
        }
    }

    fun saveTask(task: TaskItem) {
        viewModelScope.launch {
            val savedId = if (task.id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
                task.id
            }
            val taskWithId = task.copy(id = savedId)
            if (taskWithId.dueDate != null && !taskWithId.isCompleted) {
                QuestAlarmScheduler.scheduleTaskAlarm(getApplication(), taskWithId)
            } else {
                QuestAlarmScheduler.cancelTaskAlarm(getApplication(), savedId)
            }
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            QuestAlarmScheduler.cancelTaskAlarm(getApplication(), task.id)
            repository.deleteTask(task)
        }
    }

    fun bulkImportTasks(linesText: String, category: String = "Импорт") {
        viewModelScope.launch {
            repository.bulkImportTasks(linesText, category)
        }
    }

    fun redeemReward(reward: CustomReward, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.redeemCustomReward(reward)
            if (success) {
                triggerHaptic(HapticType.VICTORY)
            }
            onResult(success)
        }
    }

    fun unlockArtifact(artifact: HeroArtifact, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.unlockArtifact(artifact.id, artifact.costCoins)
            if (success) {
                triggerHaptic(HapticType.VICTORY)
                if (userProfile.value.soundEffectsEnabled) {
                    soundHelper.playVictoryChime()
                }
            }
            onResult(success)
        }
    }

    fun equipArtifact(artifact: HeroArtifact) {
        viewModelScope.launch {
            triggerHaptic(HapticType.MEDIUM)
            repository.equipArtifact(artifact.id)
        }
    }

    fun addReward(reward: CustomReward) {
        viewModelScope.launch {
            repository.addCustomReward(reward)
        }
    }

    fun deleteReward(reward: CustomReward) {
        viewModelScope.launch {
            repository.deleteCustomReward(reward)
        }
    }

    fun setEnergyLevel(level: Int) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(currentEnergyLevel = level.coerceIn(1, 3))
            repository.updateProfile(updated)
            triggerHaptic(HapticType.LIGHT)
        }
    }

    fun completeEveningCheckout(reflection: String) {
        viewModelScope.launch {
            repository.performEveningCheckout(reflection)
            triggerHaptic(HapticType.VICTORY)
            if (userProfile.value.soundEffectsEnabled) {
                soundHelper.playVictoryChime()
            }
        }
    }

    fun decomposeTask(task: TaskItem) {
        viewModelScope.launch {
            triggerHaptic(HapticType.MEDIUM)
            val subtasks = task.getSubtasksList().toMutableList()
            if (subtasks.isEmpty()) {
                subtasks.add(SubTask("Шаг 1: Подготовить материалы (2 мин)", false))
                subtasks.add(SubTask("Шаг 2: Главное действие без отвлечений (10 мин)", false))
                subtasks.add(SubTask("Шаг 3: Проверить и зафиксировать результат (3 мин)", false))
            } else {
                subtasks.add(SubTask("Микро-шаг: Финальное завершение (5 мин)", false))
            }
            val updated = task.copy(subtasksRaw = TaskItem.serializeSubtasks(subtasks))
            repository.updateTask(updated)
        }
    }

    fun startBossLevel(title: String, steps: List<String>) {
        viewModelScope.launch {
            triggerHaptic(HapticType.VICTORY)
            val subtasks = steps.map { SubTask(it, false) }
            val bossTask = TaskItem(
                title = "🐉 БОСС: $title",
                description = "Эпическая цель разбита на этапы. Победите босса шаг за шагом!",
                category = "Босс",
                priority = Priority.CRITICAL,
                energyRequired = 3,
                inCurrentQuest = true,
                subtasksRaw = TaskItem.serializeSubtasks(subtasks),
                estimatedMinutes = (steps.size * 15).coerceAtLeast(25)
            )
            val id = repository.insertTask(bossTask)
            if (bossTask.dueDate != null) {
                QuestAlarmScheduler.scheduleTaskAlarm(getApplication(), bossTask.copy(id = id))
            }
        }
    }

    fun setMood(mood: MoodType) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(activeMood = mood)
            repository.updateProfile(updated)
            _currentQuote.value = MotivationalEngine.getContextualQuote(mood)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(themeMode = mode)
            repository.updateProfile(updated)
        }
    }

    fun setTasksPerQuest(count: Int) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(tasksPerQuest = count.coerceIn(1, 5))
            repository.updateProfile(updated)
        }
    }

    fun setAppIconStyle(style: AppIconStyle) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(appIconStyle = style)
            repository.updateProfile(updated)
        }
    }

    fun setNotificationTone(tone: NotificationTone) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(notificationTone = tone)
            repository.updateProfile(updated)
        }
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(soundEffectsEnabled = enabled)
            repository.updateProfile(updated)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(hapticsEnabled = enabled)
            repository.updateProfile(updated)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(notificationsEnabled = enabled)
            repository.updateProfile(updated)
        }
    }

    fun setDailyReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(dailyReminderHour = hour, dailyReminderMinute = minute)
            repository.updateProfile(updated)
            QuestAlarmScheduler.scheduleDailyReminder(getApplication(), hour, minute)
        }
    }

    fun triggerTestNotification() {
        NotificationHelper.showTestNotification(getApplication())
        if (userProfile.value.soundEffectsEnabled) {
            soundHelper.playAlarmAlert()
        }
        triggerHaptic(HapticType.VICTORY)
    }

    fun triggerTestAlarm(delaySeconds: Int = 10) {
        QuestAlarmScheduler.scheduleTestAlarm(getApplication(), delaySeconds)
    }

    fun logFocusSession(minutes: Int) {
        viewModelScope.launch {
            triggerHaptic(HapticType.VICTORY)
            repository.logFocusMinutes(minutes)
        }
    }

    // ==========================================
    // POMODORO TIMER METHODS (Screen-Independent)
    // ==========================================

    fun startTimer() {
        val current = _timerState.value
        if (current.isRunning) return

        val durationSeconds = if (current.timeLeftSeconds > 0) current.timeLeftSeconds else current.totalTimeMinutes * 60
        val targetEnd = System.currentTimeMillis() + (durationSeconds * 1000L)

        _timerState.value = current.copy(
            isRunning = true,
            timeLeftSeconds = durationSeconds,
            targetEndTimeMs = targetEnd
        )

        // Schedule backup AlarmManager alarm in case system pauses/backgrounds the app
        QuestAlarmScheduler.scheduleFocusTimerAlarm(
            getApplication(),
            durationSeconds,
            current.spotlightTaskTitle
        )

        // Start ambient sound if enabled and not muted
        if (!current.isMuted && current.activeSound != "Тишина 🤫") {
            soundHelper.startAmbientAudio(current.activeSound)
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(400)
                val snapshot = _timerState.value
                if (!snapshot.isRunning) break

                val remainingMs = snapshot.targetEndTimeMs - System.currentTimeMillis()
                val newSeconds = ((remainingMs + 999) / 1000).toInt().coerceAtLeast(0)

                if (newSeconds <= 0) {
                    // Timer finished successfully!
                    val sessionMinutes = snapshot.totalTimeMinutes
                    _timerState.value = snapshot.copy(
                        timeLeftSeconds = 0,
                        isRunning = false,
                        showCompletedDialog = true,
                        completedMinutes = sessionMinutes
                    )
                    soundHelper.stopAmbientAudio()
                    soundHelper.playVictoryChime()
                    soundHelper.triggerVibration("VICTORY")
                    NotificationHelper.showTimerCompleted(
                        getApplication(),
                        sessionMinutes,
                        snapshot.spotlightTaskTitle
                    )
                    logFocusSession(sessionMinutes)
                    QuestAlarmScheduler.cancelFocusTimerAlarm(getApplication())
                    break
                } else {
                    _timerState.value = snapshot.copy(timeLeftSeconds = newSeconds)
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        val current = _timerState.value
        _timerState.value = current.copy(isRunning = false)
        soundHelper.stopAmbientAudio()
        QuestAlarmScheduler.cancelFocusTimerAlarm(getApplication())
    }

    fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        val current = _timerState.value
        _timerState.value = current.copy(
            isRunning = false,
            timeLeftSeconds = current.totalTimeMinutes * 60,
            targetEndTimeMs = 0L
        )
        soundHelper.stopAmbientAudio()
        QuestAlarmScheduler.cancelFocusTimerAlarm(getApplication())
    }

    fun setTimerDuration(minutes: Int) {
        val current = _timerState.value
        if (!current.isRunning) {
            _timerState.value = current.copy(
                totalTimeMinutes = minutes,
                timeLeftSeconds = minutes * 60
            )
        }
    }

    fun setSpotlightTask(task: TaskItem?) {
        val current = _timerState.value
        val estMinutes = task?.estimatedMinutes
        val updatedMinutes = if (estMinutes != null && !current.isRunning) estMinutes else current.totalTimeMinutes
        _timerState.value = current.copy(
            spotlightTaskId = task?.id,
            spotlightTaskTitle = task?.title,
            totalTimeMinutes = updatedMinutes,
            timeLeftSeconds = if (!current.isRunning) updatedMinutes * 60 else current.timeLeftSeconds
        )
    }

    fun setActiveSound(sound: String) {
        val current = _timerState.value
        _timerState.value = current.copy(activeSound = sound)
        if (current.isRunning && !current.isMuted) {
            if (sound != "Тишина 🤫") {
                soundHelper.startAmbientAudio(sound)
            } else {
                soundHelper.stopAmbientAudio()
            }
        }
    }

    fun toggleTimerMute() {
        val current = _timerState.value
        val newMuted = !current.isMuted
        _timerState.value = current.copy(isMuted = newMuted)
        if (newMuted) {
            soundHelper.stopAmbientAudio()
        } else if (current.isRunning && current.activeSound != "Тишина 🤫") {
            soundHelper.startAmbientAudio(current.activeSound)
        }
    }

    fun dismissTimerCompletedDialog() {
        val current = _timerState.value
        _timerState.value = current.copy(
            showCompletedDialog = false,
            timeLeftSeconds = current.totalTimeMinutes * 60
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundHelper.stopAmbientAudio()
    }

    fun openAntiProcrastinationDialog() {
        _showAntiProcrastinationDialog.value = true
    }

    fun closeAntiProcrastinationDialog() {
        _showAntiProcrastinationDialog.value = false
    }

    fun claimSabotageVictory() {
        viewModelScope.launch {
            triggerHaptic(HapticType.VICTORY)
            if (userProfile.value.soundEffectsEnabled) {
                soundHelper.playVictoryChime()
            }
            val current = userProfile.value
            val updated = current.copy(
                xp = current.xp + 25,
                coins = current.coins + 5
            )
            repository.updateProfile(updated)
            _showConfetti.value = true
        }
    }

    fun openBreathingExerciseDialog() {
        _showBreathingExerciseDialog.value = true
    }

    fun closeBreathingExerciseDialog() {
        _showBreathingExerciseDialog.value = false
    }

    // ==========================================
    // DEMON MENTOR & DEVIL'S PACT LOGIC
    // ==========================================

    fun toggleDemonMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                isDemonMode = enabled,
                activeMood = if (enabled) MoodType.DEMON else MoodType.FOCUS,
                themeMode = if (enabled) ThemeMode.INFERNAL else ThemeMode.SYSTEM,
                notificationTone = if (enabled) NotificationTone.DEMON else NotificationTone.CARING,
                appIconStyle = if (enabled) AppIconStyle.DEMON else AppIconStyle.SHIELD
            )
            repository.updateProfile(updated)
            if (enabled) {
                soundHelper.playDemonLaugh()
                soundHelper.triggerVibration("DEMON")
                _currentQuote.value = DemonMentorEngine.getDemonContextualQuote()
                demonMascotRoast.value = DemonMentorEngine.getRandomRoast()
            } else {
                _currentQuote.value = MotivationalEngine.getContextualQuote(MoodType.FOCUS)
            }
        }
    }

    fun tapDemonMascot() {
        val roast = DemonMentorEngine.getRandomMascotTapRoast()
        demonMascotRoast.value = roast
        soundHelper.playDemonLaugh()
        soundHelper.triggerVibration("DEMON")
    }

    fun openDevilsPactDialog() {
        showDevilsPactDialog.value = true
    }

    fun closeDevilsPactDialog() {
        showDevilsPactDialog.value = false
    }

    fun openCauldronDialog() {
        showCauldronDialog.value = true
    }

    fun closeCauldronDialog() {
        showCauldronDialog.value = false
    }

    fun startDevilsPact(taskId: Long, durationMinutes: Int) {
        viewModelScope.launch {
            val all = (currentQuestTasks.value + backlogTasks.value).distinctBy { it.id }
            val task = all.find { it.id == taskId } ?: return@launch
            val endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)

            val updated = userProfile.value.copy(
                devilPactActive = true,
                devilPactTaskId = taskId,
                devilPactTaskTitle = task.title,
                devilPactDurationMinutes = durationMinutes,
                devilPactEndTime = endTime
            )
            repository.updateProfile(updated)
            showDevilsPactDialog.value = false
            soundHelper.playHellBurst()
            soundHelper.triggerVibration("DEMON")
            startPactTimerLoop(endTime)
        }
    }

    private fun startPactTimerLoop(targetEndTime: Long) {
        pactJob?.cancel()
        pactJob = viewModelScope.launch {
            while (isActive) {
                val remainingMs = targetEndTime - System.currentTimeMillis()
                val remainingSec = ((remainingMs + 999) / 1000).toInt().coerceAtLeast(0)
                pactRemainingSeconds.value = remainingSec

                if (remainingSec <= 0) {
                    // Pact expired! User failed to complete task in time!
                    val current = userProfile.value
                    if (current.devilPactActive) {
                        val penaltyCoins = (current.coins - 15).coerceAtLeast(0)
                        val updated = current.copy(
                            devilPactActive = false,
                            devilPactTaskId = 0L,
                            devilPactTaskTitle = "",
                            devilPactEndTime = 0L,
                            devilPactFailures = current.devilPactFailures + 1,
                            coins = penaltyCoins
                        )
                        repository.updateProfile(updated)
                        soundHelper.playPactLost()
                    }
                    break
                }
                delay(1000)
            }
        }
    }

    fun completeDevilsPactSuccess() {
        viewModelScope.launch {
            val current = userProfile.value
            pactJob?.cancel()
            pactJob = null

            val taskId = current.devilPactTaskId
            val all = (currentQuestTasks.value + backlogTasks.value).distinctBy { it.id }
            val task = all.find { it.id == taskId }
            if (task != null && !task.isCompleted) {
                repository.toggleTaskCompleted(task)
            }

            // Grant Double XP and Gold (+100% bonus: +60 XP, +25 coins)
            val updated = current.copy(
                devilPactActive = false,
                devilPactTaskId = 0L,
                devilPactTaskTitle = "",
                devilPactEndTime = 0L,
                devilPactSuccesses = current.devilPactSuccesses + 1,
                xp = current.xp + 60,
                coins = current.coins + 25
            )
            repository.updateProfile(updated)
            soundHelper.playPactWon()
            _showConfetti.value = true
        }
    }

    fun cancelDevilsPact() {
        viewModelScope.launch {
            pactJob?.cancel()
            pactJob = null
            val current = userProfile.value
            val penaltyCoins = (current.coins - 15).coerceAtLeast(0)
            val updated = current.copy(
                devilPactActive = false,
                devilPactTaskId = 0L,
                devilPactTaskTitle = "",
                devilPactEndTime = 0L,
                devilPactFailures = current.devilPactFailures + 1,
                coins = penaltyCoins
            )
            repository.updateProfile(updated)
            soundHelper.playPactLost()
        }
    }

    fun burnCauldronSins() {
        viewModelScope.launch {
            val current = userProfile.value
            val overdueCount = urgentTasks.value.size
            val sinsToAdd = if (overdueCount > 0) overdueCount else 1
            val updated = current.copy(
                cauldronSinsBurned = current.cauldronSinsBurned + sinsToAdd,
                xp = current.xp + (sinsToAdd * 15)
            )
            repository.updateProfile(updated)
            showCauldronDialog.value = false
            soundHelper.playHellBurst()
            _showConfetti.value = true
        }
    }

    private fun triggerHaptic(type: HapticType) {
        if (!userProfile.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    HapticType.LIGHT -> VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.MEDIUM -> VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.VICTORY -> VibrationEffect.createWaveform(longArrayOf(0, 80, 50, 150), -1)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (e: Exception) {
            // Ignore if vibration fails
        }
    }
}

enum class HapticType {
    LIGHT, MEDIUM, VICTORY
}
