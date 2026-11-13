package com.notif.restyler.service

import android.annotation.SuppressLint
import android.app.Notification
import android.service.notification.StatusBarNotification
import com.notif.restyler.model.RestylerSettings

/**
 * Filter rules engine for deciding whether an intercepted notification should be restyled or passed through untouched.
 */
object NotificationFilter {

    val SYSTEM_PACKAGE_BLACKLIST: Set<String> = setOf(
        "android",
        "com.android.systemui",
        "com.google.android.gms",
        "com.android.vending"
    )

    @SuppressLint("InlinedApi")
    val SKIPPED_CATEGORIES: Set<String> = setOf(
        Notification.CATEGORY_CALL,
        Notification.CATEGORY_NAVIGATION,
        Notification.CATEGORY_TRANSPORT,
        Notification.CATEGORY_PROGRESS,
        Notification.CATEGORY_ALARM,
        Notification.CATEGORY_SERVICE
    )

    /**
     * Determines whether an intercepted notification should be skipped based on package, flags, category, and user settings.
     */
    fun shouldSkip(
        packageName: String,
        isOngoing: Boolean,
        flags: Int,
        category: String?,
        ownPackageName: String,
        settings: RestylerSettings
    ): Boolean {
        // 1. Never intercept our own notifications (strict loop prevention)
        if (packageName == ownPackageName) return true

        // 2. Master toggle check
        if (!settings.isServiceEnabled) return true

        // 3. Ongoing notifications (foreground services, active media players, timers)
        if (isOngoing || (flags and Notification.FLAG_ONGOING_EVENT != 0)) return true

        // 4. Group summary notifications (prevent duplicating system summaries)
        if ((flags and Notification.FLAG_GROUP_SUMMARY != 0)) return true

        // 5. System critical categories (active phone calls, turn-by-turn navigation, downloads)
        if (category != null && SKIPPED_CATEGORIES.contains(category)) return true

        // 6. Core platform packages blacklist
        if (SYSTEM_PACKAGE_BLACKLIST.contains(packageName)) return true

        // 7. User per-app exclusion toggle
        if (settings.disabledPackages.contains(packageName)) return true

        return false
    }

    /**
     * Overload for StatusBarNotification.
     */
    fun shouldSkip(
        sbn: StatusBarNotification,
        ownPackageName: String,
        settings: RestylerSettings
    ): Boolean {
        val notification = sbn.notification ?: return true
        return shouldSkip(
            packageName = sbn.packageName,
            isOngoing = sbn.isOngoing,
            flags = notification.flags,
            category = notification.category,
            ownPackageName = ownPackageName,
            settings = settings
        )
    }
}
