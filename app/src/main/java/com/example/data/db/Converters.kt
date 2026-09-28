package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.*

class Converters {
    @TypeConverter
    fun fromPriority(value: Priority?): String = value?.name ?: Priority.HIGH.name

    @TypeConverter
    fun toPriority(value: String?): Priority = try {
        Priority.valueOf(value ?: Priority.HIGH.name)
    } catch (e: Exception) {
        Priority.HIGH
    }

    @TypeConverter
    fun fromMoodType(value: MoodType?): String = value?.name ?: MoodType.FOCUS.name

    @TypeConverter
    fun toMoodType(value: String?): MoodType = try {
        MoodType.valueOf(value ?: MoodType.FOCUS.name)
    } catch (e: Exception) {
        MoodType.FOCUS
    }

    @TypeConverter
    fun fromThemeMode(value: ThemeMode?): String = value?.name ?: ThemeMode.SYSTEM.name

    @TypeConverter
    fun toThemeMode(value: String?): ThemeMode = try {
        ThemeMode.valueOf(value ?: ThemeMode.SYSTEM.name)
    } catch (e: Exception) {
        ThemeMode.SYSTEM
    }

    @TypeConverter
    fun fromAppIconStyle(value: AppIconStyle?): String = value?.name ?: AppIconStyle.SHIELD.name

    @TypeConverter
    fun toAppIconStyle(value: String?): AppIconStyle = try {
        AppIconStyle.valueOf(value ?: AppIconStyle.SHIELD.name)
    } catch (e: Exception) {
        AppIconStyle.SHIELD
    }

    @TypeConverter
    fun fromNotificationTone(value: NotificationTone?): String = value?.name ?: NotificationTone.CARING.name

    @TypeConverter
    fun toNotificationTone(value: String?): NotificationTone = try {
        NotificationTone.valueOf(value ?: NotificationTone.CARING.name)
    } catch (e: Exception) {
        NotificationTone.CARING
    }

    @TypeConverter
    fun fromRecurrenceRule(value: RecurrenceRule?): String = value?.name ?: RecurrenceRule.NONE.name

    @TypeConverter
    fun toRecurrenceRule(value: String?): RecurrenceRule = try {
        RecurrenceRule.valueOf(value ?: RecurrenceRule.NONE.name)
    } catch (e: Exception) {
        RecurrenceRule.NONE
    }
}
