package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Общие",
    val priority: Priority = Priority.HIGH,
    val isCompleted: Boolean = false,
    val inCurrentQuest: Boolean = false,
    val questLevelId: Long? = null,
    val subtasksRaw: String = "", // e.g. "Step 1|0;Step 2|1"
    val estimatedMinutes: Int = 25,
    val dueDate: Long? = null,
    val recurrence: RecurrenceRule = RecurrenceRule.NONE,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    fun getSubtasksList(): List<SubTask> {
        if (subtasksRaw.isBlank()) return emptyList()
        return subtasksRaw.split(";").filter { it.isNotBlank() }.mapNotNull { part ->
            val tokens = part.split("|")
            if (tokens.isNotEmpty()) {
                val title = tokens[0]
                val done = tokens.getOrNull(1) == "1"
                SubTask(title, done)
            } else null
        }
    }

    companion object {
        fun serializeSubtasks(list: List<SubTask>): String {
            return list.joinToString(";") { "${it.title}|${if (it.isDone) "1" else "0"}" }
        }
    }
}

data class SubTask(
    val title: String,
    val isDone: Boolean = false
)
