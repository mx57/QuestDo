package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.TaskItem
import com.example.ui.theme.GoldAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BacklogScreen(
    backlogTasks: List<TaskItem>,
    staleTasks: List<TaskItem> = emptyList(),
    completedTasks: List<TaskItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedPriority: Priority?,
    onSelectPriority: (Priority?) -> Unit,
    onToggleTask: (TaskItem) -> Unit,
    onEditTask: (TaskItem) -> Unit,
    onDecomposeTask: (TaskItem) -> Unit,
    onStartBossLevel: (title: String, steps: List<String>) -> Unit,
    onOpenBulkImport: () -> Unit,
    onAddNewTask: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Бэклог, 1: Архив выполненных
    var showBossDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("backlog_screen")
    ) {
        // Search and Actions Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Поиск среди задач...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Очистить")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("backlog_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalIconButton(
                    onClick = onOpenBulkImport,
                    modifier = Modifier.size(52.dp).testTag("bulk_import_icon_button")
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = "Массовый импорт")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Boss level launcher button
            Button(
                onClick = { showBossDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("⚔️ Запустить Босс-Цель Недели", color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Eisenhower matrix priority filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedPriority == null,
                    onClick = { onSelectPriority(null) },
                    label = { Text("Все", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedPriority == Priority.CRITICAL,
                    onClick = { onSelectPriority(if (selectedPriority == Priority.CRITICAL) null else Priority.CRITICAL) },
                    label = { Text("Q1 Срочно", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedPriority == Priority.HIGH,
                    onClick = { onSelectPriority(if (selectedPriority == Priority.HIGH) null else Priority.HIGH) },
                    label = { Text("Q2 Важно", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedPriority == Priority.MEDIUM,
                    onClick = { onSelectPriority(if (selectedPriority == Priority.MEDIUM) null else Priority.MEDIUM) },
                    label = { Text("Q3 Делегир.", fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tabs: Backlog vs Completed
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("В очереди (${backlogTasks.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Завершено (${completedTasks.size})", fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        // Weekly Review Card if stale tasks (90+ days) exist
        if (selectedTab == 0 && staleTasks.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🧹 Недельный обзор: ${staleTasks.size} задач висят 90+ дней",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Старые задачи забирают энергию. Давайте разобьем их или архивируем!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                staleTasks.firstOrNull()?.let { onDecomposeTask(it) }
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Разбить ✨", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        val displayList = if (selectedTab == 0) backlogTasks else completedTasks
        val filteredList = displayList.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true)
            val matchesPriority = selectedPriority == null || item.priority == selectedPriority
            matchesQuery && matchesPriority
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (selectedTab == 0) "📦" else "🎖️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedTab == 0) "Бэклог чист!" else "Пока нет завершенных дел",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedTab == 0)
                            "Добавьте задачи или воспользуйтесь быстрым массовым импортом"
                        else
                            "Закрывайте микро-квесты, чтобы наполнить зал славы",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList, key = { it.id }) { task ->
                    BacklogItemCard(
                        task = task,
                        onToggle = { onToggleTask(task) },
                        onEdit = { onEditTask(task) }
                    )
                }
            }
        }
    }

    if (showBossDialog) {
        BossGoalCreationDialog(
            onCreateBoss = { title, steps ->
                onStartBossLevel(title, steps)
                showBossDialog = false
            },
            onDismiss = { showBossDialog = false }
        )
    }
}

@Composable
fun BacklogItemCard(
    task: TaskItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("backlog_item_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = "Отметить",
                    tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = task.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    if (task.inCurrentQuest) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "В Квесте",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    if (task.dueDate != null) {
                        val isOverdue = !task.isCompleted && task.dueDate < System.currentTimeMillis()
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isOverdue) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = if (isOverdue) "⚠️ Просрочено" else "⏰ Напоминание",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Редактировать",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun BossGoalCreationDialog(
    onCreateBoss: (title: String, steps: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var bossTitle by remember { mutableStateOf("") }
    var step1 by remember { mutableStateOf("") }
    var step2 by remember { mutableStateOf("") }
    var step3 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("⚔️ Главная Босс-Цель Недели", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Создайте 1 крупную цель и 3 ключевых шага для её завоевания. Награда: +350 XP и +100 Монет!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = bossTitle,
                    onValueChange = { bossTitle = it },
                    label = { Text("Название Босс-Цели *") },
                    placeholder = { Text("Например: Запустить коммерческий проект") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = step1,
                    onValueChange = { step1 = it },
                    label = { Text("Шаг 1") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = step2,
                    onValueChange = { step2 = it },
                    label = { Text("Шаг 2") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = step3,
                    onValueChange = { step3 = it },
                    label = { Text("Шаг 3") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val steps = listOf(step1, step2, step3).filter { it.isNotBlank() }.ifEmpty { listOf("Первый шаг к цели") }
                    onCreateBoss(bossTitle.trim(), steps)
                },
                enabled = bossTitle.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) {
                Text("Начать Битву 🔥", fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
