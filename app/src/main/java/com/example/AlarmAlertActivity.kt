package com.example

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AppDatabase
import com.example.data.model.Priority
import com.example.data.model.TaskItem
import com.example.data.repository.QuestRepository
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.QuestDoTheme
import com.example.ui.theme.StreakFire
import com.example.util.NotificationHelper
import com.example.util.QuestAlarmScheduler
import com.example.util.SoundEffectsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmAlertActivity : ComponentActivity() {

    private lateinit var soundHelper: SoundEffectsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Turn on screen and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(NotificationHelper.EXTRA_TASK_TITLE) ?: "Важный микро-квест"
        val taskCategory = intent.getStringExtra(NotificationHelper.EXTRA_TASK_CATEGORY) ?: "Квест"
        val taskPriority = intent.getStringExtra(NotificationHelper.EXTRA_TASK_PRIORITY) ?: "Важно"

        soundHelper = SoundEffectsHelper(this)
        soundHelper.startContinuousAlarmRingtone()

        setContent {
            QuestDoTheme {
                AlarmAlertScreen(
                    taskId = taskId,
                    title = taskTitle,
                    category = taskCategory,
                    priorityTitle = taskPriority,
                    onAcceptChallenge = {
                        soundHelper.stopAlarmRingtone()
                        NotificationHelper.cancelNotification(this, taskId.toInt())
                        val openAppIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(NotificationHelper.EXTRA_TASK_ID, taskId)
                        }
                        startActivity(openAppIntent)
                        finish()
                    },
                    onStartTwoMinuteSprint = {
                        soundHelper.stopAlarmRingtone()
                        NotificationHelper.cancelNotification(this, taskId.toInt())
                        val openAppIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(NotificationHelper.EXTRA_TASK_ID, taskId)
                            putExtra("START_TWO_MINUTE_FOCUS", true)
                        }
                        startActivity(openAppIntent)
                        Toast.makeText(this, "🚀 Запущен микро-старт на 2 минуты! Только начни!", Toast.LENGTH_LONG).show()
                        finish()
                    },
                    onCompleteTask = {
                        soundHelper.stopAlarmRingtone()
                        NotificationHelper.cancelNotification(this, taskId.toInt())
                        completeTaskInBackground(taskId)
                        Toast.makeText(this, "Квест выполнен! +25 XP, +5 монет 🪙", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onSnooze5Minutes = {
                        soundHelper.stopAlarmRingtone()
                        NotificationHelper.cancelNotification(this, taskId.toInt())
                        snoozeTaskInBackground(taskId, taskTitle, taskCategory, taskPriority)
                        Toast.makeText(this, "Будильник отложен на 5 минут ⏰", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onDismissAlarm = {
                        soundHelper.stopAlarmRingtone()
                        NotificationHelper.cancelNotification(this, taskId.toInt())
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundHelper.stopAlarmRingtone()
    }

    private fun completeTaskInBackground(taskId: Long) {
        if (taskId == -1L) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val task = db.taskDao().getTaskById(taskId)
                if (task != null && !task.isCompleted) {
                    val repo = QuestRepository(
                        taskDao = db.taskDao(),
                        questLevelDao = db.questLevelDao(),
                        userProfileDao = db.userProfileDao(),
                        customRewardDao = db.customRewardDao(),
                        badgeDao = db.badgeDao()
                    )
                    repo.toggleTaskCompleted(task)
                    QuestAlarmScheduler.cancelTaskAlarm(applicationContext, taskId)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun snoozeTaskInBackground(taskId: Long, title: String, cat: String, prio: String) {
        val snoozeTime = System.currentTimeMillis() + 5 * 60 * 1000L
        val dummyTask = TaskItem(
            id = if (taskId != -1L) taskId else 99999L,
            title = title,
            category = cat,
            dueDate = snoozeTime
        )
        QuestAlarmScheduler.scheduleTaskAlarm(applicationContext, dummyTask)
    }

    companion object {
        fun launchAlarm(context: Context, task: TaskItem) {
            val intent = Intent(context, AlarmAlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(NotificationHelper.EXTRA_TASK_ID, task.id)
                putExtra(NotificationHelper.EXTRA_TASK_TITLE, task.title)
                putExtra(NotificationHelper.EXTRA_TASK_CATEGORY, task.category)
                putExtra(NotificationHelper.EXTRA_TASK_PRIORITY, task.priority.titleRu)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // If background start is blocked by system, notification will show heads-up
            }
        }
    }
}

@Composable
fun AlarmAlertScreen(
    taskId: Long,
    title: String,
    category: String,
    priorityTitle: String,
    onAcceptChallenge: () -> Unit,
    onStartTwoMinuteSprint: () -> Unit,
    onCompleteTask: () -> Unit,
    onSnooze5Minutes: () -> Unit,
    onDismissAlarm: () -> Unit
) {
    // 5-second countdown interactive state (Mel Robbins)
    var countdownSec by remember { mutableIntStateOf(5) }
    var isCountdownRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isCountdownRunning) {
        if (isCountdownRunning) {
            while (countdownSec > 0) {
                delay(1000)
                countdownSec--
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "alarmRadar")
    val waveRadius1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "wave1"
    )
    val waveAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "alpha1"
    )

    val bellScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bell"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("alarm_alert_screen"),
        color = Color(0xFF12121A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Alarm banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldAccent.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏰", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "БУДИЛЬНИК QUESTDO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = GoldAccent
                        )
                    }
                }

                IconButton(onClick = onDismissAlarm) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White.copy(alpha = 0.7f))
                }
            }

            // Centerpiece: Glowing Pulsing Bell with Radar waves
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Expanding Radar Rings
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    drawCircle(
                        color = GoldAccent.copy(alpha = waveAlpha1 * 0.4f),
                        radius = (size.minDimension / 2) * waveRadius1,
                        center = center
                    )
                }

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(bellScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(GoldAccent, StreakFire, Color(0xFF8B5CF6))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔔", fontSize = 48.sp)
                }
            }

            // Task Details Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E2C)
                ),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StreakFire.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = priorityTitle,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = StreakFire,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mel Robbins 5-Second Rule Micro-Challenge Banner
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF28283E),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (countdownSec > 0) "⏱ $countdownSec" else "🚀",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldAccent
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (countdownSec > 0) "Правило 5 секунд: не дай мозгу увильнуть!" else "Время действовать! Первый микро-шаг!",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Договоритесь с собой сделать всего 2 минуты. Это снимет страх.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAcceptChallenge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("alarm_accept_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text(
                        text = "🔥 ПРИНЯТЬ ВЫЗОВ (ОТКРЫТЬ КВЕСТ)",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                FilledTonalButton(
                    onClick = onStartTwoMinuteSprint,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("alarm_two_minute_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF8B5CF6).copy(alpha = 0.35f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⏱️ Микро-старт: 2 минуты без страха",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = onCompleteTask,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Сделано (+XP)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onSnooze5Minutes,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("💤 +5 минут", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
