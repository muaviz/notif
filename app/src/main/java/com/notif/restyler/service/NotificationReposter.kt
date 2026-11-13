package com.notif.restyler.service

import android.app.Notification
import android.content.Context
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.notif.restyler.R
import com.notif.restyler.data.SettingsRepository
import com.notif.restyler.model.NotificationDensity
import com.notif.restyler.util.BitmapUtils

/**
 * Handles extraction, transformation, RemoteInput/action preservation, density styling,
 * icon processing, and reposting of notifications.
 */
class NotificationReposter(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    /**
     * Extracts content, actions, and intents from [sbn], formats them with user settings,
     * and reposts through [NotificationCompat].
     */
    suspend fun repostNotification(sbn: StatusBarNotification) {
        val settings = settingsRepository.getLatestSettings()
        val original = sbn.notification ?: return
        val extras = original.extras ?: return

        // 1. Content Extraction
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)
            ?: try {
                val appInfo = context.packageManager.getApplicationInfo(sbn.packageName, 0)
                context.packageManager.getApplicationLabel(appInfo)
            } catch (_: Exception) {
                sbn.packageName
            }

        val text = extras.getCharSequence(Notification.EXTRA_TEXT) ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: text
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)

        // 2. Channel Resolution
        @Suppress("DEPRECATION")
        val channelId = if (original.priority <= Notification.PRIORITY_MIN) {
            NotificationChannels.CHANNEL_SILENT
        } else {
            NotificationChannels.CHANNEL_ALERTING
        }

        // 3. Builder Initialization
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

        // 4. Timestamp
        val timestamp = if (original.`when` > 0) original.`when` else sbn.postTime
        if (timestamp > 0) {
            builder.setWhen(timestamp)
            builder.setShowWhen(true)
        }

        // 5. Density & Layout Styling
        when (settings.density) {
            NotificationDensity.COMFORTABLE -> {
                val style = NotificationCompat.BigTextStyle()
                    .bigText(bigText)
                    .setBigContentTitle(title)
                if (!subText.isNullOrBlank()) {
                    style.setSummaryText(subText)
                }
                builder.setStyle(style)
            }
            NotificationDensity.COMPACT -> {
                // Collapsed footprint: no BigTextStyle expanded body
                if (!subText.isNullOrBlank()) {
                    builder.setSubText(subText)
                }
            }
        }

        // 6. Large Icon Processing (App Icon / Monochrome / Generic Glyph)
        val largeIconBitmap = BitmapUtils.getAppIconBitmap(
            context = context,
            packageName = sbn.packageName,
            iconStyle = settings.iconStyle,
            accentColorInt = settings.accentColorInt
        )
        if (largeIconBitmap != null) {
            builder.setLargeIcon(largeIconBitmap)
        }

        // 7. Action & RemoteInput (Inline Reply) Preservation
        mapActions(original.actions, builder)

        // 8. Deterministic Mapping & Dispatch
        val replacementTag = sbn.packageName
        val replacementId = sbn.key.hashCode()

        try {
            notificationManager.notify(replacementTag, replacementId, builder.build())
        } catch (_: SecurityException) {
            // Handled when POST_NOTIFICATIONS is not yet granted
        }
    }

    /**
     * Preserves original notification actions including RemoteInput for inline replies.
     */
    fun mapActions(
        originalActions: Array<Notification.Action>?,
        builder: NotificationCompat.Builder
    ) {
        if (originalActions.isNullOrEmpty()) return

        for (action in originalActions) {
            val actionTitle = action.title ?: continue
            val actionIntent = action.actionIntent ?: continue

            val compatActionBuilder = NotificationCompat.Action.Builder(
                0, // Use zero to adhere to unified minimalist action style
                actionTitle,
                actionIntent
            )

            // Reconstruct RemoteInputs for inline reply (e.g. WhatsApp, Slack, Telegram)
            action.remoteInputs?.forEach { remoteInput ->
                val compatRemoteInput = RemoteInput.Builder(remoteInput.resultKey)
                    .setLabel(remoteInput.label)
                    .setChoices(remoteInput.choices)
                    .setAllowFreeFormInput(remoteInput.allowFreeFormInput)
                    .addExtras(remoteInput.extras)
                    .build()
                compatActionBuilder.addRemoteInput(compatRemoteInput)
            }

            builder.addAction(compatActionBuilder.build())
        }
    }

    /**
     * Cancels the replacement notification corresponding to the removed source notification.
     */
    fun removeReplacement(sbn: StatusBarNotification) {
        val replacementTag = sbn.packageName
        val replacementId = sbn.key.hashCode()
        notificationManager.cancel(replacementTag, replacementId)
    }
}
