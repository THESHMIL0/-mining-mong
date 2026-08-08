package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GameStateEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SapphireBlue
import com.example.util.NumberFormatter
import kotlin.math.roundToInt

@Composable
fun ElevatorAndWarehouseView(
    state: GameStateEntity,
    onUpgradeElevator: () -> Unit,
    onUpgradeWarehouse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elevatorCost = state.elevatorLevel * 150.0
    val warehouseCost = state.warehouseLevel * 200.0

    val infiniteTransition = rememberInfiniteTransition(label = "logistics")
    val cartOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cart_move"
    )

    val elevatorHeightOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "elevator_hoist"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("logistics_surface_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Surface Logistics & Bank",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Animated Surface Transport Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF13101A))
                    .border(1.dp, Color(0xFF332945), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Bank Vault on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Elevator Shaft
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SapphireBlue, CircleShape)
                            .offset { IntOffset(0, elevatorHeightOffset.roundToInt() / 3) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Elevator Hoist",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Bank Vault Icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(GoldPrimary, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("BANK", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Black)
                    }
                }

                // Moving Cart Icon
                Box(
                    modifier = Modifier
                        .offset { IntOffset(cartOffset.roundToInt(), 0) }
                        .size(28.dp)
                        .background(EmeraldGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Ore Cart",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Logistics Upgrade Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Elevator Upgrade Box
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF1E182A), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text("Elevator Lvl ${state.elevatorLevel}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text("Load: ${NumberFormatter.format(state.elevatorLoad)}", fontSize = 12.sp, color = SapphireBlue)

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onUpgradeElevator,
                        enabled = state.gold >= elevatorCost,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("upgrade_elevator_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SapphireBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("${NumberFormatter.format(elevatorCost)} Gold", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Warehouse Upgrade Box
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF1E182A), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text("Warehouse Lvl ${state.warehouseLevel}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text("Capacity: ${NumberFormatter.format(state.warehouseLoad)}", fontSize = 12.sp, color = EmeraldGreen)

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onUpgradeWarehouse,
                        enabled = state.gold >= warehouseCost,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("upgrade_warehouse_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("${NumberFormatter.format(warehouseCost)} Gold", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
