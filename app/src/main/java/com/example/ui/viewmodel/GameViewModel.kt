package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.GameStateEntity
import com.example.data.db.GameSettingsEntity
import com.example.data.db.LeaderboardEntity
import com.example.data.db.MineShaftEntity
import com.example.data.repository.GameRepository
import com.example.util.SoundEffects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ParticleEvent(
    val x: Float,
    val y: Float,
    val amountText: String
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository

    val gameState: StateFlow<GameStateEntity?>
    val mineShafts: StateFlow<List<MineShaftEntity>>
    val leaderboards: StateFlow<List<LeaderboardEntity>>
    val settings: StateFlow<GameSettingsEntity?>

    private val _offlineGoldEarned = MutableStateFlow<Double?>(null)
    val offlineGoldEarned: StateFlow<Double?> = _offlineGoldEarned.asStateFlow()

    private val _particleEvents = MutableSharedFlow<ParticleEvent>()
    val particleEvents: SharedFlow<ParticleEvent> = _particleEvents.asSharedFlow()

    private val _incomePerSecond = MutableStateFlow(0.0)
    val incomePerSecond: StateFlow<Double> = _incomePerSecond.asStateFlow()

    private val _aiAdvice = MutableStateFlow<String?>(null)
    val aiAdvice: StateFlow<String?> = _aiAdvice.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = GameRepository(db.gameDao())

        gameState = repository.gameState.asStateFlow(viewModelScope, GameStateEntity())
        mineShafts = repository.mineShafts.asStateFlow(viewModelScope, emptyList())
        leaderboards = repository.leaderboards.asStateFlow(viewModelScope, emptyList())
        settings = repository.settings.asStateFlow(viewModelScope, GameSettingsEntity())

        viewModelScope.launch(Dispatchers.IO) {
            repository.initializeDefaultDataIfEmpty()
            checkOfflineEarnings()
            startGameEngineLoop()
        }
    }

    private fun <T> kotlinx.coroutines.flow.Flow<T?>.asStateFlow(
        scope: kotlinx.coroutines.CoroutineScope,
        initial: T
    ): StateFlow<T> {
        val state = MutableStateFlow(initial)
        scope.launch {
            this@asStateFlow.collect { value ->
                if (value != null) state.value = value
            }
        }
        return state
    }

    private suspend fun checkOfflineEarnings() {
        val state = repository.gameState.firstOrNull() ?: return
        val shafts = repository.mineShafts.firstOrNull() ?: return

        val now = System.currentTimeMillis()
        val deltaSeconds = ((now - state.lastSavedTimestamp) / 1000).coerceAtLeast(0)

        if (deltaSeconds > 10) { // Off for at least 10s
            var activeProduction = 0.0
            for (s in shafts) {
                if (s.isUnlocked && s.hasManager) {
                    val rate = (s.minerLevel * s.minerCount * s.baseOutput) * state.globalProductionMultiplier
                    activeProduction += rate
                }
            }

            val offlineEarned = activeProduction * deltaSeconds * 0.5 // 50% efficiency offline
            if (offlineEarned > 1.0) {
                _offlineGoldEarned.value = offlineEarned
                val newState = state.copy(
                    gold = state.gold + offlineEarned,
                    lifetimeGold = state.lifetimeGold + offlineEarned,
                    lastSavedTimestamp = now
                )
                repository.saveGameState(newState)
            }
        }
    }

    fun dismissOfflineDialog() {
        _offlineGoldEarned.value = null
    }

    private fun startGameEngineLoop() {
        viewModelScope.launch(Dispatchers.IO) {
            var saveCounter = 0
            while (true) {
                delay(200) // tick every 200ms (5 ticks = 1 sec)

                val state = repository.gameState.firstOrNull() ?: continue
                val shafts = repository.mineShafts.firstOrNull() ?: continue
                val curSettings = repository.settings.firstOrNull() ?: GameSettingsEntity()

                // Sync audio setting
                SoundEffects.isSfxEnabled = curSettings.isSfxEnabled

                // Calculate production
                var totalSecRate = 0.0
                val updatedShafts = shafts.map { shaft ->
                    if (!shaft.isUnlocked) return@map shaft

                    val shaftProductionPerSec = (shaft.minerLevel * shaft.minerCount * shaft.baseOutput) * state.globalProductionMultiplier
                    totalSecRate += shaftProductionPerSec

                    if (shaft.hasManager) {
                        // Manager auto mines and adds to ore buffer
                        val deltaOre = (shaftProductionPerSec * 0.2) // tick is 0.2s
                        shaft.copy(oreAccumulated = shaft.oreAccumulated + deltaOre)
                    } else {
                        shaft
                    }
                }

                _incomePerSecond.value = totalSecRate

                // Elevator / Warehouse logistics tick
                var goldEarnedThisTick = 0.0
                val finalShafts = updatedShafts.map { shaft ->
                    if (shaft.oreAccumulated > 0) {
                        val collected = shaft.oreAccumulated.coerceAtMost((state.elevatorLoad * state.elevatorSpeed * 0.2))
                        goldEarnedThisTick += collected
                        shaft.copy(oreAccumulated = shaft.oreAccumulated - collected)
                    } else {
                        shaft
                    }
                }

                val newGold = state.gold + goldEarnedThisTick
                val newLifetime = state.lifetimeGold + goldEarnedThisTick

                // Save periodically to DB
                saveCounter++
                val newState = state.copy(
                    gold = newGold,
                    lifetimeGold = newLifetime,
                    lastSavedTimestamp = System.currentTimeMillis()
                )

                if (saveCounter >= 15) { // every 3 seconds
                    saveCounter = 0
                    repository.saveGameState(newState)
                    repository.saveAllMineShafts(finalShafts)
                    updateLeaderboardUserScore(newLifetime, state.prestigeCount)
                } else {
                    // Quick state update
                    repository.saveGameState(newState)
                    repository.saveAllMineShafts(finalShafts)
                }
            }
        }
    }

    private suspend fun updateLeaderboardUserScore(totalGold: Double, prestigeLevel: Int) {
        val currentEntries = repository.leaderboards.firstOrNull() ?: return
        val updated = currentEntries.map { entry ->
            if (entry.isUser) {
                val title = when {
                    prestigeLevel >= 5 -> "Cosmic Overlord"
                    prestigeLevel >= 3 -> "Diamond Tycoon"
                    prestigeLevel >= 1 -> "Prestige Prospector"
                    totalGold > 1_000_000 -> "Gold Baron"
                    totalGold > 10_000 -> "Seasoned Miner"
                    else -> "Novice Prospector"
                }
                entry.copy(totalGold = totalGold, prestigeLevel = prestigeLevel, badgeTitle = title)
            } else {
                entry
            }
        }.sortedByDescending { it.totalGold }

        repository.updateLeaderboards(updated)
    }

    fun tapToMine(x: Float = 0f, y: Float = 0f) {
        viewModelScope.launch(Dispatchers.IO) {
            val state = repository.gameState.firstOrNull() ?: return@launch
            val tapGain = 1.0 * state.tapPowerMultiplier * state.globalProductionMultiplier

            val newState = state.copy(
                gold = state.gold + tapGain,
                lifetimeGold = state.lifetimeGold + tapGain
            )
            repository.saveGameState(newState)

            SoundEffects.playTapGold()

            if (x > 0f && y > 0f) {
                _particleEvents.emit(ParticleEvent(x, y, "+${com.example.util.NumberFormatter.format(tapGain)}"))
            }
        }
    }

    fun upgradeMineShaft(shaftIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val state = repository.gameState.firstOrNull() ?: return@launch
            val shafts = repository.mineShafts.firstOrNull() ?: return@launch
            val shaft = shafts.find { it.shaftIndex == shaftIndex } ?: return@launch

            val cost = shaft.minerLevel * 100.0 * (1 + (shaftIndex - 1) * 0.8)
            if (state.gold >= cost) {
                val newGold = state.gold - cost
                val newLevel = shaft.minerLevel + 1
                val newCount = (newLevel / 5) + 1

                val updatedShaft = shaft.copy(
                    minerLevel = newLevel,
                    minerCount = newCount
                )

                repository.saveGameState(state.copy(gold = newGold))
                repository.saveMineShaft(updatedShaft)

                SoundEffects.playLevelUp()
            }
        }
    }

    fun unlockMineShaft(shaftIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val state = repository.gameState.firstOrNull() ?: return@launch
            val shafts = repository.mineShafts.firstOrNull() ?: return@launch
            val shaft = shafts.find { it.shaftIndex == shaftIndex } ?: return@launch

            if (!shaft.isUnlocked && state.gold >= shaft.unlockCost) {
                val newGold = state.gold - shaft.unlockCost
                val updatedShaft = shaft.copy(isUnlocked = true)

                repository.saveGameState(state.copy(gold = newGold))
                repository.saveMineShaft(updatedShaft)

                SoundEffects.playLevelUp()
            }
        }
    }

    fun hireShaftManager(shaftIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val state = repository.gameState.firstOrNull() ?: return@launch
            val shafts = repository.mineShafts.firstOrNull() ?: return@launch
            val shaft = shafts.find { it.shaftIndex == shaftIndex } ?: return@launch

            val managerCost = 500.0 * shaftIndex * shaftIndex
            if (!shaft.hasManager && state.gold >= managerCost) {
                val newGold = state.gold - managerCost
                val updatedShaft = shaft.copy(hasManager = true)

                repository.saveGameState(state.copy(gold = newGold))
                repository.saveMineShaft(updatedShaft)

                SoundEffects.playLevelUp()
            }
        }
    }

    fun upgradeElevator() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = repository.gameState.firstOrNull() ?: return@launch
            val cost = state.elevatorLevel * 150.0
            if (state.gold >= cost) {
                val newState = state.copy(
                    gold = state.gold - cost,
                    elevatorLevel = state.elevatorLevel + 1,
                    elevatorLoad = state.elevatorLoad * 1.2,
                    elevatorSpeed = state.elevatorSpeed * 1.05
                )
                repository.saveGameState(newState)
                SoundEffects.playLevelUp()
            }
        }
    }

    fun upgradeWarehouse() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = repository.gameState.firstOrNull() ?: return@launch
            val cost = state.warehouseLevel * 200.0
            if (state.gold >= cost) {
                val newState = state.copy(
                    gold = state.gold - cost,
                    warehouseLevel = state.warehouseLevel + 1,
                    warehouseLoad = state.warehouseLoad * 1.25,
                    warehouseSpeed = state.warehouseSpeed * 1.05
                )
                repository.saveGameState(newState)
                SoundEffects.playLevelUp()
            }
        }
    }

    fun performPrestige() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = repository.gameState.firstOrNull() ?: return@launch
            // Calculate relics earned based on lifetime gold
            val relicsEarned = ((state.lifetimeGold / 100_000.0).coerceAtLeast(1.0)).toInt()

            repository.performPrestigeReset(relicsEarned)
            SoundEffects.playPrestige()
        }
    }

    fun updateSettings(newSettings: GameSettingsEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(newSettings)
            SoundEffects.isSfxEnabled = newSettings.isSfxEnabled
        }
    }

    suspend fun getExportSaveCode(): String {
        return repository.exportSaveJson()
    }

    suspend fun importSaveCode(code: String): Boolean {
        return repository.importSaveJson(code)
    }

    fun generateAiStrategyAdvice() {
        viewModelScope.launch(Dispatchers.IO) {
            _isAiLoading.value = true
            delay(1200) // Simulate intelligent analysis

            val state = repository.gameState.firstOrNull() ?: return@launch
            val shafts = repository.mineShafts.firstOrNull() ?: return@launch

            val unlockedCount = shafts.count { it.isUnlocked }
            val unmanagedCount = shafts.count { it.isUnlocked && !it.hasManager }

            val advice = when {
                unmanagedCount > 0 -> "💡 **Strategy Recommendation**: Hire managers for your active mine shafts! Automated workers keep collecting gold even when you're away or offline."
                state.gold > 50000 && unlockedCount < 5 -> "🚀 **Deep Shaft Expansion**: You have plenty of gold reserved! Unlock Mine Shaft ${unlockedCount + 1} to multiply your gold output exponentially."
                state.elevatorLevel < unlockedCount * 2 -> "⚙️ **Logistics Bottleneck**: Your elevator level (${state.elevatorLevel}) is lagging behind your shaft output! Upgrade the elevator to haul gold faster."
                else -> "✨ **Prestige Master Tip**: Your mine is running at peak efficiency! Accumulate 100K gold to perform a Prestige Rebirth and earn permanent Relic Multipliers."
            }

            _aiAdvice.value = advice
            _isAiLoading.value = false
        }
    }
}
