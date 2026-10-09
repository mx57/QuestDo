package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DemonMentorEngine
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import kotlin.random.Random

/**
 * Animated Little Imp Avatar (Чертик Люцик) with horns, pitchfork, and blinking eyes.
 */
@Composable
fun DemonImpAvatar(
    modifier: Modifier = Modifier,
    sizeDp: Int = 48,
    isSpeaking: Boolean = false,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "impMotion")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val hornGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hornGlow"
    )

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clickable { onClick() }
            .testTag("demon_imp_avatar"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cy = h * 0.52f + bounceOffset
            val cx = w * 0.5f

            // Left Horn
            val hornPathLeft = Path().apply {
                moveTo(cx - w * 0.22f, cy - h * 0.15f)
                quadraticBezierTo(cx - w * 0.38f, cy - h * 0.42f, cx - w * 0.26f, cy - h * 0.46f)
                quadraticBezierTo(cx - w * 0.16f, cy - h * 0.35f, cx - w * 0.12f, cy - h * 0.22f)
                close()
            }
            drawPath(
                path = hornPathLeft,
                color = Color(0xFFFF1744).copy(alpha = hornGlow)
            )

            // Right Horn
            val hornPathRight = Path().apply {
                moveTo(cx + w * 0.22f, cy - h * 0.15f)
                quadraticBezierTo(cx + w * 0.38f, cy - h * 0.42f, cx + w * 0.26f, cy - h * 0.46f)
                quadraticBezierTo(cx + w * 0.16f, cy - h * 0.35f, cx + w * 0.12f, cy - h * 0.22f)
                close()
            }
            drawPath(
                path = hornPathRight,
                color = Color(0xFFFF1744).copy(alpha = hornGlow)
            )

            // Imp Head (Fiery Crimson circle with dark shading)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF3D00), Color(0xFFD50000), Color(0xFF4A0007)),
                    center = Offset(cx, cy),
                    radius = w * 0.34f
                ),
                radius = w * 0.30f,
                center = Offset(cx, cy)
            )

            // Eyes (Glowing Yellowish Devil slit eyes)
            val eyeRadius = w * 0.065f
            drawCircle(
                color = Color(0xFFFFD600),
                radius = eyeRadius,
                center = Offset(cx - w * 0.11f, cy - h * 0.03f)
            )
            drawCircle(
                color = Color(0xFFFFD600),
                radius = eyeRadius,
                center = Offset(cx + w * 0.11f, cy - h * 0.03f)
            )

            // Dark pupils
            drawCircle(
                color = Color.Black,
                radius = eyeRadius * 0.55f,
                center = Offset(cx - w * 0.10f, cy - h * 0.03f)
            )
            drawCircle(
                color = Color.Black,
                radius = eyeRadius * 0.55f,
                center = Offset(cx + w * 0.12f, cy - h * 0.03f)
            )

            // Mischievous smirk or talking mouth
            val mouthPath = Path().apply {
                if (isSpeaking) {
                    moveTo(cx - w * 0.09f, cy + h * 0.10f)
                    quadraticBezierTo(cx, cy + h * 0.20f, cx + w * 0.09f, cy + h * 0.10f)
                    close()
                } else {
                    moveTo(cx - w * 0.09f, cy + h * 0.10f)
                    quadraticBezierTo(cx, cy + h * 0.16f, cx + w * 0.09f, cy + h * 0.08f)
                }
            }
            drawPath(
                path = mouthPath,
                color = Color.Black
            )

            // Little Pitchfork on the right
            drawLine(
                color = Color(0xFFFFAB00),
                start = Offset(cx + w * 0.32f, cy - h * 0.25f),
                end = Offset(cx + w * 0.38f, cy + h * 0.40f),
                strokeWidth = 3f
            )
            // Pitchfork prongs
            drawLine(
                color = Color(0xFFFFAB00),
                start = Offset(cx + w * 0.24f, cy - h * 0.32f),
                end = Offset(cx + w * 0.40f, cy - h * 0.20f),
                strokeWidth = 2.5f
            )
        }
    }
}

/**
 * Animated rising ember sparks canvas.
 */
@Composable
fun HellEmbersEffect(
    modifier: Modifier = Modifier,
    emberCount: Int = 18
) {
    val infiniteTransition = rememberInfiniteTransition(label = "embers")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "emberPhase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val rand = Random(42)

        for (i in 0 until emberCount) {
            val seedX = (rand.nextFloat() * w)
            val speed = 0.5f + rand.nextFloat() * 0.8f
            val yProgress = (phase * speed + (i.toFloat() / emberCount)) % 1f
            val y = h * (1f - yProgress)
            val xWiggle = seedX + (kotlin.math.sin((yProgress + i) * 6.28) * 16f).toFloat()
            val radius = 2f + rand.nextFloat() * 4f
            val alpha = (1f - yProgress).coerceIn(0f, 0.9f)

            val emberColor = when (i % 3) {
                0 -> Color(0xFFFF3D00).copy(alpha = alpha)
                1 -> Color(0xFFFF9100).copy(alpha = alpha)
                else -> Color(0xFFFFD600).copy(alpha = alpha)
            }

            drawCircle(
                color = emberColor,
                radius = radius,
                center = Offset(xWiggle.coerceIn(0f, w), y)
            )
        }
    }
}

/**
 * Interactive Lucifer Demon Mentor Card.
 * Displays the Demon's current sarcastic roast, allows instant tapping to get a new roast,
 * and launches the Devil's Pact or Cauldron of Sins.
 */
@Composable
fun DemonLuciferMascotCard(
    userProfile: UserProfile,
    onTapRoast: () -> Unit,
    onOpenDevilsPact: () -> Unit,
    onOpenCauldron: () -> Unit,
    onDisableDemonMode: () -> Unit
) {
    var isImpTalking by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF26060B)
        ),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFFF1744), Color(0xFFFF6D00), Color(0xFFFF1744))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("demon_mascot_card")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            HellEmbersEffect(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp)),
                emberCount = 14
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with Badge & Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD50000).copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF1744))
                        ) {
                            Text(
                                text = "🔥 РЕЖИМ ДЕМОНА-НАСТАВНИКА",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFF8A80),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF4A0007),
                        modifier = Modifier.clickable { onDisableDemonMode() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("😇 Ангел", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Imp Mascot + Speech Bubble
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    DemonImpAvatar(
                        sizeDp = 64,
                        isSpeaking = isImpTalking,
                        onClick = {
                            isImpTalking = true
                            onTapRoast()
                        }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Speech bubble with Demon Quote
                    Surface(
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                        color = Color(0xFF38080F),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF3D00).copy(alpha = 0.6f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                isImpTalking = true
                                onTapRoast()
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Люцик (Наставник):",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFFD600)
                                )
                                Text(
                                    text = "тыркни меня 😈",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFF8A80).copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = userProfile.devilPactTaskTitle.takeIf { it.isNotBlank() }
                                    ?: DemonMentorEngine.getRandomRoast(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    lineHeight = 20.sp
                                ),
                                color = Color(0xFFFFEBEE)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Infernal Actions Bar: Devil's Pact & Cauldron of Sins
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenDevilsPact,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD50000),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("devils_pact_button")
                    ) {
                        Text("📜 Сделка с Дьяволом", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onOpenCauldron,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFFAB00)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFAB00)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cauldron_button")
                    ) {
                        Text("🫕 Котёл Грехов", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Active Devil's Pact Countdown Header when a wager is running.
 */
@Composable
fun ActiveDevilsPactBanner(
    userProfile: UserProfile,
    remainingSeconds: Int,
    onCompletePactSuccess: () -> Unit,
    onCancelPact: () -> Unit
) {
    val progress = (remainingSeconds.toFloat() / (userProfile.devilPactDurationMinutes * 60f)).coerceIn(0f, 1f)
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF33050C)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF1744)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_devils_pact_banner")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏳🔥", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "СДЕЛКА С ДЬЯВОЛОМ АКТИВНА!",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFFD600)
                        )
                        Text(
                            text = "Награда: 2x XP и Золото! Проигрыш: -15 🪙",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF8A80)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFD50000)
                ) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFFF1744),
                trackColor = Color(0xFF540C16),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCompletePactSuccess,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🔥 Сжег квест! Забрать 2x", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCancelPact,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF757575)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB0BEC5))
                ) {
                    Text("Сдаться", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Dialog to sign the Devil's Pact wager.
 */
@Composable
fun DevilsPactDialog(
    availableTasks: List<TaskItem>,
    onSignPact: (taskId: Long, durationMinutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTaskId by remember { mutableStateOf(availableTasks.firstOrNull()?.id ?: 0L) }
    var selectedMinutes by remember { mutableStateOf(15) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📜😈", fontSize = 26.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Пакт с Дьяволом",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFFF1744)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "«Подпиши этот договор кровью и потом, смертный! Поставь таймер против своей лени. Закроешь квест вовремя — я удвою твою награду (+100% XP и золота). Не успеешь — 15 монет сгорят в моем котле!»",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = Color(0xFFFF1744).copy(alpha = 0.3f))

                Text(
                    text = "Выберите квест для пари:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (availableTasks.isEmpty()) {
                    Text(
                        text = "Сначала добавьте задачи в квест или бэклог!",
                        color = Color(0xFFFF5252),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        availableTasks.take(4).forEach { task ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedTaskId == task.id) Color(0xFFD50000).copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (selectedTaskId == task.id) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF1744)) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTaskId = task.id }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedTaskId == task.id,
                                        onClick = { selectedTaskId = task.id }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Время испытания:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(5, 10, 15, 25).forEach { mins ->
                        FilterChip(
                            selected = selectedMinutes == mins,
                            onClick = { selectedMinutes = mins },
                            label = { Text("$mins мин") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedTaskId != 0L) {
                        onSignPact(selectedTaskId, selectedMinutes)
                    }
                },
                enabled = selectedTaskId != 0L,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD50000),
                    contentColor = Color.White
                )
            ) {
                Text("🔥 Заключить Пакт!")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

/**
 * Cauldron of Sins Dialog - displays accumulated sins of procrastination.
 */
@Composable
fun CauldronOfSinsDialog(
    overdueTasksCount: Int,
    sinsBurnedCount: Int,
    onPurgeSins: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🫕🔥", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Котёл Грехов и Лени",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFFF5722)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Здесь варятся все твои отложенные на потом дела, просроченные дедлайны и невыполненные обещания. Не дай котлу переполниться!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF3B0B13),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF1744)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "$overdueTasksCount", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                            Text(text = "Просрочено", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF261904),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFAB00)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "$sinsBurnedCount", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD600))
                            Text(text = "Сожжено грехов", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E080C),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (overdueTasksCount == 0)
                            "«Котёл пуст! Черти скучают, а твой ленивый зад сегодня молодец!»"
                        else
                            "«$overdueTasksCount грехов требуют немедленного очищения! Нажми очистить, чтобы активировать ускоренный режим искупления!»",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        color = Color(0xFFFF8A80),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPurgeSins,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00))
            ) {
                Text("🔥 Сжечь грехи в лаве!")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}
