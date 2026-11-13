package com.notif.restyler.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val CHANNEL_ALERTING = "channel_restyled_alerting"
    const val CHANNEL_SILENT = "channel_restyled_silent"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val alertingChannel = NotificationChannel(
                CHANNEL_ALERTING,
                "Restyled Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Primary channel for restyled notifications with sound and vibration"
                enableVibration(true)
            }

            val silentChannel = NotificationChannel(
                CHANNEL_SILENT,
                "Restyled Notifications (Silent)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent channel for low-priority restyled notifications"
                enableVibration(false)
                setSound(null, null)
            }

            notificationManager.createNotificationChannels(listOf(alertingChannel, silentChannel))
        }
    }
}
