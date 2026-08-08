package com.example.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GameSettingsEntity
import com.example.ui.theme.GoldPrimary

@Composable
fun SettingsDialog(
    settings: GameSettingsEntity,
    onSaveSettings: (GameSettingsEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var isDark by remember { mutableStateOf(settings.isDarkMode) }
    var isSfx by remember { mutableStateOf(settings.isSfxEnabled) }
    var isRetro by remember { mutableStateOf(settings.retroCrtTheme) }
    var highHz by remember { mutableStateOf(settings.highRefreshRateMode) }
    var doubleTap by remember { mutableStateOf(settings.doubleTapBoost) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Game Customization", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        },
        text = {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                // Sound Effects Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("8-Bit Retro SFX", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Coin clinks and level-up chimes", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isSfx,
                        onCheckedChange = { isSfx = it },
                        modifier = Modifier.testTag("sfx_switch")
                    )
                }

                // Dark Theme Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dark Mode Theme", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Obsidian mine aesthetic", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { isDark = it },
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }

                // Retro CRT Theme Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Retro CRT Green Theme", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Phosphor arcade terminal look", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isRetro,
                        onCheckedChange = { isRetro = it },
                        modifier = Modifier.testTag("retro_crt_switch")
                    )
                }

                // High Refresh Rate (120Hz)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("High Refresh Rate (120Hz)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Ultra smooth mobile animation", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = highHz,
                        onCheckedChange = { highHz = it },
                        modifier = Modifier.testTag("high_refresh_switch")
                    )
                }

                // Double-Tap Boost
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Quick-Tap Input Boost", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Haptic multi-touch mining", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = doubleTap,
                        onCheckedChange = { doubleTap = it },
                        modifier = Modifier.testTag("double_tap_switch")
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSaveSettings(
                        settings.copy(
                            isDarkMode = isDark,
                            isSfxEnabled = isSfx,
                            retroCrtTheme = isRetro,
                            highRefreshRateMode = highHz,
                            doubleTapBoost = doubleTap
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("save_settings_button")
            ) {
                Text("Save & Apply", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
