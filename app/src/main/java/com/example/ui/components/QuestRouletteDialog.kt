package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TaskItem
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun QuestRouletteDialog(
    tasks: List<TaskItem>,
    onSelectTaskForFocus: (TaskItem) -> Unit,
    onDismiss: () -> Unit
) {
    if (tasks.isEmpty()) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎯 Рулетка Квестов", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("В текущем квесте нет открытых задач! Добавьте задачи из бэклога.", textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                        Text("Понятно")
                    }
                }
            }
        }
        return
    }

    val coroutineScope = rememberCoroutineScope()
    val rotationAnim = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<TaskItem?>(null) }

    val segmentColors = remember {
        listOf(
            Color(0xFF6366F1), // Indigo
            Color(0xFFEC4899), // Pink
            Color(0xFFF59E0B), // Amber
            Color(0xFF10B981), // Emerald
            Color(0xFF3B82F6), // Blue
            Color(0xFF8B5CF6)  // Purple
        )
    }

    val segmentAngle = 360f / tasks.size

    fun spinWheel() {
        if (isSpinning) return
        isSpinning = true
        selectedTask = null

        coroutineScope.launch {
            val randomTurns = 5 + Random.nextInt(5)
            val randomFinalAngle = Random.nextFloat() * 360f
            val targetDegrees = rotationAnim.value + (randomTurns * 360f) + randomFinalAngle

            rotationAnim.animateTo(
                targetValue = targetDegrees,
                animationSpec = tween(
                    durationMillis = 3500,
                    easing = FastOutSlowInEasing
                )
            )

            // The pointer is at 270 degrees (top: -90 / 270)
            val normalizedAngle = (360f - (rotationAnim.value % 360f) + 270f) % 360f
            val chosenIndex = (normalizedAngle / segmentAngle).toInt() % tasks.size
            selectedTask = tasks[chosenIndex]
            isSpinning = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("quest_roulette_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎰 Колесо Фортуны",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                        )
                        Text(
                            text = "Доверьтесь случаю, если трудно выбрать",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Wheel Container with Top Pointer
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Rotating wheel
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f

                        rotate(rotationAnim.value, pivot = center) {
                            tasks.forEachIndexed { i, _ ->
                                val startAngle = i * segmentAngle
                                val color = segmentColors[i % segmentColors.size]
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = segmentAngle,
                                    useCenter = true,
                                    topLeft = Offset.Zero,
                                    size = size
                                )
                            }

                            // Inner dividers
                            tasks.forEachIndexed { i, _ ->
                                val angleRad = Math.toRadians((i * segmentAngle).toDouble())
                                val endX = (center.x + radius * kotlin.math.cos(angleRad)).toFloat()
                                val endY = (center.y + radius * kotlin.math.sin(angleRad)).toFloat()
                                drawLine(
                                    color = Color.White.copy(alpha = 0.4f),
                                    start = center,
                                    end = Offset(endX, endY),
                                    strokeWidth = 2.5f
                                )
                            }
                        }

                        // Outer border ring
                        drawCircle(
                            color = Color(0xFFFFD700),
                            radius = radius,
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                        )

                        // Center hub
                        drawCircle(
                            color = Color(0xFF1E293B),
                            radius = 28f,
                            center = center
                        )
                        drawCircle(
                            color = Color(0xFFFFD700),
                            radius = 16f,
                            center = center
                        )
                    }

                    // Static Pointer at the TOP of the wheel
                    Canvas(
                        modifier = Modifier
                            .size(26.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        val path = Path().apply {
                            moveTo(size.width / 2f, size.height) // tip pointing down into wheel
                            lineTo(0f, 0f)
                            lineTo(size.width, 0f)
                            close()
                        }
                        drawPath(path = path, color = Color(0xFFFF3D00))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chosen Task Result Card
                if (selectedTask != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GoldAccent.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎯", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Выбор судьбы (+50% бонус XP):",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedTask!!.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 2
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            selectedTask?.let { onSelectTaskForFocus(it) }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("В бой! Запустить фокус-таймер", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Spin Action Button
                FilledTonalButton(
                    onClick = { spinWheel() },
                    enabled = !isSpinning,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSpinning) "Колесо вращается..." else "Крутить колесо! 🎲",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
