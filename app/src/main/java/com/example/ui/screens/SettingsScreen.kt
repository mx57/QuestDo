package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.example.data.model.*
import com.example.ui.theme.GoldAccent
import java.util.Calendar

@Composable
fun SettingsScreen(
    userProfile: UserProfile,
    onSetMood: (MoodType) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetTasksPerQuest: (Int) -> Unit,
    onSetAppIconStyle: (AppIconStyle) -> Unit,
    onSetNotificationTone: (NotificationTone) -> Unit,
    onSetSoundEffects: (Boolean) -> Unit,
    onSetHaptics: (Boolean) -> Unit,
    onSetNotifications: (Boolean) -> Unit,
    onSetDailyReminderTime: (hour: Int, minute: Int) -> Unit,
    onTestNotification: () -> Unit,
    onTestAlarm: () -> Unit,
    onToggleDemonMode: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Разрешение на уведомления получено!", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SECTION: Alarms, Timers & Notifications System Check
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔔", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Будильники, таймеры и уведомления",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Проверка работы всех сигналов и напоминаний",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Permission Status Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (hasNotificationPermission) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (hasNotificationPermission) "Системные уведомления активны" else "Требуется разрешение системы",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (hasNotificationPermission) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = if (hasNotificationPermission) "Будильники и таймеры доставят сигналы вовремя" else "Нажмите, чтобы включить всплывающие окна и звуки",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (hasNotificationPermission) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Включить", fontSize = 12.sp)
                                }
                            } else {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Уведомления и будильники", fontWeight = FontWeight.SemiBold)
                            Text("Отправка напоминаний о квестах", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = userProfile.notificationsEnabled,
                            onCheckedChange = { onSetNotifications(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Звуковые эффекты и мелодии", fontWeight = FontWeight.SemiBold)
                            Text("Сигнал таймера и колокольчик побед", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = userProfile.soundEffectsEnabled,
                            onCheckedChange = { onSetSoundEffects(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Тактильная отдача (вибрация)", fontWeight = FontWeight.SemiBold)
                            Text("Вибросигналы будильника и квестов", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = userProfile.hapticsEnabled,
                            onCheckedChange = { onSetHaptics(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Daily Reminder Time
                    Text("Ежедневный страж дисциплины (время):", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(Pair(8, 0), Pair(9, 0), Pair(10, 0), Pair(20, 0)).forEach { (h, m) ->
                            val isSelected = userProfile.dailyReminderHour == h && userProfile.dailyReminderMinute == m
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetDailyReminderTime(h, m) },
                                label = { Text(String.format("%02d:%02d", h, m), fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Spacer(modifier = Modifier.height(14.dp))

                    val alarmManager = remember(context) { context.getSystemService(android.app.AlarmManager::class.java) }
                    val canScheduleExact = remember(context) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            alarmManager?.canScheduleExactAlarms() != false
                        } else true
                    }
                    val canDrawOverlays = remember(context) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Settings.canDrawOverlays(context)
                        } else true
                    }

                    // Exact Alarm Warning banner if permission missing
                    if (!canScheduleExact) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        try {
                                            context.startActivity(
                                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                    data = Uri.parse("package:${context.packageName}")
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                            )
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Откройте Настройки -> Приложения -> QuestDo -> Будильники", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚠️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Точные будильники отключены в системе",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Нажмите, чтобы разрешить точные сигналы для своевременных напоминаний",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    // Proprietary QuestDo Alarm & Overlay Info Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⏰", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Фирменный Будильник QuestDo",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Собственное окно поверх экрана при срабатывании, звуковой сигнал набата, правило 5 секунд и кнопка мгновенного старта вместо внешних приложений.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Overlay Permission Button / Status
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !canDrawOverlays) {
                                            try {
                                                context.startActivity(
                                                    Intent(
                                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                        Uri.parse("package:${context.packageName}")
                                                    ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                                )
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Откройте Настройки -> Поверх других приложений", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Layers,
                                        contentDescription = null,
                                        tint = if (canDrawOverlays) MaterialTheme.colorScheme.primary else GoldAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Поверх других приложений (Наложение)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = if (canDrawOverlays) "Разрешено: экран будильника всплывет поверх любого окна" else "Нажмите, чтобы включить наложение поверх других окон",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                SuggestionChip(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !canDrawOverlays) {
                                            try {
                                                context.startActivity(
                                                    Intent(
                                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                        Uri.parse("package:${context.packageName}")
                                                    ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                                )
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Откройте Настройки -> Поверх других приложений", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    label = { Text(if (canDrawOverlays) "Активно ✓" else "Включить ⚙️", fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Verification Action Buttons
                    Text(
                        "Проверка работы будильника и уведомлений:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onTestNotification()
                                Toast.makeText(context, "🔔 Всплывающее уведомление отправлено!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🔔 Тест пуша", fontSize = 11.sp)
                        }

                        FilledTonalButton(
                            onClick = {
                                val dummyTask = TaskItem(
                                    id = 777123L,
                                    title = "⚔️ Победа над прокрастинацией: Первый микро-шаг",
                                    category = "Квест",
                                    priority = Priority.CRITICAL
                                )
                                com.example.AlarmAlertActivity.launchAlarm(context, dummyTask)
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🚀 Экран-наложение", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            onTestAlarm()
                            Toast.makeText(context, "⏰ Будильник сработает через 10 секунд! Сверните приложение или заблокируйте экран для проверки.", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("⏳ Тест звонка в фоне через 10 сек (свернуть приложение)", fontSize = 12.sp)
                    }
                }
            }
        }

        // Section: Difficulty & Quest Length
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🎮 Сложность микро-квестов",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Количество задач, формирующих один уровень. Меньше задач — меньше тревоги и выше дофамин от частых побед!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(1, 2, 3, 5).forEach { count ->
                            val isSelected = userProfile.tasksPerQuest == count
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetTasksPerQuest(count) },
                                label = {
                                    Text(
                                        text = "$count ${if (count == 1) "задача" else if (count in 2..4) "задачи" else "задач"}",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.testTag("tasks_per_quest_$count")
                            )
                        }
                    }
                }
            }
        }

        // Section: Infernal Demon Mentor Mode
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (userProfile.isDemonMode) androidx.compose.ui.graphics.Color(0xFF26050A) else MaterialTheme.colorScheme.surface
                ),
                border = if (userProfile.isDemonMode) androidx.compose.foundation.BorderStroke(1.5.dp, androidx.compose.ui.graphics.Color(0xFFFF1744)) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("😈🔥", fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Режим Демона-наставника",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (userProfile.isDemonMode) androidx.compose.ui.graphics.Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Черный юмор, сарказм и адская тема",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (userProfile.isDemonMode) androidx.compose.ui.graphics.Color(0xFFFF8A80) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = userProfile.isDemonMode,
                            onCheckedChange = { onToggleDemonMode(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Включает Люцика — едкого адского наставника с базой из 120+ язвительных цитат, черную риторику, перекрашивает интерфейс в кроваво-алый цвет, добавляет чертиков, «Сделку с Дьяволом» и Котёл Грехов.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (userProfile.isDemonMode) androidx.compose.ui.graphics.Color(0xFFFFCDD2) else MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Section: Psychological Moods
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🧠 Психологический настрой (Муд)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Переключает цветовую палитру интерфейса, характер цитат и психологические советы под ваше состояние.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MoodType.values().forEach { mood ->
                            val isSelected = userProfile.activeMood == mood
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSetMood(mood) }
                                    .testTag("mood_option_${mood.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(mood.iconEmoji, fontSize = 26.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mood.titleRu,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = mood.descriptionRu,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Visual Themes (Light, Dark, AMOLED)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🎨 Тема оформления",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ThemeMode.values().forEach { mode ->
                            val isSelected = userProfile.themeMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetThemeMode(mode) },
                                label = { Text(mode.titleRu, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Section: App Icon Style Customizer
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "📱 Стиль иконки приложения",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Персонализируйте герб приложения на экране и в виджетах:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AppIconStyle.values().forEach { iconStyle ->
                            val isSelected = userProfile.appIconStyle == iconStyle
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { onSetAppIconStyle(iconStyle) }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(iconStyle.emoji, fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = iconStyle.titleRu.split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Notification & Mentor Tone
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🔔 Тональность мотивации",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NotificationTone.values().forEach { tone ->
                        val isSelected = userProfile.notificationTone == tone
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSetNotificationTone(tone) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = isSelected, onClick = { onSetNotificationTone(tone) })
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(tone.titleRu, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "«${tone.sampleMessage}»",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Desktop Widget Preview
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "📲 Виджет для рабочего стола Android",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Отображает текущий микро-квест и мотивацию дня прямо на экране телефона.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(userProfile.appIconStyle.emoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("QuestDo Widget", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text("🔥 ${userProfile.streakDays} дн.", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GoldAccent)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Уровень ${userProfile.level}: Врата Концентрации", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Закрыто 2/3 задач • Опыт +120 XP", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { 0.66f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
