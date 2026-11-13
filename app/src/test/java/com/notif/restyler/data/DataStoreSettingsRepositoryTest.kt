package com.notif.restyler.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.notif.restyler.model.IconStyle
import com.notif.restyler.model.NotificationDensity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreSettingsRepositoryTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var repository: SettingsRepository

    @Before
    fun setup() {
        val testFile = tmpFolder.newFile("test_settings.preferences_pb")
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        repository = DataStoreSettingsRepository(testDataStore)
    }

    @Test
    fun getLatestSettings_returnsDefaultsInitially() = runTest(testDispatcher) {
        val settings = repository.getLatestSettings()
        assertEquals("#6750A4", settings.accentColorHex)
        assertEquals(NotificationDensity.COMFORTABLE, settings.density)
        assertEquals(IconStyle.APP_ICON, settings.iconStyle)
        assertTrue(settings.disabledPackages.isEmpty())
        assertTrue(settings.isServiceEnabled)
    }

    @Test
    fun setAccentColor_updatesColor() = runTest(testDispatcher) {
        repository.setAccentColor("#00FF00")
        val updated = repository.getLatestSettings()
        assertEquals("#00FF00", updated.accentColorHex)
    }

    @Test
    fun setDensity_updatesDensity() = runTest(testDispatcher) {
        repository.setDensity(NotificationDensity.COMPACT)
        val updated = repository.getLatestSettings()
        assertEquals(NotificationDensity.COMPACT, updated.density)
    }

    @Test
    fun setIconStyle_updatesIconStyle() = runTest(testDispatcher) {
        repository.setIconStyle(IconStyle.MONOCHROME_APP_ICON)
        val updated = repository.getLatestSettings()
        assertEquals(IconStyle.MONOCHROME_APP_ICON, updated.iconStyle)
    }

    @Test
    fun toggleAppRestyle_addsAndRemovesFromDisabledSet() = runTest(testDispatcher) {
        val pkg = "com.example.chat"

        // Disable app (restyle = false)
        repository.toggleAppRestyle(pkg, restyle = false)
        var settings = repository.getLatestSettings()
        assertTrue(settings.disabledPackages.contains(pkg))

        // Re-enable app (restyle = true)
        repository.toggleAppRestyle(pkg, restyle = true)
        settings = repository.getLatestSettings()
        assertFalse(settings.disabledPackages.contains(pkg))
    }

    @Test
    fun setServiceEnabled_updatesState() = runTest(testDispatcher) {
        repository.setServiceEnabled(false)
        var settings = repository.getLatestSettings()
        assertFalse(settings.isServiceEnabled)

        repository.setServiceEnabled(true)
        settings = repository.getLatestSettings()
        assertTrue(settings.isServiceEnabled)
    }

    @Test
    fun resetToDefaults_clearsCustomSettings() = runTest(testDispatcher) {
        repository.setAccentColor("#123456")
        repository.setDensity(NotificationDensity.COMPACT)
        repository.setServiceEnabled(false)

        repository.resetToDefaults()

        val settings = repository.getLatestSettings()
        assertEquals("#6750A4", settings.accentColorHex)
        assertEquals(NotificationDensity.COMFORTABLE, settings.density)
        assertTrue(settings.isServiceEnabled)
    }
}
