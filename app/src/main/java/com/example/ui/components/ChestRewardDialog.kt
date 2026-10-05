package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CustomReward
import com.example.data.model.QuestLevel
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.StreakFire
import com.example.ui.theme.XpPurple

@Composable
fun ChestRewardDialog(
    level: QuestLevel,
    availableRewards: List<CustomReward>,
    onClaimReward: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isChestOpened by remember { mutableStateOf(false) }
    var selectedRewardTitle by remember { mutableStateOf("15 минут отдыха и чашка чая") }

    val infiniteTransition = rememberInfiniteTransition(label = "sunburst")
    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rays"
    )

    val chestPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val scale by animateFloatAsState(
        targetValue = if (isChestOpened) 1.2f else chestPulse,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "chestBounce"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("chest_reward_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "🏆 УРОВЕНЬ ЗАВЕРШЕН!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = GoldAccent,
                        letterSpacing = 1.2.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = level.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Interactive Chest Graphic
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    if (isChestOpened) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val rayCount = 12
                            val sweepAngle = 360f / rayCount
                            rotate(degrees = rayRotation, pivot = center) {
                                for (i in 0 until rayCount step 2) {
                                    drawArc(
                                        color = GoldAccent.copy(alpha = 0.22f),
                                        startAngle = i * sweepAngle,
                                        sweepAngle = sweepAngle * 0.7f,
                                        useCenter = true,
                                        topLeft = Offset.Zero,
                                        size = size
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        if (isChestOpened) GoldAccent.copy(alpha = 0.45f) else GoldAccent.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .clickable { isChestOpened = true }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isChestOpened) "✨💎✨" else "🎁",
                            fontSize = if (isChestOpened) 42.sp else 54.sp
                        )
                    }
                }

                if (!isChestOpened) {
                    Text(
                        text = "Нажмите на сундук, чтобы открыть награду!",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    )
                } else {
                    // Spoils of War
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = XpPurple.copy(alpha = 0.15f),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚡", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+${level.xpEarned} Опыта",
                                    fontWeight = FontWeight.Bold,
                                    color = XpPurple
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GoldAccent.copy(alpha = 0.15f),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🪙", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+${level.coinsEarned} Монет",
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Real-world self reward selection
                    Text(
                        text = "Выбери реальную награду для себя:",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val defaultOptions = listOf(
                        "☕ Вкусный кофе или чай",
                        "🛋️ 15 минут законного отдыха",
                        "🎬 Серия любимого сериала",
                        "🚶 Небольшая прогулка на свежем воздухе"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        defaultOptions.forEach { opt ->
                            val isSelected = selectedRewardTitle == opt
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedRewardTitle = opt }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedRewardTitle = opt },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = opt,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Button(
                    onClick = {
                        if (!isChestOpened) {
                            isChestOpened = true
                        } else {
                            onClaimReward(selectedRewardTitle)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("claim_reward_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (!isChestOpened) "ОТКРЫТЬ СУНДУК ✨" else "ЗАБРАТЬ И НАЧАТЬ СЛЕДУЮЩИЙ 🚀",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
