package com.notif.restyler.ui

import com.notif.restyler.model.AppInfo
import com.notif.restyler.model.RestylerSettings

data class MainUiState(
    val isListenerConnected: Boolean = false,
    val isNotificationAccessGranted: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false,
    val isPostNotificationsGranted: Boolean = false,
    val settings: RestylerSettings = RestylerSettings(),
    val installedApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val isLoadingApps: Boolean = false
)
