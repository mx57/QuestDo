package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalAiEngineTest {

    @Test
    fun testDecomposeTask_SoftwareKeywords() {
        val softwareInputs = listOf(
            "Создать сайт компании",
            "Мобильное приложение для заметок",
            "Дипломный проект по IT",
            "Написать код на Kotlin"
        )

        for (input in softwareInputs) {
            val result = LocalAiEngine.decomposeTask(input)
            assertEquals(5, result.size)
            assertTrue(result[0].contains("Сформулировать проблему"))
            assertTrue(result[1].contains("структуру проекта"))
            assertTrue(result[2].contains("главный интерфейс"))
            assertTrue(result[3].contains("быструю проверку"))
            assertTrue(result[4].contains("Зафиксировать результат"))
        }
    }

    @Test
    fun testDecomposeTask_CleaningKeywords() {
        val cleaningInputs = listOf(
            "Генеральная уборка",
            "Убрать на кухне",
            "Навести порядок",
            "Комната в чистом состоянии"
        )

        for (input in cleaningInputs) {
            val result = LocalAiEngine.decomposeTask(input)
            assertEquals(5, result.size)
            assertTrue(result[0].contains("мусор"))
            assertTrue(result[1].contains("вещи"))
            assertTrue(result[2].contains("поверхности"))
            assertTrue(result[3].contains("пол"))
            assertTrue(result[4].contains("Проветрить"))
        }
    }

    @Test
    fun testDecomposeTask_DocumentKeywords() {
        val docInputs = listOf(
            "Годовой отчет",
            "Важный документ",
            "Написать статью",
            "Презентация для клиента"
        )

        for (input in docInputs) {
            val result = LocalAiEngine.decomposeTask(input)
            assertEquals(5, result.size)
            assertTrue(result[0].contains("данные"))
            assertTrue(result[1].contains("черновой план"))
            assertTrue(result[2].contains("первый главный абзац"))
            assertTrue(result[3].contains("цифрами"))
            assertTrue(result[4].contains("опечатки"))
        }
    }

    @Test
    fun testDecomposeTask_WorkoutKeywords() {
        val workoutInputs = listOf(
            "Утренняя тренировка",
            "Заняться спортом",
            "Здоровье и фитнес",
            "Вечерний бег"
        )

        for (input in workoutInputs) {
            val result = LocalAiEngine.decomposeTask(input)
            assertEquals(5, result.size)
            assertTrue(result[0].contains("спортивную форму"))
            assertTrue(result[1].contains("разминку"))
            assertTrue(result[2].contains("подход упражнений"))
            assertTrue(result[3].contains("растяжкой"))
            assertTrue(result[4].contains("душ"))
        }
    }

    @Test
    fun testDecomposeTask_LearningKeywords() {
        val learningInputs = listOf(
            "Прочитать книгу",
            "Подготовить учебник",
            "Пройти курс по Kotlin",
            "Изучить новый язык"
        )

        for (input in learningInputs) {
            val result = LocalAiEngine.decomposeTask(input)
            assertEquals(5, result.size)
            assertTrue(result[0].contains("Не беспокоить"))
            assertTrue(result[1].contains("первые 2 страницы"))
            assertTrue(result[2].contains("3 главные мысли"))
            assertTrue(result[3].contains("конспект"))
            assertTrue(result[4].contains("паузу"))
        }
    }

    @Test
    fun testDecomposeTask_GenericFallback() {
        val title = "Купить продукты в магазине"
        val result = LocalAiEngine.decomposeTask(title)

        assertEquals(5, result.size)
        assertTrue(result[0].contains("Купить продукты в магазине"))
        assertTrue(result[2].contains("Купить"))
    }

    @Test
    fun testDecomposeTask_GenericFallbackWithTrimming() {
        val title = "   Починить велосипед   "
        val result = LocalAiEngine.decomposeTask(title)

        assertEquals(5, result.size)
        assertTrue(result[0].contains("Починить велосипед"))
        assertTrue(result[2].contains("Починить"))
    }

    @Test
    fun testDecomposeTask_EmptyOrWhitespaceTitle() {
        val result = LocalAiEngine.decomposeTask("   ")
        assertEquals(5, result.size)
        assertTrue(result[0].contains("Подготовить всё необходимое"))
        assertTrue(result[2].contains("Выполнить основной объем работы"))
    }

    @Test
    fun testDecomposeTask_CaseInsensitiveMatching() {
        val upperInput = "СОЗДАТЬ САЙТ"
        val resultUpper = LocalAiEngine.decomposeTask(upperInput)
        assertEquals(5, resultUpper.size)
        assertTrue(resultUpper[0].contains("Сформулировать проблему"))
    }
}
