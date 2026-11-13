package com.notif.restyler.util

import android.content.Intent
import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class PermissionUtilsTest {

    private val testPackage = "com.notif.restyler"

    @Test
    fun createNotificationAccessIntent_targetsNotificationListenerSettings() {
        val intent = PermissionUtils.createNotificationAccessIntent()
        assertEquals(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS, intent.action)
        assertTrue((intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }

    @Test
    fun createBatteryOptimizationIntent_targetsPackageSpecificRequest() {
        val intent = PermissionUtils.createBatteryOptimizationIntent(testPackage)
        assertEquals(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, intent.action)
        assertEquals("package:$testPackage", intent.dataString)
        assertTrue((intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }

    @Test
    fun createGeneralBatteryOptimizationIntent_targetsGeneralSettings() {
        val intent = PermissionUtils.createGeneralBatteryOptimizationIntent()
        assertEquals(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, intent.action)
        assertTrue((intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }

    @Test
    fun createAppDetailsIntent_targetsAppDetailsPage() {
        val intent = PermissionUtils.createAppDetailsIntent(testPackage)
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals("package:$testPackage", intent.dataString)
        assertTrue((intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }

    @Test
    fun isNotificationAccessGranted_returnsFalseWhenNotGranted() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        val isGranted = PermissionUtils.isNotificationAccessGranted(context)
        // By default in fresh test environment, listener is not enabled
        org.junit.Assert.assertFalse(isGranted)
    }
}
