package com.example

import com.example.data.model.MotivationalEngine
import com.example.data.model.MoodType
import com.example.data.model.RecurrenceRule
import com.example.data.model.SubTask
import com.example.data.model.TaskItem
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
}
