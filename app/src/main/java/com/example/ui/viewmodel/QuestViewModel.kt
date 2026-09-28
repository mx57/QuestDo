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
import com.example.util.DataExportImportEngine
import com.example.util.LocalAiEngine
import com.example.util.LocalNlpParser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuestRepository
    private val vibrator: Vibrator?

    init {
        val db = AppDatabase.getInstance(application)
        repository = QuestRepository(
            taskDao = db.taskDao(),
            questLevelDao = db.questLevelDao(),
            userProfileDao = db.userProfileDao(),
            customRewardDao = db.customRewardDao(),
            badgeDao = db.badgeDao()
        )

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
        }
    }

    val currentQuestTasks = repository.currentQuestTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val backlogTasks = repository.backlogTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val urgentDeadlineTasks = repository.urgentDeadlineTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val staleTasks = repository.getStaleTasks(90).stateIn(
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

    private val _showEveningCheckoutDialog = MutableStateFlow(false)
    val showEveningCheckoutDialog: StateFlow<Boolean> = _showEveningCheckoutDialog.asStateFlow()

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
            val allCleared = repository.toggleTaskCompleted(task)
            if (allCleared) {
                val currentLevel = activeLevel.value
                if (currentLevel != null && !currentLevel.isCompleted) {
                    triggerHaptic(HapticType.VICTORY)
                    _clearedLevel.value = currentLevel
                    _showConfetti.value = true
                    _showLevelClearDialog.value = true
                }
            }
        }
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

    fun setEnergyLevel(energy: Int) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(currentEnergyLevel = energy.coerceIn(1, 3))
            repository.updateProfile(updated)
        }
    }

    fun completeOnboarding(tasksPerQuest: Int) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(
                hasCompletedOnboarding = true,
                tasksPerQuest = tasksPerQuest.coerceIn(1, 5)
            )
            repository.updateProfile(updated)
        }
    }

    fun addNlpTask(rawInput: String) {
        viewModelScope.launch {
            val parsed = LocalNlpParser.parseInput(rawInput)
            val newTask = TaskItem(
                title = parsed.title,
                priority = parsed.priority,
                dueDate = parsed.dueDate,
                isStrictDeadline = parsed.isStrictDeadline
            )
            repository.insertTask(newTask)
        }
    }

    fun decomposeTaskWithAi(task: TaskItem) {
        viewModelScope.launch {
            val steps = LocalAiEngine.decomposeTask(task.title)
            val existing = task.getSubtasksList()
            val newSubtasks = steps.map { SubTask(it, false) }
            val combined = TaskItem.serializeSubtasks(existing + newSubtasks)
            repository.updateTask(task.copy(subtasksRaw = combined))
        }
    }

    fun startBossLevel(goalTitle: String, steps: List<String>) {
        viewModelScope.launch {
            repository.startBossLevel(goalTitle, steps)
        }
    }

    fun openEveningCheckout() {
        _showEveningCheckoutDialog.value = true
    }

    fun closeEveningCheckout() {
        _showEveningCheckoutDialog.value = false
    }

    fun performEveningCheckout(reflection: String) {
        viewModelScope.launch {
            triggerHaptic(HapticType.VICTORY)
            repository.performEveningCheckout(reflection)
            _showEveningCheckoutDialog.value = false
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
            if (task.id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
            }
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun bulkImportTasks(linesText: String, category: String = "Импорт") {
        viewModelScope.launch {
            repository.bulkImportTasks(linesText, category)
        }
    }

    fun exportToJson(): String {
        return DataExportImportEngine.exportToJson(userProfile.value, currentQuestTasks.value + backlogTasks.value)
    }

    fun exportToCsv(): String {
        return DataExportImportEngine.exportToCsv(currentQuestTasks.value + backlogTasks.value)
    }

    fun importFromJson(jsonStr: String) {
        viewModelScope.launch {
            val tasks = DataExportImportEngine.parseJsonTasks(jsonStr)
            for (t in tasks) {
                repository.insertTask(t)
            }
        }
    }

    fun importFromCsv(csvStr: String) {
        viewModelScope.launch {
            val tasks = DataExportImportEngine.parseCsvTasks(csvStr)
            for (t in tasks) {
                repository.insertTask(t)
            }
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

    fun toggleShowHorizon(show: Boolean) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(showHorizon = show)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
