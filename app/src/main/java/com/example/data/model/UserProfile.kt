package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val heroName: String = "Искатель Задач",
    val heroTitle: String = "Новичок фокуса",
    val level: Int = 1,
    val xp: Int = 0,
    val coins: Int = 50,
    val streakDays: Int = 1,
    val bestStreak: Int = 1,
    val streakFreezes: Int = 2, // 2 freezes per month
    val lastFreezeResetMonth: String = "", // "YYYY-MM"
    val inRecoveryMode: Boolean = false,
    val recoveryDaysRemaining: Int = 0,
    val consecutiveFailures: Int = 0,
    val currentEnergyLevel: Int = 2, // 1 = Low, 2 = Medium, 3 = High
    val hasCompletedOnboarding: Boolean = false,
    val showHorizon: Boolean = true, // Progress visualizer metaphor
    val eveningCheckoutDoneToday: Boolean = false,
    val lastEveningCheckoutDate: String = "",
    val lastActiveDate: String = "", // "YYYY-MM-DD"
    val tasksPerQuest: Int = 3, // 1, 2, 3, or 5
    val activeMood: MoodType = MoodType.FOCUS,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appIconStyle: AppIconStyle = AppIconStyle.SHIELD,
    val notificationTone: NotificationTone = NotificationTone.CARING,
    val soundEffectsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val dailyReminderHour: Int = 9,
    val dailyReminderMinute: Int = 0,
    val totalQuestsCompleted: Int = 0,
    val totalTasksCompleted: Int = 0,
    val totalFocusMinutes: Int = 0
) {
    val xpNeededForNextLevel: Int
        get() = level * 200

    val xpProgress: Float
        get() = (xp % xpNeededForNextLevel).toFloat() / xpNeededForNextLevel.toFloat().coerceAtLeast(1f)
}
