package com.example

import com.example.data.model.Priority
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile
import com.example.util.DataExportImportEngine
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DataExportImportEngineTest {

    @Test
    fun `exportToJson serializes UserProfile and TaskItems correctly`() {
        val profile = UserProfile(
            heroName = "Герой Фокуса",
            heroTitle = "Мастер Тасков",
            level = 5,
            xp = 1200,
            coins = 350,
            streakDays = 10,
            bestStreak = 14,
            totalQuestsCompleted = 12,
            totalTasksCompleted = 45
        )

        val task1 = TaskItem(
            id = 101L,
            title = "Тестовая задача 1",
            description = "Описание задачи 1",
            category = "Работа",
            priority = Priority.CRITICAL,
            energyRequired = 3,
            fieldWhy = "Важная цель",
            tagsRaw = "работа,срочно",
            isCompleted = true,
            isArchived = false,
            isStrictDeadline = true,
            subtasksRaw = "Sub1|1;Sub2|0",
            estimatedMinutes = 30,
            dueDate = 1700000000000L
        )

        val task2 = TaskItem(
            id = 102L,
            title = "Тестовая задача 2",
            description = "Описание 2",
            category = "Здоровье",
            priority = Priority.LOW,
            energyRequired = 1,
            fieldWhy = "Здоровье важна",
            tagsRaw = "спорт",
            isCompleted = false,
            isArchived = true,
            isStrictDeadline = false,
            subtasksRaw = "",
            estimatedMinutes = 15,
            dueDate = null
        )

        val jsonOutput = DataExportImportEngine.exportToJson(profile, listOf(task1, task2))
        assertNotNull(jsonOutput)
        assertTrue(jsonOutput.isNotBlank())

        val root = JSONObject(jsonOutput)
        assertTrue(root.has("profile"))
        assertTrue(root.has("tasks"))

        val profileJson = root.getJSONObject("profile")
        assertEquals("Герой Фокуса", profileJson.getString("heroName"))
        assertEquals("Мастер Тасков", profileJson.getString("heroTitle"))
        assertEquals(5, profileJson.getInt("level"))
        assertEquals(1200, profileJson.getInt("xp"))
        assertEquals(350, profileJson.getInt("coins"))
        assertEquals(10, profileJson.getInt("streakDays"))
        assertEquals(14, profileJson.getInt("bestStreak"))
        assertEquals(12, profileJson.getInt("totalQuestsCompleted"))
        assertEquals(45, profileJson.getInt("totalTasksCompleted"))

        val tasksArray = root.getJSONArray("tasks")
        assertEquals(2, tasksArray.length())

        val t1Json = tasksArray.getJSONObject(0)
        assertEquals(101L, t1Json.getLong("id"))
        assertEquals("Тестовая задача 1", t1Json.getString("title"))
        assertEquals("Описание задачи 1", t1Json.getString("description"))
        assertEquals("Работа", t1Json.getString("category"))
        assertEquals("CRITICAL", t1Json.getString("priority"))
        assertEquals(3, t1Json.getInt("energyRequired"))
        assertEquals("Важная цель", t1Json.getString("fieldWhy"))
        assertEquals("работа,срочно", t1Json.getString("tagsRaw"))
        assertTrue(t1Json.getBoolean("isCompleted"))
        assertFalse(t1Json.getBoolean("isArchived"))
        assertTrue(t1Json.getBoolean("isStrictDeadline"))
        assertEquals("Sub1|1;Sub2|0", t1Json.getString("subtasksRaw"))
        assertEquals(30, t1Json.getInt("estimatedMinutes"))
        assertEquals(1700000000000L, t1Json.getLong("dueDate"))

        val t2Json = tasksArray.getJSONObject(1)
        assertEquals(102L, t2Json.getLong("id"))
        assertEquals(0L, t2Json.getLong("dueDate"))
    }

    @Test
    fun `exportToJson handles empty tasks list`() {
        val profile = UserProfile()
        val jsonOutput = DataExportImportEngine.exportToJson(profile, emptyList())
        val root = JSONObject(jsonOutput)

        assertTrue(root.has("profile"))
        assertTrue(root.has("tasks"))
        assertEquals(0, root.getJSONArray("tasks").length())
    }

    @Test
    fun `parseJsonTasks correctly parses tasks from full export JSON format`() {
        val jsonStr = """
            {
              "profile": {
                "heroName": "Искатель"
              },
              "tasks": [
                {
                  "title": "Импортированная задача",
                  "description": "Подробности",
                  "category": "Учеба",
                  "priority": "MEDIUM",
                  "energyRequired": 3,
                  "fieldWhy": "Развитие",
                  "tagsRaw": "учеба",
                  "subtasksRaw": "Шаг 1|1",
                  "isCompleted": true,
                  "isArchived": false,
                  "isStrictDeadline": true
                }
              ]
            }
        """.trimIndent()

        val parsedTasks = DataExportImportEngine.parseJsonTasks(jsonStr)
        assertEquals(1, parsedTasks.size)

        val task = parsedTasks[0]
        assertEquals("Импортированная задача", task.title)
        assertEquals("Подробности", task.description)
        assertEquals("Учеба", task.category)
        assertEquals(Priority.MEDIUM, task.priority)
        assertEquals(3, task.energyRequired)
        assertEquals("Развитие", task.fieldWhy)
        assertEquals("учеба", task.tagsRaw)
        assertEquals("Шаг 1|1", task.subtasksRaw)
        assertTrue(task.isCompleted)
        assertFalse(task.isArchived)
        assertTrue(task.isStrictDeadline)
    }

    @Test
    fun `parseJsonTasks correctly parses raw JSONArray string format`() {
        val jsonStr = """
            [
              {
                "title": "Задача из массива",
                "description": "Описание из массива",
                "category": "Дом",
                "priority": "LOW",
                "energyRequired": 1,
                "isCompleted": false
              }
            ]
        """.trimIndent()

        val parsedTasks = DataExportImportEngine.parseJsonTasks(jsonStr)
        assertEquals(1, parsedTasks.size)

        val task = parsedTasks[0]
        assertEquals("Задача из массива", task.title)
        assertEquals("Описание из массива", task.description)
        assertEquals("Дом", task.category)
        assertEquals(Priority.LOW, task.priority)
        assertEquals(1, task.energyRequired)
        assertFalse(task.isCompleted)
    }

    @Test
    fun `parseJsonTasks handles invalid priority gracefully by defaulting to HIGH`() {
        val jsonStr = """
            {
              "tasks": [
                {
                  "title": "Задача с неизвестным priority",
                  "priority": "UNKNOWN_PRIORITY_LEVEL"
                }
              ]
            }
        """.trimIndent()

        val parsedTasks = DataExportImportEngine.parseJsonTasks(jsonStr)
        assertEquals(1, parsedTasks.size)
        assertEquals(Priority.HIGH, parsedTasks[0].priority)
    }

    @Test
    fun `parseJsonTasks handles missing optional fields with default fallback values`() {
        val jsonStr = """
            {
              "tasks": [
                {}
              ]
            }
        """.trimIndent()

        val parsedTasks = DataExportImportEngine.parseJsonTasks(jsonStr)
        assertEquals(1, parsedTasks.size)

        val task = parsedTasks[0]
        assertEquals("Задача", task.title)
        assertEquals("", task.description)
        assertEquals("Общие", task.category)
        assertEquals(Priority.HIGH, task.priority)
        assertEquals(2, task.energyRequired)
        assertEquals("", task.fieldWhy)
        assertFalse(task.isCompleted)
        assertFalse(task.isArchived)
        assertFalse(task.isStrictDeadline)
    }

    @Test
    fun `parseJsonTasks returns empty list on malformed or empty JSON`() {
        val invalidJson = "{ invalid_json_content }"
        val tasksFromInvalid = DataExportImportEngine.parseJsonTasks(invalidJson)
        assertTrue(tasksFromInvalid.isEmpty())

        val emptyTasks = DataExportImportEngine.parseJsonTasks("")
        assertTrue(emptyTasks.isEmpty())
    }

    @Test
    fun `exportToCsv outputs header and correctly escapes quotes in string fields`() {
        val task = TaskItem(
            title = "Задача \"в кавычках\"",
            description = "Описание с \"кавычками\"",
            category = "Категория \"Тест\"",
            priority = Priority.HIGH,
            energyRequired = 2,
            fieldWhy = "Причина \"Зачем\"",
            isCompleted = true,
            isArchived = false,
            isStrictDeadline = true
        )

        val csv = DataExportImportEngine.exportToCsv(listOf(task))
        val lines = csv.lines().filter { it.isNotBlank() }

        assertEquals(2, lines.size)
        assertEquals("Title,Description,Category,Priority,Energy,Why,Completed,Archived,StrictDeadline", lines[0])

        val expectedRow = "\"Задача \"\"в кавычках\"\"\",\"Описание с \"\"кавычками\"\"\",\"Категория \"\"Тест\"\"\",HIGH,2,\"Причина \"\"Зачем\"\"\",true,false,true"
        assertEquals(expectedRow, lines[1])
    }

    @Test
    fun `parseCsvTasks parses CSV correctly and removes surrounding quotes`() {
        val csvStr = """
            Title,Description,Category,Priority,Energy,Why,Completed,Archived,StrictDeadline
            "Импортированная CSV задача","Описание из CSV","Развитие",HIGH,2,"Причина",false,false,false
            "Простая задача без опциональных полей"
        """.trimIndent()

        val tasks = DataExportImportEngine.parseCsvTasks(csvStr)
        assertEquals(2, tasks.size)

        val task1 = tasks[0]
        assertEquals("Импортированная CSV задача", task1.title)
        assertEquals("Описание из CSV", task1.description)
        assertEquals("Развитие", task1.category)

        val task2 = tasks[1]
        assertEquals("Простая задача без опциональных полей", task2.title)
        assertEquals("", task2.description)
        assertEquals("Импорт", task2.category)
    }

    @Test
    fun `parseCsvTasks returns empty list for empty or header-only CSV`() {
        assertTrue(DataExportImportEngine.parseCsvTasks("").isEmpty())

        val headerOnly = "Title,Description,Category,Priority,Energy,Why,Completed,Archived,StrictDeadline"
        assertTrue(DataExportImportEngine.parseCsvTasks(headerOnly).isEmpty())
    }
}
