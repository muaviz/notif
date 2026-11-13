package com.notif.restyler.model

import android.graphics.drawable.Drawable

/**
 * Metadata for an installed application to support per-app restyling configuration.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isRestyled: Boolean
)
