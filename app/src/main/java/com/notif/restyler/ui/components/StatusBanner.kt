package com.notif.restyler.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatusBanner(
    isListenerConnected: Boolean,
    isNotificationAccessGranted: Boolean,
    isBatteryOptimizationIgnored: Boolean,
    onEnableAccessClick: () -> Unit,
    onReconnectClick: () -> Unit,
    onBatteryOptimizeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFullyActive = isNotificationAccessGranted && isListenerConnected

    Column(modifier = modifier.fillMaxWidth()) {
        // Active Pill / Status Header
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isFullyActive) {
                Color(0xFFE8F5E9)
            } else {
                Color(0xFFFFF3E0)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = if (isFullyActive) Color(0xFF2E7D32) else Color(0xFFE65100),
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isFullyActive) "Restyler Active" else "Action Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isFullyActive) Color(0xFF1B5E20) else Color(0xFFBF360C)
                    )
                    Text(
                        text = when {
                            !isNotificationAccessGranted -> "Notification access permission not granted"
                            !isListenerConnected -> "Listener disconnected by OEM background management"
                            else -> "Intercepting & restyling incoming notifications"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFullyActive) Color(0xFF2E7D32) else Color(0xFFD84315)
                    )
                }
            }
        }

        // Action Banner when setup or reconnect is needed
        AnimatedVisibility(visible = !isFullyActive || !isBatteryOptimizationIgnored) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                if (!isNotificationAccessGranted) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Permission Missing",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Grant Notification Access in Android Settings so the app can intercept and restyle incoming notifications.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onEnableAccessClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Enable Notification Access")
                            }
                        }
                    }
                } else if (!isListenerConnected) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Listener Disconnected",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "The OS system server disconnected the listener. Tap Reconnect to rebind the service.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onReconnectClick) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reconnect Service")
                            }
                        }
                    }
                }

                if (!isBatteryOptimizationIgnored) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onBatteryOptimizeClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.BatteryAlert, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Disable Battery Optimization (Prevent Kills)")
                    }
                }
            }
        }
    }
}
