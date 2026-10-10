package com.example.util

import com.example.data.model.Priority
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DataExportImportEngineTest {

    @Test
    fun exportToCsv_formatsHeaderAndTasksCorrectly() {
        val tasks = listOf(
            TaskItem(
                id = 1L,
                title = "Test Task \"Quote\"",
                description = "Task description, with comma and \"quotes\"",
                category = "Work",
                priority = Priority.CRITICAL,
                energyRequired = 3,
                fieldWhy = "Important reason",
                isCompleted = true,
                isArchived = false,
                isStrictDeadline = true
            ),
            TaskItem(
                id = 2L,
                title = "Simple Task",
                description = "No special chars",
                category = "Personal",
                priority = Priority.LOW,
                energyRequired = 1,
                fieldWhy = "Relaxation",
                isCompleted = false,
                isArchived = true,
                isStrictDeadline = false
            )
        )

        val csvOutput = DataExportImportEngine.exportToCsv(tasks)
        val lines = csvOutput.trim().lines()

        assertEquals(3, lines.size)
        assertEquals("Title,Description,Category,Priority,Energy,Why,Completed,Archived,StrictDeadline", lines[0])

        val expectedLine1 = "\"Test Task \"\"Quote\"\"\",\"Task description, with comma and \"\"quotes\"\"\",\"Work\",CRITICAL,3,\"Important reason\",true,false,true"
        assertEquals(expectedLine1, lines[1])

        val expectedLine2 = "\"Simple Task\",\"No special chars\",\"Personal\",LOW,1,\"Relaxation\",false,true,false"
        assertEquals(expectedLine2, lines[2])
    }

    @Test
    fun exportToCsv_handlesEmptyTaskList() {
        val csvOutput = DataExportImportEngine.exportToCsv(emptyList())
        val lines = csvOutput.trim().lines()

        assertEquals(1, lines.size)
        assertEquals("Title,Description,Category,Priority,Energy,Why,Completed,Archived,StrictDeadline", lines[0])
    }

    @Test
    fun parseCsvTasks_parsesValidCsvLinesCorrectly() {
        val csvData = """
            Title,Description,Category,Priority,Energy,Why,Completed,Archived,StrictDeadline
            "First Task","First Description","Work",HIGH,2,"Why 1",false,false,false
            "Second Task","Second Description","Personal",LOW,1,"Why 2",true,false,true
        """.trimIndent()

        val tasks = DataExportImportEngine.parseCsvTasks(csvData)

        assertEquals(2, tasks.size)
        assertEquals("First Task", tasks[0].title)
        assertEquals("First Description", tasks[0].description)
        assertEquals("Work", tasks[0].category)
        assertEquals(Priority.HIGH, tasks[0].priority)

        assertEquals("Second Task", tasks[1].title)
        assertEquals("Second Description", tasks[1].description)
        assertEquals("Personal", tasks[1].category)
        assertEquals(Priority.HIGH, tasks[1].priority) // parseCsvTasks sets Priority.HIGH by default
    }

    @Test
    fun parseCsvTasks_handlesMissingFieldsAndDefaults() {
        val csvData = """
            Title,Description,Category
            "Minimal Task"
        """.trimIndent()

        val tasks = DataExportImportEngine.parseCsvTasks(csvData)

        assertEquals(1, tasks.size)
        assertEquals("Minimal Task", tasks[0].title)
        assertEquals("", tasks[0].description)
        assertEquals("Импорт", tasks[0].category)
    }

    @Test
    fun parseCsvTasks_returnsEmptyListForEmptyOrHeaderOnlyCsv() {
        assertTrue(DataExportImportEngine.parseCsvTasks("").isEmpty())
        assertTrue(DataExportImportEngine.parseCsvTasks("Title,Description,Category").isEmpty())
    }

    @Test
    fun parseCsvTasks_skipsLinesWithBlankTitle() {
        val csvData = """
            Title,Description,Category
            "", "Desc", "Cat"
            "   ", "Desc2", "Cat2"
            "Valid Title", "Desc3", "Cat3"
        """.trimIndent()

        val tasks = DataExportImportEngine.parseCsvTasks(csvData)

        assertEquals(1, tasks.size)
        assertEquals("Valid Title", tasks[0].title)
    }

    @Test
    fun exportToJson_and_parseJsonTasks_roundtrip() {
        val profile = UserProfile(
            heroName = "Hero",
            heroTitle = "Warrior",
            level = 10,
            xp = 1500,
            coins = 200,
            streakDays = 5,
            bestStreak = 12,
            totalQuestsCompleted = 30,
            totalTasksCompleted = 45
        )

        val originalTasks = listOf(
            TaskItem(
                id = 101L,
                title = "JSON Task 1",
                description = "Desc 1",
                category = "Study",
                priority = Priority.MEDIUM,
                energyRequired = 4,
                fieldWhy = "Growth",
                tagsRaw = "tag1,tag2",
                subtasksRaw = "[{\"title\":\"Sub 1\",\"isDone\":true}]",
                isCompleted = true,
                isArchived = false,
                isStrictDeadline = true
            )
        )

        val jsonStr = DataExportImportEngine.exportToJson(profile, originalTasks)
        val parsedTasks = DataExportImportEngine.parseJsonTasks(jsonStr)

        assertEquals(1, parsedTasks.size)
        val parsed = parsedTasks[0]
        assertEquals("JSON Task 1", parsed.title)
        assertEquals("Desc 1", parsed.description)
        assertEquals("Study", parsed.category)
        assertEquals(Priority.MEDIUM, parsed.priority)
        assertEquals(4, parsed.energyRequired)
        assertEquals("Growth", parsed.fieldWhy)
        assertEquals("tag1,tag2", parsed.tagsRaw)
        assertEquals("[{\"title\":\"Sub 1\",\"isDone\":true}]", parsed.subtasksRaw)
        assertTrue(parsed.isCompleted)
        assertFalse(parsed.isArchived)
        assertTrue(parsed.isStrictDeadline)
    }

    @Test
    fun parseJsonTasks_handlesArrayOnlyJsonAndInvalidJson() {
        val jsonArrayOnly = """
            [
                {
                    "title": "Array Task",
                    "priority": "LOW",
                    "energyRequired": 1
                }
            ]
        """.trimIndent()

        val tasksFromArray = DataExportImportEngine.parseJsonTasks(jsonArrayOnly)
        assertEquals(1, tasksFromArray.size)
        assertEquals("Array Task", tasksFromArray[0].title)
        assertEquals(Priority.LOW, tasksFromArray[0].priority)

        val invalidTasks = DataExportImportEngine.parseJsonTasks("invalid json string {{{")
        assertTrue(invalidTasks.isEmpty())
    }
}
