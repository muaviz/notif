package com.notif.restyler

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.notif.restyler.data.DataStoreSettingsRepository
import com.notif.restyler.data.SettingsRepository

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "restyler_settings")

class RestylerApplication : Application() {

    lateinit var settingsRepository: SettingsRepository
        private set

    companion object {
        lateinit var instance: RestylerApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        settingsRepository = DataStoreSettingsRepository(dataStore)
    }
}
