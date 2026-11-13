package com.notif.restyler.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.notif.restyler.RestylerApplication
import com.notif.restyler.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Collections

class RestylerNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var reposter: NotificationReposter
    private lateinit var settingsRepo: SettingsRepository

    /**
     * Set of notification keys that were explicitly cancelled by this listener during interception.
     * Prevents onNotificationRemoved from immediately cancelling our own restyled replacement.
     */
    private val interceptedKeys: MutableSet<String> = Collections.synchronizedSet(mutableSetOf<String>())

    override fun onCreate() {
        super.onCreate()
        settingsRepo = (application as RestylerApplication).settingsRepository
        reposter = NotificationReposter(this, settingsRepo)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        ServiceStateHolder.setConnected(true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        ServiceStateHolder.setConnected(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        serviceScope.launch {
            val settings = settingsRepo.getLatestSettings()
            if (NotificationFilter.shouldSkip(sbn, packageName, settings)) {
                return@launch
            }

            // Track this key so the resulting onNotificationRemoved callback doesn't dismiss our replacement
            interceptedKeys.add(sbn.key)

            // 1. Cancel the original notification to prevent duplicate shade entries
            try {
                cancelNotification(sbn.key)
            } catch (_: Exception) {
                // If cancel fails, remove from interceptedKeys so we don't leak state
                interceptedKeys.remove(sbn.key)
            }

            // 2. Delegate restyling and reposting
            reposter.repostNotification(sbn)
        }
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification?,
        rankingMap: RankingMap?,
        reason: Int
    ) {
        super.onNotificationRemoved(sbn, rankingMap, reason)
        if (sbn == null || sbn.packageName == packageName) return

        // If this removal was caused by our own cancellation during interception, consume and ignore
        if (reason == REASON_LISTENER_CANCEL || interceptedKeys.remove(sbn.key)) {
            interceptedKeys.remove(sbn.key)
            return
        }

        // If the source app dismissed it (e.g., read on another client or timed out), dismiss our replacement
        serviceScope.launch {
            reposter.removeReplacement(sbn)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Handled by the rankingMap + reason overload above on API 26+
    }

    override fun onDestroy() {
        ServiceStateHolder.setConnected(false)
        serviceScope.cancel()
        interceptedKeys.clear()
        super.onDestroy()
    }
}
