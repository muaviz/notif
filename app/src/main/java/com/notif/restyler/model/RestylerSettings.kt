package com.notif.restyler.model

/**
 * Immutable data representation of all user-configurable styling and restyler preferences.
 */
data class RestylerSettings(
    val accentColorHex: String = DEFAULT_ACCENT_COLOR,
    val density: NotificationDensity = NotificationDensity.COMFORTABLE,
    val iconStyle: IconStyle = IconStyle.APP_ICON,
    val disabledPackages: Set<String> = emptySet(),
    val isServiceEnabled: Boolean = true
) {
    /**
     * Resolves the accent hex string into a 32-bit ARGB color integer.
     */
    val accentColorInt: Int
        get() = parseHexColor(accentColorHex, DEFAULT_COLOR_INT)

    companion object {
        const val DEFAULT_ACCENT_COLOR = "#6750A4"
        const val DEFAULT_COLOR_INT = 0xFF6750A4.toInt()

        /**
         * Parses a hex color string (#RRGGBB or #AARRGGBB) into a 32-bit ARGB integer.
         * Pure Kotlin implementation to allow running in standard JVM unit tests.
         */
        fun parseHexColor(hex: String, fallback: Int = DEFAULT_COLOR_INT): Int {
            return try {
                val cleanHex = hex.trim().removePrefix("#")
                when (cleanHex.length) {
                    6 -> (0xFF000000.toInt()) or cleanHex.toLong(16).toInt()
                    8 -> cleanHex.toLong(16).toInt()
                    else -> fallback
                }
            } catch (_: Exception) {
                fallback
            }
        }
    }
}
