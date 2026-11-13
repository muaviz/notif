package com.notif.restyler.ui.components

import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingDialog(
    isNotificationAccessGranted: Boolean,
    isBatteryOptimizationIgnored: Boolean,
    onOpenNotificationAccess: () -> Unit,
    onOpenAppDetails: () -> Unit,
    onOpenBatteryOptimization: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Setup & Permissions Guide",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Follow these steps to allow Notification Restyler to intercept, restyle, and present your notifications smoothly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Step 1: Android 13+ Restricted Settings Guidance (Crucial for Sideloaded APKs)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    GuideStepCard(
                        stepNumber = "1",
                        title = "Allow Restricted Settings (Android 13+)",
                        description = "If the Notification Access toggle is greyed out in settings:\n• Open App Info\n• Tap the 3 dots (⋮) in the top-right corner\n• Select \"Allow restricted settings\"",
                        icon = Icons.Default.LockOpen,
                        isComplete = isNotificationAccessGranted,
                        actionLabel = "Open App Info",
                        onAction = onOpenAppDetails
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Step 2: Grant Notification Access
                GuideStepCard(
                    stepNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) "2" else "1",
                    title = "Grant Notification Access",
                    description = "Allow Notification Restyler to read incoming notifications and cancel originals.",
                    icon = Icons.Default.Security,
                    isComplete = isNotificationAccessGranted,
                    actionLabel = "Open Notification Access",
                    onAction = onOpenNotificationAccess
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Step 3: Battery Optimization Exemption
                GuideStepCard(
                    stepNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) "3" else "2",
                    title = "Prevent OEM Background Kills",
                    description = "Disable battery optimization so Xiaomi, Samsung, or OnePlus doesn't kill the listener service in the background.",
                    icon = Icons.Default.BatteryAlert,
                    isComplete = isBatteryOptimizationIgnored,
                    actionLabel = "Exempt from Battery Saver",
                    onAction = onOpenBatteryOptimization
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Got It")
            }
        }
    )
}

@Composable
private fun GuideStepCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector,
    isComplete: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isComplete) {
                Color(0xFFE8F5E9)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (isComplete) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isComplete) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (isComplete) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isComplete) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isComplete) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}
