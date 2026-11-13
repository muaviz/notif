package com.notif.restyler.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.notif.restyler.RestylerApplication
import com.notif.restyler.data.SettingsRepository
import com.notif.restyler.model.IconStyle
import com.notif.restyler.model.NotificationDensity
import com.notif.restyler.service.ServiceStateHolder
import com.notif.restyler.util.PermissionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    val uiState: StateFlow<MainUiState> = combine(
        ServiceStateHolder.isConnected,
        settingsRepository.settingsFlow,
        _permissionState
    ) { isConnected, settings, (isNotifAccess, isBatteryIgnored, isPostNotifGranted) ->
        MainUiState(
            isListenerConnected = isConnected,
            isNotificationAccessGranted = isNotifAccess,
            isBatteryOptimizationIgnored = isBatteryIgnored,
            isPostNotificationsGranted = isPostNotifGranted,
            settings = settings
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
