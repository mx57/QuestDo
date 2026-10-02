package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TaskItem
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.QuestDoTheme
import com.example.ui.viewmodel.QuestViewModel

enum class MainTab(val titleRu: String) {
    QUEST("Квест"),
    BACKLOG("Бэклог"),
    FOCUS("Фокус"),
    REWARDS("Награды"),
    CALENDAR("Календарь"),
    SETTINGS("Настройки")
}

class MainActivity : ComponentActivity() {

    private val viewModel: QuestViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
            val currentTasks by viewModel.currentQuestTasks.collectAsStateWithLifecycle()
            val backlogTasks by viewModel.backlogTasks.collectAsStateWithLifecycle()
            val completedTasks by viewModel.completedTasks.collectAsStateWithLifecycle()
            val activeLevel by viewModel.activeLevel.collectAsStateWithLifecycle()
            val customRewards by viewModel.customRewards.collectAsStateWithLifecycle()
            val badges by viewModel.badges.collectAsStateWithLifecycle()
            val currentQuote by viewModel.currentQuote.collectAsStateWithLifecycle()

            val showLevelClearDialog by viewModel.showLevelClearDialog.collectAsStateWithLifecycle()
            val clearedLevel by viewModel.clearedLevel.collectAsStateWithLifecycle()
            val showConfetti by viewModel.showConfetti.collectAsStateWithLifecycle()
            val showAntiProcrastination by viewModel.showAntiProcrastinationDialog.collectAsStateWithLifecycle()
            val showBreathing by viewModel.showBreathingExerciseDialog.collectAsStateWithLifecycle()

            val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
            val selectedPriority by viewModel.selectedPriorityFilter.collectAsStateWithLifecycle()

            var currentTab by remember { mutableStateOf(MainTab.QUEST) }
            var taskToEdit by remember { mutableStateOf<TaskItem?>(null) }
            var showTaskEditDialog by remember { mutableStateOf(false) }
            var showBulkImportDialog by remember { mutableStateOf(false) }
            var spotlightFocusTask by remember { mutableStateOf<TaskItem?>(null) }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* Handled */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            BackHandler(enabled = currentTab != MainTab.QUEST) {
                currentTab = MainTab.QUEST
            }

            QuestDoTheme(
                mood = userProfile.activeMood,
                themeMode = userProfile.themeMode
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        Text(userProfile.appIconStyle.emoji, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = when (currentTab) {
                                                MainTab.QUEST -> "QuestDo • Квест"
                                                MainTab.BACKLOG -> "База задач"
                                                MainTab.FOCUS -> "Фокус-Таймер"
                                                MainTab.REWARDS -> "Зал наград"
                                                MainTab.CALENDAR -> "Календарь & Статистика"
                                                MainTab.SETTINGS -> "Настройки"
                                            },
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                },
                                actions = {
                                    // Mood indicator button
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = "${userProfile.activeMood.iconEmoji} ${userProfile.activeMood.titleRu}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            currentTab = if (currentTab == MainTab.SETTINGS) MainTab.QUEST else MainTab.SETTINGS
                                        },
                                        modifier = Modifier.testTag("settings_top_button")
                                    ) {
                                        Icon(
                                            if (currentTab == MainTab.SETTINGS) Icons.Default.Close else Icons.Default.Settings,
                                            contentDescription = "Настройки"
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.testTag("main_bottom_nav")
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == MainTab.QUEST,
                                    onClick = { currentTab = MainTab.QUEST },
                                    icon = { Icon(if (currentTab == MainTab.QUEST) Icons.Default.Shield else Icons.Outlined.Shield, contentDescription = null) },
                                    label = { Text("Квест") },
                                    modifier = Modifier.testTag("nav_quest")
                                )
                                NavigationBarItem(
                                    selected = currentTab == MainTab.BACKLOG,
                                    onClick = { currentTab = MainTab.BACKLOG },
                                    icon = { Icon(if (currentTab == MainTab.BACKLOG) Icons.Default.ListAlt else Icons.Outlined.ListAlt, contentDescription = null) },
                                    label = { Text("Бэклог") },
                                    modifier = Modifier.testTag("nav_backlog")
                                )
                                NavigationBarItem(
                                    selected = currentTab == MainTab.FOCUS,
                                    onClick = { currentTab = MainTab.FOCUS },
                                    icon = { Icon(if (currentTab == MainTab.FOCUS) Icons.Default.HourglassBottom else Icons.Outlined.HourglassBottom, contentDescription = null) },
                                    label = { Text("Фокус") },
                                    modifier = Modifier.testTag("nav_focus")
                                )
                                NavigationBarItem(
                                    selected = currentTab == MainTab.REWARDS,
                                    onClick = { currentTab = MainTab.REWARDS },
                                    icon = { Icon(if (currentTab == MainTab.REWARDS) Icons.Default.EmojiEvents else Icons.Outlined.EmojiEvents, contentDescription = null) },
                                    label = { Text("Награды") },
                                    modifier = Modifier.testTag("nav_rewards")
                                )
                                NavigationBarItem(
                                    selected = currentTab == MainTab.CALENDAR,
                                    onClick = { currentTab = MainTab.CALENDAR },
                                    icon = { Icon(if (currentTab == MainTab.CALENDAR) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth, contentDescription = null) },
                                    label = { Text("Календарь") },
                                    modifier = Modifier.testTag("nav_calendar")
                                )
                            }
                        },
                        floatingActionButton = {
                            if (currentTab == MainTab.QUEST || currentTab == MainTab.BACKLOG) {
                                FloatingActionButton(
                                    onClick = {
                                        taskToEdit = null
                                        showTaskEditDialog = true
                                    },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.testTag("add_task_fab")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Новый квест")
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                MainTab.QUEST -> QuestScreen(
                                    currentTasks = currentTasks,
                                    activeLevel = activeLevel,
                                    userProfile = userProfile,
                                    currentQuote = currentQuote,
                                    onToggleTask = { viewModel.toggleTask(it) },
                                    onToggleSubtask = { task, idx -> viewModel.toggleSubtask(task, idx) },
                                    onAddSubtask = { task, title -> viewModel.addSubtask(task, title) },
                                    onEditTask = {
                                        taskToEdit = it
                                        showTaskEditDialog = true
                                    },
                                    onShuffleQuest = { viewModel.shuffleCurrentQuest() },
                                    onStartFocusOnTask = { task ->
                                        spotlightFocusTask = task
                                        currentTab = MainTab.FOCUS
                                    },
                                    onOpenSOS = { viewModel.openAntiProcrastinationDialog() },
                                    onOpenBreathing = { viewModel.openBreathingExerciseDialog() },
                                    onAddNewTask = {
                                        taskToEdit = null
                                        showTaskEditDialog = true
                                    },
                                    onClaimRewardManual = {
                                        activeLevel?.let { level ->
                                            viewModel.claimLevelReward("Победа над уровнем!")
                                        }
                                    }
                                )

                                MainTab.BACKLOG -> BacklogScreen(
                                    backlogTasks = backlogTasks,
                                    completedTasks = completedTasks,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { viewModel.searchQuery.value = it },
                                    selectedPriority = selectedPriority,
                                    onSelectPriority = { viewModel.selectedPriorityFilter.value = it },
                                    onToggleTask = { viewModel.toggleTask(it) },
                                    onEditTask = {
                                        taskToEdit = it
                                        showTaskEditDialog = true
                                    },
                                    onOpenBulkImport = { showBulkImportDialog = true },
                                    onAddNewTask = {
                                        taskToEdit = null
                                        showTaskEditDialog = true
                                    }
                                )

                                MainTab.FOCUS -> FocusTimerScreen(
                                    spotlightTask = spotlightFocusTask,
                                    currentQuestTasks = currentTasks,
                                    onCompleteSession = { mins -> viewModel.logFocusSession(mins) },
                                    onSelectTask = { spotlightFocusTask = it }
                                )

                                MainTab.REWARDS -> RewardsScreen(
                                    userProfile = userProfile,
                                    customRewards = customRewards,
                                    badges = badges,
                                    onRedeemReward = { reward, cb -> viewModel.redeemReward(reward, cb) },
                                    onAddReward = { viewModel.addReward(it) },
                                    onDeleteReward = { viewModel.deleteReward(it) }
                                )

                                MainTab.CALENDAR -> CalendarStatsScreen(
                                    userProfile = userProfile,
                                    allTasks = currentTasks + backlogTasks + completedTasks
                                )

                                MainTab.SETTINGS -> SettingsScreen(
                                    userProfile = userProfile,
                                    onSetMood = { viewModel.setMood(it) },
                                    onSetThemeMode = { viewModel.setThemeMode(it) },
                                    onSetTasksPerQuest = { viewModel.setTasksPerQuest(it) },
                                    onSetAppIconStyle = { viewModel.setAppIconStyle(it) },
                                    onSetNotificationTone = { viewModel.setNotificationTone(it) },
                                    onSetSoundEffects = { viewModel.setSoundEffectsEnabled(it) },
                                    onSetHaptics = { viewModel.setHapticsEnabled(it) },
                                    onSetNotifications = { viewModel.setNotificationsEnabled(it) },
                                    onSetDailyReminderTime = { h, m -> viewModel.setDailyReminderTime(h, m) },
                                    onTestNotification = { viewModel.triggerTestNotification() },
                                    onTestAlarm = { viewModel.triggerTestAlarm(10) }
                                )
                            }
                        }
                    }

                    // Confetti Particle Celebration Overlay
                    AnimatedVisibility(
                        visible = showConfetti,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        ConfettiEffect()
                    }

                    // Dialogs
                    if (showLevelClearDialog && clearedLevel != null) {
                        ChestRewardDialog(
                            level = clearedLevel!!,
                            availableRewards = customRewards,
                            onClaimReward = { chosenReward ->
                                viewModel.claimLevelReward(chosenReward)
                            },
                            onDismiss = { viewModel.dismissLevelClearDialog() }
                        )
                    }

                    if (showAntiProcrastination) {
                        AntiProcrastinationDialog(
                            onShuffleQuest = { viewModel.shuffleCurrentQuest() },
                            onStartBreathing = { viewModel.openBreathingExerciseDialog() },
                            onDismiss = { viewModel.closeAntiProcrastinationDialog() }
                        )
                    }

                    if (showBreathing) {
                        BreathingExerciseDialog(
                            onDismiss = { viewModel.closeBreathingExerciseDialog() }
                        )
                    }

                    if (showTaskEditDialog) {
                        TaskEditDialog(
                            initialTask = taskToEdit,
                            onSaveTask = { viewModel.saveTask(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onDismiss = {
                                showTaskEditDialog = false
                                taskToEdit = null
                            }
                        )
                    }

                    if (showBulkImportDialog) {
                        BulkImportDialog(
                            onImport = { text, cat -> viewModel.bulkImportTasks(text, cat) },
                            onDismiss = { showBulkImportDialog = false }
                        )
                    }
                }
            }
        }
    }
}
