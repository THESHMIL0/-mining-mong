package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.GameStateEntity
import com.example.data.db.GameSettingsEntity
import com.example.data.db.LeaderboardEntity
import com.example.data.db.MineShaftEntity
import com.example.ui.components.AiAdvisorDialog
import com.example.ui.components.CloudSaveDialog
import com.example.ui.components.ElevatorAndWarehouseView
import com.example.ui.components.LeaderboardDialog
import com.example.ui.components.MineShaftCard
import com.example.ui.components.OfflineEarningsDialog
import com.example.ui.components.ParticlesOverlay
import com.example.ui.components.PrestigeDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.AmethystPurple
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.PixelMineTheme
import com.example.ui.theme.SapphireBlue
import com.example.ui.viewmodel.GameViewModel
import com.example.util.NumberFormatter

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val gameState by viewModel.gameState.collectAsStateWithLifecycle()
            val mineShafts by viewModel.mineShafts.collectAsStateWithLifecycle()
            val leaderboards by viewModel.leaderboards.collectAsStateWithLifecycle()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val offlineGold by viewModel.offlineGoldEarned.collectAsStateWithLifecycle()
            val incomePerSec by viewModel.incomePerSecond.collectAsStateWithLifecycle()
            val aiAdvice by viewModel.aiAdvice.collectAsStateWithLifecycle()
            val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

            val curSettings = settings ?: GameSettingsEntity()
            val curState = gameState ?: GameStateEntity()

            PixelMineTheme(
                darkTheme = curSettings.isDarkMode,
                isRetroCrt = curSettings.retroCrtTheme
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        MainMiningScreen(
                            state = curState,
                            shafts = mineShafts,
                            leaderboards = leaderboards,
                            settings = curSettings,
                            offlineGold = offlineGold,
                            incomePerSec = incomePerSec,
                            aiAdvice = aiAdvice,
                            isAiLoading = isAiLoading,
                            viewModel = viewModel
                        )

                        // Floating particles canvas
                        ParticlesOverlay(
                            particleEvents = viewModel.particleEvents,
                            particleQuality = curSettings.particleQuality
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainMiningScreen(
    state: GameStateEntity,
    shafts: List<MineShaftEntity>,
    leaderboards: List<LeaderboardEntity>,
    settings: GameSettingsEntity,
    offlineGold: Double?,
    incomePerSec: Double,
    aiAdvice: String?,
    isAiLoading: Boolean,
    viewModel: GameViewModel
) {
    var showPrestigeDialog by remember { mutableStateOf(false) }
    var showCloudDialog by remember { mutableStateOf(false) }
    var showLeaderboardDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAiAdvisorDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Top Header HUD
        HeaderHud(
            state = state,
            incomePerSec = incomePerSec,
            onOpenPrestige = { showPrestigeDialog = true },
            onOpenCloud = { showCloudDialog = true },
            onOpenLeaderboard = { showLeaderboardDialog = true },
            onOpenSettings = { showSettingsDialog = true },
            onOpenAiAdvisor = { showAiAdvisorDialog = true }
        )

        // Main Scrollable Mining Content
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp)
                .testTag("mine_shafts_list")
        ) {
            // Surface Elevator & Warehouse Section
            item {
                ElevatorAndWarehouseView(
                    state = state,
                    onUpgradeElevator = { viewModel.upgradeElevator() },
                    onUpgradeWarehouse = { viewModel.upgradeWarehouse() }
                )
            }

            // Mine Shafts List
            items(shafts, key = { it.shaftIndex }) { shaft ->
                MineShaftCard(
                    shaft = shaft,
                    currentGold = state.gold,
                    globalMultiplier = state.globalProductionMultiplier,
                    onTapShaft = { x, y -> viewModel.tapToMine(x, y) },
                    onUpgrade = { idx -> viewModel.upgradeMineShaft(idx) },
                    onUnlock = { idx -> viewModel.unlockMineShaft(idx) },
                    onHireManager = { idx -> viewModel.hireShaftManager(idx) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Dialogs
    if (offlineGold != null) {
        OfflineEarningsDialog(
            offlineGold = offlineGold,
            onCollect = { viewModel.dismissOfflineDialog() }
        )
    }

    if (showPrestigeDialog) {
        PrestigeDialog(
            state = state,
            onConfirmPrestige = { viewModel.performPrestige() },
            onDismiss = { showPrestigeDialog = false }
        )
    }

    if (showCloudDialog) {
        CloudSaveDialog(
            state = state,
            onExportSave = { viewModel.getExportSaveCode() },
            onImportSave = { code -> viewModel.importSaveCode(code) },
            onDismiss = { showCloudDialog = false }
        )
    }

    if (showLeaderboardDialog) {
        LeaderboardDialog(
            leaderboards = leaderboards,
            onDismiss = { showLeaderboardDialog = false }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
            onSaveSettings = { newS -> viewModel.updateSettings(newS) },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showAiAdvisorDialog) {
        AiAdvisorDialog(
            aiAdvice = aiAdvice,
            isLoading = isAiLoading,
            onRequestAdvice = { viewModel.generateAiStrategyAdvice() },
            onDismiss = { showAiAdvisorDialog = false }
        )
    }
}

@Composable
fun HeaderHud(
    state: GameStateEntity,
    incomePerSec: Double,
    onOpenPrestige: () -> Unit,
    onOpenCloud: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAiAdvisor: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .testTag("top_header_hud"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Gold & Income Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🪙", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = NumberFormatter.format(state.gold) + " Gold",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Text(
                            text = "+${NumberFormatter.format(incomePerSec)} / sec",
                            fontSize = 13.sp,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Relics Balance
                if (state.relics > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(AmethystPurple.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("💎", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.relics} Relics",
                            color = AmethystPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenPrestige,
                    modifier = Modifier.testTag("action_prestige_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Prestige", tint = AmethystPurple)
                }

                IconButton(
                    onClick = onOpenCloud,
                    modifier = Modifier.testTag("action_cloud_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Cloud Save", tint = SapphireBlue)
                }

                IconButton(
                    onClick = onOpenLeaderboard,
                    modifier = Modifier.testTag("action_leaderboard_button")
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Leaderboard", tint = GoldPrimary)
                }

                IconButton(
                    onClick = onOpenAiAdvisor,
                    modifier = Modifier.testTag("action_ai_advisor_button")
                ) {
                    Icon(Icons.Default.Info, contentDescription = "AI Advisor", tint = EmeraldGreen)
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("action_settings_button")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.LightGray)
                }
            }
        }
    }
}
