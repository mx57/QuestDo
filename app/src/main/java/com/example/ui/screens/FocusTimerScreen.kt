package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.viewmodel.FocusTimerUiState
import com.example.util.SystemAlarmHelper

@Composable
fun FocusTimerScreen(
    timerState: FocusTimerUiState,
    spotlightTask: TaskItem?,
    currentQuestTasks: List<TaskItem>,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSetDuration: (Int) -> Unit,
    onSetActiveSound: (String) -> Unit,
    onToggleMute: () -> Unit,
    onDismissCompletedDialog: () -> Unit,
    onSelectTask: (TaskItem) -> Unit
) {
    val context = LocalContext.current
    var showTaskPickerMenu by remember { mutableStateOf(false) }

    val soundOptions = listOf("Тишина 🤫", "Шум дождя 🌧️", "Белый шум 📻", "Костер 🔥", "Космос 🌌")

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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("focus_timer_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                .testTag("spotlight_task_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎯", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Фокус на квесте:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = spotlightTask?.title ?: timerState.spotlightTaskTitle ?: "Свободная сессия концентрации",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                    }
                }
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Выбрать квест",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Circular Neon Progress Timer Display
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp)
                .padding(8.dp)
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            val animatedProgress by animateFloatAsState(
                targetValue = timerState.progress,
                animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                label = "progress"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)

                // Background Track
                drawCircle(
                    color = trackColor,
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokeWidth)
                )

                // Neon Gradient Arc
                val sweepAngle = animatedProgress * 360f
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(primaryColor, GoldAccent, primaryColor),
                        center = centerOffset
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Digital Counter & Status Inside Circle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timerState.formattedTime,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (timerState.isRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (timerState.isRunning) "🔥 В ПОТОКЕ" else "⏸️ ПАУЗА",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (timerState.isRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Mini soundwave indicators when running
                if (timerState.isRunning && !timerState.isMuted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height((wave1 * 16).dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height((wave2 * 20).dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(GoldAccent)
                        )
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height((wave3 * 14).dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // Ambient Sound Atmosphere Selector
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎧 Фоновая атмосфера",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (timerState.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Mute",
                            tint = if (timerState.isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(soundOptions) { sound ->
                        val isSelected = timerState.activeSound == sound
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSetActiveSound(sound) },
                            label = { Text(sound, fontSize = 12.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        // Pomodoro Duration Presets
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱️ Длительность сессии",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                val newDuration = (timerState.totalTimeMinutes - 5).coerceAtLeast(5)
                                onSetDuration(newDuration)
                            },
                            enabled = !timerState.isRunning && timerState.totalTimeMinutes > 5,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("-5", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "${timerState.totalTimeMinutes}м",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        FilledTonalIconButton(
                            onClick = {
                                val newDuration = (timerState.totalTimeMinutes + 5).coerceAtMost(180)
                                onSetDuration(newDuration)
                            },
                            enabled = !timerState.isRunning && timerState.totalTimeMinutes < 180,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("+5", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf(
                        Pair(5, "5м ☕"),
                        Pair(15, "15м 🌿"),
                        Pair(25, "25м 🍅"),
                        Pair(45, "45м 🚀"),
                        Pair(60, "60м ⚔️")
                    )) { (mins, label) ->
                        FilterChip(
                            selected = timerState.totalTimeMinutes == mins,
                            onClick = { onSetDuration(mins) },
                            enabled = !timerState.isRunning,
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Primary Controls: Start, Pause, Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onResetTimer,
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
                    if (timerState.isRunning) onPauseTimer() else onStartTimer()
                },
                modifier = Modifier
                    .weight(1.5f)
                    .height(52.dp)
                    .testTag("toggle_focus_timer_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (timerState.isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (timerState.isRunning) "Пауза" else "Старт Потока",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // System Clock Integration Row (Direct Android System Alarm & Timer)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Системные часы Android",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Запустить нативный таймер или будильник",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = {
                            val taskTitle = spotlightTask?.title ?: timerState.spotlightTaskTitle ?: "Фокус-сессия"
                            val secondsToRun = if (timerState.timeLeftSeconds > 0) timerState.timeLeftSeconds else timerState.totalTimeMinutes * 60
                            SystemAlarmHelper.setSystemTimer(context, secondsToRun, "🍅 $taskTitle")
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("⏱️ В часы", fontSize = 11.sp)
                    }

                    IconButton(
                        onClick = { SystemAlarmHelper.openSystemAlarms(context) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Launch,
                            contentDescription = "Открыть часы",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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
                            text = "В текущем квесте пока нет активных задач. Вы можете фокусироваться в свободной сессии.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 280.dp)
                        ) {
                            items(currentQuestTasks) { task ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (task.id == spotlightTask?.id || task.id == timerState.spotlightTaskId)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectTask(task)
                                            showTaskPickerMenu = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(task.categoryEmoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = task.title,
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "${task.estimatedMinutes} мин • ${task.priority.titleRu}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TextButton(
                        onClick = { showTaskPickerMenu = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Закрыть")
                    }
                }
            }
        }
    }

    // Modal: Session Completed Victory Dialog
    if (timerState.showCompletedDialog) {
        val completedMins = if (timerState.completedMinutes > 0) timerState.completedMinutes else timerState.totalTimeMinutes
        val coinsEarned = (completedMins / 5).coerceAtLeast(1)
        val xpEarned = completedMins * 3

        Dialog(onDismissRequest = onDismissCompletedDialog) {
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
                        text = "Вы провели $completedMins мин в состоянии кристальной концентрации.",
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
                        onClick = onDismissCompletedDialog,
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
