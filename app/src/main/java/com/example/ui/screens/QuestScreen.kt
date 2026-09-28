package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.StreakFire
import com.example.ui.theme.XpPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestScreen(
    currentTasks: List<TaskItem>,
    urgentTasks: List<TaskItem> = emptyList(),
    activeLevel: QuestLevel?,
    userProfile: UserProfile,
    currentQuote: MotivationalMessage,
    onToggleTask: (TaskItem) -> Unit,
    onToggleSubtask: (TaskItem, Int) -> Unit,
    onAddSubtask: (TaskItem, String) -> Unit,
    onEditTask: (TaskItem) -> Unit,
    onShuffleQuest: () -> Unit,
    onStartFocusOnTask: (TaskItem) -> Unit,
    onOpenSOS: () -> Unit,
    onOpenBreathing: () -> Unit,
    onOpenEveningCheckout: () -> Unit,
    onSelectEnergy: (Int) -> Unit,
    onAddNewTask: () -> Unit,
    onClaimRewardManual: () -> Unit
) {
    val totalInQuest = currentTasks.size
    val completedInQuest = currentTasks.count { it.isCompleted }
    val questProgress = if (totalInQuest > 0) completedInQuest.toFloat() / totalInQuest.toFloat() else 0f
    val allCompleted = totalInQuest > 0 && completedInQuest == totalInQuest

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quest_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero HUD Bar
        item {
            HeroHeaderCard(
                userProfile = userProfile,
                onOpenBreathing = onOpenBreathing,
                onOpenEveningCheckout = onOpenEveningCheckout
            )
        }

        // Energy Check-In Selector Bar
        item {
            EnergyCheckInBar(
                currentEnergy = userProfile.currentEnergyLevel,
                onSelectEnergy = onSelectEnergy
            )
        }

        // Recovery Mode Soft Amber Notification if active
        if (userProfile.inRecoveryMode) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GoldAccent.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌱", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Режим восстановления (Мягкий перезапуск)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Уровни временно сокращены до 1 задачи. Пауза серии без потерь.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Urgent Layer Banner (strict deadlines - prohibited to hide!)
        if (urgentTasks.isNotEmpty()) {
            item {
                UrgentLayerBanner(
                    urgentTasks = urgentTasks,
                    onToggleTask = onToggleTask
                )
            }
        }

        // Horizon Progress Visualizer Metaphor (togglable)
        if (userProfile.showHorizon) {
            item {
                HorizonProgressCard(
                    completedQuests = userProfile.totalQuestsCompleted,
                    totalTasksDone = userProfile.totalTasksCompleted
                )
            }
        }

        // Dynamic Motivational Quote Card
        item {
            MotivationalQuoteCard(
                quote = currentQuote,
                activeMood = userProfile.activeMood,
                onOpenSOS = onOpenSOS
            )
        }

        // Quest Level Status Banner
        item {
            QuestStatusBanner(
                activeLevel = activeLevel,
                completedCount = completedInQuest,
                totalCount = totalInQuest,
                progress = questProgress,
                allCompleted = allCompleted,
                onClaimReward = onClaimRewardManual,
                onShuffleQuest = onShuffleQuest
            )
        }

        // Tasks in Current Level
        if (currentTasks.isEmpty()) {
            item {
                EmptyQuestPlaceholder(
                    onShuffle = onShuffleQuest,
                    onAddNewTask = onAddNewTask
                )
            }
        } else {
            items(currentTasks, key = { it.id }) { task ->
                QuestTaskCard(
                    task = task,
                    onToggle = { onToggleTask(task) },
                    onToggleSubtask = { idx -> onToggleSubtask(task, idx) },
                    onAddSubtask = { title -> onAddSubtask(task, title) },
                    onEdit = { onEditTask(task) },
                    onStartFocus = { onStartFocusOnTask(task) }
                )
            }
        }

        // Evening Checkout Banner Ritual
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenEveningCheckout() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌙", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Вечерний чек-аут ритуал",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "30-секундная рефлексия и закрытие уровня дня",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // Anti-Procrastination Assistant Footer
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSOS() }
                    .testTag("sos_footer_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🛡️", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Трудно сделать первый шаг?",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Нажмите для подсказок, дыхания или быстрой перетасовки",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HeroHeaderCard(
    userProfile: UserProfile,
    onOpenBreathing: () -> Unit,
    onOpenEveningCheckout: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hero Avatar & Level
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(userProfile.appIconStyle.emoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = userProfile.heroName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Ур. ${userProfile.level} • ${userProfile.heroTitle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Streak & Coins
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Streak Badge (with Freeze icon if active)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StreakFire.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔥", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userProfile.streakDays} дн. (❄️${userProfile.streakFreezes})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StreakFire
                                )
                            )
                        }
                    }

                    // Gold Coins Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GoldAccent.copy(alpha = 0.18f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🪙", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userProfile.coins}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // XP Bar to Next Level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Опыт: ${userProfile.xp % userProfile.xpNeededForNextLevel} / ${userProfile.xpNeededForNextLevel} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Настроение: ${userProfile.activeMood.iconEmoji} ${userProfile.activeMood.titleRu}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { userProfile.xpProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = XpPurple,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun MotivationalQuoteCard(
    quote: MotivationalMessage,
    activeMood: MoodType,
    onOpenSOS: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(activeMood.iconEmoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "НАСТРОЙ ДНЯ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = quote.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = quote.quote,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "— ${quote.authorOrTip}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun QuestStatusBanner(
    activeLevel: QuestLevel?,
    completedCount: Int,
    totalCount: Int,
    progress: Float,
    allCompleted: Boolean,
    onClaimReward: () -> Unit,
    onShuffleQuest: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (allCompleted) GoldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (allCompleted) 6.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = activeLevel?.title ?: "Текущий Уровень",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                    )
                    Text(
                        text = if (allCompleted) "Все задачи закрыты! Заберите награду ✨" else "Выполнено $completedCount из $totalCount задач",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (allCompleted) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!allCompleted) {
                    IconButton(
                        onClick = onShuffleQuest,
                        modifier = Modifier.testTag("shuffle_quest_icon_button")
                    ) {
                        Icon(
                            Icons.Default.Cached,
                            contentDescription = "Сменить задачи уровня",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (allCompleted) GoldAccent else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (allCompleted) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onClaimReward,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("claim_quest_reward_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("ОТКРЫТЬ СУНДУК УРОВНЯ 🎁", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QuestTaskCard(
    task: TaskItem,
    onToggle: () -> Unit,
    onToggleSubtask: (Int) -> Unit,
    onAddSubtask: (String) -> Unit,
    onEdit: () -> Unit,
    onStartFocus: () -> Unit
) {
    var expandedSubtasks by remember { mutableStateOf(false) }
    var newSubtaskInput by remember { mutableStateOf("") }
    val subtasks = task.getSubtasksList()

    val cardColor by animateColorAsState(
        targetValue = if (task.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "taskCardColor"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(if (task.isCompleted) 1.dp else 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quest_task_card_${task.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Checkbox
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("toggle_task_${task.id}")
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = "Выполнить задачу",
                        tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onEdit() }
                ) {
                    // Category & Priority Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = task.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (task.priority == Priority.CRITICAL) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "Срочно",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "⚡${task.energyRequired}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Text(
                            text = "⏱ ${task.estimatedMinutes} мин",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (task.fieldWhy.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🎯 Зачем: ${task.fieldWhy}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Focus Timer Launcher Icon
                if (!task.isCompleted) {
                    IconButton(
                        onClick = onStartFocus,
                        modifier = Modifier.testTag("focus_task_button_${task.id}")
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = "Фокус-режим",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Subtasks toggle button if present
            if (subtasks.isNotEmpty()) {
                val doneCount = subtasks.count { it.isDone }
                TextButton(
                    onClick = { expandedSubtasks = !expandedSubtasks },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Подзадачи ($doneCount/${subtasks.size}) ${if (expandedSubtasks) "▲" else "▼"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Expanded Subtasks List
            AnimatedVisibility(visible = expandedSubtasks) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, start = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    subtasks.forEachIndexed { idx, st ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleSubtask(idx) }
                        ) {
                            Checkbox(
                                checked = st.isDone,
                                onCheckedChange = { onToggleSubtask(idx) },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = st.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = if (st.isDone) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (st.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Add quick micro-subtask field
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSubtaskInput,
                            onValueChange = { newSubtaskInput = it },
                            placeholder = { Text("Быстрый микро-шаг...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (newSubtaskInput.isNotBlank()) {
                                    onAddSubtask(newSubtaskInput.trim())
                                    newSubtaskInput = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Добавить шаг")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyQuestPlaceholder(
    onShuffle: () -> Unit,
    onAddNewTask: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎉", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Все активные квесты завершены!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Вы очистили текущий уровень. Добавьте новые задачи или извлеките следующую пачку из бэклога.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShuffle,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Взять из бэклога")
                }
                Button(
                    onClick = onAddNewTask,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Создать квест +")
                }
            }
        }
    }
}
