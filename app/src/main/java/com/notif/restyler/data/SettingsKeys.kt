package com.notif.restyler.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object SettingsKeys {
    val KEY_ACCENT_COLOR = stringPreferencesKey("accent_color_hex")
    val KEY_DENSITY = stringPreferencesKey("density")
    val KEY_ICON_STYLE = stringPreferencesKey("icon_style")
    val KEY_DISABLED_PACKAGES = stringSetPreferencesKey("disabled_packages")
    val KEY_SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
}
