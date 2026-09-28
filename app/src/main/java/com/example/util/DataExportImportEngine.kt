package com.example.util

import com.example.data.model.Priority
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile
import org.json.JSONArray
import org.json.JSONObject

object DataExportImportEngine {

    fun exportToJson(profile: UserProfile, tasks: List<TaskItem>): String {
        val root = JSONObject()

        val profileJson = JSONObject().apply {
            put("heroName", profile.heroName)
            put("heroTitle", profile.heroTitle)
            put("level", profile.level)
            put("xp", profile.xp)
            put("coins", profile.coins)
            put("streakDays", profile.streakDays)
            put("bestStreak", profile.bestStreak)
            put("totalQuestsCompleted", profile.totalQuestsCompleted)
            put("totalTasksCompleted", profile.totalTasksCompleted)
        }
        root.put("profile", profileJson)

        val tasksArray = JSONArray()
        for (task in tasks) {
            val tJson = JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("description", task.description)
                put("category", task.category)
                put("priority", task.priority.name)
                put("energyRequired", task.energyRequired)
                put("fieldWhy", task.fieldWhy)
                put("tagsRaw", task.tagsRaw)
                put("isCompleted", task.isCompleted)
                put("isArchived", task.isArchived)
                put("isStrictDeadline", task.isStrictDeadline)
                put("subtasksRaw", task.subtasksRaw)
                put("estimatedMinutes", task.estimatedMinutes)
                put("dueDate", task.dueDate ?: 0L)
            }
            tasksArray.put(tJson)
        }
        root.put("tasks", tasksArray)

        return root.toString(2)
    }

    fun exportToCsv(tasks: List<TaskItem>): String {
        val sb = java.lang.StringBuilder()
        sb.append("Title,Description,Category,Priority,Energy,Why,Completed,Archived,StrictDeadline\n")
        for (t in tasks) {
            val safeTitle = t.title.replace("\"", "\"\"")
            val safeDesc = t.description.replace("\"", "\"\"")
            val safeCategory = t.category.replace("\"", "\"\"")
            val safeWhy = t.fieldWhy.replace("\"", "\"\"")
            sb.append("\"$safeTitle\",\"$safeDesc\",\"$safeCategory\",${t.priority.name},${t.energyRequired},\"$safeWhy\",${t.isCompleted},${t.isArchived},${t.isStrictDeadline}\n")
        }
        return sb.toString()
    }

    fun parseJsonTasks(jsonStr: String): List<TaskItem> {
        val result = mutableListOf<TaskItem>()
        try {
            val root = JSONObject(jsonStr)
            val array = if (root.has("tasks")) root.getJSONArray("tasks") else JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val title = item.optString("title", "Задача")
                val desc = item.optString("description", "")
                val category = item.optString("category", "Общие")
                val priorityStr = item.optString("priority", Priority.HIGH.name)
                val priority = try { Priority.valueOf(priorityStr) } catch (e: Exception) { Priority.HIGH }
                val energy = item.optInt("energyRequired", 2)
                val why = item.optString("fieldWhy", "")
                val tags = item.optString("tagsRaw", "")
                val subtasks = item.optString("subtasksRaw", "")
                val isDone = item.optBoolean("isCompleted", false)
                val isArchived = item.optBoolean("isArchived", false)
                val isStrict = item.optBoolean("isStrictDeadline", false)

                result.add(
                    TaskItem(
                        title = title,
                        description = desc,
                        category = category,
                        priority = priority,
                        energyRequired = energy,
                        fieldWhy = why,
                        tagsRaw = tags,
                        subtasksRaw = subtasks,
                        isCompleted = isDone,
                        isArchived = isArchived,
                        isStrictDeadline = isStrict
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun parseCsvTasks(csvStr: String): List<TaskItem> {
        val result = mutableListOf<TaskItem>()
        val lines = csvStr.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return result

        for (i in 1 until lines.size) {
            val line = lines[i]
            val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
            if (parts.isNotEmpty()) {
                val title = parts[0]
                if (title.isNotBlank()) {
                    val desc = parts.getOrNull(1) ?: ""
                    val cat = parts.getOrNull(2) ?: "Импорт"
                    result.add(
                        TaskItem(
                            title = title,
                            description = desc,
                            category = cat,
                            priority = Priority.HIGH
                        )
                    )
                }
            }
        }
        return result
    }
}
