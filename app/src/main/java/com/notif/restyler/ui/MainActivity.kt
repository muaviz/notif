package com.notif.restyler.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.notif.restyler.ui.components.LivePreviewCard
import com.notif.restyler.ui.components.StatusBanner
import com.notif.restyler.ui.components.StylingControls
import com.notif.restyler.ui.theme.NotificationRestylerTheme
import com.notif.restyler.util.PermissionUtils

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NotificationRestylerTheme {
                val uiState by viewModel.uiState.collectAsState()
                val lifecycleOwner = LocalLifecycleOwner.current

                // Refresh permission and service state when returning from system settings
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            viewModel.refreshSystemState()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                // Request POST_NOTIFICATIONS on Android 13+ (API 33+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val postNotificationLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = {
                            viewModel.refreshSystemState()
                        }
                    )
                    LaunchedEffect(Unit) {
                        if (!uiState.isPostNotificationsGranted) {
                            postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Notification Restyler",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            actions = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    Text(
                                        text = if (uiState.settings.isServiceEnabled) "Enabled" else "Paused",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Switch(
                                        checked = uiState.settings.isServiceEnabled,
                                        onCheckedChange = { viewModel.setServiceEnabled(it) }
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            // 1. Connection & Permission Status
                            StatusBanner(
                                isListenerConnected = uiState.isListenerConnected,
                                isNotificationAccessGranted = uiState.isNotificationAccessGranted,
                                isBatteryOptimizationIgnored = uiState.isBatteryOptimizationIgnored,
                                onEnableAccessClick = {
                                    startActivity(PermissionUtils.createNotificationAccessIntent())
                                },
                                onReconnectClick = {
                                    viewModel.reconnectService()
                                },
                                onBatteryOptimizeClick = {
                                    viewModel.launchBatteryOptimization()
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // 2. Dynamic Live Preview Card
                            LivePreviewCard(
                                settings = uiState.settings
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // 3. Customization & Styling Controls
                            StylingControls(
                                settings = uiState.settings,
                                onAccentColorChange = { viewModel.setAccentColor(it) },
                                onDensityChange = { viewModel.setDensity(it) },
                                onIconStyleChange = { viewModel.setIconStyle(it) }
                            )

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}
