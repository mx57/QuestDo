package com.example.util

import com.example.data.model.Priority
import java.util.Calendar

data class ParsedTaskResult(
    val title: String,
    val dueDate: Long? = null,
    val isStrictDeadline: Boolean = false,
    val priority: Priority = Priority.HIGH
)

object LocalNlpParser {

    /**
     * Parses Russian input such as:
     * "позвонить маме завтра в 18:00"
     * "сдать отчет через 2 дня"
     * "! срочно купить продукты"
     * "встреча в пятницу в 15:00"
     */
    fun parseInput(rawInput: String): ParsedTaskResult {
        var text = rawInput.trim()
        var priority = Priority.HIGH
        var isStrictDeadline = false

        if (text.startsWith("!") || text.lowercase().contains("срочно") || text.lowercase().contains("важно")) {
            priority = Priority.CRITICAL
            text = text.removePrefix("!").trim()
        }

        val now = Calendar.getInstance()
        var targetCal: Calendar? = null

        val lower = text.lowercase()

        // Check time pattern (e.g. "в 18:00" or "в 6")
        val hourMatch = Regex("""в\s+(\d{1,2})(?::(\d{2}))?""").find(lower)
        var targetHour = 12
        var targetMinute = 0
        if (hourMatch != null) {
            targetHour = hourMatch.groupValues[1].toIntOrNull()?.coerceIn(0, 23) ?: 12
            targetMinute = hourMatch.groupValues[2].toIntOrNull()?.coerceIn(0, 59) ?: 0
            isStrictDeadline = true
        }

        if (lower.contains("сегодня")) {
            targetCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, targetHour)
                set(Calendar.MINUTE, targetMinute)
                set(Calendar.SECOND, 0)
            }
            isStrictDeadline = true
        } else if (lower.contains("завтра")) {
            targetCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, targetHour)
                set(Calendar.MINUTE, targetMinute)
                set(Calendar.SECOND, 0)
            }
            isStrictDeadline = true
        } else if (lower.contains("послезавтра")) {
            targetCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 2)
                set(Calendar.HOUR_OF_DAY, targetHour)
                set(Calendar.MINUTE, targetMinute)
                set(Calendar.SECOND, 0)
            }
            isStrictDeadline = true
        } else {
            val relativeDaysMatch = Regex("""через\s+(\d+)\s+дн""").find(lower)
            if (relativeDaysMatch != null) {
                val days = relativeDaysMatch.groupValues[1].toIntOrNull() ?: 1
                targetCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, days)
                    set(Calendar.HOUR_OF_DAY, targetHour)
                    set(Calendar.MINUTE, targetMinute)
                    set(Calendar.SECOND, 0)
                }
                isStrictDeadline = true
            }
        }

        // Clean extracted keywords from title
        var cleanTitle = text
            .replace(Regex("""\b(сегодня|завтра|послезавтра)\b""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""через\s+\d+\s+дн(ей|я|ь)?""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""в\s+\d{1,2}(:\d{2})?""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\b(срочно|важно)\b""", RegexOption.IGNORE_CASE), "")
            .trim()
            .replace(Regex("""\s+"""), " ")

        if (cleanTitle.isBlank()) cleanTitle = text

        return ParsedTaskResult(
            title = cleanTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            dueDate = targetCal?.timeInMillis,
            isStrictDeadline = isStrictDeadline,
            priority = priority
        )
    }
}
