package com.example.util

import com.example.data.model.Priority
import java.util.Calendar

data class ParsedTaskResult(
    val title: String,
    val dueDate: Long? = null,
    val isStrictDeadline: Boolean = false,
    val priority: Priority = Priority.HIGH,
    val energyRequired: Int = 2,
    val estimatedMinutes: Int = 25
)

object LocalNlpParser {

    /**
     * Parses natural Russian input such as:
     * "позвонить маме завтра в 18:00"
     * "сдать отчет через 2 дня"
     * "выпить воды через 15 минут"
     * "встреча в пятницу в 15:00"
     * "! срочно купить продукты"
     * "сделать презентацию в 19:30"
     */
    fun parseInput(rawInput: String): ParsedTaskResult {
        var text = rawInput.trim()
        var priority = Priority.HIGH
        var isStrictDeadline = false
        var energyRequired = 2
        var estimatedMinutes = 25

        if (text.startsWith("!") || text.lowercase().contains("срочно") || text.lowercase().contains("важно")) {
            priority = Priority.CRITICAL
            text = text.removePrefix("!").trim()
        }

        val lower = text.lowercase()

        // Detect energy hints
        if (lower.contains("легк") || lower.contains("быстро") || lower.contains("микро")) {
            energyRequired = 1
            estimatedMinutes = 10
        } else if (lower.contains("сложн") || lower.contains("босс") || lower.contains("долго") || lower.contains("тяжел")) {
            energyRequired = 3
            estimatedMinutes = 45
        }

        val now = Calendar.getInstance()
        var targetCal: Calendar? = null

        // 1. Check relative minutes / hours: "через 15 минут", "через час", "через 2 часа"
        val relativeMinMatch = Regex("""через\s+(\d+)\s+мин""").find(lower)
        val relativeHourMatch = Regex("""через\s+(\d+)\s+час""").find(lower)
        val isThroughOneHour = Regex("""через\s+час\b""").containsMatchIn(lower)

        if (relativeMinMatch != null) {
            val mins = relativeMinMatch.groupValues[1].toIntOrNull() ?: 15
            targetCal = Calendar.getInstance().apply {
                add(Calendar.MINUTE, mins)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            isStrictDeadline = true
        } else if (relativeHourMatch != null) {
            val hrs = relativeHourMatch.groupValues[1].toIntOrNull() ?: 1
            targetCal = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, hrs)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            isStrictDeadline = true
        } else if (isThroughOneHour) {
            targetCal = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            isStrictDeadline = true
        }

        // 2. Specific time: "в 18:00" or "в 9:30"
        val hourMatch = Regex("""в\s+(\d{1,2})(?::(\d{2}))?""").find(lower)
        var targetHour: Int? = null
        var targetMinute = 0
        if (hourMatch != null) {
            val parsedHour = hourMatch.groupValues[1].toIntOrNull()?.coerceIn(0, 23)
            val parsedMin = hourMatch.groupValues[2].toIntOrNull()?.coerceIn(0, 59) ?: 0
            if (parsedHour != null) {
                targetHour = parsedHour
                targetMinute = parsedMin
                isStrictDeadline = true
            }
        }

        // 3. Day of the week: "в понедельник", "во вторник", "в среду", "в четверг", "в пятницу", "в субботу", "в воскресенье"
        val dayOfWeekMap = mapOf(
            "понедельник" to Calendar.MONDAY,
            "вторник" to Calendar.TUESDAY,
            "сред" to Calendar.WEDNESDAY,
            "четверг" to Calendar.THURSDAY,
            "пятниц" to Calendar.FRIDAY,
            "суббот" to Calendar.SATURDAY,
            "воскресен" to Calendar.SUNDAY
        )

        var targetDayOfWeek: Int? = null
        for ((word, dayConst) in dayOfWeekMap) {
            if (lower.contains(word)) {
                targetDayOfWeek = dayConst
                break
            }
        }

        if (targetCal == null) {
            val h = targetHour ?: 12
            val m = targetMinute

            if (lower.contains("сегодня")) {
                targetCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                isStrictDeadline = true
            } else if (lower.contains("завтра")) {
                targetCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                isStrictDeadline = true
            } else if (lower.contains("послезавтра")) {
                targetCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 2)
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                isStrictDeadline = true
            } else if (targetDayOfWeek != null) {
                targetCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    while (get(Calendar.DAY_OF_WEEK) != targetDayOfWeek || timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
                isStrictDeadline = true
            } else {
                val relativeDaysMatch = Regex("""через\s+(\d+)\s+дн""").find(lower)
                if (relativeDaysMatch != null) {
                    val days = relativeDaysMatch.groupValues[1].toIntOrNull() ?: 1
                    targetCal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, days)
                        set(Calendar.HOUR_OF_DAY, h)
                        set(Calendar.MINUTE, m)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    isStrictDeadline = true
                } else if (targetHour != null) {
                    // Only time specified (e.g. "в 17:00")
                    targetCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, h)
                        set(Calendar.MINUTE, m)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        if (timeInMillis <= System.currentTimeMillis()) {
                            // If time already passed today, schedule for tomorrow
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                    isStrictDeadline = true
                }
            }
        }

        // Clean extracted keywords from title
        var cleanTitle = text
            .replace(Regex("""(?<=\s|^)(сегодня|завтра|послезавтра)(?=\s|$)""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""через\s+\d+\s+дн(ей|я|ь)?""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""через\s+(\d+\s+)?(минут|мин|часа|часов|час)""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""в(о)?\s+(понедельник|вторник|среду|четверг|пятницу|субботу|воскресенье)""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""в\s+\d{1,2}(:\d{2})?""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""(?<=\s|^)(срочно|важно|микро|легко|быстро)(?=\s|$)""", RegexOption.IGNORE_CASE), "")
            .trim()
            .replace(Regex("""\s+"""), " ")

        if (cleanTitle.isBlank()) cleanTitle = text

        return ParsedTaskResult(
            title = cleanTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            dueDate = targetCal?.timeInMillis,
            isStrictDeadline = isStrictDeadline,
            priority = priority,
            energyRequired = energyRequired,
            estimatedMinutes = estimatedMinutes
        )
    }
}
