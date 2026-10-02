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
import kotlinx.coroutines.flow.*
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

    fun openAntiProcrastinationDialog() {
        _showAntiProcrastinationDialog.value = true
    }

    fun closeAntiProcrastinationDialog() {
        _showAntiProcrastinationDialog.value = false
    }

    fun openBreathingExerciseDialog() {
        _showBreathingExerciseDialog.value = true
    }

    fun closeBreathingExerciseDialog() {
        _showBreathingExerciseDialog.value = false
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
