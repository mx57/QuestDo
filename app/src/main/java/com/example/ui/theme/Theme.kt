package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.MoodType
import com.example.data.model.ThemeMode

fun getMoodColorScheme(mood: MoodType, isDark: Boolean, isAmoled: Boolean = false): androidx.compose.material3.ColorScheme {
    return when (mood) {
        MoodType.FOCUS -> {
            if (isDark) {
                darkColorScheme(
                    primary = FocusPrimaryDark,
                    onPrimary = Color.Black,
                    primaryContainer = FocusPrimaryLight,
                    onPrimaryContainer = Color.White,
                    secondary = FocusSecondary,
                    onSecondary = Color.White,
                    background = if (isAmoled) AmoledBackground else FocusSurfaceDark,
                    surface = if (isAmoled) AmoledSurface else FocusSurfaceDark,
                    surfaceVariant = if (isAmoled) AmoledCard else FocusCardDark,
                    tertiary = GoldAccent
                )
            } else {
                lightColorScheme(
                    primary = FocusPrimaryLight,
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFE0E7FF),
                    onPrimaryContainer = FocusPrimaryLight,
                    secondary = FocusSecondary,
                    onSecondary = Color.White,
                    background = FocusSurfaceLight,
                    surface = Color.White,
                    surfaceVariant = Color(0xFFEEF2F6),
                    tertiary = GoldAccent
                )
            }
        }
        MoodType.CALM -> {
            if (isDark) {
                darkColorScheme(
                    primary = CalmPrimaryDark,
                    onPrimary = Color.Black,
                    primaryContainer = CalmPrimaryLight,
                    onPrimaryContainer = Color.White,
                    secondary = CalmSecondary,
                    onSecondary = Color.White,
                    background = if (isAmoled) AmoledBackground else CalmSurfaceDark,
                    surface = if (isAmoled) AmoledSurface else CalmSurfaceDark,
                    surfaceVariant = if (isAmoled) AmoledCard else CalmCardDark,
                    tertiary = GoldAccent
                )
            } else {
                lightColorScheme(
                    primary = CalmPrimaryLight,
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFCCFBF1),
                    onPrimaryContainer = CalmPrimaryLight,
                    secondary = CalmSecondary,
                    onSecondary = Color.White,
                    background = CalmSurfaceLight,
                    surface = Color.White,
                    surfaceVariant = Color(0xFFE6F4EA),
                    tertiary = GoldAccent
                )
            }
        }
        MoodType.OVERCOME -> {
            if (isDark) {
                darkColorScheme(
                    primary = OvercomePrimaryDark,
                    onPrimary = Color.Black,
                    primaryContainer = OvercomePrimaryLight,
                    onPrimaryContainer = Color.White,
                    secondary = OvercomeSecondary,
                    onSecondary = Color.Black,
                    background = if (isAmoled) AmoledBackground else OvercomeSurfaceDark,
                    surface = if (isAmoled) AmoledSurface else OvercomeSurfaceDark,
                    surfaceVariant = if (isAmoled) AmoledCard else OvercomeCardDark,
                    tertiary = GoldAccent
                )
            } else {
                lightColorScheme(
                    primary = OvercomePrimaryLight,
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFFEE2E2),
                    onPrimaryContainer = OvercomePrimaryLight,
                    secondary = OvercomeSecondary,
                    onSecondary = Color.White,
                    background = OvercomeSurfaceLight,
                    surface = Color.White,
                    surfaceVariant = Color(0xFFFDE8E8),
                    tertiary = GoldAccent
                )
            }
        }
        MoodType.CARE -> {
            if (isDark) {
                darkColorScheme(
                    primary = CarePrimaryDark,
                    onPrimary = Color.Black,
                    primaryContainer = CarePrimaryLight,
                    onPrimaryContainer = Color.White,
                    secondary = CareSecondary,
                    onSecondary = Color.White,
                    background = if (isAmoled) AmoledBackground else CareSurfaceDark,
                    surface = if (isAmoled) AmoledSurface else CareSurfaceDark,
                    surfaceVariant = if (isAmoled) AmoledCard else CareCardDark,
                    tertiary = GoldAccent
                )
            } else {
                lightColorScheme(
                    primary = CarePrimaryLight,
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFFCE7F3),
                    onPrimaryContainer = CarePrimaryLight,
                    secondary = CareSecondary,
                    onSecondary = Color.White,
                    background = CareSurfaceLight,
                    surface = Color.White,
                    surfaceVariant = Color(0xFFFDF2F8),
                    tertiary = GoldAccent
                )
            }
        }
        MoodType.DEMON -> {
            // Hell / Infernal fiery theme
            darkColorScheme(
                primary = DemonPrimaryDark,
                onPrimary = Color.White,
                primaryContainer = Color(0xFF4A0812),
                onPrimaryContainer = Color(0xFFFFB4AB),
                secondary = DemonSecondary,
                onSecondary = Color.Black,
                secondaryContainer = Color(0xFF6B2200),
                onSecondaryContainer = Color(0xFFFFDBCF),
                background = if (isAmoled) AmoledBackground else DemonSurfaceDark,
                surface = if (isAmoled) AmoledSurface else DemonSurfaceDark,
                surfaceVariant = if (isAmoled) AmoledCard else DemonCardDark,
                tertiary = DemonTertiary,
                onTertiary = Color.Black,
                outline = DemonBorder,
                error = Color(0xFFFF5252)
            )
        }
    }
}

@Composable
fun QuestDoTheme(
    mood: MoodType = MoodType.FOCUS,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    isDemonMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val effectiveMood = if (isDemonMode) MoodType.DEMON else mood
    val systemDark = isSystemInDarkTheme()
    val isDark = when {
        isDemonMode || themeMode == ThemeMode.INFERNAL -> true
        themeMode == ThemeMode.SYSTEM -> systemDark
        themeMode == ThemeMode.LIGHT -> false
        themeMode == ThemeMode.DARK -> true
        themeMode == ThemeMode.AMOLED -> true
        else -> systemDark
    }
    val isAmoled = themeMode == ThemeMode.AMOLED

    val colorScheme = getMoodColorScheme(mood = effectiveMood, isDark = isDark, isAmoled = isAmoled)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
