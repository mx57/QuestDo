package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Priority
import com.example.data.model.SubTask
import com.example.data.model.TaskItem

data class QuestTemplate(
    val title: String,
    val description: String,
    val category: String,
    val iconEmoji: String,
    val energyRequired: Int,
    val estimatedMinutes: Int,
    val priority: Priority,
    val subtasks: List<String> = emptyList()
)

@Composable
fun QuickTemplatesDialog(
    onSelectTemplate: (TaskItem) -> Unit,
    onDismiss: () -> Unit
) {
    val templates = remember {
        listOf(
            QuestTemplate(
                title = "Выпить 2 стакана чистой воды",
                description = "Восстановить гидратацию мозга и запустить метаболизм",
                category = "Здоровье",
                iconEmoji = "💧",
                energyRequired = 1,
                estimatedMinutes = 5,
                priority = Priority.HIGH
            ),
            QuestTemplate(
                title = "Разминка шеи, спины и 20 приседаний",
                description = "Снять зажимы в теле и разогнать кровообращение",
                category = "Здоровье",
                iconEmoji = "🏃",
                energyRequired = 1,
                estimatedMinutes = 7,
                priority = Priority.HIGH,
                subtasks = listOf("Растяжка шеи и плеч", "20 приседаний", "Глубокое дыхание 1 мин")
            ),
            QuestTemplate(
                title = "25 минут глубокого чтения книги",
                description = "Без соцсетей и всплывающих уведомлений",
                category = "Развитие",
                iconEmoji = "📖",
                energyRequired = 2,
                estimatedMinutes = 25,
                priority = Priority.MEDIUM,
                subtasks = listOf("Открыть книгу и засечь таймер", "Сделать 2 краткие заметки")
            ),
            QuestTemplate(
                title = "Экспресс-уборка рабочего стола",
                description = "Чистый физический стол рождает ясность в мыслях",
                category = "Дом",
                iconEmoji = "🧹",
                energyRequired = 1,
                estimatedMinutes = 10,
                priority = Priority.MEDIUM
            ),
            QuestTemplate(
                title = "Цифровой детокс на 30 минут",
                description = "Перевести телефон в режим 'Не беспокоить' и дать отдых дофамину",
                category = "Фокус",
                iconEmoji = "📵",
                energyRequired = 2,
                estimatedMinutes = 30,
                priority = Priority.CRITICAL
            ),
            QuestTemplate(
                title = "1 урок иностранного языка / 10 слов",
                description = "Ежедневный микро-шаг к свободному владению",
                category = "Учеба",
                iconEmoji = "🇬🇧",
                energyRequired = 2,
                estimatedMinutes = 15,
                priority = Priority.HIGH
            ),
            QuestTemplate(
                title = "Разобрать входящие сообщения и почту (Inbox Zero)",
                description = "Ответить, архивировать или превратить в задачу",
                category = "Работа",
                iconEmoji = "📬",
                energyRequired = 2,
                estimatedMinutes = 20,
                priority = Priority.HIGH
            ),
            QuestTemplate(
                title = "Финансовая ревизия дня",
                description = "Зафиксировать расходы и закрыть финансовый баланс",
                category = "Финансы",
                iconEmoji = "💰",
                energyRequired = 1,
                estimatedMinutes = 5,
                priority = Priority.LOW
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("quick_templates_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "⚡ Быстрые Квесты-Привычки",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Нажмите, чтобы добавить в план в 1 клик",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(templates) { item ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val subtasksList = item.subtasks.map { SubTask(it, false) }
                                    val newTask = TaskItem(
                                        title = item.title,
                                        description = item.description,
                                        category = item.category,
                                        priority = item.priority,
                                        energyRequired = item.energyRequired,
                                        estimatedMinutes = item.estimatedMinutes,
                                        inCurrentQuest = false,
                                        subtasksRaw = if (subtasksList.isNotEmpty()) TaskItem.serializeSubtasks(subtasksList) else ""
                                    )
                                    onSelectTemplate(newTask)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.iconEmoji, fontSize = 26.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "${item.category} • ⏱ ${item.estimatedMinutes} мин • ⚡${item.energyRequired}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Добавить",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
