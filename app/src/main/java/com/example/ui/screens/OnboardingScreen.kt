package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent

@Composable
fun OnboardingScreen(
    onCompleteOnboarding: (tasksPerQuest: Int) -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    var selectedTasksPerQuest by remember { mutableStateOf(3) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Progress Dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { idx ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (idx == currentStep) 24.dp else 10.dp, 10.dp)
                            .clip(CircleShape)
                            .background(
                                if (idx == currentStep) GoldAccent else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            // Main Animated Content
            AnimatedContent(
                targetState = currentStep,
                label = "onboarding_step"
            ) { step ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (step) {
                        0 -> {
                            Text(text = "🛡️", fontSize = 72.sp)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Добро пожаловать в «Уровень»",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Забудьте о бесконечных списках из сотен дел. Мы разбиваем хаос бэклога на маленькие, победоносные микро-уровни.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("🎯 Закрыл уровень — получил награду", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Никакого чувства вины и тёмных паттернов. Только дофамин и чистый фокус.", fontSize = 14.sp)
                                }
                            }
                        }

                        1 -> {
                            Text(text = "⚙️", fontSize = 72.sp)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Калибровка ритма",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Сколько задач в одном микро-уровне комфортно выполнять именно вам?",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            val options = listOf(1, 2, 3, 5)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                options.forEach { count ->
                                    val isSelected = count == selectedTasksPerQuest
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedTasksPerQuest = count },
                                        label = {
                                            Text(
                                                text = "$count ${if (count == 1) "задача" else if (count < 5) "задачи" else "задач"}",
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = GoldAccent,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = when (selectedTasksPerQuest) {
                                    1 -> "💡 Идеально при сильной усталости или высокой тревожности."
                                    2 -> "💡 Лаконичный и быстрый темп для максимального комфорта."
                                    3 -> "💡 Сбалансированный стандарт: 1 крупная + 2 мелкие."
                                    else -> "💡 Для сфокусированных штурмов и высокого уровня энергии."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        2 -> {
                            Text(text = "🎁", fontSize = 72.sp)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Первый уровень готов!",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Мы автоматически собрали для вас 3 первых легких шага. Выполните их и заберите первую награду уже в первые 90 секунд!",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("✨ Награда за стартовый рывок", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("50 монет + 100 XP + Огонек Серии 🔥", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    TextButton(onClick = { currentStep-- }) {
                        Text("Назад")
                    }
                } else {
                    Spacer(modifier = Modifier.width(64.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < 2) {
                            currentStep++
                        } else {
                            onCompleteOnboarding(selectedTasksPerQuest)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text(
                        text = if (currentStep == 2) "Начать квест 🚀" else "Далее",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}
