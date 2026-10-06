package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.receiver.TaskActionReceiver

object NotificationHelper {

    const val CHANNEL_TASK_REMINDERS = "quest_task_reminders"
    const val CHANNEL_FOCUS_TIMER = "quest_focus_timer"
    const val CHANNEL_DAILY_MOTIVATION = "quest_daily_motivation"

    const val ACTION_COMPLETE_TASK = "com.example.ACTION_COMPLETE_TASK"
    const val ACTION_SNOOZE_TASK = "com.example.ACTION_SNOOZE_TASK"
    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"
    const val EXTRA_TASK_CATEGORY = "extra_task_category"
    const val EXTRA_TASK_PRIORITY = "extra_task_priority"

    /**
     * Initializes all notification channels with high priority, sound, and vibration.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val alarmAudioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notificationAudioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            // 1. Task Reminders & Alarms Channel
            val taskChannel = NotificationChannel(
                CHANNEL_TASK_REMINDERS,
                "Будильники и напоминания о задачах",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Громкие звуковые напоминания и будильники для запланированных квестов"
                enableLights(true)
                lightColor = Color.rgb(255, 193, 7) // Gold
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300, 150, 450)
                setSound(alarmSoundUri, alarmAudioAttributes)
            }

            // 2. Focus Timer Finished Channel
            val timerChannel = NotificationChannel(
                CHANNEL_FOCUS_TIMER,
                "Фокус-таймер и будильник потока",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о завершении интервалов фокуса и отдыха"
                enableLights(true)
                lightColor = Color.rgb(103, 80, 164)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 250, 100, 350)
                setSound(alarmSoundUri, alarmAudioAttributes)
            }

            // 3. Daily Motivation Channel
            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_MOTIVATION,
                "Ежедневная мотивация и защита серии",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Утренний настрой и напоминания о сохранении огня дисциплины"
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(listOf(taskChannel, timerChannel, dailyChannel))
        }
    }

    /**
     * Shows high-priority alarm notification for a scheduled task.
     */
    fun showTaskReminder(
        context: Context,
        taskId: Long,
        title: String,
        category: String,
        priorityTitle: String
    ) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Full Screen Alert Activity Intent (Ringing lock-screen / heads-up overlay)
        val fullScreenIntent = Intent(context, com.example.AlarmAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, title)
            putExtra(EXTRA_TASK_CATEGORY, category)
            putExtra(EXTRA_TASK_PRIORITY, priorityTitle)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            (taskId + 200000).toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Complete task directly from notification
        val completeIntent = Intent(context, TaskActionReceiver::class.java).apply {
            action = ACTION_COMPLETE_TASK
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId + 100000).toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze 5 minutes directly from notification
        val snoozeIntent = Intent(context, TaskActionReceiver::class.java).apply {
            action = ACTION_SNOOZE_TASK
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, title)
            putExtra(EXTRA_TASK_CATEGORY, category)
            putExtra(EXTRA_TASK_PRIORITY, priorityTitle)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId + 300000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_TASK_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ Пора выполнить квест: $title")
            .setContentText("[$category • $priorityTitle] Сделайте первый шаг!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("[$category • $priorityTitle]\n$title\n\nНе откладывайте на потом — закройте задачу сейчас и заработайте опыт!")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setColor(0xFFFFB300.toInt()) // Gold
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 150, 250, 150, 400))
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_launcher_foreground, "💤 +5 мин", snoozePendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "✅ Сделано", completePendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "🔔 Открыть", openPendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(taskId.toInt(), builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    /**
     * Shows notification when a focus timer finishes.
     */
    fun showTimerCompleted(
        context: Context,
        minutes: Int,
        taskTitle: String? = null
    ) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            99999,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val earnedCoins = (minutes / 5).coerceAtLeast(1)
        val earnedXp = minutes * 3
        val bodyText = if (taskTitle != null) {
            "Квест: «$taskTitle»\nСессия $minutes мин завершена! Награда: +$earnedCoins 🪙, +$earnedXp XP"
        } else {
            "Сессия $minutes мин завершена! Награда: +$earnedCoins 🪙, +$earnedXp XP"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_FOCUS_TIMER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🏆 Время вышло! Фокус завершен")
            .setContentText("Сессия $minutes мин завершена. +$earnedCoins 🪙, +$earnedXp XP")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setColor(0xFF7C4DFF.toInt())
            .setContentIntent(openPendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(88888, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    /**
     * Shows daily motivation / streak reminder.
     */
    fun showDailyMotivation(
        context: Context,
        streakDays: Int,
        quote: String
    ) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            77777,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_DAILY_MOTIVATION)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🔥 Огонь дисциплины: Серия $streakDays дн.")
            .setContentText(quote)
            .setStyle(NotificationCompat.BigTextStyle().bigText("«$quote»\n\nЗайдите в QuestDo и закройте хотя бы 1 микро-квест сегодня!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(0xFFFF5722.toInt())
            .setContentIntent(openPendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(77777, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    /**
     * Test notification for instant verification in Settings.
     */
    fun showTestNotification(context: Context) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            12345,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_TASK_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🔔 QuestDo: Проверка уведомлений")
            .setContentText("Уведомления и звуки работают безупречно! 🚀")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Система напоминаний и будильников полностью активна. Ваши квесты и таймеры теперь под надежной защитой!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(0xFFFFB300.toInt())
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(12345, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun cancelNotification(context: Context, id: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.cancel(id)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
