package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TaskItem
import com.example.ui.theme.GoldAccent
import com.example.util.NotificationHelper
import com.example.util.QuestAlarmScheduler
import com.example.util.SoundEffectsHelper
import kotlinx.coroutines.delay

@Composable
fun FocusTimerScreen(
    spotlightTask: TaskItem?,
    currentQuestTasks: List<TaskItem>,
    onCompleteSession: (minutes: Int) -> Unit,
    onSelectTask: (TaskItem) -> Unit
) {
    val context = LocalContext.current
    val soundHelper = remember { SoundEffectsHelper(context) }

    var totalTimeMinutes by remember { mutableIntStateOf(spotlightTask?.estimatedMinutes ?: 25) }
    var timeLeftSeconds by remember { mutableIntStateOf(totalTimeMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var targetEndTimeMs by remember { mutableLongStateOf(0L) }
    var activeSound by remember { mutableStateOf("Шум дождя 🌧️") }
    var isMuted by remember { mutableStateOf(false) }

    var showCompletedDialog by remember { mutableStateOf(false) }
    var showTaskPickerMenu by remember { mutableStateOf(false) }

    val soundOptions = listOf("Тишина 🤫", "Шум дождя 🌧️", "Белый шум 📻", "Костер 🔥", "Космос 🌌")

    // Stop ambient audio on screen dispose
    DisposableEffect(Unit) {
        onDispose {
            soundHelper.stopAmbientAudio()
            if (isRunning) {
                QuestAlarmScheduler.cancelFocusTimerAlarm(context)
            }
        }
    }

    // Reset timeLeftSeconds when totalTimeMinutes changes while stopped
    LaunchedEffect(totalTimeMinutes) {
        if (!isRunning) {
            timeLeftSeconds = totalTimeMinutes * 60
        }
    }

    // Handle ambient sound playback when running status or active sound changes
    LaunchedEffect(isRunning, activeSound, isMuted) {
        if (isRunning && !isMuted && activeSound != "Тишина 🤫") {
            soundHelper.startAmbientAudio(activeSound)
        } else {
            soundHelper.stopAmbientAudio()
        }
    }

    // Rock-solid timer loop with wall-clock sync and zero drift
    LaunchedEffect(isRunning) {
        if (isRunning) {
            targetEndTimeMs = System.currentTimeMillis() + (timeLeftSeconds * 1000L)
            // Schedule AlarmManager alert in case user minimizes app or locks screen
            QuestAlarmScheduler.scheduleFocusTimerAlarm(context, timeLeftSeconds, spotlightTask?.title)

            while (isRunning && timeLeftSeconds > 0) {
                delay(500)
                val remainingMs = targetEndTimeMs - System.currentTimeMillis()
                val newSeconds = ((remainingMs + 999) / 1000).toInt().coerceAtLeast(0)
                timeLeftSeconds = newSeconds

                if (timeLeftSeconds <= 0) {
                    isRunning = false
                    soundHelper.stopAmbientAudio()
                    soundHelper.playVictoryChime()
                    soundHelper.triggerVibration("VICTORY")
                    NotificationHelper.showTimerCompleted(context, totalTimeMinutes, spotlightTask?.title)
                    onCompleteSession(totalTimeMinutes)
                    showCompletedDialog = true
                    break
                }
            }
        } else {
            QuestAlarmScheduler.cancelFocusTimerAlarm(context)
        }
    }

    val totalSeconds = (totalTimeMinutes * 60).coerceAtLeast(1)
    val progress = (totalSeconds - timeLeftSeconds).toFloat() / totalSeconds.toFloat()

    val minutes = timeLeftSeconds / 60
    val seconds = timeLeftSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    // Animated Soundwave Visualizer Bars
    val infiniteTransition = rememberInfiniteTransition(label = "soundwave")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w2"
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w3"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("focus_timer_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Spotlight Task Selector Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTaskPickerMenu = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎯", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ФОКУС НА КВЕСТЕ (нажмите для смены)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = spotlightTask?.title ?: (currentQuestTasks.firstOrNull()?.title ?: "Свободная сессия глубокого фокуса"),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }

        // Circular Timer Display with Glowing Ring
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
                val arcSize = Size(radius * 2, radius * 2)

                // Background track
                drawArc(
                    color = surfaceVariantColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active progress arc with gradient
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(primaryColor, GoldAccent, primaryColor)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isRunning) "ПОТОК АКТИВЕН ⚡" else "ГОТОВ К ШТУРМУ",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isRunning) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Ambient Sound Controls with Live Synthesizer
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Звук",
                                tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMuted) "Звук отключен" else "Фон: $activeSound",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }

                    // Animated sound visualizer bars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.height(18.dp)
                    ) {
                        val heights = listOf(wave1, wave2, wave3, wave2, wave1)
                        heights.forEach { h ->
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight(if (isRunning && !isMuted && activeSound != "Тишина 🤫") h else 0.2f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    soundOptions.take(3).forEach { snd ->
                        FilterChip(
                            selected = activeSound == snd,
                            onClick = { activeSound = snd },
                            label = { Text(snd, fontSize = 10.sp) }
                        )
                    }
                    soundOptions.drop(3).forEach { snd ->
                        FilterChip(
                            selected = activeSound == snd,
                            onClick = { activeSound = snd },
                            label = { Text(snd, fontSize = 10.sp) }
                        )
                    }
                }
            }
        }

        // Pomodoro Intervals & Duration Adjustment
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Длительность интервала:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Plus / Minus Stepper
                if (!isRunning) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(
                            onClick = {
                                if (totalTimeMinutes > 5) {
                                    totalTimeMinutes -= 5
                                    timeLeftSeconds = totalTimeMinutes * 60
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("-5", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        FilledTonalIconButton(
                            onClick = {
                                if (totalTimeMinutes < 120) {
                                    totalTimeMinutes += 5
                                    timeLeftSeconds = totalTimeMinutes * 60
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("+5", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair(5, "5м ☕"),
                    Pair(15, "15м 🌿"),
                    Pair(25, "25м 🍅"),
                    Pair(45, "45м 🚀"),
                    Pair(60, "60м ⚔️")
                ).forEach { (mins, label) ->
                    FilterChip(
                        selected = totalTimeMinutes == mins,
                        onClick = {
                            if (!isRunning) {
                                totalTimeMinutes = mins
                                timeLeftSeconds = mins * 60
                            }
                        },
                        label = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Controls: Start, Pause, Reset
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 72.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    isRunning = false
                    soundHelper.stopAmbientAudio()
                    QuestAlarmScheduler.cancelFocusTimerAlarm(context)
                    timeLeftSeconds = totalTimeMinutes * 60
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Сброс")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Сброс")
            }

            Button(
                onClick = {
                    isRunning = !isRunning
                },
                modifier = Modifier
                    .weight(1.5f)
                    .height(52.dp)
                    .testTag("toggle_focus_timer_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "Пауза" else "Старт Потока",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }

    // Modal: Task Selector
    if (showTaskPickerMenu) {
        Dialog(onDismissRequest = { showTaskPickerMenu = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Выберите задачу для фокуса",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentQuestTasks.isEmpty()) {
                        Text(
                            text = "В текущем квесте нет открытых задач",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        currentQuestTasks.forEach { task ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (spotlightTask?.id == task.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onSelectTask(task)
                                        totalTimeMinutes = task.estimatedMinutes
                                        timeLeftSeconds = task.estimatedMinutes * 60
                                        showTaskPickerMenu = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(if (task.isCompleted) "✅" else "⚔️")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showTaskPickerMenu = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Закрыть")
                    }
                }
            }
        }
    }

    // Modal: Session Completed Victory Dialog
    if (showCompletedDialog) {
        val coinsEarned = (totalTimeMinutes / 5).coerceAtLeast(1)
        val xpEarned = totalTimeMinutes * 3

        Dialog(onDismissRequest = { showCompletedDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏆", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Фокус завершен!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Вы провели $totalTimeMinutes мин в состоянии кристальной концентрации.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GoldAccent.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "+$coinsEarned 🪙 Монет",
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "+$xpEarned XP Опыта",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            showCompletedDialog = false
                            timeLeftSeconds = totalTimeMinutes * 60
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Принять награду ✨", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
