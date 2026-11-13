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

class RestylerNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var reposter: NotificationReposter
    private lateinit var settingsRepo: SettingsRepository

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

            // 1. Immediately cancel the original notification to prevent duplicate shade entries
            try {
                cancelNotification(sbn.key)
            } catch (_: Exception) {
                // Ignore cancel failure if already dismissed or restricted
            }

            // 2. Delegate restyling and reposting
            reposter.repostNotification(sbn)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null || sbn.packageName == packageName) return

        serviceScope.launch {
            reposter.removeReplacement(sbn)
        }
    }

    override fun onDestroy() {
        ServiceStateHolder.setConnected(false)
        serviceScope.cancel()
        super.onDestroy()
    }
}
