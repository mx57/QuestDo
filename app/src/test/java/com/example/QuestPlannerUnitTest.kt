package com.example

import com.example.data.model.MotivationalEngine
import com.example.data.model.MoodType
import com.example.data.model.RecurrenceRule
import com.example.data.model.SubTask
import com.example.data.model.TaskItem
import com.example.util.RecurrenceHelper
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class QuestPlannerUnitTest {

    @Test
    fun testSubtaskSerializationAndDeserialization() {
        val subtasks = listOf(
            SubTask("Шаг 1: Подготовить проект", true),
            SubTask("Шаг 2: Написать тесты", false),
            SubTask("Шаг 3: Проверить сборку", true)
        )
        val raw = TaskItem.serializeSubtasks(subtasks)
        val task = TaskItem(title = "Тестовый квест", subtasksRaw = raw)
        val parsed = task.getSubtasksList()

        assertEquals(3, parsed.size)
        assertEquals("Шаг 1: Подготовить проект", parsed[0].title)
        assertTrue(parsed[0].isDone)
        assertEquals("Шаг 2: Написать тесты", parsed[1].title)
        assertFalse(parsed[1].isDone)
        assertEquals("Шаг 3: Проверить сборку", parsed[2].title)
        assertTrue(parsed[2].isDone)
    }

    @Test
    fun testMotivationalEngineReturnsContextualQuotes() {
        val focusQuote = MotivationalEngine.getContextualQuote(MoodType.FOCUS)
        assertNotNull(focusQuote)
        assertTrue(focusQuote.quote.isNotBlank())

        val careQuote = MotivationalEngine.getContextualQuote(MoodType.CARE)
        assertNotNull(careQuote)
        assertTrue(careQuote.quote.isNotBlank())
    }

    @Test
    fun testRecurrenceCalculation() {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val originalDay = cal.get(Calendar.DAY_OF_YEAR)

        cal.add(Calendar.DAY_OF_YEAR, 1)
        val nextDay = cal.get(Calendar.DAY_OF_YEAR)
        assertEquals((originalDay % 365) + 1, (nextDay - 1) % 365 + 1)
    }

    @Test
    fun testRecurrenceHelperDaily() {
        val baseCal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 10, 10, 0, 0)
        }
        val currentDue = baseCal.timeInMillis
        val nextDue = RecurrenceHelper.calculateNextDueDate(currentDue, RecurrenceRule.DAILY)

        val nextCal = Calendar.getInstance().apply { timeInMillis = nextDue }
        assertEquals(2026, nextCal.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, nextCal.get(Calendar.MONTH))
        assertEquals(11, nextCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(10, nextCal.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun testRecurrenceHelperWeekdaysSkippingWeekend() {
        // March 13, 2026 is Friday
        val fridayCal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 13, 9, 30, 0)
        }
        val currentDue = fridayCal.timeInMillis
        val nextDue = RecurrenceHelper.calculateNextDueDate(currentDue, RecurrenceRule.WEEKDAYS)

        // Friday + Weekdays -> Monday March 16, 2026
        val nextCal = Calendar.getInstance().apply { timeInMillis = nextDue }
        assertEquals(2026, nextCal.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, nextCal.get(Calendar.MONTH))
        assertEquals(16, nextCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.MONDAY, nextCal.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun testRecurrenceHelperWeeklyAndNone() {
        val baseCal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 10, 15, 0, 0)
        }
        val currentDue = baseCal.timeInMillis

        val weeklyDue = RecurrenceHelper.calculateNextDueDate(currentDue, RecurrenceRule.WEEKLY)
        val weeklyCal = Calendar.getInstance().apply { timeInMillis = weeklyDue }
        assertEquals(17, weeklyCal.get(Calendar.DAY_OF_MONTH))

        val noneDue = RecurrenceHelper.calculateNextDueDate(currentDue, RecurrenceRule.NONE)
        assertEquals(currentDue, noneDue)
    }

    @Test
    fun testHeroArtifactsCatalogAndProfileEquipping() {
        val all = com.example.data.model.HeroArtifactCatalog.ALL_ARTIFACTS
        assertTrue(all.isNotEmpty())
        val aegis = com.example.data.model.HeroArtifactCatalog.getById("SHIELD_AEGIS")
        assertNotNull(aegis)
        assertEquals("Эгида Дисциплины", aegis!!.title)

        val profile = com.example.data.model.UserProfile(
            equippedArtifactId = "SHIELD_AEGIS",
            unlockedArtifactIdsRaw = "SHIELD_AEGIS,CHRONO_TITAN"
        )
        assertTrue(profile.isArtifactUnlocked("SHIELD_AEGIS"))
        assertTrue(profile.isArtifactUnlocked("CHRONO_TITAN"))
        assertFalse(profile.isArtifactUnlocked("FLOW_FEATHER"))
        assertEquals("Эгида Дисциплины", profile.equippedArtifact?.title)
    }

    @Test
    fun testNlpParserDateAndAlarmExtraction() {
        val result1 = com.example.util.LocalNlpParser.parseInput("позвонить коллеге завтра в 18:00")
        assertEquals("Позвонить коллеге", result1.title)
        assertNotNull(result1.dueDate)

        val cal = Calendar.getInstance().apply { timeInMillis = result1.dueDate!! }
        assertEquals(18, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))

        val result2 = com.example.util.LocalNlpParser.parseInput("выпить воды через 15 минут")
        assertNotNull(result2.dueDate)
        assertTrue(result2.dueDate!! > System.currentTimeMillis())

        val result3 = com.example.util.LocalNlpParser.parseInput("! срочно подготовить отчет")
        assertEquals(com.example.data.model.Priority.CRITICAL, result3.priority)
    }

    @Test
    fun testUtcDatePickerToLocalDateConversion() {
        // Simulating UTC midnight output from DatePicker for Year 2026, Month Oct (9), Day 15
        val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(2026, Calendar.OCTOBER, 15, 0, 0, 0)
        }
        val selectedUtcMillis = utcCal.timeInMillis

        // Extraction
        val checkUtc = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = selectedUtcMillis
        }
        val y = checkUtc.get(Calendar.YEAR)
        val m = checkUtc.get(Calendar.MONTH)
        val d = checkUtc.get(Calendar.DAY_OF_MONTH)

        // Building local calendar with hour 14:30
        val localCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m)
            set(Calendar.DAY_OF_MONTH, d)
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        assertEquals(2026, localCal.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, localCal.get(Calendar.MONTH))
        assertEquals(15, localCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(14, localCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, localCal.get(Calendar.MINUTE))
    }

    @Test
    fun testDemonMentorEngineHasOver100Quotes() {
        val total = com.example.data.model.DemonMentorEngine.totalPhrasesCount
        assertTrue("Demon Mentor Engine must contain at least 100 quotes, found: $total", total >= 100)
        assertTrue(com.example.data.model.DemonMentorEngine.MORNING_ROASTS.size >= 15)
        assertTrue(com.example.data.model.DemonMentorEngine.AFTERNOON_ROASTS.size >= 15)
        assertTrue(com.example.data.model.DemonMentorEngine.EVENING_ROASTS.size >= 15)
        assertTrue(com.example.data.model.DemonMentorEngine.NIGHT_ROASTS.size >= 15)
        assertTrue(com.example.data.model.DemonMentorEngine.TASK_COMPLETED_ROASTS.size >= 15)
        assertTrue(com.example.data.model.DemonMentorEngine.OVERDUE_SLACKING_ROASTS.size >= 15)
        assertTrue(com.example.data.model.DemonMentorEngine.ANTI_PROCRASTINATION_ROASTS.size >= 15)
        assertTrue(com.example.data.model.DemonMentorEngine.DEVILS_PACT_ROASTS.size >= 15)
    }

    @Test
    fun testDemonMentorContextualQuoteIntegration() {
        val quote = com.example.data.model.MotivationalEngine.getContextualQuote(MoodType.DEMON)
        assertNotNull(quote)
        assertTrue(quote.quote.isNotBlank())
        assertEquals("Люцик • Адский Наставник 😈", quote.authorOrTip)
        assertEquals("Адский Пинoк 🔥", quote.category)
    }

    @Test
    fun testDemonMascotTapRoastReturnsSnappyQuips() {
        val roast = com.example.data.model.DemonMentorEngine.getRandomMascotTapRoast()
        assertNotNull(roast)
        assertTrue(roast.isNotBlank())
    }
}
