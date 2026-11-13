package com.notif.restyler.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global reactive connection state holder for the NotificationListenerService.
 * Since NotificationListenerService lifecycle is managed directly by the Android OS System Server,
 * this holder bridges service connectivity status to the UI layer.
 */
object ServiceStateHolder {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }
}
