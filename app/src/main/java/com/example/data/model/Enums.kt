package com.example.data.model

enum class Priority(val titleRu: String, val level: Int) {
    CRITICAL("Срочно и Важно (Q1)", 4),
    HIGH("Важно, не срочно (Q2)", 3),
    MEDIUM("Срочно, не важно (Q3)", 2),
    LOW("Не срочно, не важно (Q4)", 1)
}

enum class MoodType(val titleRu: String, val descriptionRu: String, val iconEmoji: String) {
    FOCUS("Фокус", "Максимальная концентрация и минимализм", "🎯"),
    CALM("Спокойствие", "Мягкий темп, дыхание и антистресс", "🌿"),
    OVERCOME("Преодоление", "Энергия воина, дисциплина и напор", "⚡"),
    CARE("Забота о себе", "Теплота, признание усталости и баланс", "💖")
}

enum class ThemeMode(val titleRu: String) {
    SYSTEM("Как в системе"),
    LIGHT("Светлая тема"),
    DARK("Темная тема"),
    AMOLED("AMOLED Черная")
}

enum class AppIconStyle(val titleRu: String, val emoji: String) {
    SHIELD("Рыцарский Щит", "🛡️"),
    TROPHY("Золотой Кубок", "🏆"),
    LOTUS("Дзен Лотос", "🪷"),
    CYBER("Кибернеон", "⚡"),
    PHOENIX("Пламенный Феникс", "🔥")
}

enum class NotificationTone(val titleRu: String, val sampleMessage: String) {
    CARING("Заботливый друг", "Привет! Ты отлично справляешься. Давай сделаем один крошечный шаг?"),
    STRICT("Строгий наставник", "Уровень сам себя не пройдет. Соберись и закрой задачу!"),
    MINDFUL("Осознанный помощник", "Заметь свое дыхание. Какое одно простое действие продвинет тебя вперед?")
}

enum class RecurrenceRule(val titleRu: String) {
    NONE("Без повтора"),
    DAILY("Каждый день"),
    WEEKDAYS("По будням"),
    WEEKLY("Раз в неделю")
}
