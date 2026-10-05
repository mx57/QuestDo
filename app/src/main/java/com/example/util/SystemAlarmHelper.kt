package com.example.util

import android.app.AlarmManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import android.provider.Settings
import android.widget.Toast
import java.util.Calendar

object SystemAlarmHelper {

    /**
     * Sets an alarm in the system Clock app via AlarmClock.ACTION_SET_ALARM.
     */
    fun setSystemAlarm(
        context: Context,
        hour: Int,
        minute: Int,
        message: String,
        skipUi: Boolean = false,
        vibrate: Boolean = true,
        days: ArrayList<Int>? = null,
        targetTimestamp: Long? = null
    ): Boolean {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_VIBRATE, vibrate)
            putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)
            if (days != null && days.isNotEmpty()) {
                putIntegerArrayListExtra(AlarmClock.EXTRA_DAYS, days)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            val dateLabel = if (targetTimestamp != null) {
                val cal = Calendar.getInstance().apply { timeInMillis = targetTimestamp }
                val now = Calendar.getInstance()
                val isToday = cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
                        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                val isTomorrow = cal.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR) &&
                        cal.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR)
                val timeStr = "%02d:%02d".format(hour, minute)
                when {
                    isToday -> "сегодня в $timeStr"
                    isTomorrow -> "завтра в $timeStr"
                    else -> java.text.SimpleDateFormat("d MMMM в HH:mm", java.util.Locale("ru")).format(cal.time)
                }
            } else {
                "%02d:%02d".format(hour, minute)
            }
            Toast.makeText(
                context,
                "⏰ Будильник установлен в системных часах на $dateLabel",
                Toast.LENGTH_SHORT
            ).show()
            true
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "Приложение «Часы» не найдено на устройстве",
                Toast.LENGTH_SHORT
            ).show()
            false
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Ошибка запуска системного будильника: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }

    /**
     * Sets a system alarm directly from a millisecond timestamp.
     */
    fun setSystemAlarmFromTimestamp(
        context: Context,
        timestampMs: Long,
        message: String,
        skipUi: Boolean = false
    ): Boolean {
        if (timestampMs <= System.currentTimeMillis()) {
            Toast.makeText(context, "Указанное время напоминания уже прошло", Toast.LENGTH_SHORT).show()
            return false
        }
        val calendar = Calendar.getInstance().apply { timeInMillis = timestampMs }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val now = Calendar.getInstance()
        val isToday = calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)

        val daysList = if (!isToday) arrayListOf(dayOfWeek) else null

        return setSystemAlarm(
            context = context,
            hour = hour,
            minute = minute,
            message = message,
            skipUi = skipUi,
            days = daysList,
            targetTimestamp = timestampMs
        )
    }

    /**
     * Sets a countdown timer in the system Clock app via AlarmClock.ACTION_SET_TIMER.
     */
    fun setSystemTimer(
        context: Context,
        seconds: Int,
        message: String,
        skipUi: Boolean = false
    ): Boolean {
        val safeSeconds = seconds.coerceAtLeast(1)
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, safeSeconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            val mins = safeSeconds / 60
            val sec = safeSeconds % 60
            val timeStr = if (mins > 0) "$mins мин $sec с" else "$sec с"
            Toast.makeText(
                context,
                "⏱️ Таймер на $timeStr запущен в системных часах",
                Toast.LENGTH_SHORT
            ).show()
            true
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "Приложение «Часы» не найдено для установки системного таймера",
                Toast.LENGTH_SHORT
            ).show()
            false
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Ошибка запуска системного таймера: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }

    /**
     * Opens the device's native Clock app to the Alarms screen.
     */
    fun openSystemAlarms(context: Context): Boolean {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Не удалось открыть системный будильник", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Opens the device's native Clock app to the Timers screen.
     */
    fun openSystemTimers(context: Context): Boolean {
        val intent = Intent(AlarmClock.ACTION_SHOW_TIMERS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            // Fallback to SHOW_ALARMS
            openSystemAlarms(context)
        }
    }

    /**
     * Opens system settings to allow scheduling exact alarms if needed (Android 12+).
     */
    fun checkAndOpenExactAlarmSettings(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(intent)
                    return true
                } catch (e: Exception) {
                    return false
                }
            }
        }
        return false
    }

    /**
     * Checks if exact alarm permission is currently granted.
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: true
        } else {
            true
        }
    }
}
