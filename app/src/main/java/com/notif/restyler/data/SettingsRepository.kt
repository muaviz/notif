package com.notif.restyler.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.notif.restyler.model.IconStyle
import com.notif.restyler.model.NotificationDensity
import com.notif.restyler.model.RestylerSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

interface SettingsRepository {
    val settingsFlow: Flow<RestylerSettings>
    suspend fun getLatestSettings(): RestylerSettings
    suspend fun setAccentColor(hex: String)
    suspend fun setDensity(density: NotificationDensity)
    suspend fun setIconStyle(iconStyle: IconStyle)
    suspend fun toggleAppRestyle(packageName: String, restyle: Boolean)
    suspend fun setDisabledPackages(packages: Set<String>)
    suspend fun setServiceEnabled(enabled: Boolean)
    suspend fun resetToDefaults()
}

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val settingsFlow: Flow<RestylerSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            RestylerSettings(
                accentColorHex = prefs[SettingsKeys.KEY_ACCENT_COLOR]
                    ?: RestylerSettings.DEFAULT_ACCENT_COLOR,
                density = NotificationDensity.fromString(prefs[SettingsKeys.KEY_DENSITY]),
                iconStyle = IconStyle.fromString(prefs[SettingsKeys.KEY_ICON_STYLE]),
                disabledPackages = prefs[SettingsKeys.KEY_DISABLED_PACKAGES] ?: emptySet(),
                isServiceEnabled = prefs[SettingsKeys.KEY_SERVICE_ENABLED] ?: true
            )
        }

    override suspend fun getLatestSettings(): RestylerSettings = settingsFlow.first()

    override suspend fun setAccentColor(hex: String) {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.KEY_ACCENT_COLOR] = hex
        }
    }

    override suspend fun setDensity(density: NotificationDensity) {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.KEY_DENSITY] = density.name
        }
    }

    override suspend fun setIconStyle(iconStyle: IconStyle) {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.KEY_ICON_STYLE] = iconStyle.name
        }
    }

    override suspend fun toggleAppRestyle(packageName: String, restyle: Boolean) {
        dataStore.edit { prefs ->
            val currentDisabled = prefs[SettingsKeys.KEY_DISABLED_PACKAGES]?.toMutableSet()
                ?: mutableSetOf()
            if (restyle) {
                currentDisabled.remove(packageName)
            } else {
                currentDisabled.add(packageName)
            }
            prefs[SettingsKeys.KEY_DISABLED_PACKAGES] = currentDisabled
        }
    }

    override suspend fun setDisabledPackages(packages: Set<String>) {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.KEY_DISABLED_PACKAGES] = packages
        }
    }

    override suspend fun setServiceEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.KEY_SERVICE_ENABLED] = enabled
        }
    }

    override suspend fun resetToDefaults() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
