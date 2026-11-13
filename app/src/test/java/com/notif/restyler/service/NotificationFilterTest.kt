package com.notif.restyler.service

import android.app.Notification
import com.notif.restyler.model.RestylerSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationFilterTest {

    private val ownPackage = "com.notif.restyler"
    private val defaultSettings = RestylerSettings()

    @Test
    fun shouldSkip_returnsTrue_forOwnPackage() {
        val result = NotificationFilter.shouldSkip(
            packageName = ownPackage,
            isOngoing = false,
            flags = 0,
            category = null,
            ownPackageName = ownPackage,
            settings = defaultSettings
        )
        assertTrue("Own package must be skipped to avoid infinite loops", result)
    }

    @Test
    fun shouldSkip_returnsTrue_whenServiceDisabled() {
        val disabledSettings = defaultSettings.copy(isServiceEnabled = false)
        val result = NotificationFilter.shouldSkip(
            packageName = "org.telegram.messenger",
            isOngoing = false,
            flags = 0,
            category = null,
            ownPackageName = ownPackage,
            settings = disabledSettings
        )
        assertTrue("All notifications must be skipped when master switch is disabled", result)
    }

    @Test
    fun shouldSkip_returnsTrue_forOngoingNotifications() {
        // Flag-based ongoing
        val resultFlag = NotificationFilter.shouldSkip(
            packageName = "com.spotify.music",
            isOngoing = false,
            flags = Notification.FLAG_ONGOING_EVENT,
            category = null,
            ownPackageName = ownPackage,
            settings = defaultSettings
        )
        assertTrue("Ongoing flagged notifications must be skipped", resultFlag)

        // Boolean isOngoing
        val resultBool = NotificationFilter.shouldSkip(
            packageName = "com.spotify.music",
            isOngoing = true,
            flags = 0,
            category = null,
            ownPackageName = ownPackage,
            settings = defaultSettings
        )
        assertTrue("isOngoing = true notifications must be skipped", resultBool)
    }

    @Test
    fun shouldSkip_returnsTrue_forGroupSummary() {
        val result = NotificationFilter.shouldSkip(
            packageName = "com.whatsapp",
            isOngoing = false,
            flags = Notification.FLAG_GROUP_SUMMARY,
            category = null,
            ownPackageName = ownPackage,
            settings = defaultSettings
        )
        assertTrue("Group summaries must be skipped", result)
    }

    @Test
    fun shouldSkip_returnsTrue_forSkippedCategories() {
        val skipped = listOf(
            Notification.CATEGORY_CALL,
            Notification.CATEGORY_NAVIGATION,
            Notification.CATEGORY_TRANSPORT,
            Notification.CATEGORY_PROGRESS,
            Notification.CATEGORY_ALARM,
            Notification.CATEGORY_SERVICE
        )

        for (category in skipped) {
            val result = NotificationFilter.shouldSkip(
                packageName = "com.example.app",
                isOngoing = false,
                flags = 0,
                category = category,
                ownPackageName = ownPackage,
                settings = defaultSettings
            )
            assertTrue("Category '$category' must be skipped", result)
        }
    }

    @Test
    fun shouldSkip_returnsTrue_forSystemBlacklist() {
        val systemPackages = listOf(
            "android",
            "com.android.systemui",
            "com.google.android.gms",
            "com.android.vending"
        )

        for (pkg in systemPackages) {
            val result = NotificationFilter.shouldSkip(
                packageName = pkg,
                isOngoing = false,
                flags = 0,
                category = null,
                ownPackageName = ownPackage,
                settings = defaultSettings
            )
            assertTrue("System package '$pkg' must be skipped", result)
        }
    }

    @Test
    fun shouldSkip_returnsTrue_forUserDisabledPackage() {
        val targetPkg = "com.twitter.android"
        val settings = defaultSettings.copy(disabledPackages = setOf(targetPkg))

        val result = NotificationFilter.shouldSkip(
            packageName = targetPkg,
            isOngoing = false,
            flags = 0,
            category = null,
            ownPackageName = ownPackage,
            settings = settings
        )
        assertTrue("User disabled package must be skipped", result)
    }

    @Test
    fun shouldSkip_returnsFalse_forStandardAppNotification() {
        val result = NotificationFilter.shouldSkip(
            packageName = "org.telegram.messenger",
            isOngoing = false,
            flags = 0,
            category = Notification.CATEGORY_MESSAGE,
            ownPackageName = ownPackage,
            settings = defaultSettings
        )
        assertFalse("Standard messaging notification should NOT be skipped", result)
    }
}
