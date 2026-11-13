package com.notif.restyler.ui

import android.app.Application
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.notif.restyler.data.DataStoreSettingsRepository
import com.notif.restyler.data.SettingsRepository
import com.notif.restyler.model.IconStyle
import com.notif.restyler.model.NotificationDensity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class MainViewModelTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var application: Application
    private lateinit var repository: SettingsRepository
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = RuntimeEnvironment.getApplication()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(scope: kotlinx.coroutines.CoroutineScope): MainViewModel {
        val testFile = tmpFolder.newFile("vm_test_settings_${System.nanoTime()}.preferences_pb")
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { testFile }
        )
        repository = DataStoreSettingsRepository(testDataStore)
        return MainViewModel(application, repository)
    }

    @Test
    fun initialState_hasDefaultSettings() = runTest(testDispatcher) {
        viewModel = createViewModel(backgroundScope)
        val state = viewModel.uiState.value
        assertEquals("#6750A4", state.settings.accentColorHex)
        assertEquals(NotificationDensity.COMFORTABLE, state.settings.density)
        assertEquals(IconStyle.APP_ICON, state.settings.iconStyle)
        assertTrue(state.settings.isServiceEnabled)
    }

    @Test
    fun setAccentColor_updatesState() = runTest(testDispatcher) {
        viewModel = createViewModel(backgroundScope)
        viewModel.setAccentColor("#059669")
        val updated = viewModel.uiState.first { it.settings.accentColorHex == "#059669" }
        assertEquals("#059669", updated.settings.accentColorHex)
    }

    @Test
    fun setDensity_updatesState() = runTest(testDispatcher) {
        viewModel = createViewModel(backgroundScope)
        viewModel.setDensity(NotificationDensity.COMPACT)
        val updated = viewModel.uiState.first { it.settings.density == NotificationDensity.COMPACT }
        assertEquals(NotificationDensity.COMPACT, updated.settings.density)
    }

    @Test
    fun setIconStyle_updatesState() = runTest(testDispatcher) {
        viewModel = createViewModel(backgroundScope)
        viewModel.setIconStyle(IconStyle.MONOCHROME_APP_ICON)
        val updated = viewModel.uiState.first { it.settings.iconStyle == IconStyle.MONOCHROME_APP_ICON }
        assertEquals(IconStyle.MONOCHROME_APP_ICON, updated.settings.iconStyle)
    }

    @Test
    fun setServiceEnabled_updatesState() = runTest(testDispatcher) {
        viewModel = createViewModel(backgroundScope)
        viewModel.setServiceEnabled(false)
        val disabled = viewModel.uiState.first { !it.settings.isServiceEnabled }
        assertFalse(disabled.settings.isServiceEnabled)

        viewModel.setServiceEnabled(true)
        val enabled = viewModel.uiState.first { it.settings.isServiceEnabled }
        assertTrue(enabled.settings.isServiceEnabled)
    }

    @Test
    fun setSearchQuery_updatesSearchQuery() = runTest(testDispatcher) {
        viewModel = createViewModel(backgroundScope)
        viewModel.setSearchQuery("whatsapp")
        val updated = viewModel.uiState.first { it.searchQuery == "whatsapp" }
        assertEquals("whatsapp", updated.searchQuery)
    }

    @Test
    fun toggleAppRestyle_updatesDisabledPackages() = runTest(testDispatcher) {
        viewModel = createViewModel(backgroundScope)
        // Disabling restyling adds package to disabledPackages
        viewModel.toggleAppRestyle("com.example.chat", false)
        val disabled = viewModel.uiState.first {
            it.settings.disabledPackages.contains("com.example.chat")
        }
        assertTrue(disabled.settings.disabledPackages.contains("com.example.chat"))

        // Re-enabling restyling removes package from disabledPackages
        viewModel.toggleAppRestyle("com.example.chat", true)
        val reEnabled = viewModel.uiState.first {
            !it.settings.disabledPackages.contains("com.example.chat")
        }
        assertFalse(reEnabled.settings.disabledPackages.contains("com.example.chat"))
    }

    @Test
    fun restyleAllApps_clearsDisabledPackages() = runTest(testDispatcher) {
        viewModel = createViewModel(backgroundScope)
        viewModel.toggleAppRestyle("com.app.one", false)
        viewModel.toggleAppRestyle("com.app.two", false)
        viewModel.uiState.first { it.settings.disabledPackages.size == 2 }

        viewModel.restyleAllApps()
        val allRestyled = viewModel.uiState.first { it.settings.disabledPackages.isEmpty() }
        assertTrue(allRestyled.settings.disabledPackages.isEmpty())
    }
}
