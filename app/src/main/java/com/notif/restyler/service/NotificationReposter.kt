package com.notif.restyler.service

import android.app.Notification
import android.content.Context
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.notif.restyler.R
import com.notif.restyler.data.SettingsRepository

/**
 * Handles the extraction, restyling, deterministic key-to-id mapping, and reposting
 * of intercepted notifications.
 */
class NotificationReposter(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    suspend fun repostNotification(sbn: StatusBarNotification) {
        val settings = settingsRepository.getLatestSettings()
        val original = sbn.notification ?: return
        val extras = original.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)
            ?: try {
                val appInfo = context.packageManager.getApplicationInfo(sbn.packageName, 0)
                context.packageManager.getApplicationLabel(appInfo)
            } catch (_: Exception) {
                sbn.packageName
            }
        val text = extras.getCharSequence(Notification.EXTRA_TEXT) ?: ""

        val replacementTag = sbn.packageName
        val replacementId = sbn.key.hashCode()

        @Suppress("DEPRECATION")
        val channelId = if (original.priority <= Notification.PRIORITY_MIN) {
            NotificationChannels.CHANNEL_SILENT
        } else {
            NotificationChannels.CHANNEL_ALERTING
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_glyph)
            .setContentTitle(title)
            .setContentText(text)
            .setColor(settings.accentColorInt)
            .setColorized(false)
            .setGroup(sbn.packageName)
            .setContentIntent(original.contentIntent)
            .setDeleteIntent(original.deleteIntent)
            .setAutoCancel(true)

        if (original.`when` > 0) {
            builder.setWhen(original.`when`)
        }

        try {
            notificationManager.notify(replacementTag, replacementId, builder.build())
        } catch (_: SecurityException) {
            // Handled on Android 13+ when POST_NOTIFICATIONS is not granted
        }
    }

    fun removeReplacement(sbn: StatusBarNotification) {
        val replacementTag = sbn.packageName
        val replacementId = sbn.key.hashCode()
        notificationManager.cancel(replacementTag, replacementId)
    }
}
