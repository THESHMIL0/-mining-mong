package com.example.ui.components

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.MineShaftEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.util.NumberFormatter

@Composable
fun MineShaftCard(
    shaft: MineShaftEntity,
    currentGold: Double,
    globalMultiplier: Double,
    onTapShaft: (Float, Float) -> Unit,
    onUpgrade: (Int) -> Unit,
    onUnlock: (Int) -> Unit,
    onHireManager: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val upgradeCost = shaft.minerLevel * 100.0 * (1 + (shaft.shaftIndex - 1) * 0.8)
    val canAffordUpgrade = currentGold >= upgradeCost
    val managerCost = 500.0 * shaft.shaftIndex * shaft.shaftIndex
    val canAffordManager = currentGold >= managerCost

    val infiniteTransition = rememberInfiniteTransition(label = "pickaxe")
    val pickaxeAngle by infiniteTransition.animateFloat(
        initialValue = -35f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swing"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("shaft_card_${shaft.shaftIndex}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (shaft.isUnlocked) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF1B1622)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (!shaft.isUnlocked) {
            // Locked Shaft View
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF2A2236), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked shaft",
                            tint = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Mine Shaft #${shaft.shaftIndex}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = "Unlock Cost: ${NumberFormatter.format(shaft.unlockCost)} Gold",
                            fontSize = 13.sp,
                            color = GoldPrimary
                        )
                    }
                }

                Button(
                    onClick = { onUnlock(shaft.shaftIndex) },
                    enabled = currentGold >= shaft.unlockCost,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.testTag("unlock_shaft_button_${shaft.shaftIndex}")
                ) {
                    Text("Unlock")
                }
            }
        } else {
            // Unlocked Shaft View
            Column(modifier = Modifier.padding(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(GoldSecondary, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#${shaft.shaftIndex}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Shaft Level ${shaft.minerLevel}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val secRate = (shaft.minerLevel * shaft.minerCount * shaft.baseOutput) * globalMultiplier
                            Text(
                                text = "+${NumberFormatter.format(secRate)} / sec",
                                fontSize = 12.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Manager Status
                    if (shaft.hasManager) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(EmeraldGreen.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "Manager Active",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = shaft.managerName,
                                fontSize = 11.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onHireManager(shaft.shaftIndex) },
                            enabled = canAffordManager,
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("hire_manager_button_${shaft.shaftIndex}"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Hire", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hire Manager (${NumberFormatter.format(managerCost)})", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mine Action Area (Rock & Swinging Pickaxe)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF15101A))
                        .border(1.dp, Color(0xFF3D324E), RoundedCornerShape(8.dp))
                        .clickable { onTapShaft(200f, 200f) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Miners & Swinging Pickaxe
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Pickaxe swing",
                                tint = GoldPrimary,
                                modifier = Modifier
                                    .size(28.dp)
                                    .rotate(if (shaft.hasManager) pickaxeAngle else 0f)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${shaft.minerCount} Miners Working",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tap rock to mine gold manually",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Ore Buffer Bar
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Ore: ${NumberFormatter.format(shaft.oreAccumulated)}",
                                fontSize = 12.sp,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Upgrade Button
                Button(
                    onClick = { onUpgrade(shaft.shaftIndex) },
                    enabled = canAffordUpgrade,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("upgrade_shaft_button_${shaft.shaftIndex}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color.Black,
                        disabledContainerColor = Color(0xFF2A2635)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Upgrade Level (Cost: ${NumberFormatter.format(upgradeCost)} Gold)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
