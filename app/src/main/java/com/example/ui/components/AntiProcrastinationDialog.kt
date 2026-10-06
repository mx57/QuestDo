package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.StreakFire
import kotlinx.coroutines.delay

enum class AntiProcrastinationTab(val title: String, val emoji: String) {
    DIAGNOSIS("Диагностика", "🩺"),
    FIVE_SECONDS("5 Секунд", "🚀"),
    SWISS_CHEESE("Сыр Лакейна", "🧀"),
    CBT_REFRAME("КПТ-Разбор", "🪞")
}

data class ResistanceCause(
    val title: String,
    val emoji: String,
    val subtitle: String,
    val psychologicalMechanism: String,
    val antidoteRecipe: String,
    val microActionText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AntiProcrastinationDialog(
    onShuffleQuest: () -> Unit,
    onStartBreathing: () -> Unit,
    onStartTwoMinuteFocus: () -> Unit,
    onClaimVictory: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(AntiProcrastinationTab.DIAGNOSIS) }
    var selectedCauseIndex by remember { mutableIntStateOf(0) }

    // 5-second rule countdown
    var countdownValue by remember { mutableIntStateOf(5) }
    var isCountdownActive by remember { mutableStateOf(false) }

    LaunchedEffect(isCountdownActive) {
        if (isCountdownActive) {
            countdownValue = 5
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
            }
        }
    }

    val causes = remember {
        listOf(
            ResistanceCause(
                title = "Страх ошибки и перфекционизм",
                emoji = "😨",
                subtitle = "«Боюсь сделать криво или неидеально»",
                psychologicalMechanism = "Перфекционизм — это защитная маска страха оценки. Мозг защищает эго, заставляя избегать начала.",
                antidoteRecipe = "Легализация черновика: разрешите себе сделать «на троечку». Любой корявый черновик бесконечно ценнее идеальной пустоты.",
                microActionText = "«Сделаю черновой набросок ровно на 3 минуты»"
            ),
            ResistanceCause(
                title = "Паралич масштаба задачи",
                emoji = "🏔️",
                subtitle = "«Задача слишком огромная, опускаются руки»",
                psychologicalMechanism = "Префронтальная кора видит невыполнимый объем и включает энергосберегающий ступор.",
                antidoteRecipe = "Метод микро-входа: сократите задачу до первого физического действия руки (открыть файл, взять ручку).",
                microActionText = "«Только открыть приложение и прочитать 1 строчку»"
            ),
            ResistanceCause(
                title = "Дофаминовый дефицит и скука",
                emoji = "🥱",
                subtitle = "«Задача нудная, хочется полистать соцсети»",
                psychologicalMechanism = "Мозг жаждет немедленного вознаграждения и бежит от отложенного результата.",
                antidoteRecipe = "Искусственное пари с таймером: пообещайте себе маленькую приятную награду (чай, монеты в QuestDo) ровно через 5 минут работы.",
                microActionText = "«Спринт 5 минут с таймером и награда»"
            ),
            ResistanceCause(
                title = "Усталость и перегруз коры",
                emoji = "🔋",
                subtitle = "«Голова гудит, нет сил думать»",
                psychologicalMechanism = "Биохимическое истощение силы воли. Давить на газ с пустым баком контрпродуктивно.",
                antidoteRecipe = "Восстановление дофаминовых рецепторов: 5 минут дыхания 4-7-8, стакан воды или прогулка без экранов.",
                microActionText = "«5 минут дыхательной паузы для перезапуска»"
            ),
            ResistanceCause(
                title = "Когнитивный туман",
                emoji = "🌫️",
                subtitle = "«Непонятно, за что вообще браться»",
                psychologicalMechanism = "Неопределенность воспринимается миндалевидным телом как прямая угроза безопасности.",
                antidoteRecipe = "Примитивная декомпозиция: запишите самый элементарный шаг, понятный даже первокласснику.",
                microActionText = "«Записать 1 простой вопрос по задаче»"
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("anti_procrastination_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Психология & Анти-Саботаж",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Научные методы преодоления барьера",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Selector Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(AntiProcrastinationTab.values()) { tab ->
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = { Text("${tab.emoji} ${tab.title}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    when (selectedTab) {
                        AntiProcrastinationTab.DIAGNOSIS -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Почему мозг сопротивляется прямо сейчас?",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(causes.indices.toList()) { idx ->
                                        val cause = causes[idx]
                                        InputChip(
                                            selected = selectedCauseIndex == idx,
                                            onClick = { selectedCauseIndex = idx },
                                            label = { Text("${cause.emoji} ${cause.title.take(16)}...", fontSize = 11.sp) }
                                        )
                                    }
                                }

                                val currentCause = causes[selectedCauseIndex]
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "${currentCause.emoji} ${currentCause.title}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = currentCause.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "🧠 Механизм: ${currentCause.psychologicalMechanism}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "💊 Противоядие: ${currentCause.antidoteRecipe}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        AntiProcrastinationTab.FIVE_SECONDS -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF1E1E2C),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Метод 5 секунд (Мел Роббинс)",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = GoldAccent
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Между мыслью и самосаботажем есть окно в 5 секунд. Обратный отсчет отключает миндалевидное тело и включает префронтальную кору.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.8f),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Text(
                                            text = if (isCountdownActive && countdownValue > 0) "$countdownValue" else if (countdownValue == 0) "🚀 СТАРТ!" else "5",
                                            fontSize = 44.sp,
                                            fontWeight = FontWeight.Black,
                                            color = GoldAccent
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            onClick = {
                                                if (!isCountdownActive) {
                                                    isCountdownActive = true
                                                } else if (countdownValue == 0) {
                                                    onStartTwoMinuteFocus()
                                                }
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                                        ) {
                                            Text(
                                                text = if (isCountdownActive && countdownValue == 0) "В бой! Запустить 2 минуты" else if (isCountdownActive) "Считаем..." else "Запустить обратный отсчет 5..4..3..2..1",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AntiProcrastinationTab.SWISS_CHEESE -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Метод «Швейцарского сыра» (Алан Лакейн)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Не пытайтесь съесть сложную задачу целиком. Пробейте в ней несколько маленьких дырок за 2-3 минуты:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                listOf(
                                    "🧀 Дырка №1: Просто открыть файл и напечатать 1 заголовок",
                                    "🧀 Дырка №2: Найти нужную ссылку или папку",
                                    "🧀 Дырка №3: Посидеть 2 минуты в тишине перед задачей без телефона"
                                ).forEach { hole ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = hole,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        AntiProcrastinationTab.CBT_REFRAME -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Когнитивная декатастрофизация (КПТ)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                listOf(
                                    Pair("1. Что самое худшее случится, если начать прямо сейчас?", "Вы сделаете несовершенно. Это исправимо за 5 минут."),
                                    Pair("2. Какова вероятность катастрофы?", "Менее 1%. Мир не рухнет от черновика."),
                                    Pair("3. Какова цена продолжения саботажа?", "Потерянные нервы, фоновое чувство вины и вечер без отдыха.")
                                ).forEach { (q, a) ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(text = q, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = a, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Tools
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start 2-Minute Sprint
                    Button(
                        onClick = onStartTwoMinuteFocus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🚀 Начать правило 2 минут (Микро-старт)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Claim Victory
                        FilledTonalButton(
                            onClick = {
                                onClaimVictory()
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🏆 Преодолел! (+XP)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Breathing
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onStartBreathing()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Дыхание 4-7-8", fontSize = 11.sp)
                        }
                    }

                    // Soft Shuffle
                    OutlinedButton(
                        onClick = {
                            onShuffleQuest()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Cached, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Перетасовать задачи квеста без штрафа", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

