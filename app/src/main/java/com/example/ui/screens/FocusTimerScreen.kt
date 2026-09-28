package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskItem
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.delay

@Composable
fun FocusTimerScreen(
    spotlightTask: TaskItem?,
    currentQuestTasks: List<TaskItem>,
    onCompleteSession: (minutes: Int) -> Unit,
    onSelectTask: (TaskItem) -> Unit
) {
    var totalTimeMinutes by remember { mutableIntStateOf(spotlightTask?.estimatedMinutes ?: 25) }
    var timeLeftSeconds by remember { mutableIntStateOf(totalTimeMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var activeSound by remember { mutableStateOf("Шум дождя 🌧️") }

    // Sound ambient options
    val soundOptions = listOf("Тишина 🤫", "Шум дождя 🌧️", "Белый шум 📻", "Костер 🔥", "Космос 🌌")

    LaunchedEffect(totalTimeMinutes) {
        if (!isRunning) {
            timeLeftSeconds = totalTimeMinutes * 60
        }
    }

    LaunchedEffect(isRunning, timeLeftSeconds) {
        if (isRunning && timeLeftSeconds > 0) {
            delay(1000)
            timeLeftSeconds--
            if (timeLeftSeconds == 0) {
                isRunning = false
                onCompleteSession(totalTimeMinutes)
            }
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
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .testTag("focus_timer_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Spotlight Task Header
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ФОКУС НА КВЕСТЕ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = spotlightTask?.title ?: (currentQuestTasks.firstOrNull()?.title ?: "Выберите задачу для фокуса"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2
                )
            }
        }

        // Circular Timer Display
        Box(
            modifier = Modifier
                .size(260.dp),
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

                // Active progress arc
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isRunning) "ПОТОК АКТИВЕН ⚡" else "НАЖМИТЕ СТАРТ",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isRunning) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Ambient Sound & Visualizer
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Фоновый звук: $activeSound",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )

                    // Soundwave animation bars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.height(20.dp)
                    ) {
                        val heights = listOf(wave1, wave2, wave3, wave2, wave1)
                        heights.forEach { h ->
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight(if (isRunning && activeSound != "Тишина 🤫") h else 0.2f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    soundOptions.take(3).forEach { snd ->
                        FilterChip(
                            selected = activeSound == snd,
                            onClick = { activeSound = snd },
                            label = { Text(snd, fontSize = 10.sp) }
                        )
                    }
                }
            }
        }

        // Preset Duration Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(15, 25, 45).forEach { mins ->
                FilterChip(
                    selected = totalTimeMinutes == mins,
                    onClick = {
                        if (!isRunning) {
                            totalTimeMinutes = mins
                            timeLeftSeconds = mins * 60
                        }
                    },
                    label = { Text("${mins} мин") }
                )
            }
        }

        // Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 72.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    isRunning = false
                    timeLeftSeconds = totalTimeMinutes * 60
                },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Сброс")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Сброс")
            }

            Button(
                onClick = { isRunning = !isRunning },
                modifier = Modifier
                    .weight(1.5f)
                    .height(54.dp)
                    .testTag("toggle_focus_timer_button"),
                shape = RoundedCornerShape(18.dp),
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
}
