package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import com.example.util.TaskScoringEngine
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
    val urgentDeadlineTasks: Flow<List<TaskItem>> = taskDao.getUrgentDeadlineTasks()
    val completedTasks: Flow<List<TaskItem>> = taskDao.getCompletedTasks()
    val archivedTasks: Flow<List<TaskItem>> = taskDao.getArchivedTasks()
    val activeLevel: Flow<QuestLevel?> = questLevelDao.getActiveLevel()
    val completedLevelsCount: Flow<Int> = questLevelDao.getCompletedLevelsCount()
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()
    val customRewards: Flow<List<CustomReward>> = customRewardDao.getAllRewards()
    val badges: Flow<List<BadgeAchievement>> = badgeDao.getAllBadges()

    fun getStaleTasks(daysOld: Int = 90): Flow<List<TaskItem>> {
        val threshold = System.currentTimeMillis() - (daysOld.toLong() * 24 * 60 * 60 * 1000)
        return taskDao.getStaleTasks(threshold)
    }

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
                streakFreezes = 2,
                lastFreezeResetMonth = getCurrentMonthString(),
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
        } else {
            // Check monthly reset for streak freezes
            val currentMonth = getCurrentMonthString()
            if (existingProfile.lastFreezeResetMonth != currentMonth) {
                userProfileDao.insertOrUpdateProfile(
                    existingProfile.copy(
                        streakFreezes = 2,
                        lastFreezeResetMonth = currentMonth
                    )
                )
            }
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
                BadgeAchievement("BOSS_SLAYER", "Победитель Босса", "Сокрушите недельного Босса Прокрастинации", "🐉"),
                BadgeAchievement("NIGHT_OWL", "Вечерний стратег", "Завершите квест в вечернее время", "🦉"),
                BadgeAchievement("DEMON_PACT_WIN", "Сделка с Дьяволом", "Обыграйте Демона в пакте и заберите 2x награду", "😈"),
                BadgeAchievement("CAULDRON_PURGE", "Очищение в Котле", "Сожгите накопившиеся грехи лени в адском огне", "🫕"),
                BadgeAchievement("DEMON_DEVOTEE", "Любимчик Преисподней", "Закройте 5 квестов под саркастичным надзором Демона", "🔱")
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
            customRewardDao.insertAllRewards(defaultRewards)
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
                    energyRequired = 1,
                    inCurrentQuest = true,
                    subtasksRaw = "Выбросить бумажки|0;Протереть экран и пыль|0;Налить стакан воды|0"
                ),
                TaskItem(
                    title = "Выбрать 1 ключевую задачу на день",
                    description = "Сфокусируйся на самом важном деле, которое сдвинет проект вперед.",
                    category = "Работа",
                    priority = Priority.CRITICAL,
                    energyRequired = 2,
                    inCurrentQuest = true,
                    subtasksRaw = "Открыть проект|0;Сформулировать первый шаг|0"
                ),
                TaskItem(
                    title = "Сделать 5 глубоких вдохов перед стартом",
                    description = "Психологическая настройка: вдох 4 сек, задержка 4 сек, выдох 4 сек.",
                    category = "Здоровье",
                    priority = Priority.HIGH,
                    energyRequired = 1,
                    inCurrentQuest = true,
                    subtasksRaw = "Закрыть глаза|0;Сделать цикл дыхания|0"
                ),
                // Backlog tasks
                TaskItem(
                    title = "Разобрать входящие письма и сообщения",
                    description = "Ответить на срочные, архивировать спам",
                    category = "Работа",
                    priority = Priority.HIGH,
                    energyRequired = 2,
                    inCurrentQuest = false
                ),
                TaskItem(
                    title = "20-минутная разминка или прогулка",
                    description = "Размять спину и плечи",
                    category = "Здоровье",
                    priority = Priority.HIGH,
                    energyRequired = 2,
                    inCurrentQuest = false
                ),
                TaskItem(
                    title = "Прочитать 1 главу полезной книги",
                    description = "Без отвлечений на уведомления",
                    category = "Развитие",
                    priority = Priority.MEDIUM,
                    energyRequired = 2,
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

    suspend fun updateTasks(tasks: List<TaskItem>) = withContext(Dispatchers.IO) {
        taskDao.updateTasks(tasks)
    }

    suspend fun deleteTask(task: TaskItem) = withContext(Dispatchers.IO) {
        taskDao.deleteTask(task)
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
            val profile = userProfileDao.getUserProfile().firstOrNull() ?: UserProfile()
            var earnedCoins = 5
            var earnedXp = 25
            
            // Artifact bonus perks
            if (profile.equippedArtifactId == "FLOW_FEATHER") {
                earnedCoins += 5
            }
            if (task.category == "Босс" && profile.equippedArtifactId == "BOSS_BLADE") {
                earnedCoins += 15
                earnedXp += 50
            }

            val updatedProfile = profile.copy(
                totalTasksCompleted = profile.totalTasksCompleted + 1,
                xp = profile.xp + earnedXp,
                coins = profile.coins + earnedCoins
            )
            updateProfileAndCheckLevelUp(updatedProfile)

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

        val profile = userProfileDao.getUserProfile().firstOrNull() ?: UserProfile()
        val today = getTodayDateString()
        val isConsecutive = isConsecutiveDay(profile.lastActiveDate, today)

        var newStreak = profile.streakDays
        var freezesLeft = profile.streakFreezes

        if (profile.lastActiveDate == today) {
            // Already active today
        } else if (isConsecutive) {
            newStreak += 1
        } else {
            // Day missed - check streak freeze protection!
            if (freezesLeft > 0) {
                freezesLeft -= 1 // Protect streak!
            } else {
                // Soft restart without guilt
                newStreak = 1
            }
        }

        val bestStreak = maxOf(profile.bestStreak, newStreak)

        val updatedProfile = profile.copy(
            xp = profile.xp + level.xpEarned,
            coins = profile.coins + level.coinsEarned,
            totalQuestsCompleted = profile.totalQuestsCompleted + 1,
            streakDays = newStreak,
            bestStreak = bestStreak,
            streakFreezes = freezesLeft,
            consecutiveFailures = 0, // reset failure streak
            lastActiveDate = today
        )
        updateProfileAndCheckLevelUp(updatedProfile)

        checkBadges(updatedProfile)
        return@withContext completed
    }

    suspend fun startNextQuest(): QuestLevel = withContext(Dispatchers.IO) {
        taskDao.clearCurrentQuest()

        val profile = userProfileDao.getUserProfile().firstOrNull() ?: UserProfile()
        val backlog = taskDao.getBacklogTasks().firstOrNull() ?: emptyList()

        // Use TaskScoringEngine to pick best tasks based on energy, rules, and recovery mode
        val selectedTasks = TaskScoringEngine.assembleLevelTasks(
            backlog = backlog,
            targetCount = profile.tasksPerQuest,
            userEnergyLevel = profile.currentEnergyLevel,
            isRecoveryMode = profile.inRecoveryMode
        )

        if (selectedTasks.isNotEmpty()) {
            taskDao.markTasksInCurrentQuest(selectedTasks.map { it.id })
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

    suspend fun startBossLevel(bossGoalTitle: String, steps: List<String>): QuestLevel = withContext(Dispatchers.IO) {
        taskDao.clearCurrentQuest()

        val bossTask = TaskItem(
            title = "⚔️ БОСС: $bossGoalTitle",
            description = "Главная цель недели! Закрой все шаги для сокрушения Босса.",
            category = "Босс-Цель",
            priority = Priority.CRITICAL,
            energyRequired = 4,
            inCurrentQuest = true,
            subtasksRaw = TaskItem.serializeSubtasks(steps.map { SubTask(it, false) })
        )
        val taskId = taskDao.insertTask(bossTask)

        val completedCount = questLevelDao.getCompletedLevelsCount().firstOrNull() ?: 0
        val nextNumber = completedCount + 1
        val bossLevel = QuestLevel(
            levelNumber = nextNumber,
            title = "🔥 БОСС-УРОВЕНЬ: $bossGoalTitle",
            isBossLevel = true,
            bossName = bossGoalTitle,
            bossIconEmoji = "🐉",
            startedAt = System.currentTimeMillis(),
            xpEarned = 350,
            coinsEarned = 100
        )
        questLevelDao.insertLevel(bossLevel)
        return@withContext bossLevel
    }

    suspend fun shuffleCurrentQuest() = withContext(Dispatchers.IO) {
        val currentTasks = taskDao.getCurrentQuestTasks().firstOrNull() ?: emptyList()
        val uncompleted = currentTasks.filter { !it.isCompleted }

        // Track postpones for uncompleted tasks
        val updatedUncompleted = uncompleted.map { t ->
            t.copy(postponeCount = t.postponeCount + 1)
        }
        if (updatedUncompleted.isNotEmpty()) {
            taskDao.updateTasks(updatedUncompleted)
        }

        taskDao.clearCurrentQuest()

        val profile = userProfileDao.getUserProfile().firstOrNull() ?: UserProfile()
        val newFailures = profile.consecutiveFailures + 1

        // Check trigger for Recovery Mode (after 3 failures/reshuffles)
        val entersRecovery = newFailures >= 3 || profile.inRecoveryMode
        val updatedProfile = profile.copy(
            consecutiveFailures = newFailures,
            inRecoveryMode = entersRecovery,
            recoveryDaysRemaining = if (entersRecovery) 3 else 0
        )
        userProfileDao.insertOrUpdateProfile(updatedProfile)

        val backlog = taskDao.getBacklogTasks().firstOrNull() ?: emptyList()
        val freshPicks = TaskScoringEngine.assembleLevelTasks(
            backlog = backlog.filterNot { item -> uncompleted.any { it.id == item.id } }.ifEmpty { backlog },
            targetCount = updatedProfile.tasksPerQuest,
            userEnergyLevel = updatedProfile.currentEnergyLevel,
            isRecoveryMode = updatedProfile.inRecoveryMode
        )

        if (freshPicks.isNotEmpty()) {
            taskDao.markTasksInCurrentQuest(freshPicks.map { it.id })
        }
    }

    suspend fun performEveningCheckout(reflectionText: String) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile().firstOrNull() ?: return@withContext
        val today = getTodayDateString()
        val updated = profile.copy(
            eveningCheckoutDoneToday = true,
            lastEveningCheckoutDate = today,
            coins = profile.coins + 15,
            xp = profile.xp + 50
        )
        updateProfileAndCheckLevelUp(updated)
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
        var bonusXp = minutes * 3
        if (profile.equippedArtifactId == "CHRONO_TITAN") {
            bonusXp += 15
        }
        val updated = profile.copy(
            totalFocusMinutes = newFocus,
            coins = profile.coins + bonusCoins,
            xp = profile.xp + bonusXp
        )
        updateProfileAndCheckLevelUp(updated)
        checkBadges(updated)
    }

    suspend fun unlockArtifact(artifactId: String, costCoins: Int): Boolean = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile().firstOrNull() ?: return@withContext false
        if (profile.coins >= costCoins) {
            val unlockedList = profile.unlockedArtifactIdsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
            if (!unlockedList.contains(artifactId)) {
                unlockedList.add(artifactId)
            }
            val updated = profile.copy(
                coins = profile.coins - costCoins,
                unlockedArtifactIdsRaw = unlockedList.joinToString(",")
            )
            userProfileDao.insertOrUpdateProfile(updated)
            return@withContext true
        }
        return@withContext false
    }

    suspend fun equipArtifact(artifactId: String) = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfile().firstOrNull() ?: return@withContext
        if (profile.isArtifactUnlocked(artifactId)) {
            val updated = profile.copy(equippedArtifactId = artifactId)
            userProfileDao.insertOrUpdateProfile(updated)
        }
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
                    "DEMON_PACT_WIN" -> if (profile.devilPactSuccesses >= 1) shouldUnlock = true
                    "CAULDRON_PURGE" -> if (profile.cauldronSinsBurned >= 1) shouldUnlock = true
                    "DEMON_DEVOTEE" -> if (profile.isDemonMode && profile.totalTasksCompleted >= 5) shouldUnlock = true
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

    private fun getCurrentMonthString(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
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
