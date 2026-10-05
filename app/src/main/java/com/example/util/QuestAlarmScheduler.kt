package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.TaskItem
import com.example.receiver.AlarmReceiver
import java.util.Calendar

object QuestAlarmScheduler {

    const val ACTION_TASK_REMINDER = "com.example.ACTION_TASK_REMINDER"
    const val ACTION_TIMER_EXPIRED = "com.example.ACTION_TIMER_EXPIRED"
    const val ACTION_DAILY_REMINDER = "com.example.ACTION_DAILY_REMINDER"
    const val ACTION_TEST_ALARM = "com.example.ACTION_TEST_ALARM"

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"
    const val EXTRA_TASK_CATEGORY = "extra_task_category"
    const val EXTRA_TASK_PRIORITY = "extra_task_priority"
    const val EXTRA_TIMER_MINUTES = "extra_timer_minutes"

    private const val TIMER_ALARM_REQUEST_CODE = 99911
    private const val DAILY_ALARM_REQUEST_CODE = 88811
    private const val TEST_ALARM_REQUEST_CODE = 77711

    /**
     * Schedules or cancels an alarm for a TaskItem based on its dueDate and completion status.
     */
    fun scheduleTaskAlarm(context: Context, task: TaskItem) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val dueDate = task.dueDate

        if (dueDate == null || dueDate <= System.currentTimeMillis() || task.isCompleted) {
            cancelTaskAlarm(context, task.id)
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TASK_REMINDER
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)
            putExtra(EXTRA_TASK_CATEGORY, task.category)
            putExtra(EXTRA_TASK_PRIORITY, task.priority.titleRu)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(dueDate, pendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueDate, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, dueDate, pendingIntent)
            }
            Log.d("QuestAlarmScheduler", "Alarm scheduled for task ${task.id} at $dueDate")
        } catch (e: Exception) {
            Log.w("QuestAlarmScheduler", "setAlarmClock failed, falling back to setAndAllowWhileIdle: ${e.message}")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueDate, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, dueDate, pendingIntent)
                }
            } catch (fallbackEx: Exception) {
                Log.e("QuestAlarmScheduler", "Failed to schedule alarm completely: ${fallbackEx.message}")
            }
        }
    }

    /**
     * Cancels an existing alarm for a task.
     */
    fun cancelTaskAlarm(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TASK_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Schedules a background timer alarm for when the user leaves the screen/app.
     */
    fun scheduleFocusTimerAlarm(context: Context, durationSeconds: Int, taskTitle: String?) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerAtMs = System.currentTimeMillis() + (durationSeconds * 1000L)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TIMER_EXPIRED
            putExtra(EXTRA_TIMER_MINUTES, durationSeconds / 60)
            if (taskTitle != null) {
                putExtra(EXTRA_TASK_TITLE, taskTitle)
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            TIMER_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("QuestAlarmScheduler", "Failed to schedule timer alarm: ${e.message}")
        }
    }

    fun cancelFocusTimerAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TIMER_EXPIRED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            TIMER_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Schedules daily motivation reminder at given hour:minute.
     */
    fun scheduleDailyReminder(context: Context, hour: Int = 9, minute: Int = 0) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_DAILY_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e("QuestAlarmScheduler", "Failed to schedule daily reminder: ${e.message}")
        }
    }

    /**
     * Sets a quick test alarm (e.g. 10 seconds) for user verification.
     */
    fun scheduleTestAlarm(context: Context, delaySeconds: Int = 10) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerAtMs = System.currentTimeMillis() + (delaySeconds * 1000L)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TEST_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            TEST_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("QuestAlarmScheduler", "Failed to schedule test alarm: ${e.message}")
        }
    }
}
