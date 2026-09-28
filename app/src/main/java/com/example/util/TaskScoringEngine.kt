package com.example.util

import com.example.data.model.Priority
import com.example.data.model.TaskItem
import java.util.Calendar

object TaskScoringEngine {

    /**
     * Scores a task based on deadline + priority (Eisenhower) + energy required + task age + time of day context.
     */
    fun calculateScore(task: TaskItem, userEnergyLevel: Int): Double {
        var score = 0.0

        // 1. Eisenhower Priority Score
        score += when (task.priority) {
            Priority.CRITICAL -> 50.0
            Priority.HIGH -> 30.0
            Priority.MEDIUM -> 15.0
            Priority.LOW -> 5.0
        }

        // 2. Strict Deadline / Proximity Boost
        if (task.isStrictDeadline && task.dueDate != null) {
            val diffMs = task.dueDate - System.currentTimeMillis()
            val diffHours = diffMs / (1000.0 * 60 * 60)
            if (diffHours < 24) {
                score += 40.0
            } else if (diffHours < 72) {
                score += 20.0
            }
        }

        // 3. Task Age Boost (older tasks shouldn't rot)
        val ageDays = (System.currentTimeMillis() - task.createdAt) / (1000.0 * 60 * 60 * 24)
        score += ageDays.coerceAtMost(30.0) * 1.5

        // 4. Energy Match Filter/Boost
        // userEnergyLevel: 1 (Low), 2 (Medium), 3 (High)
        when (userEnergyLevel) {
            1 -> { // Low energy
                if (task.energyRequired <= 2 || task.estimatedMinutes <= 10) {
                    score += 25.0 // Boost quick/easy tasks when user is tired
                } else if (task.energyRequired >= 4) {
                    score -= 30.0 // Avoid draining tasks
                }
            }
            3 -> { // High energy
                if (task.energyRequired >= 3) {
                    score += 15.0 // Perfect time for heavy lifting
                }
            }
        }

        // 5. Time of Day Context
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour >= 21 || currentHour <= 5) { // Late night
            if (task.estimatedMinutes <= 15) score += 15.0
            else score -= 15.0
        }

        return score
    }

    /**
     * Assembles optimal tasks for a level based on rules:
     * - First slot: "Quick Win" (task <= 5 mins or easy)
     * - Balanced mix (1 heavy + 1-2 small)
     * - Target count (1, 2, 3, 5) or 1 task if in recovery mode.
     */
    fun assembleLevelTasks(
        backlog: List<TaskItem>,
        targetCount: Int,
        userEnergyLevel: Int,
        isRecoveryMode: Boolean
    ): List<TaskItem> {
        if (backlog.isEmpty()) return emptyList()

        val activeCount = if (isRecoveryMode) 1 else targetCount.coerceIn(1, 5)
        if (activeCount == 1) {
            // Pick highest scoring single task
            return listOf(backlog.maxByOrNull { calculateScore(it, userEnergyLevel) } ?: backlog.first())
        }

        val scoredTasks = backlog.map { task ->
            task to calculateScore(task, userEnergyLevel)
        }.sortedByDescending { it.second }

        val result = mutableListOf<TaskItem>()

        // 1st Slot: Quick Win (<= 5 min or energy 1)
        val quickWinCandidate = scoredTasks.firstOrNull { (task, _) ->
            task.estimatedMinutes <= 5 || task.energyRequired == 1
        }?.first

        if (quickWinCandidate != null) {
            result.add(quickWinCandidate)
        }

        // Fill remaining slots with balanced mix
        for ((task, _) in scoredTasks) {
            if (result.size >= activeCount) break
            if (!result.contains(task)) {
                // Avoid picking 3 heavy tasks in a row
                val heavyInResult = result.count { it.energyRequired >= 4 || it.estimatedMinutes >= 45 }
                if (heavyInResult >= 1 && (task.energyRequired >= 4 || task.estimatedMinutes >= 45)) {
                    continue // Skip 2nd/3rd heavy task in favor of a lighter one
                }
                result.add(task)
            }
        }

        // If still under target, fill remaining regardless
        for ((task, _) in scoredTasks) {
            if (result.size >= activeCount) break
            if (!result.contains(task)) {
                result.add(task)
            }
        }

        return result
    }
}
