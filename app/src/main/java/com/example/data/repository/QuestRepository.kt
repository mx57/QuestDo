package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class QuestRepository(
    private val taskDao: TaskDao,
    private val questLevelDao: QuestLevelDao,
    private val userProfileDao: UserProfileDao,
    private val customRewardDao: CustomRewardDao,
    private val badgeDao: BadgeDao
) {
    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()
    val currentQuestTasks: Flow<List<TaskItem>> = taskDao.getCurrentQuestTasks()
    val backlogTasks: Flow<List<TaskItem>> = taskDao.getBacklogTasks()
    val completedTasks: Flow<List<TaskItem>> = taskDao.getCompletedTasks()
    val activeLevel: Flow<QuestLevel?> = questLevelDao.getActiveLevel()
    val completedLevelsCount: Flow<Int> = questLevelDao.getCompletedLevelsCount()
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()
    val customRewards: Flow<List<CustomReward>> = customRewardDao.getAllRewards()
    val badges: Flow<List<BadgeAchievement>> = badgeDao.getAllBadges()

    suspend fun initializeDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        val existingProfile = userProfileDao.getUserProfile().firstOrNull()
        if (existingProfile == null) {
            val defaultProfile = UserProfile(
                id = 1,
                heroName = "Искатель Задач",
                heroTitle = "Новичок фокуса",
                level = 1,
                xp = 0,
                coins = 50,
                streakDays = 1,
                bestStreak = 1,
                lastActiveDate = getTodayDateString(),
                tasksPerQuest = 3,
                activeMood = MoodType.FOCUS,
                themeMode = ThemeMode.SYSTEM,
                appIconStyle = AppIconStyle.SHIELD,
                notificationTone = NotificationTone.CARING,
                soundEffectsEnabled = true,
                hapticsEnabled = true
            )
            userProfileDao.insertOrUpdateProfile(defaultProfile)
        }

        // Initialize default badges if not present
        val existingBadges = badgeDao.getAllBadges().firstOrNull()
        if (existingBadges.isNullOrEmpty()) {
            val defaultBadges = listOf(
                BadgeAchievement("FIRST_QUEST", "Первый рубеж", "Завершите свой первый микро-квест", "🛡️"),
                BadgeAchievement("STREAK_3", "Пламя дисциплины", "Поддерживайте серию 3 дня подряд", "🔥"),
                BadgeAchievement("STREAK_7", "Непоколебимый", "Поддерживайте серию 7 дней подряд", "⚡"),
                BadgeAchievement("FOCUS_50", "Мастер потока", "Проведите 50 минут в фокус-таймере", "⏳"),
                BadgeAchievement("CENTURION", "Сотня подвигов", "Выполните 100 задач из бэклога", "👑"),
                BadgeAchievement("SELF_CARE", "В гармонии с собой", "Активируйте режим заботы и выполните квест", "🌸"),
                BadgeAchievement("NIGHT_OWL", "Вечерний стратег", "Завершите квест в вечернее время", "🦉")
            )
            badgeDao.insertAllBadges(defaultBadges)
        }

        // Initialize default custom rewards
        val existingRewards = customRewardDao.getAllRewards().firstOrNull()
        if (existingRewards.isNullOrEmpty()) {
            val defaultRewards = listOf(
                CustomReward(title = "Чашка кофе с круассаном", costCoins = 30, iconEmoji = "☕", description = "Вкусная пауза в уютном кафе"),
                CustomReward(title = "Серия любимого сериала", costCoins = 40, iconEmoji = "🎬", description = "45 минут без угрызений совести"),
                CustomReward(title = "15 минут прогулки на свежем воздухе", costCoins = 20, iconEmoji = "🌳", description = "Проветрить голову без смартфона"),
                CustomReward(title = "Поиграть в любимую игру", costCoins = 60, iconEmoji = "🎮", description = "1 час в хорошей игре"),
                CustomReward(title = "Купить желанную книгу", costCoins = 150, iconEmoji = "📚", description = "Инвестиция в знания и вдохновение")
            )
            for (r in defaultRewards) {
                customRewardDao.insertReward(r)
            }
        }

        // Check if there are tasks. If empty, seed rich sample tasks and form Quest #1!
        val currentTasks = taskDao.getAllTasks().firstOrNull()
        if (currentTasks.isNullOrEmpty()) {
            val sampleTasks = listOf(
                TaskItem(
                    title = "Убрать рабочий стол и организовать пространство",
                    description = "Чистый стол — чистая голова. Освободи пространство перед штурмом.",
                    category = "Фокус",
                    priority = Priority.CRITICAL,
                    inCurrentQuest = true,
                    subtasksRaw = "Выбросить бумажки|0;Протереть экран и пыль|0;Налить стакан воды|0"
                ),
                TaskItem(
                    title = "Выбрать 1 ключевую задачу на день",
                    description = "Сфокусируйся на самом важном деле, которое сдвинет проект вперед.",
                    category = "Работа",
                    priority = Priority.CRITICAL,
                    inCurrentQuest = true,
                    subtasksRaw = "Открыть проект|0;Сформулировать первый шаг|0"
                ),
                TaskItem(
                    title = "Сделать 5 глубоких вдохов перед стартом",
                    description = "Психологическая настройка: вдох 4 сек, задержка 4 сек, выдох 4 сек.",
                    category = "Здоровье",
                    priority = Priority.HIGH,
                    inCurrentQuest = true,
                    subtasksRaw = "Закрыть глаза|0;Сделать цикл дыхания|0"
                ),
                // Backlog tasks (waiting for next levels)
                TaskItem(
                    title = "Разобрать входящие письма и сообщения",
                    description = "Ответить на срочные, архивировать спам",
                    category = "Работа",
                    priority = Priority.HIGH,
                    inCurrentQuest = false
                ),
                TaskItem(
                    title = "20-минутная разминка или прогулка",
                    description = "Размять спину и плечи",
                    category = "Здоровье",
                    priority = Priority.HIGH,
                    inCurrentQuest = false
                ),
                TaskItem(
                    title = "Прочитать 1 главу полезной книги",
                    description = "Без отвлечений на уведомления",
                    category = "Развитие",
                    priority = Priority.MEDIUM,
                    inCurrentQuest = false
                ),
                TaskItem(
                    title = "Составить меню и список покупок на неделю",
                    description = "Сбережет время и силы на принятие решений",
                    category = "Дом",
                    priority = Priority.LOW,
                    inCurrentQuest = false
                ),
                TaskItem(
                    title = "Запланировать бюджет на следующий месяц",
                    description = "Подвести итоги доходов и обязательных платежей",
                    category = "Финансы",
                    priority = Priority.HIGH,
                    inCurrentQuest = false
                )
            )
            taskDao.insertAllTasks(sampleTasks)

            val initialLevel = QuestLevel(
                levelNumber = 1,
                title = "Уровень 1: Инициация и Первые Шаги",
                startedAt = System.currentTimeMillis(),
                xpEarned = 120,
                coinsEarned = 35
            )
            questLevelDao.insertLevel(initialLevel)
        }
    }

    suspend fun insertTask(task: TaskItem): Long = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskItem) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskItem) = withContext(Dispatchers.IO) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Long) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskById(id)
    }

    suspend fun toggleSubtask(task: TaskItem, subtaskIndex: Int) = withContext(Dispatchers.IO) {
        val subtasks = task.getSubtasksList().toMutableList()
        if (subtaskIndex in subtasks.indices) {
            val old = subtasks[subtaskIndex]
            subtasks[subtaskIndex] = old.copy(isDone = !old.isDone)
            val updatedRaw = TaskItem.serializeSubtasks(subtasks)
            taskDao.updateTask(task.copy(subtasksRaw = updatedRaw))
        }
    }

    suspend fun addSubtask(task: TaskItem, subtaskTitle: String) = withContext(Dispatchers.IO) {
        if (subtaskTitle.isBlank()) return@withContext
        val subtasks = task.getSubtasksList().toMutableList()
        subtasks.add(SubTask(subtaskTitle.trim(), false))
        taskDao.updateTask(task.copy(subtasksRaw = TaskItem.serializeSubtasks(subtasks)))
    }

    suspend fun toggleTaskCompleted(task: TaskItem): Boolean = withContext(Dispatchers.IO) {
        val newStatus = !task.isCompleted
        val completedAt = if (newStatus) System.currentTimeMillis() else null
        val updated = task.copy(isCompleted = newStatus, completedAt = completedAt)
        taskDao.updateTask(updated)

        if (newStatus) {
            // Update stats
            val profile = userProfileDao.getUserProfile().firstOrNull() ?: UserProfile()
            val updatedProfile = profile.copy(
                totalTasksCompleted = profile.totalTasksCompleted + 1,
                xp = profile.xp + 25,
                coins = profile.coins + 5
            )
            updateProfileAndCheckLevelUp(updatedProfile)

            // Check if current quest is now fully cleared
            val currentQuestTasks = taskDao.getCurrentQuestTasks().firstOrNull() ?: emptyList()
            val allCleared = currentQuestTasks.isNotEmpty() && currentQuestTasks.all { it.isCompleted }
            return@withContext allCleared
        }
        return@withContext false
    }

    suspend fun completeActiveQuest(level: QuestLevel, chosenReward: String? = null): QuestLevel = withContext(Dispatchers.IO) {
        val completed = level.copy(
            isCompleted = true,
            completedAt = System.currentTimeMillis(),
            rewardClaimed = true,
            rewardTitle = chosenReward ?: "Победа над уровнем!"
        )
        questLevelDao.updateLevel(completed)

        // Award XP and Coins
        val profile = userProfileDao.getUserProfile().firstOrNull() ?: UserProfile()
        val today = getTodayDateString()
        val isConsecutive = isConsecutiveDay(profile.lastActiveDate, today)
        val newStreak = if (profile.lastActiveDate == today) {
            profile.streakDays
        } else if (isConsecutive) {
            profile.streakDays + 1
        } else {
            1
        }
        val bestStreak = maxOf(profile.bestStreak, newStreak)

        val updatedProfile = profile.copy(
            xp = profile.xp + level.xpEarned,
            coins = profile.coins + level.coinsEarned,
            totalQuestsCompleted = profile.totalQuestsCompleted + 1,
            streakDays = newStreak,
            bestStreak = bestStreak,
            lastActiveDate = today
        )
        updateProfileAndCheckLevelUp(updatedProfile)

        // Check badges
        checkBadges(updatedProfile)

        return@withContext completed
    }

    suspend fun startNextQuest(): QuestLevel = withContext(Dispatchers.IO) {
        taskDao.clearCurrentQuest()

        val profile = userProfileDao.getUserProfile().firstOrNull() ?: UserProfile()
        val targetCount = profile.tasksPerQuest.coerceIn(1, 5)

        val backlog = taskDao.getBacklogTasks().firstOrNull() ?: emptyList()
        val nextTasks = backlog.take(targetCount)

        if (nextTasks.isNotEmpty()) {
            taskDao.markTasksInCurrentQuest(nextTasks.map { it.id })
        }

        val completedCount = questLevelDao.getCompletedLevelsCount().firstOrNull() ?: 0
        val nextNumber = completedCount + 1
        val levelNames = listOf(
            "Врата Концентрации",
            "Очищение Разума",
            "Штурм Приоритетов",
            "Оазис Продуктивности",
            "Бастион Силы Воли",
            "Цитадель Дисциплины",
            "Тропа Мастера Потока",
            "Обитель Спокойствия",
            "Пик Достижений"
        )
        val nameIndex = (nextNumber - 1) % levelNames.size
        val newLevel = QuestLevel(
            levelNumber = nextNumber,
            title = "Уровень $nextNumber: ${levelNames[nameIndex]}",
            startedAt = System.currentTimeMillis(),
            xpEarned = 100 + nextNumber * 10,
            coinsEarned = 25 + nextNumber * 2
        )
        questLevelDao.insertLevel(newLevel)
        return@withContext newLevel
    }

    suspend fun shuffleCurrentQuest() = withContext(Dispatchers.IO) {
        // Return uncompleted current quest tasks to backlog, then pick different ones
        val currentTasks = taskDao.getCurrentQuestTasks().firstOrNull() ?: emptyList()
        val uncompleted = currentTasks.filter { !it.isCompleted }
        taskDao.clearCurrentQuest()

        val profile = userProfileDao.getUserProfile().firstOrNull() ?: UserProfile()
        val targetCount = profile.tasksPerQuest.coerceIn(1, 5)

        // Put uncompleted at the end by updating their created timestamps slightly, then pick fresh ones
        val allBacklog = taskDao.getBacklogTasks().firstOrNull() ?: emptyList()
        val candidates = allBacklog.filterNot { item -> uncompleted.any { it.id == item.id } }
        val freshPicks = if (candidates.size >= targetCount) {
            candidates.take(targetCount)
        } else {
            allBacklog.take(targetCount)
        }

        if (freshPicks.isNotEmpty()) {
            taskDao.markTasksInCurrentQuest(freshPicks.map { it.id })
        }
    }

    suspend fun bulkImportTasks(linesText: String, defaultCategory: String = "Импорт") = withContext(Dispatchers.IO) {
        val lines = linesText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return@withContext

        val currentInQuestCount = taskDao.getCurrentQuestTasks().firstOrNull()?.size ?: 0
        val profile = userProfileDao.getUserProfile().firstOrNull() ?: UserProfile()
        val targetCount = profile.tasksPerQuest

        var addedToQuest = currentInQuestCount
        val tasksToInsert = lines.map { line ->
            val priority = if (line.startsWith("!")) Priority.CRITICAL else Priority.HIGH
            val cleanTitle = line.removePrefix("!").trim()
            val shouldPutInQuest = addedToQuest < targetCount
            if (shouldPutInQuest) addedToQuest++

            TaskItem(
                title = cleanTitle,
                category = defaultCategory,
                priority = priority,
                inCurrentQuest = shouldPutInQuest
            )
        }
        taskDao.insertAllTasks(tasksToInsert)
    }

    suspend fun redeemCustomReward(reward: CustomReward): Boolean = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile().firstOrNull() ?: return@withContext false
        if (profile.coins >= reward.costCoins) {
            val updatedProfile = profile.copy(coins = profile.coins - reward.costCoins)
            userProfileDao.insertOrUpdateProfile(updatedProfile)
            customRewardDao.updateReward(
                reward.copy(
                    timesClaimed = reward.timesClaimed + 1,
                    lastClaimedAt = System.currentTimeMillis()
                )
            )
            return@withContext true
        }
        return@withContext false
    }

    suspend fun addCustomReward(reward: CustomReward) = withContext(Dispatchers.IO) {
        customRewardDao.insertReward(reward)
    }

    suspend fun deleteCustomReward(reward: CustomReward) = withContext(Dispatchers.IO) {
        customRewardDao.deleteReward(reward)
    }

    suspend fun updateProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun logFocusMinutes(minutes: Int) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile().firstOrNull() ?: return@withContext
        val newFocus = profile.totalFocusMinutes + minutes
        val bonusCoins = (minutes / 5).coerceAtLeast(1)
        val bonusXp = minutes * 3
        val updated = profile.copy(
            totalFocusMinutes = newFocus,
            coins = profile.coins + bonusCoins,
            xp = profile.xp + bonusXp
        )
        updateProfileAndCheckLevelUp(updated)
        checkBadges(updated)
    }

    private suspend fun updateProfileAndCheckLevelUp(profile: UserProfile) {
        var currentLevel = profile.level
        var currentXp = profile.xp
        var title = profile.heroTitle

        val titles = listOf(
            "Новичок фокуса",
            "Искатель продуктивности",
            "Мастер микро-квестов",
            "Хранитель времени",
            "Покоритель прокрастинации",
            "Владыка потока",
            "Грандмастер эффективности",
            "Легенда баланса"
        )

        while (currentXp >= currentLevel * 200) {
            currentXp -= currentLevel * 200
            currentLevel++
            val titleIndex = (currentLevel - 1).coerceAtMost(titles.lastIndex)
            title = titles[titleIndex]
        }

        userProfileDao.insertOrUpdateProfile(
            profile.copy(
                level = currentLevel,
                xp = currentXp,
                heroTitle = title
            )
        )
    }

    private suspend fun checkBadges(profile: UserProfile) {
        val badges = badgeDao.getAllBadges().firstOrNull() ?: return
        for (b in badges) {
            if (!b.isUnlocked) {
                var shouldUnlock = false
                when (b.id) {
                    "FIRST_QUEST" -> if (profile.totalQuestsCompleted >= 1) shouldUnlock = true
                    "STREAK_3" -> if (profile.streakDays >= 3) shouldUnlock = true
                    "STREAK_7" -> if (profile.streakDays >= 7) shouldUnlock = true
                    "FOCUS_50" -> if (profile.totalFocusMinutes >= 50) shouldUnlock = true
                    "CENTURION" -> if (profile.totalTasksCompleted >= 100) shouldUnlock = true
                    "SELF_CARE" -> if (profile.activeMood == MoodType.CARE && profile.totalQuestsCompleted >= 1) shouldUnlock = true
                    "NIGHT_OWL" -> {
                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        if (hour >= 20 || hour <= 4) shouldUnlock = true
                    }
                }
                if (shouldUnlock) {
                    badgeDao.updateBadge(b.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis()))
                }
            }
        }
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    private fun isConsecutiveDay(prevDateStr: String, todayDateStr: String): Boolean {
        if (prevDateStr.isBlank()) return false
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return try {
            val prev = sdf.parse(prevDateStr) ?: return false
            val today = sdf.parse(todayDateStr) ?: return false
            val diff = (today.time - prev.time) / (1000 * 60 * 60 * 24)
            diff == 1L
        } catch (e: Exception) {
            false
        }
    }
}
