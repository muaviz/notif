package com.notif.restyler.model

/**
 * Defines the iconography aesthetic applied to restyled notifications.
 */
enum class IconStyle {
    /** Uses the source application's original full-color icon as the large icon. */
    APP_ICON,

    /** Converts the source application's icon into a grayscale, accent-tinted monochrome icon. */
    MONOCHROME_APP_ICON,

    /** Omits the large icon entirely, showing only the minimalist status-bar glyph. */
    GENERIC_GLYPH;

    companion object {
        fun fromString(value: String?): IconStyle {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: APP_ICON
        }
    }
}
