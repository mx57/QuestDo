package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Priority
import com.example.data.model.RecurrenceRule
import com.example.data.model.SubTask
import com.example.data.model.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditDialog(
    initialTask: TaskItem? = null,
    onSaveTask: (TaskItem) -> Unit,
    onDeleteTask: ((TaskItem) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var category by remember { mutableStateOf(initialTask?.category ?: "Работа") }
    var priority by remember { mutableStateOf(initialTask?.priority ?: Priority.HIGH) }
    var recurrence by remember { mutableStateOf(initialTask?.recurrence ?: RecurrenceRule.NONE) }
    var estimatedMinutes by remember { mutableIntStateOf(initialTask?.estimatedMinutes ?: 25) }
    var inCurrentQuest by remember { mutableStateOf(initialTask?.inCurrentQuest ?: false) }

    var subtasks by remember {
        mutableStateOf(initialTask?.getSubtasksList() ?: emptyList())
    }
    var newSubtaskText by remember { mutableStateOf("") }

    val categories = listOf("Работа", "Учеба", "Здоровье", "Дом", "Развитие", "Финансы")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("task_edit_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTask == null) "Новый микро-квест" else "Редактировать квест",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title field
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Название задачи / шага *") },
                            placeholder = { Text("Например: Написать 1 главу статьи") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("task_title_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    // Description / notes field
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Заметки / Ссылки / Детали") },
                            placeholder = { Text("Контекст задачи, полезные ссылки...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp)
                                .testTag("task_desc_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    // Category Selection
                    item {
                        Text(
                            text = "Категория",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.take(3).forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 12.sp) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.drop(3).forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    // Eisenhower Matrix Priority
                    item {
                        Text(
                            text = "Приоритет (Матрица Эйзенхауэра)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Priority.values().forEach { prio ->
                                val isSelected = priority == prio
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { priority = prio }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { priority = prio },
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = prio.titleRu,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Subtasks Checklist
                    item {
                        Text(
                            text = "Подзадачи (Чек-лист)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newSubtaskText,
                                onValueChange = { newSubtaskText = it },
                                placeholder = { Text("Добавить подзадачу...", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("subtask_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledIconButton(
                                onClick = {
                                    if (newSubtaskText.isNotBlank()) {
                                        subtasks = subtasks + SubTask(newSubtaskText.trim(), false)
                                        newSubtaskText = ""
                                    }
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Добавить шаг")
                            }
                        }
                    }

                    itemsIndexed(subtasks) { idx, st ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = st.isDone,
                                    onCheckedChange = { checked ->
                                        subtasks = subtasks.toMutableList().also {
                                            it[idx] = it[idx].copy(isDone = checked)
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = st.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        subtasks = subtasks.toMutableList().also { it.removeAt(idx) }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Удалить подзадачу",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Estimated Focus Time
                    item {
                        Text(
                            text = "Оценка времени: $estimatedMinutes минут",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(15, 25, 45, 60).forEach { mins ->
                                FilterChip(
                                    selected = estimatedMinutes == mins,
                                    onClick = { estimatedMinutes = mins },
                                    label = { Text("${mins}м") }
                                )
                            }
                        }
                    }

                    // Direct add to current active quest checkbox
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { inCurrentQuest = !inCurrentQuest }
                        ) {
                            Checkbox(
                                checked = inCurrentQuest,
                                onCheckedChange = { inCurrentQuest = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Поместить прямо в текущий Уровень квеста",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom actions: Save and optional Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (initialTask != null && onDeleteTask != null) {
                        OutlinedButton(
                            onClick = {
                                onDeleteTask(initialTask)
                                onDismiss()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val taskToSave = (initialTask ?: TaskItem(title = title.trim())).copy(
                                    title = title.trim(),
                                    description = description.trim(),
                                    category = category,
                                    priority = priority,
                                    recurrence = recurrence,
                                    estimatedMinutes = estimatedMinutes,
                                    inCurrentQuest = inCurrentQuest,
                                    subtasksRaw = TaskItem.serializeSubtasks(subtasks)
                                )
                                onSaveTask(taskToSave)
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_task_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (initialTask == null) "Создать квест ✨" else "Сохранить изменения")
                    }
                }
            }
        }
    }
}
