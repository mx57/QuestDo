package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.db.AppDatabase
import com.example.data.model.RecurrenceRule
import com.example.data.model.TaskItem
import com.example.data.repository.QuestRepository
import com.example.util.NotificationHelper
import com.example.util.QuestAlarmScheduler
import com.example.util.SoundEffectsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == NotificationHelper.ACTION_COMPLETE_TASK) {
            val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
            if (taskId != -1L) {
                // Cancel notification
                NotificationHelper.cancelNotification(context, taskId.toInt())

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val repository = QuestRepository(
                            taskDao = db.taskDao(),
                            questLevelDao = db.questLevelDao(),
                            userProfileDao = db.userProfileDao(),
                            customRewardDao = db.customRewardDao(),
                            badgeDao = db.badgeDao()
                        )
                        val task = db.taskDao().getTaskById(taskId)
                        if (task != null && !task.isCompleted) {
                            QuestAlarmScheduler.cancelTaskAlarm(context, taskId)
                            repository.toggleTaskCompleted(task)

                            // Handle recurrence if configured
                            if (task.recurrence != RecurrenceRule.NONE && task.dueDate != null) {
                                val nextDue = calculateNextDueDate(task.dueDate, task.recurrence)
                                val recurringTask = task.copy(
                                    id = 0,
                                    isCompleted = false,
                                    completedAt = null,
                                    dueDate = nextDue,
                                    createdAt = System.currentTimeMillis()
                                )
                                val newId = db.taskDao().insertTask(recurringTask)
                                QuestAlarmScheduler.scheduleTaskAlarm(context, recurringTask.copy(id = newId))
                            }

                            // Sound & haptics feedback
                            val soundHelper = SoundEffectsHelper(context)
                            soundHelper.playVictoryChime()
                            soundHelper.triggerVibration("VICTORY")
                        }
                    } catch (e: Exception) {
                        // Log
                    }
                }

                Toast.makeText(context, "Квест выполнен! +25 XP, +5 монет 🪙", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateNextDueDate(currentDueDate: Long, recurrence: RecurrenceRule): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = currentDueDate }
        when (recurrence) {
            RecurrenceRule.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            RecurrenceRule.WEEKDAYS -> {
                do {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                } while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            }
            RecurrenceRule.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            RecurrenceRule.NONE -> {}
        }
        return cal.timeInMillis
    }
}
