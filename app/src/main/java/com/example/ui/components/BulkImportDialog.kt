package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun BulkImportDialog(
    onImport: (text: String, category: String) -> Unit,
    onDismiss: () -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Импорт") }

    val presetLifeRPG = """
        Выпить 2 стакана теплой воды с лимоном
        !Проверить почту и ответить на 3 срочных письма
        15 минут утренней гимнастики и растяжки
        Прочитать 15 страниц развивающей книги
        !Завершить отчет по текущему проекту
        Запланировать задачи на завтра
        Прогулка на улице не менее 30 минут
        Медитация осознанности 10 минут перед сном
    """.trimIndent()

    val presetDevProject = """
        !Провести код-ревью пулреквеста
        Написать юнит-тесты для нового сервиса
        Обновить документацию API в Swagger
        Очистить неиспользуемые зависимости в проекте
        !Собрать релизную сборку для тестирования
        Зафиксировать баг-репорты в трекере
    """.trimIndent()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("bulk_import_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Массовый импорт",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Text(
                    text = "Вставьте список задач (по одной на строке). Знак «!» в начале строки сделает задачу критической. Алгоритм сам разделит их по микро-квестам!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Fast Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            rawText = presetLifeRPG
                            selectedCategory = "Привычки"
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Пак «Life RPG»", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            rawText = presetDevProject
                            selectedCategory = "Работа"
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Пак «Проект»", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    placeholder = { Text("Купить продукты\n!Позвонить врачу\nПомыть машину...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("bulk_import_textarea"),
                    shape = RoundedCornerShape(14.dp)
                )

                val lineCount = rawText.lines().filter { it.isNotBlank() }.size
                Text(
                    text = "Задач для импорта: $lineCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Button(
                    onClick = {
                        if (rawText.isNotBlank()) {
                            onImport(rawText, selectedCategory)
                            onDismiss()
                        }
                    },
                    enabled = rawText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_bulk_import_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Импортировать в базу квестов 📥")
                }
            }
        }
    }
}
