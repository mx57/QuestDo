package com.example.util

object LocalAiEngine {

    /**
     * Local AI task decomposition algorithm that generates 4-6 concrete micro-steps (15 mins each).
     * Works 100% offline without external network calls.
     */
    fun decomposeTask(taskTitle: String): List<String> {
        val title = taskTitle.trim()
        val lower = title.lowercase()

        return when {
            lower.contains("сайт") || lower.contains("приложение") || lower.contains("проект") || lower.contains("код") -> listOf(
                "Сформулировать проблему и требования к первому шагу",
                "Подготовить структуру проекта и базовые файлы",
                "Реализовать главный интерфейс / ключевую функцию",
                "Провести быструю проверку и исправить мелкие ошибки",
                "Зафиксировать результат и подготовить релиз"
            )

            lower.contains("уборк") || lower.contains("убрать") || lower.contains("порядок") || lower.contains("комнат") -> listOf(
                "Выбросить ненужный мусор и бумаги",
                "Разложить вещи по своим местам",
                "Протереть рабочие поверхности и стол",
                "Пропылесосить или помыть пол",
                "Проветрить помещение и налить чистой воды"
            )

            lower.contains("отчет") || lower.contains("документ") || lower.contains("стать") || lower.contains("презентац") -> listOf(
                "Собрать исходные данные и ключевые факты",
                "Набросать черновой план из 3 главных разделов",
                "Написать первый главный абзац / слайд",
                "Дополнить фактами, цифрами и оформить список",
                "Перечитать, исправить опечатки и сохранить"
            )

            lower.contains("тренировк") || lower.contains("спорт") || lower.contains("здоров") || lower.contains("бег") -> listOf(
                "Переодеться в удобную спортивную форму",
                "Сделать 5-минутную легкую разминку суставов",
                "Выполнить главный подход упражнений / пробежки",
                "Завершить мягкой растяжкой и заминкой",
                "Выпить стакан воды и принять душ"
            )

            lower.contains("книг") || lower.contains("учеб") || lower.contains("курс") || lower.contains("изучи") -> listOf(
                "Убрать телефон и включить режим «Не беспокоить»",
                "Прочитать заглавие и первые 2 страницы",
                "Выделить 3 главные мысли или ключевых термина",
                "Записать краткий конспект в блокнот",
                "Сделать 1-минутную паузу и обдумать прочитанное"
            )

            else -> generateGenericMicroSteps(title)
        }
    }

    private fun generateGenericMicroSteps(title: String): List<String> {
        val actionWord = title.split(" ").firstOrNull() ?: "Действие"
        return listOf(
            "Подготовить всё необходимое для «$title»",
            "Сделать первый легкий шаг на 2 минуты (без идеализма)",
            "Выполнить основной объем работы ($actionWord)",
            "Проверить промежуточный результат",
            "Завершить дело и зафиксировать маленькую победу"
        )
    }
}
