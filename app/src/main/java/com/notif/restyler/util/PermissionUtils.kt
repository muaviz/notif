package com.notif.restyler.util

import android.annotation.SuppressLint
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.notif.restyler.service.RestylerNotificationListenerService

object PermissionUtils {

    /**
     * Checks if notification listener access is granted for this app in system settings.
     */
    fun isNotificationAccessGranted(context: Context): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledPackages.contains(context.packageName)
    }

    /**
     * Checks if this app is exempted from battery optimizations (Doze mode background kills).
     */
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return try {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Checks if POST_NOTIFICATIONS runtime permission is granted (required on Android 13+ / API 33+).
     */
    fun isPostNotificationsGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Requests the Android Notification Manager Service to rebind to our listener service.
     */
    fun requestRebind(context: Context) {
        val component = ComponentName(context, RestylerNotificationListenerService::class.java)
        try {
            NotificationListenerService.requestRebind(component)
        } catch (_: Exception) {
            // Fallback to force component refresh if requestRebind fails or throws
            forceRebind(context)
        }
    }

    /**
     * Forces the system notification manager to reconnect the listener service by toggling
     * the component enabled state. Proven technique for recovering from aggressive OEM background kills.
     */
    fun forceRebind(context: Context) {
        val component = ComponentName(context, RestylerNotificationListenerService::class.java)
        val pm = context.packageManager
        try {
            pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (_: Exception) {
            // Ignore security or package manager exceptions
        }
    }

    /**
     * Creates an Intent to navigate directly to the Notification Access system settings page.
     */
    fun createNotificationAccessIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Creates an Intent to request battery optimization exemption for this app.
     */
    @SuppressLint("BatteryLife")
    fun createBatteryOptimizationIntent(packageName: String): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Fallback intent to the general battery optimization settings list.
     */
    fun createGeneralBatteryOptimizationIntent(): Intent {
        return Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Creates an Intent to navigate to App Info settings (required for Android 13+ "Allow restricted settings").
     */
    fun createAppDetailsIntent(packageName: String): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Launches the battery optimization exemption request with safe fallback.
     */
    fun launchBatteryOptimizationRequest(context: Context) {
        try {
            context.startActivity(createBatteryOptimizationIntent(context.packageName))
        } catch (_: ActivityNotFoundException) {
            try {
                context.startActivity(createGeneralBatteryOptimizationIntent())
            } catch (_: Exception) {
                // Ignore if device has no battery settings activity
            }
        }
    }
}
