package com.example.data.model

data class HeroArtifact(
    val id: String,
    val title: String,
    val description: String,
    val perkRu: String,
    val iconEmoji: String,
    val costCoins: Int,
    val levelRequired: Int
)

object HeroArtifactCatalog {
    val ALL_ARTIFACTS = listOf(
        HeroArtifact(
            id = "SHIELD_AEGIS",
            title = "Эгида Дисциплины",
            description = "Древний щит, закаленный в огне ежедневных привычек.",
            perkRu = "Оберегает серию квестов и дает +10% к стойкости",
            iconEmoji = "🛡️",
            costCoins = 0,
            levelRequired = 1
        ),
        HeroArtifact(
            id = "CHRONO_TITAN",
            title = "Хронометр Титана",
            description = "Магические песочные часы, замедляющие бег отвлекающих мыслей.",
            perkRu = "+15 XP за каждую сессию глубокого фокуса",
            iconEmoji = "⏳",
            costCoins = 60,
            levelRequired = 1
        ),
        HeroArtifact(
            id = "ENERGY_AMULET",
            title = "Амулет Свежей Энергии",
            description = "Самоцвет, излучающий бодрость даже при упадке сил.",
            perkRu = "Снижает сопротивление перед стартом сложных задач",
            iconEmoji = "⚡",
            costCoins = 100,
            levelRequired = 2
        ),
        HeroArtifact(
            id = "BOSS_BLADE",
            title = "Клинок Победителя Боссов",
            description = "Легендарный меч, рассекающий монстров прокрастинации.",
            perkRu = "Удваивает награду (+100% 🪙) за сокрушение Босс-задач",
            iconEmoji = "🐉",
            costCoins = 150,
            levelRequired = 2
        ),
        HeroArtifact(
            id = "FLOW_FEATHER",
            title = "Перо Абсолютного Потока",
            description = "Невесомое перо мифической птицы вдохновения.",
            perkRu = "+5 монет дополнительно за каждый закрытый микро-квест",
            iconEmoji = "🪶",
            costCoins = 200,
            levelRequired = 3
        )
    )

    fun getById(id: String): HeroArtifact? = ALL_ARTIFACTS.find { it.id == id }
}
