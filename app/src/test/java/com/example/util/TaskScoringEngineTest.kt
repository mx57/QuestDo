package com.example.util

import com.example.data.model.Priority
import com.example.data.model.TaskItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class TaskScoringEngineTest {

    private fun createBaseTask(
        id: Long = 1L,
        title: String = "Test Task",
        priority: Priority = Priority.MEDIUM,
        isStrictDeadline: Boolean = false,
        dueDate: Long? = null,
        createdAt: Long = System.currentTimeMillis(),
        energyRequired: Int = 2,
        estimatedMinutes: Int = 20
    ): TaskItem {
        return TaskItem(
            id = id,
            title = title,
            priority = priority,
            isStrictDeadline = isStrictDeadline,
            dueDate = dueDate,
            createdAt = createdAt,
            energyRequired = energyRequired,
            estimatedMinutes = estimatedMinutes
        )
    }

    @Test
    fun testCalculateScore_priorityScores() {
        val now = System.currentTimeMillis()
        val critical = createBaseTask(id = 1, priority = Priority.CRITICAL, createdAt = now)
        val high = createBaseTask(id = 2, priority = Priority.HIGH, createdAt = now)
        val medium = createBaseTask(id = 3, priority = Priority.MEDIUM, createdAt = now)
        val low = createBaseTask(id = 4, priority = Priority.LOW, createdAt = now)

        val energyLevel = 2 // Neutral energy

        val scoreCritical = TaskScoringEngine.calculateScore(critical, energyLevel)
        val scoreHigh = TaskScoringEngine.calculateScore(high, energyLevel)
        val scoreMedium = TaskScoringEngine.calculateScore(medium, energyLevel)
        val scoreLow = TaskScoringEngine.calculateScore(low, energyLevel)

        assertEquals(20.0, scoreCritical - scoreHigh, 0.1)
        assertEquals(15.0, scoreHigh - scoreMedium, 0.1)
        assertEquals(10.0, scoreMedium - scoreLow, 0.1)
    }

    @Test
    fun testCalculateScore_strictDeadlineProximityBoost() {
        val now = System.currentTimeMillis()

        // Due in 12 hours (< 24h) -> +40.0
        val dueIn12h = createBaseTask(
            id = 1,
            isStrictDeadline = true,
            dueDate = now + 12 * 60 * 60 * 1000L,
            createdAt = now
        )

        // Due in 48 hours (< 72h) -> +20.0
        val dueIn48h = createBaseTask(
            id = 2,
            isStrictDeadline = true,
            dueDate = now + 48 * 60 * 60 * 1000L,
            createdAt = now
        )

        // Due in 100 hours (>= 72h) -> +0.0
        val dueIn100h = createBaseTask(
            id = 3,
            isStrictDeadline = true,
            dueDate = now + 100 * 60 * 60 * 1000L,
            createdAt = now
        )

        // Non-strict due in 12 hours -> +0.0
        val nonStrictDueIn12h = createBaseTask(
            id = 4,
            isStrictDeadline = false,
            dueDate = now + 12 * 60 * 60 * 1000L,
            createdAt = now
        )

        val energyLevel = 2

        val score12h = TaskScoringEngine.calculateScore(dueIn12h, energyLevel)
        val score48h = TaskScoringEngine.calculateScore(dueIn48h, energyLevel)
        val score100h = TaskScoringEngine.calculateScore(dueIn100h, energyLevel)
        val scoreNonStrict = TaskScoringEngine.calculateScore(nonStrictDueIn12h, energyLevel)

        assertEquals(40.0, score12h - score100h, 0.5)
        assertEquals(20.0, score48h - score100h, 0.5)
        assertEquals(0.0, scoreNonStrict - score100h, 0.5)
    }

    @Test
    fun testCalculateScore_taskAgeBoost() {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        val taskNew = createBaseTask(createdAt = now)
        val task10DaysOld = createBaseTask(createdAt = now - 10 * oneDayMs)
        val task40DaysOld = createBaseTask(createdAt = now - 40 * oneDayMs)

        val energyLevel = 2

        val scoreNew = TaskScoringEngine.calculateScore(taskNew, energyLevel)
        val score10Days = TaskScoringEngine.calculateScore(task10DaysOld, energyLevel)
        val score40Days = TaskScoringEngine.calculateScore(task40DaysOld, energyLevel)

        // 10 days * 1.5 = +15.0
        assertEquals(15.0, score10Days - scoreNew, 0.5)

        // 40 days -> capped at 30 days * 1.5 = +45.0
        assertEquals(45.0, score40Days - scoreNew, 0.5)
    }

    @Test
    fun testCalculateScore_energyMatchBoost() {
        val now = System.currentTimeMillis()

        // Low energy tasks (energyRequired <= 2 or estimatedMinutes <= 10)
        val easyTask = createBaseTask(id = 1, energyRequired = 1, estimatedMinutes = 30, createdAt = now)
        val quickTask = createBaseTask(id = 2, energyRequired = 3, estimatedMinutes = 5, createdAt = now)

        // Heavy task (energyRequired >= 4 and estimatedMinutes > 10)
        val heavyTask = createBaseTask(id = 3, energyRequired = 4, estimatedMinutes = 60, createdAt = now)

        // Neutral task (energyRequired = 3, estimatedMinutes = 20)
        val neutralTask = createBaseTask(id = 4, energyRequired = 3, estimatedMinutes = 20, createdAt = now)

        // User Low Energy (1)
        val scoreEasyLowEnergy = TaskScoringEngine.calculateScore(easyTask, 1)
        val scoreEasyNeutralEnergy = TaskScoringEngine.calculateScore(easyTask, 2)
        assertEquals(25.0, scoreEasyLowEnergy - scoreEasyNeutralEnergy, 0.1)

        val scoreQuickLowEnergy = TaskScoringEngine.calculateScore(quickTask, 1)
        val scoreQuickNeutralEnergy = TaskScoringEngine.calculateScore(quickTask, 2)
        assertEquals(25.0, scoreQuickLowEnergy - scoreQuickNeutralEnergy, 0.1)

        val scoreHeavyLowEnergy = TaskScoringEngine.calculateScore(heavyTask, 1)
        val scoreHeavyNeutralEnergy = TaskScoringEngine.calculateScore(heavyTask, 2)
        assertEquals(-30.0, scoreHeavyLowEnergy - scoreHeavyNeutralEnergy, 0.1)

        // User High Energy (3)
        val scoreHeavyHighEnergy = TaskScoringEngine.calculateScore(heavyTask, 3)
        assertEquals(15.0, scoreHeavyHighEnergy - scoreHeavyNeutralEnergy, 0.1)

        val scoreNeutralHighEnergy = TaskScoringEngine.calculateScore(neutralTask, 3)
        val scoreNeutralNeutralEnergy = TaskScoringEngine.calculateScore(neutralTask, 2)
        assertEquals(15.0, scoreNeutralHighEnergy - scoreNeutralNeutralEnergy, 0.1)

        val scoreEasyHighEnergy = TaskScoringEngine.calculateScore(easyTask, 3)
        assertEquals(0.0, scoreEasyHighEnergy - scoreEasyNeutralEnergy, 0.1)
    }

    @Test
    fun testCalculateScore_timeOfDayContext() {
        val now = System.currentTimeMillis()
        val shortTask = createBaseTask(id = 1, estimatedMinutes = 10, createdAt = now)
        val longTask = createBaseTask(id = 2, estimatedMinutes = 30, createdAt = now)

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val scoreShort = TaskScoringEngine.calculateScore(shortTask, 2)
        val scoreLong = TaskScoringEngine.calculateScore(longTask, 2)

        if (currentHour >= 21 || currentHour <= 5) {
            // Late night mode: short task +15, long task -15 -> diff 30
            assertEquals(30.0, scoreShort - scoreLong, 0.1)
        } else {
            // Daytime mode: no time of day adjustments -> same priority/energy/age score
            assertEquals(0.0, scoreShort - scoreLong, 0.1)
        }
    }

    @Test
    fun testAssembleLevelTasks_emptyBacklog() {
        val result = TaskScoringEngine.assembleLevelTasks(
            backlog = emptyList(),
            targetCount = 3,
            userEnergyLevel = 2,
            isRecoveryMode = false
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun testAssembleLevelTasks_recoveryMode() {
        val now = System.currentTimeMillis()
        val task1 = createBaseTask(id = 1, priority = Priority.LOW, createdAt = now)
        val task2 = createBaseTask(id = 2, priority = Priority.CRITICAL, createdAt = now)
        val task3 = createBaseTask(id = 3, priority = Priority.HIGH, createdAt = now)

        val backlog = listOf(task1, task2, task3)

        val result = TaskScoringEngine.assembleLevelTasks(
            backlog = backlog,
            targetCount = 3,
            userEnergyLevel = 2,
            isRecoveryMode = true
        )

        assertEquals(1, result.size)
        assertEquals(task2.id, result.first().id)
    }

    @Test
    fun testAssembleLevelTasks_targetCountOne() {
        val now = System.currentTimeMillis()
        val task1 = createBaseTask(id = 1, priority = Priority.LOW, createdAt = now)
        val task2 = createBaseTask(id = 2, priority = Priority.CRITICAL, createdAt = now)

        val result = TaskScoringEngine.assembleLevelTasks(
            backlog = listOf(task1, task2),
            targetCount = 1,
            userEnergyLevel = 2,
            isRecoveryMode = false
        )

        assertEquals(1, result.size)
        assertEquals(task2.id, result.first().id)
    }

    @Test
    fun testAssembleLevelTasks_quickWinInFirstSlot() {
        val now = System.currentTimeMillis()

        // Heavy/High Priority Task (will have high score, but not a quick win)
        val heavyHighPriority = createBaseTask(
            id = 10,
            title = "Heavy High Priority",
            priority = Priority.CRITICAL,
            energyRequired = 4,
            estimatedMinutes = 60,
            createdAt = now
        )

        // Medium Priority Heavy Task
        val mediumHeavy = createBaseTask(
            id = 20,
            title = "Medium Heavy",
            priority = Priority.HIGH,
            energyRequired = 3,
            estimatedMinutes = 40,
            createdAt = now
        )

        // Quick win task (estimatedMinutes <= 5 or energyRequired == 1)
        val quickWinTask = createBaseTask(
            id = 30,
            title = "Quick Win Task",
            priority = Priority.LOW,
            energyRequired = 1,
            estimatedMinutes = 5,
            createdAt = now
        )

        val backlog = listOf(heavyHighPriority, mediumHeavy, quickWinTask)

        val result = TaskScoringEngine.assembleLevelTasks(
            backlog = backlog,
            targetCount = 3,
            userEnergyLevel = 2,
            isRecoveryMode = false
        )

        assertEquals(3, result.size)
        // First slot should be the quick win task
        assertEquals(quickWinTask.id, result[0].id)
    }

    @Test
    fun testAssembleLevelTasks_balancedMixAndThrottling() {
        val now = System.currentTimeMillis()

        // Two heavy tasks and two light tasks
        val heavy1 = createBaseTask(id = 1, title = "Heavy 1", priority = Priority.CRITICAL, energyRequired = 5, estimatedMinutes = 60, createdAt = now)
        val heavy2 = createBaseTask(id = 2, title = "Heavy 2", priority = Priority.CRITICAL, energyRequired = 4, estimatedMinutes = 50, createdAt = now)
        val light1 = createBaseTask(id = 3, title = "Light 1", priority = Priority.HIGH, energyRequired = 1, estimatedMinutes = 5, createdAt = now)
        val light2 = createBaseTask(id = 4, title = "Light 2", priority = Priority.MEDIUM, energyRequired = 2, estimatedMinutes = 15, createdAt = now)

        val backlog = listOf(heavy1, heavy2, light1, light2)

        val result = TaskScoringEngine.assembleLevelTasks(
            backlog = backlog,
            targetCount = 3,
            userEnergyLevel = 2,
            isRecoveryMode = false
        )

        assertEquals(3, result.size)
        // Quick win in slot 1: light1
        assertEquals(light1.id, result[0].id)
        // Slot 2: heavy1
        assertEquals(heavy1.id, result[1].id)
        // Slot 3: heavy2 is skipped during throttling pass because heavyInResult >= 1, so light2 is picked
        assertEquals(light2.id, result[2].id)
    }

    @Test
    fun testAssembleLevelTasks_backfillWhenFilteredShort() {
        val now = System.currentTimeMillis()

        // Quick win + 2 heavy tasks only in backlog, targetCount = 3
        val quickWin = createBaseTask(id = 1, energyRequired = 1, estimatedMinutes = 5, createdAt = now)
        val heavy1 = createBaseTask(id = 2, priority = Priority.CRITICAL, energyRequired = 5, estimatedMinutes = 60, createdAt = now)
        val heavy2 = createBaseTask(id = 3, priority = Priority.HIGH, energyRequired = 5, estimatedMinutes = 60, createdAt = now)

        val backlog = listOf(quickWin, heavy1, heavy2)

        val result = TaskScoringEngine.assembleLevelTasks(
            backlog = backlog,
            targetCount = 3,
            userEnergyLevel = 2,
            isRecoveryMode = false
        )

        // Should backfill to reach target count of 3 despite heavy throttling
        assertEquals(3, result.size)
        assertTrue(result.contains(quickWin))
        assertTrue(result.contains(heavy1))
        assertTrue(result.contains(heavy2))
    }

    @Test
    fun testAssembleLevelTasks_targetCountClamping() {
        val now = System.currentTimeMillis()
        val tasks = (1..10).map { i ->
            createBaseTask(id = i.toLong(), title = "Task $i", createdAt = now)
        }

        // Target count > 5 clamped to 5
        val resultMax = TaskScoringEngine.assembleLevelTasks(
            backlog = tasks,
            targetCount = 10,
            userEnergyLevel = 2,
            isRecoveryMode = false
        )
        assertEquals(5, resultMax.size)

        // Target count < 1 clamped to 1
        val resultMin = TaskScoringEngine.assembleLevelTasks(
            backlog = tasks,
            targetCount = 0,
            userEnergyLevel = 2,
            isRecoveryMode = false
        )
        assertEquals(1, resultMin.size)
    }
}
