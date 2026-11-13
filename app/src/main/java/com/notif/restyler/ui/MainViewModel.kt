package com.notif.restyler.ui

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.notif.restyler.RestylerApplication
import com.notif.restyler.data.SettingsRepository
import com.notif.restyler.model.AppInfo
import com.notif.restyler.model.IconStyle
import com.notif.restyler.model.NotificationDensity
import com.notif.restyler.service.ServiceStateHolder
import com.notif.restyler.util.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class AppsState(
    val apps: List<AppInfo> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false
)

class MainViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository =
        (application as RestylerApplication).settingsRepository
) : AndroidViewModel(application) {

    private val _permissionState = MutableStateFlow(
        Triple(
            PermissionUtils.isNotificationAccessGranted(application),
            PermissionUtils.isBatteryOptimizationIgnored(application),
            PermissionUtils.isPostNotificationsGranted(application)
        )
    )

    private val _searchQuery = MutableStateFlow("")
    private val _isLoadingApps = MutableStateFlow(false)
    private val _rawInstalledApps = MutableStateFlow<List<AppInfo>>(emptyList())

    private val appsStateFlow = combine(
        _rawInstalledApps,
        _searchQuery,
        _isLoadingApps
    ) { apps, query, isLoading ->
        AppsState(apps, query, isLoading)
    }

    val uiState: StateFlow<MainUiState> = combine(
        ServiceStateHolder.isConnected,
        settingsRepository.settingsFlow,
        _permissionState,
        appsStateFlow
    ) { isConnected, settings, (isNotifAccess, isBatteryIgnored, isPostNotifGranted), appsState ->
        val mappedApps = appsState.apps.map { app ->
            app.copy(isRestyled = !settings.disabledPackages.contains(app.packageName))
        }

        val filteredApps = if (appsState.query.isBlank()) {
            mappedApps
        } else {
            mappedApps.filter {
                it.appName.contains(appsState.query, ignoreCase = true) ||
                it.packageName.contains(appsState.query, ignoreCase = true)
            }
        }

        MainUiState(
            isListenerConnected = isConnected,
            isNotificationAccessGranted = isNotifAccess,
            isBatteryOptimizationIgnored = isBatteryIgnored,
            isPostNotificationsGranted = isPostNotifGranted,
            settings = settings,
            installedApps = filteredApps,
            searchQuery = appsState.query,
            isLoadingApps = appsState.isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState(
            isNotificationAccessGranted = PermissionUtils.isNotificationAccessGranted(application),
            isBatteryOptimizationIgnored = PermissionUtils.isBatteryOptimizationIgnored(application),
            isPostNotificationsGranted = PermissionUtils.isPostNotificationsGranted(application)
        )
    )

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingApps.value = true
            val app = getApplication<Application>()
            val pm = app.packageManager
            try {
                val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                val userApps = packages
                    .filter {
                        pm.getLaunchIntentForPackage(it.packageName) != null &&
                        it.packageName != app.packageName
                    }
                    .map { pkg ->
                        AppInfo(
                            packageName = pkg.packageName,
                            appName = pm.getApplicationLabel(pkg).toString(),
                            icon = try { pm.getApplicationIcon(pkg) } catch (_: Exception) { null },
                            isRestyled = true
                        )
                    }
                    .sortedBy { it.appName.lowercase() }
                _rawInstalledApps.value = userApps
            } catch (_: Exception) {
                _rawInstalledApps.value = emptyList()
            } finally {
                _isLoadingApps.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun restyleAllApps() {
        viewModelScope.launch {
            settingsRepository.setDisabledPackages(emptySet())
        }
    }

    fun disableAllApps() {
        viewModelScope.launch {
            val allPkgs = _rawInstalledApps.value.map { it.packageName }.toSet()
            settingsRepository.setDisabledPackages(allPkgs)
        }
    }

    fun refreshSystemState() {
        val app = getApplication<Application>()
        _permissionState.value = Triple(
            PermissionUtils.isNotificationAccessGranted(app),
            PermissionUtils.isBatteryOptimizationIgnored(app),
            PermissionUtils.isPostNotificationsGranted(app)
        )
    }

    fun setAccentColor(hex: String) {
        viewModelScope.launch {
            settingsRepository.setAccentColor(hex)
        }
    }

    fun setDensity(density: NotificationDensity) {
        viewModelScope.launch {
            settingsRepository.setDensity(density)
        }
    }

    fun setIconStyle(iconStyle: IconStyle) {
        viewModelScope.launch {
            settingsRepository.setIconStyle(iconStyle)
        }
    }

    fun setServiceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setServiceEnabled(enabled)
        }
    }

    fun toggleAppRestyle(packageName: String, restyle: Boolean) {
        viewModelScope.launch {
            settingsRepository.toggleAppRestyle(packageName, restyle)
        }
    }

    fun reconnectService() {
        val app = getApplication<Application>()
        PermissionUtils.requestRebind(app)
        refreshSystemState()
    }

    fun launchBatteryOptimization() {
        val app = getApplication<Application>()
        PermissionUtils.launchBatteryOptimizationRequest(app)
    }
}
