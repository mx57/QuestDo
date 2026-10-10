package com.example

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AlarmAlertActivitySecurityTest {

    @Test
    fun `verify AlarmAlertActivity is not exported`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val componentName = ComponentName(context, AlarmAlertActivity::class.java)
        val activityInfo = context.packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA)

        assertFalse("AlarmAlertActivity should not be exported for security reasons", activityInfo.exported)
    }
}
