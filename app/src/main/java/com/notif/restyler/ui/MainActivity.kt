package com.notif.restyler.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.notif.restyler.ui.components.AppListSheet
import com.notif.restyler.ui.components.LivePreviewCard
import com.notif.restyler.ui.components.OnboardingDialog
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

                var showAppListSheet by remember { mutableStateOf(false) }
                var showOnboardingDialog by remember { mutableStateOf(false) }

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
                                IconButton(onClick = { showOnboardingDialog = true }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                        contentDescription = "Setup Guide & Permissions"
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = if (uiState.settings.isServiceEnabled) "Enabled" else "Paused",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(end = 6.dp)
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

                            Spacer(modifier = Modifier.height(20.dp))

                            // 4. Per-App Management Card
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Apps,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Per-App Restyling",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Choose which apps should be restyled or left alone in their original form.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = { showAppListSheet = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Manage Apps (${uiState.installedApps.size})")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }

                // App Management Bottom Sheet
                if (showAppListSheet) {
                    AppListSheet(
                        installedApps = uiState.installedApps,
                        searchQuery = uiState.searchQuery,
                        isLoading = uiState.isLoadingApps,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onToggleApp = { pkg, isRestyled ->
                            viewModel.toggleAppRestyle(pkg, isRestyled)
                        },
                        onRestyleAll = { viewModel.restyleAllApps() },
                        onDisableAll = { viewModel.disableAllApps() },
                        onDismiss = { showAppListSheet = false }
                    )
                }

                // Onboarding & Permissions Dialog
                if (showOnboardingDialog) {
                    OnboardingDialog(
                        isNotificationAccessGranted = uiState.isNotificationAccessGranted,
                        isBatteryOptimizationIgnored = uiState.isBatteryOptimizationIgnored,
                        onOpenNotificationAccess = {
                            startActivity(PermissionUtils.createNotificationAccessIntent())
                        },
                        onOpenAppDetails = {
                            startActivity(PermissionUtils.createAppDetailsIntent(packageName))
                        },
                        onOpenBatteryOptimization = {
                            viewModel.launchBatteryOptimization()
                        },
                        onDismiss = { showOnboardingDialog = false }
                    )
                }
            }
        }
    }
}
