package com.example.util

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.provider.AlarmClock
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SystemAlarmHelperTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun setSystemTimer_success_launchesCorrectIntentAndDisplaysToast() {
        val result = SystemAlarmHelper.setSystemTimer(
            context = context,
            seconds = 125,
            message = "Pomodoro Timer",
            skipUi = false
        )

        assertTrue(result)

        val shadowApp = shadowOf(context as Application)
        val startedIntent = shadowApp.nextStartedActivity
        assertNotNull(startedIntent)
        assertEquals(AlarmClock.ACTION_SET_TIMER, startedIntent.action)
        assertEquals(125, startedIntent.getIntExtra(AlarmClock.EXTRA_LENGTH, 0))
        assertEquals("Pomodoro Timer", startedIntent.getStringExtra(AlarmClock.EXTRA_MESSAGE))
        assertEquals(false, startedIntent.getBooleanExtra(AlarmClock.EXTRA_SKIP_UI, true))
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, startedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK)

        val latestToast = ShadowToast.getTextOfLatestToast()
        assertEquals("⏱️ Таймер на 2 мин 5 с запущен в системных часах", latestToast)
    }

    @Test
    fun setSystemTimer_skipUiTrue_setsSkipUiExtra() {
        val result = SystemAlarmHelper.setSystemTimer(
            context = context,
            seconds = 45,
            message = "Short Break",
            skipUi = true
        )

        assertTrue(result)

        val shadowApp = shadowOf(context as Application)
        val startedIntent = shadowApp.nextStartedActivity
        assertNotNull(startedIntent)
        assertEquals(true, startedIntent.getBooleanExtra(AlarmClock.EXTRA_SKIP_UI, false))
        assertEquals(45, startedIntent.getIntExtra(AlarmClock.EXTRA_LENGTH, 0))

        val latestToast = ShadowToast.getTextOfLatestToast()
        assertEquals("⏱️ Таймер на 45 с запущен в системных часах", latestToast)
    }

    @Test
    fun setSystemTimer_zeroOrNegativeSeconds_coercesToOneSecond() {
        val resultZero = SystemAlarmHelper.setSystemTimer(
            context = context,
            seconds = 0,
            message = "Zero Timer"
        )
        assertTrue(resultZero)

        val shadowApp = shadowOf(context as Application)
        val intentZero = shadowApp.nextStartedActivity
        assertNotNull(intentZero)
        assertEquals(1, intentZero.getIntExtra(AlarmClock.EXTRA_LENGTH, 0))
        assertEquals("⏱️ Таймер на 1 с запущен в системных часах", ShadowToast.getTextOfLatestToast())

        val resultNegative = SystemAlarmHelper.setSystemTimer(
            context = context,
            seconds = -15,
            message = "Negative Timer"
        )
        assertTrue(resultNegative)

        val intentNegative = shadowApp.nextStartedActivity
        assertNotNull(intentNegative)
        assertEquals(1, intentNegative.getIntExtra(AlarmClock.EXTRA_LENGTH, 0))
    }

    @Test
    fun setSystemTimer_activityNotFoundException_returnsFalseAndShowsErrorToast() {
        val failingContext = object : ContextWrapper(context) {
            override fun startActivity(intent: Intent?) {
                throw ActivityNotFoundException("Clock app missing")
            }
        }

        val result = SystemAlarmHelper.setSystemTimer(
            context = failingContext,
            seconds = 60,
            message = "Timer"
        )

        assertFalse(result)
        val latestToast = ShadowToast.getTextOfLatestToast()
        assertEquals("Приложение «Часы» не найдено для установки системного таймера", latestToast)
    }

    @Test
    fun setSystemTimer_genericException_returnsFalseAndShowsErrorToast() {
        val failingContext = object : ContextWrapper(context) {
            override fun startActivity(intent: Intent?) {
                throw SecurityException("Permission denied")
            }
        }

        val result = SystemAlarmHelper.setSystemTimer(
            context = failingContext,
            seconds = 60,
            message = "Timer"
        )

        assertFalse(result)
        val latestToast = ShadowToast.getTextOfLatestToast()
        assertEquals("Ошибка запуска системного таймера: Permission denied", latestToast)
    }
}
