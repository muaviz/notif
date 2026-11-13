package com.notif.restyler.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RestylerSettingsTest {

    @Test
    fun defaultSettings_hasCorrectDefaults() {
        val settings = RestylerSettings()
        assertEquals("#6750A4", settings.accentColorHex)
        assertEquals(NotificationDensity.COMFORTABLE, settings.density)
        assertEquals(IconStyle.APP_ICON, settings.iconStyle)
        assertEquals(emptySet<String>(), settings.disabledPackages)
        assertEquals(true, settings.isServiceEnabled)
    }

    @Test
    fun parseHexColor_handlesVariousFormats() {
        // Standard 6-digit hex with hash
        assertEquals(0xFF123456.toInt(), RestylerSettings.parseHexColor("#123456"))

        // Standard 6-digit hex without hash
        assertEquals(0xFF123456.toInt(), RestylerSettings.parseHexColor("123456"))

        // 8-digit ARGB hex with hash
        assertEquals(0x80123456.toInt(), RestylerSettings.parseHexColor("#80123456"))

        // Fallback on invalid strings
        val fallback = 0xFF6750A4.toInt()
        assertEquals(fallback, RestylerSettings.parseHexColor("invalid"))
        assertEquals(fallback, RestylerSettings.parseHexColor("#12"))
        assertEquals(fallback, RestylerSettings.parseHexColor(""))
    }

    @Test
    fun notificationDensity_fromString_handlesCaseAndFallback() {
        assertEquals(NotificationDensity.COMPACT, NotificationDensity.fromString("compact"))
        assertEquals(NotificationDensity.COMPACT, NotificationDensity.fromString("COMPACT"))
        assertEquals(NotificationDensity.COMFORTABLE, NotificationDensity.fromString("comfortable"))
        assertEquals(NotificationDensity.COMFORTABLE, NotificationDensity.fromString("unknown_value"))
        assertEquals(NotificationDensity.COMFORTABLE, NotificationDensity.fromString(null))
    }

    @Test
    fun iconStyle_fromString_handlesCaseAndFallback() {
        assertEquals(IconStyle.APP_ICON, IconStyle.fromString("app_icon"))
        assertEquals(IconStyle.MONOCHROME_APP_ICON, IconStyle.fromString("monochrome_app_icon"))
        assertEquals(IconStyle.GENERIC_GLYPH, IconStyle.fromString("generic_glyph"))
        assertEquals(IconStyle.APP_ICON, IconStyle.fromString("non_existent"))
        assertEquals(IconStyle.APP_ICON, IconStyle.fromString(null))
    }
}
