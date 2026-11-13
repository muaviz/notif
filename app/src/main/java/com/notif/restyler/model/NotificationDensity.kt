package com.notif.restyler.model

/**
 * Defines vertical density and expansion level of restyled notifications.
 */
enum class NotificationDensity {
    /** Single line or collapsed summary with minimum vertical footprint. */
    COMPACT,

    /** Expanded BigTextStyle enabled with generous spacing and full text preview. */
    COMFORTABLE;

    companion object {
        fun fromString(value: String?): NotificationDensity {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: COMFORTABLE
        }
    }
}
