package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.StreakFire
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarStatsScreen(
    userProfile: UserProfile,
    allTasks: List<TaskItem>
) {
    var selectedDateOffsetDays by remember { mutableIntStateOf(0) }

    val calendar = Calendar.getInstance()
    val todayDate = calendar.time

    // Generate 7-day strip (3 days before, today, 3 days after)
    val daysList = remember {
        val list = mutableListOf<CalendarDay>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -3)
        for (i in -3..10) {
            val d = cal.time
            val dayName = SimpleDateFormat("EEE", Locale("ru")).format(d).uppercase()
            val dayNum = SimpleDateFormat("d", Locale("ru")).format(d)
            val fullDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(d)
            list.add(CalendarDay(dayName, dayNum, fullDateStr, i))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val selectedDay = daysList.find { it.offsetDays == selectedDateOffsetDays } ?: daysList[3]

    val dayTasks = remember(allTasks, selectedDay.fullDate, selectedDateOffsetDays) {
        val matchingDueTasks = allTasks.filter { task ->
            task.dueDate?.let { due ->
                val dueStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(due))
                dueStr == selectedDay.fullDate
            } ?: false
        }
        if (matchingDueTasks.isNotEmpty()) {
            matchingDueTasks
        } else if (selectedDateOffsetDays == 0) {
            allTasks.filter { it.inCurrentQuest || !it.isCompleted }
        } else {
            allTasks.filter { !it.isCompleted }.take(3)
        }
    }

    val completedCount = allTasks.count { it.isCompleted }
    val totalCount = allTasks.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calendar_stats_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Analytics Summary Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "📊 Аналитика Продуктивности",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricCard(
                            title = "Выполнено",
                            value = "$completedCount / $totalCount",
                            emoji = "✅",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Серия квестов",
                            value = "${userProfile.streakDays} дн.",
                            emoji = "🔥",
                            subtitle = "Рекорд: ${userProfile.bestStreak} дн.",
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Фокус-поток",
                            value = "${userProfile.totalFocusMinutes} мин.",
                            emoji = "⏳",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Productivity by Time of Day Visualization
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Пики концентрации по времени суток",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        TimeBar(label = "Утро\n(06-12)", ratio = 0.85f, isPeak = true)
                        TimeBar(label = "День\n(12-18)", ratio = 0.65f, isPeak = false)
                        TimeBar(label = "Вечер\n(18-22)", ratio = 0.45f, isPeak = false)
                        TimeBar(label = "Ночь\n(22-06)", ratio = 0.20f, isPeak = false)
                    }
                }
            }
        }

        // Interactive Calendar Strip
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 Календарь Квестов",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (selectedDateOffsetDays == 0) "Сегодня" else if (selectedDateOffsetDays == 1) "Завтра" else if (selectedDateOffsetDays == -1) "Вчера" else "${selectedDay.dayNum} число",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(daysList) { day ->
                            val isSelected = day.offsetDays == selectedDateOffsetDays
                            val isToday = day.offsetDays == 0
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else if (isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .width(52.dp)
                                    .clickable { selectedDateOffsetDays = day.offsetDays }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = day.dayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = day.dayNum,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Schedule / Tasks for Selected Day
        item {
            Text(
                text = "Задачи на выбранную дату (${if (selectedDateOffsetDays == 0) "Сегодня" else selectedDay.fullDate}):",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (dayTasks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "На этот день нет запланированных дел ✨",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(dayTasks, key = { it.id }) { task ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (task.isCompleted) "✅" else "⚔️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            val reminderText = if (task.dueDate != null) {
                                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                " • ⏰ ${timeFormat.format(Date(task.dueDate))}"
                            } else ""
                            Text(
                                text = "${task.category} • ${task.estimatedMinutes} мин$reminderText",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    emoji: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun TimeBar(label: String, ratio: Float, isPeak: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height((80 * ratio).dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(if (isPeak) GoldAccent else MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 10.sp
        )
    }
}

data class CalendarDay(
    val dayName: String,
    val dayNum: String,
    val fullDate: String,
    val offsetDays: Int
)
