package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTemplatesDialog(
    onSelectTemplate: (TaskItem) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Все") }
    var filterQuery by remember { mutableStateOf("") }

    val categories = listOf("Все", "Анти-Саботаж ⚡", "Психология 🧠", "Работа 💻", "Здоровье 💧", "Учеба 📖", "Быт 🧹", "Финансы 💰")

    val allTemplates = remember {
        listOf(
            // АНТИ-САБОТАЖ & ПРОКРАСТИНАЦИЯ
            QuestTemplate(
                title = "Метод 5 секунд: запуск без раздумий",
                description = "Сосчитать 5-4-3-2-1 и физически сделать первое действие до того, как мозг включит сомнения",
                category = "Анти-Саботаж ⚡",
                iconEmoji = "🚀",
                energyRequired = 1,
                estimatedMinutes = 5,
                priority = Priority.CRITICAL,
                subtasks = listOf("Вслух сосчитать 5-4-3-2-1", "Сесть за рабочее место", "Сделать первое физическое движение")
            ),
            QuestTemplate(
                title = "Правило 2 минут: сделать микро-вход",
                description = "Договоритесь с собой делать задачу ровно 2 минуты. Порог входа исчезает",
                category = "Анти-Саботаж ⚡",
                iconEmoji = "⏱️",
                energyRequired = 1,
                estimatedMinutes = 2,
                priority = Priority.HIGH,
                subtasks = listOf("Засечь 2 минуты", "Делать без оглядки на качество", "Решить: продолжить или остановиться")
            ),
            QuestTemplate(
                title = "Метод швейцарского сыра: 3 дырки в задаче",
                description = "Пробейте три случайных простых действия в пугающей задаче, не пытаясь решить её целиком",
                category = "Анти-Саботаж ⚡",
                iconEmoji = "🧀",
                energyRequired = 1,
                estimatedMinutes = 10,
                priority = Priority.HIGH,
                subtasks = listOf("Дырка 1: Открыть документ и написать заголовок", "Дырка 2: Найти 1 нужный файл", "Дырка 3: Записать 1 мысль")
            ),
            QuestTemplate(
                title = "Съесть лягушку с утра (Самое неприятное дело)",
                description = "Закройте самое откладываемое дело первым с утра и освободите дофамин на весь день",
                category = "Анти-Саботаж ⚡",
                iconEmoji = "🐸",
                energyRequired = 3,
                estimatedMinutes = 20,
                priority = Priority.CRITICAL,
                subtasks = listOf("Выбрать самое противное дело", "Отключить все уведомления", "Закрыть задачу до 12:00")
            ),
            QuestTemplate(
                title = "Черновик на тройку (Анти-перфекционизм)",
                description = "Разрешите себе сделать намеренно корявый черновик. Сделанное на троечку лучше идеального в мечтах",
                category = "Анти-Саботаж ⚡",
                iconEmoji = "🎯",
                energyRequired = 1,
                estimatedMinutes = 15,
                priority = Priority.MEDIUM,
                subtasks = listOf("Сказать себе: «Я делаю черновик»", "Набросать грубый скелет задачи", "Не редактировать во время написания")
            ),
            QuestTemplate(
                title = "10-минутный blitz-спринт с таймером",
                description = "Держать фокус ровно 10 минут под давлением тикающего таймера",
                category = "Анти-Саботаж ⚡",
                iconEmoji = "⏳",
                energyRequired = 2,
                estimatedMinutes = 10,
                priority = Priority.HIGH,
                subtasks = listOf("Запустить таймер на 10 мин", "Не отрывать взгляд от задачи", "Похвалить себя по окончании")
            ),
            QuestTemplate(
                title = "Нарезать слона на 4 микро-стейка",
                description = "Разбить неподъемный проект на элементарные части размером по 5-10 минут",
                category = "Анти-Саботаж ⚡",
                iconEmoji = "🐘",
                energyRequired = 1,
                estimatedMinutes = 8,
                priority = Priority.HIGH,
                subtasks = listOf("Выписать проект на бумагу", "Разделить на 4 конкретных шага", "Выбрать самый первый")
            ),

            // ПСИХОЛОГИЯ & КПТ
            QuestTemplate(
                title = "Когнитивная переоценка (КПТ-разбор тревоги)",
                description = "Отделить реальные факты от катастрофических фантазий ума",
                category = "Психология 🧠",
                iconEmoji = "🪞",
                energyRequired = 1,
                estimatedMinutes = 10,
                priority = Priority.HIGH,
                subtasks = listOf("Записать пугающую мысль", "Найти 3 доказательства «против» неё", "Сформулировать трезвую реалистичную оценку")
            ),
            QuestTemplate(
                title = "Практика самосострадания (Кристин Нефф)",
                description = "Снять самобичевание за прокрастинацию. Ошибаться — часть человеческого опыта",
                category = "Психология 🧠",
                iconEmoji = "🌸",
                energyRequired = 1,
                estimatedMinutes = 5,
                priority = Priority.MEDIUM,
                subtasks = listOf("Положить руку на сердце", "Сказать: «Мне тяжело, но я справлюсь»", "Простить себя за вчерашнее")
            ),
            QuestTemplate(
                title = "Заземление 5-4-3-2-1 при панике и ступоре",
                description = "Нейробиологическая техника возвращения в реальность из вихря мыслей",
                category = "Психология 🧠",
                iconEmoji = "⚓",
                energyRequired = 1,
                estimatedMinutes = 5,
                priority = Priority.HIGH,
                subtasks = listOf("5 предметов, которые вижу", "4 вещи, которые осязаю", "3 звука, 2 запаха, 1 вкус")
            ),
            QuestTemplate(
                title = "Метод WOOP (Желание, Итог, Препятствие, План)",
                description = "Научно доказанный метод психолога Габриэль Эттинген для преодоления барьеров",
                category = "Психология 🧠",
                iconEmoji = "🌟",
                energyRequired = 2,
                estimatedMinutes = 12,
                priority = Priority.HIGH,
                subtasks = listOf("Wish: Какова цель?", "Outcome: Какой лучший результат?", "Obstacle: Какое главное препятствие внутри?", "Plan: Если [X], то я сделаю [Y]")
            ),
            QuestTemplate(
                title = "Осознанное сканирование тела (Body Scan)",
                description = "Снять физические зажимы в челюсти, плечах и диафрагме",
                category = "Психология 🧠",
                iconEmoji = "🧘",
                energyRequired = 1,
                estimatedMinutes = 7,
                priority = Priority.LOW,
                subtasks = listOf("Расслабить челюсть и язык", "Опустить плечи от ушей", "Мягкий глубокий выдох животом")
            ),
            QuestTemplate(
                title = "Выписать 3 автоматические мысли",
                description = "Выгрузить ментальный шум и протестировать мысли на логические искажения",
                category = "Психология 🧠",
                iconEmoji = "📝",
                energyRequired = 1,
                estimatedMinutes = 8,
                priority = Priority.MEDIUM
            ),

            // ЗДОРОВЬЕ
            QuestTemplate(
                title = "Выпить 2 стакана чистой воды с лимоном",
                description = "Восстановить гидратацию мозга и запустить метаболизм",
                category = "Здоровье 💧",
                iconEmoji = "💧",
                energyRequired = 1,
                estimatedMinutes = 5,
                priority = Priority.HIGH,
                subtasks = listOf("Налить стакан теплой воды", "Выпить не спеша", "Заметить ясность в теле")
            ),
            QuestTemplate(
                title = "Разминка шеи, спины и 20 приседаний",
                description = "Снять зажимы в теле и разогнать кровообращение",
                category = "Здоровье 💧",
                iconEmoji = "🏃",
                energyRequired = 1,
                estimatedMinutes = 7,
                priority = Priority.HIGH,
                subtasks = listOf("Вращение плечами и шеей 2 мин", "20 приседаний", "Глубокое дыхание 1 мин")
            ),
            QuestTemplate(
                title = "Прогулка на свежем воздухе без телефона",
                description = "20 минут ходьбы для насыщения мозга кислородом",
                category = "Здоровье 💧",
                iconEmoji = "🌲",
                energyRequired = 1,
                estimatedMinutes = 20,
                priority = Priority.MEDIUM
            ),
            QuestTemplate(
                title = "15 минут растяжки и йоги перед сном",
                description = "Расслабить мышцы и подготовить нервную систему ко сну",
                category = "Здоровье 💧",
                iconEmoji = "🌙",
                energyRequired = 1,
                estimatedMinutes = 15,
                priority = Priority.LOW,
                subtasks = listOf("Поза кошки-коровы", "Растяжка подколенных сухожилий", "Шавасана 3 мин")
            ),
            QuestTemplate(
                title = "Дыхание 4-7-8 для мгновенного спокойствия",
                description = "Снизить кортизол и вернуть вегетативный баланс за 4 цикла",
                category = "Здоровье 💧",
                iconEmoji = "🌿",
                energyRequired = 1,
                estimatedMinutes = 5,
                priority = Priority.HIGH,
                subtasks = listOf("Вдох носом 4с", "Задержка 7с", "Мягкий выдох ртом 8с (4 цикла)")
            ),

            // РАБОТА & IT
            QuestTemplate(
                title = "Спринт концентрации (Pomodoro 25 мин)",
                description = "Погружение в главную задачу без переключения контекста",
                category = "Работа 💻",
                iconEmoji = "💻",
                energyRequired = 2,
                estimatedMinutes = 25,
                priority = Priority.HIGH,
                subtasks = listOf("Закрыть лишние вкладки браузера", "Включить режим 'Не беспокоить'", "Работать 25 мин до звонка")
            ),
            QuestTemplate(
                title = "Deep Work: 50 минут глубокого погружения",
                description = "Работа над ключевым проектом в состоянии абсолютного потока по Кэлу Ньюпорту",
                category = "Работа 💻",
                iconEmoji = "⚔️",
                energyRequired = 3,
                estimatedMinutes = 50,
                priority = Priority.CRITICAL,
                subtasks = listOf("Телефон убрать в другую комнату", "Открыть только один рабочий экран", "Фокус 50 минут")
            ),
            QuestTemplate(
                title = "Inbox Zero: разбор почты и чатов",
                description = "Ответить, архивировать или превратить в задачи",
                category = "Работа 💻",
                iconEmoji = "📬",
                energyRequired = 2,
                estimatedMinutes = 15,
                priority = Priority.HIGH,
                subtasks = listOf("Разобрать рабочую почту", "Ответить на важные сообщения", "Очистить список уведомлений")
            ),
            QuestTemplate(
                title = "Составить план на завтра (Правило 3 побед)",
                description = "Выбрать не более 3 главных результатов завтрашнего дня",
                category = "Работа 💻",
                iconEmoji = "🎯",
                energyRequired = 1,
                estimatedMinutes = 10,
                priority = Priority.CRITICAL,
                subtasks = listOf("Выписать главную задачу №1", "Определить две вспомогательные", "Подготовить файлы с вечера")
            ),
            QuestTemplate(
                title = "Ревью выполненных задач и рефлексия",
                description = "Зафиксировать прогресс, победы и извлеченные уроки",
                category = "Работа 💻",
                iconEmoji = "📋",
                energyRequired = 1,
                estimatedMinutes = 10,
                priority = Priority.MEDIUM
            ),

            // УЧЕБА & ЯЗЫКИ
            QuestTemplate(
                title = "1 урок иностранного языка / 10 новых слов",
                description = "Ежедневный микро-шаг к свободному владению языком",
                category = "Учеба 📖",
                iconEmoji = "🇬🇧",
                energyRequired = 2,
                estimatedMinutes = 15,
                priority = Priority.HIGH,
                subtasks = listOf("Открыть приложение или карточки", "Выучить 10 фраз в контексте", "Составить 3 своих предложения")
            ),
            QuestTemplate(
                title = "Прочитать 15 страниц развивающей книги",
                description = "Без соцсетей и с карандашом для ключевых мыслей",
                category = "Учеба 📖",
                iconEmoji = "📖",
                energyRequired = 2,
                estimatedMinutes = 20,
                priority = Priority.MEDIUM,
                subtasks = listOf("Прочесть 15 страниц", "Записать 1 ключевую мысль в заметки")
            ),
            QuestTemplate(
                title = "Повторение материала по методу Фейнмана",
                description = "Объяснить сложную концепцию простыми словами ребенку",
                category = "Учеба 📖",
                iconEmoji = "🧠",
                energyRequired = 3,
                estimatedMinutes = 25,
                priority = Priority.HIGH,
                subtasks = listOf("Выбрать сложную тему", "Записать простое объяснение", "Найти и устранить пробелы")
            ),

            // БЫТ & ДОМ
            QuestTemplate(
                title = "Экспресс-уборка рабочего стола (10 мин)",
                description = "Чистый физический стол рождает абсолютную ясность в голове",
                category = "Быт 🧹",
                iconEmoji = "🧹",
                energyRequired = 1,
                estimatedMinutes = 10,
                priority = Priority.MEDIUM,
                subtasks = listOf("Выкинуть чеки и мусор", "Убрать чашки", "Протереть пыль со стола")
            ),
            QuestTemplate(
                title = "Выбросить или раздать 5 ненужных вещей",
                description = "Освободить пространство и снизить визуальный шум в комнате",
                category = "Быт 🧹",
                iconEmoji = "🗑️",
                energyRequired = 1,
                estimatedMinutes = 10,
                priority = Priority.LOW
            ),
            QuestTemplate(
                title = "Проветрить все комнаты и полить цветы",
                description = "Свежий воздух стимулирует активность префронтальной коры",
                category = "Быт 🧹",
                iconEmoji = "🪴",
                energyRequired = 1,
                estimatedMinutes = 5,
                priority = Priority.LOW
            ),
            QuestTemplate(
                title = "Правило одного касания (Сразу убрать на место)",
                description = "Не откладывать вещь «на потом», а положить на место за 5 секунд",
                category = "Быт 🧹",
                iconEmoji = "✨",
                energyRequired = 1,
                estimatedMinutes = 5,
                priority = Priority.LOW
            ),

            // ФИНАНСЫ
            QuestTemplate(
                title = "Финансовая ревизия дня и учет трат",
                description = "Зафиксировать расходы за день и проверить остаток бюджета",
                category = "Финансы 💰",
                iconEmoji = "💰",
                energyRequired = 1,
                estimatedMinutes = 5,
                priority = Priority.MEDIUM
            ),
            QuestTemplate(
                title = "Ревизия платных подписок и счетов",
                description = "Найти и отменить неиспользуемые автоплатежи",
                category = "Финансы 💰",
                iconEmoji = "💳",
                energyRequired = 2,
                estimatedMinutes = 15,
                priority = Priority.HIGH,
                subtasks = listOf("Проверить выписку по карте за месяц", "Отменить ненужные сервисы", "Зафиксировать экономию")
            )
        )
    }

    val filteredTemplates = remember(selectedCategory, filterQuery, allTemplates) {
        allTemplates.filter { t ->
            val matchesCategory = selectedCategory == "Все" || t.category == selectedCategory
            val matchesSearch = filterQuery.isBlank() ||
                    t.title.contains(filterQuery, ignoreCase = true) ||
                    t.description.contains(filterQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
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
                            text = "⚡ Каталог Готовых Квестов",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Проверенные привычки и микро-шаги (${allTemplates.size} шаблонов)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Box
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = { Text("Поиск по шаблонам...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Template List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredTemplates.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Шаблоны не найдены",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredTemplates) { item ->
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
                                            text = item.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "${item.category} • ⏱ ${item.estimatedMinutes}м",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (item.subtasks.isNotEmpty()) {
                                                Text(
                                                    text = "Чек-лист: ${item.subtasks.size}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
                                    FilledIconButton(
                                        onClick = {
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
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Добавить квест",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
