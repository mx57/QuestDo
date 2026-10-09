package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.util.NotificationHelper
import com.example.util.QuestAlarmScheduler
import com.example.util.SoundEffectsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("AlarmReceiver", "Received alarm action: $action")

        val soundHelper = SoundEffectsHelper(context)

        when (action) {
            QuestAlarmScheduler.ACTION_TASK_REMINDER -> {
                val taskId = intent.getLongExtra(QuestAlarmScheduler.EXTRA_TASK_ID, -1L)
                val title = intent.getStringExtra(QuestAlarmScheduler.EXTRA_TASK_TITLE) ?: "Важная задача"
                val category = intent.getStringExtra(QuestAlarmScheduler.EXTRA_TASK_CATEGORY) ?: "Квест"
                val priority = intent.getStringExtra(QuestAlarmScheduler.EXTRA_TASK_PRIORITY) ?: "Важно"

                if (taskId != -1L) {
                    // Check database if task is still pending and not completed
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getInstance(context)
                            val task = db.taskDao().getTaskById(taskId)
                            val profile = db.userProfileDao().getUserProfile().firstOrNull()
                            val isDemon = profile?.isDemonMode == true || profile?.notificationTone == com.example.data.model.NotificationTone.DEMON
                            if (task != null && !task.isCompleted) {
                                NotificationHelper.showTaskReminder(context, taskId, title, category, priority, isDemonTone = isDemon)
                                if (isDemon) {
                                    soundHelper.playDemonLaugh()
                                } else {
                                    soundHelper.playAlarmAlert()
                                }
                                soundHelper.triggerVibration(if (isDemon) "DEMON" else "ALARM")
                                com.example.AlarmAlertActivity.launchAlarm(context, task)
                            }
                        } catch (e: Exception) {
                            // Fallback show notification anyway
                            NotificationHelper.showTaskReminder(context, taskId, title, category, priority)
                            soundHelper.playAlarmAlert()
                        }
                    }
                }
            }

            QuestAlarmScheduler.ACTION_TIMER_EXPIRED -> {
                val minutes = intent.getIntExtra(QuestAlarmScheduler.EXTRA_TIMER_MINUTES, 25)
                val taskTitle = intent.getStringExtra(QuestAlarmScheduler.EXTRA_TASK_TITLE)
                NotificationHelper.showTimerCompleted(context, minutes, taskTitle)
                soundHelper.playVictoryChime()
                soundHelper.triggerVibration("VICTORY")
            }

            QuestAlarmScheduler.ACTION_DAILY_REMINDER -> {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val profile = db.userProfileDao().getUserProfile().firstOrNull()
                        val streak = profile?.streakDays ?: 1
                        val quote = "Каждый день — это шаг к мастерству. Защитите свой огонь дисциплины!"
                        NotificationHelper.showDailyMotivation(context, streak, quote)
                        soundHelper.playVictoryChime()
                    } catch (e: Exception) {
                        NotificationHelper.showDailyMotivation(context, 1, "Время закрыть микро-квест дня!")
                    }
                }
            }

            QuestAlarmScheduler.ACTION_TEST_ALARM -> {
                NotificationHelper.showTestNotification(context)
                soundHelper.playAlarmAlert()
                soundHelper.triggerVibration("ALARM")
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                // Reschedule all active alarms after phone reboot
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val now = System.currentTimeMillis()
                        val pendingTasks = db.taskDao().getAllTasks().firstOrNull() ?: emptyList()
                        for (task in pendingTasks) {
                            if (!task.isCompleted && task.dueDate != null && task.dueDate > now) {
                                QuestAlarmScheduler.scheduleTaskAlarm(context, task)
                            }
                        }
                        QuestAlarmScheduler.scheduleDailyReminder(context, 9, 0)
                    } catch (e: Exception) {
                        Log.e("AlarmReceiver", "Failed to reschedule on boot: ${e.message}")
                    }
                }
            }
        }
    }
}
