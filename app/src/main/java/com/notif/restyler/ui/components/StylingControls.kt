package com.notif.restyler.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.notif.restyler.model.IconStyle
import com.notif.restyler.model.NotificationDensity
import com.notif.restyler.model.RestylerSettings
import com.notif.restyler.ui.theme.SwatchAmber
import com.notif.restyler.ui.theme.SwatchCharcoal
import com.notif.restyler.ui.theme.SwatchEmerald
import com.notif.restyler.ui.theme.SwatchIndigo
import com.notif.restyler.ui.theme.SwatchRose
import com.notif.restyler.ui.theme.SwatchSlate

private data class ColorPreset(val hex: String, val color: Color, val name: String)

private val PRESET_COLORS = listOf(
    ColorPreset("#6750A4", Color(0xFF6750A4), "Purple"),
    ColorPreset("#475569", SwatchSlate, "Slate"),
    ColorPreset("#059669", SwatchEmerald, "Emerald"),
    ColorPreset("#4F46E5", SwatchIndigo, "Indigo"),
    ColorPreset("#D97706", SwatchAmber, "Amber"),
    ColorPreset("#E11D48", SwatchRose, "Rose"),
    ColorPreset("#1E293B", SwatchCharcoal, "Charcoal")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylingControls(
    settings: RestylerSettings,
    onAccentColorChange: (String) -> Unit,
    onDensityChange: (NotificationDensity) -> Unit,
    onIconStyleChange: (IconStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomHex by remember { mutableStateOf(false) }
    var customHexInput by remember(settings.accentColorHex) { mutableStateOf(settings.accentColorHex) }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxWidth()) {
        // --- 1. ACCENT COLOR PICKER ---
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
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = Color(settings.accentColorInt),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Accent Color",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    IconButton(onClick = { showCustomHex = !showCustomHex }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Custom Hex",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Presets Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PRESET_COLORS) { preset ->
                        val isSelected = settings.accentColorHex.equals(preset.hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(preset.color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    onAccentColorChange(preset.hex)
                                    customHexInput = preset.hex
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Custom Hex Input Field
                AnimatedVisibility(visible = showCustomHex) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        OutlinedTextField(
                            value = customHexInput,
                            onValueChange = { input ->
                                customHexInput = input
                                val formatted = if (input.startsWith("#")) input else "#$input"
                                if (formatted.matches(Regex("^#([0-9a-fA-F]{6}|[0-9a-fA-F]{8})$"))) {
                                    onAccentColorChange(formatted)
                                }
                            },
                            label = { Text("Custom Hex Color (#RRGGBB)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. NOTIFICATION DENSITY SELECTOR ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Notification Density",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (settings.density == NotificationDensity.COMFORTABLE) {
                        "Comfortable: Expanded multi-line preview with generous spacing"
                    } else {
                        "Compact: Single-line minimal notification profile"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = settings.density == NotificationDensity.COMFORTABLE,
                        onClick = { onDensityChange(NotificationDensity.COMFORTABLE) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Comfortable")
                    }
                    SegmentedButton(
                        selected = settings.density == NotificationDensity.COMPACT,
                        onClick = { onDensityChange(NotificationDensity.COMPACT) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Compact")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. ICON STYLE SELECTOR ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Icon Aesthetics",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = when (settings.iconStyle) {
                        IconStyle.APP_ICON -> "App Icon: Original full-color source app icon"
                        IconStyle.MONOCHROME_APP_ICON -> "Monochrome: Grayscale app icon tinted with accent color"
                        IconStyle.GENERIC_GLYPH -> "Generic Glyph: Minimalist dot glyph only"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = settings.iconStyle == IconStyle.APP_ICON,
                        onClick = { onIconStyleChange(IconStyle.APP_ICON) },
                        label = { Text("App Icon", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = settings.iconStyle == IconStyle.MONOCHROME_APP_ICON,
                        onClick = { onIconStyleChange(IconStyle.MONOCHROME_APP_ICON) },
                        label = { Text("Monochrome", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = settings.iconStyle == IconStyle.GENERIC_GLYPH,
                        onClick = { onIconStyleChange(IconStyle.GENERIC_GLYPH) },
                        label = { Text("Glyph Only", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}
