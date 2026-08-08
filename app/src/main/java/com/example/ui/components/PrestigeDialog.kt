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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GameStateEntity
import com.example.ui.theme.AmethystPurple
import com.example.ui.theme.GoldPrimary
import com.example.util.NumberFormatter

@Composable
fun PrestigeDialog(
    state: GameStateEntity,
    onConfirmPrestige: () -> Unit,
    onDismiss: () -> Unit
) {
    val relicsToGain = ((state.lifetimeGold / 100_000.0).coerceAtLeast(1.0)).toInt()
    val canPrestige = state.lifetimeGold >= 100_000.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(AmethystPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Prestige",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Super Mine Rebirth", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        },
        text = {
            Column {
                Text(
                    text = "Reset current mine shafts and gold to claim permanent Cosmic Relic Artifacts!",
                    fontSize = 14.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF231A30)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Relics:", color = Color.Gray, fontSize = 13.sp)
                            Text("${state.relics} Gems", fontWeight = FontWeight.Bold, color = AmethystPurple)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Relics to Gain:", color = Color.Gray, fontSize = 13.sp)
                            Text("+$relicsToGain Gems", fontWeight = FontWeight.Bold, color = GoldPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("New Production Boost:", color = Color.Gray, fontSize = 13.sp)
                            val boostPercent = ((state.relics + relicsToGain) * 25)
                            Text("+$boostPercent% Global Gold", fontWeight = FontWeight.Bold, color = Color(0xFF50C878))
                        }
                    }
                }

                if (!canPrestige) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Requires at least 100K Lifetime Gold to Prestige.",
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmPrestige()
                    onDismiss()
                },
                enabled = canPrestige,
                colors = ButtonDefaults.buttonColors(containerColor = AmethystPurple, contentColor = Color.White),
                modifier = Modifier.testTag("confirm_prestige_button")
            ) {
                Text("Rebirth Now (+$relicsToGain Relics)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
