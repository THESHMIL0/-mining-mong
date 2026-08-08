package com.example.data.repository

import com.example.data.db.GameDao
import com.example.data.db.GameStateEntity
import com.example.data.db.GameSettingsEntity
import com.example.data.db.LeaderboardEntity
import com.example.data.db.MineShaftEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject

class GameRepository(private val gameDao: GameDao) {

    val gameState: Flow<GameStateEntity?> = gameDao.getGameState()
    val mineShafts: Flow<List<MineShaftEntity>> = gameDao.getAllMineShafts()
    val leaderboards: Flow<List<LeaderboardEntity>> = gameDao.getLeaderboards()
    val settings: Flow<GameSettingsEntity?> = gameDao.getSettings()

    suspend fun initializeDefaultDataIfEmpty() {
        // Initial Game State
        val state = gameDao.getGameStateOnce()
        if (state == null) {
            gameDao.saveGameState(GameStateEntity())
        }

        // Initial Shafts (Shafts 1 to 10)
        val currentShafts = gameDao.getAllMineShaftsOnce()
        if (currentShafts.isEmpty()) {
            val initialShafts = listOf(
                MineShaftEntity(shaftIndex = 1, isUnlocked = true, minerLevel = 1, unlockCost = 0.0, baseOutput = 5.0, managerName = "Miner Bob"),
                MineShaftEntity(shaftIndex = 2, isUnlocked = false, minerLevel = 1, unlockCost = 250.0, baseOutput = 20.0, managerName = "Gemma Stone"),
                MineShaftEntity(shaftIndex = 3, isUnlocked = false, minerLevel = 1, unlockCost = 1000.0, baseOutput = 100.0, managerName = "Boulder Bill"),
                MineShaftEntity(shaftIndex = 4, isUnlocked = false, minerLevel = 1, unlockCost = 5000.0, baseOutput = 500.0, managerName = "Goldie Rush"),
                MineShaftEntity(shaftIndex = 5, isUnlocked = false, minerLevel = 1, unlockCost = 25000.0, baseOutput = 2500.0, managerName = "Sledge Sam"),
                MineShaftEntity(shaftIndex = 6, isUnlocked = false, minerLevel = 1, unlockCost = 150000.0, baseOutput = 12000.0, managerName = "Diamond Dan"),
                MineShaftEntity(shaftIndex = 7, isUnlocked = false, minerLevel = 1, unlockCost = 1000000.0, baseOutput = 75000.0, managerName = "Obsidian Otto"),
                MineShaftEntity(shaftIndex = 8, isUnlocked = false, minerLevel = 1, unlockCost = 8000000.0, baseOutput = 500000.0, managerName = "Emerald Eva"),
                MineShaftEntity(shaftIndex = 9, isUnlocked = false, minerLevel = 1, unlockCost = 50000000.0, baseOutput = 3500000.0, managerName = "Ruby Rex"),
                MineShaftEntity(shaftIndex = 10, isUnlocked = false, minerLevel = 1, unlockCost = 400000000.0, baseOutput = 25000000.0, managerName = "Cosmic Carl")
            )
            gameDao.saveAllMineShafts(initialShafts)
        }

        // Initial Leaderboard Mock Entries
        val currentLeaderboards = gameDao.getLeaderboards().firstOrNull()
        if (currentLeaderboards.isNullOrEmpty()) {
            val initialLeaderboards = listOf(
                LeaderboardEntity("user_0", "You (Prospector)", 50.0, 0, isUser = true, badgeTitle = "Novice Prospector", avatarColorHex = "#FFD700"),
                LeaderboardEntity("lead_1", "PixelKing99", 5_800_000_000.0, 12, isUser = false, badgeTitle = "Overlord", avatarColorHex = "#E91E63"),
                LeaderboardEntity("lead_2", "GoldDigger_xX", 1_200_000_000.0, 8, isUser = false, badgeTitle = "Tycoon", avatarColorHex = "#9C27B0"),
                LeaderboardEntity("lead_3", "MineCraftian", 450_000_000.0, 5, isUser = false, badgeTitle = "Master Miner", avatarColorHex = "#2196F3"),
                LeaderboardEntity("lead_4", "CryptoDwarf", 85_000_000.0, 3, isUser = false, badgeTitle = "Senior Miner", avatarColorHex = "#4CAF50"),
                LeaderboardEntity("lead_5", "SatoshiOre", 12_000_000.0, 2, isUser = false, badgeTitle = "Journeyman", avatarColorHex = "#FF9800"),
                LeaderboardEntity("lead_6", "AstroMiner", 2_500_000.0, 1, isUser = false, badgeTitle = "Apprentice", avatarColorHex = "#00BCD4")
            )
            gameDao.saveLeaderboards(initialLeaderboards)
        }

        // Initial Settings
        val currentSettings = gameDao.getSettingsOnce()
        if (currentSettings == null) {
            gameDao.saveSettings(GameSettingsEntity())
        }
    }

    suspend fun saveGameState(state: GameStateEntity) {
        gameDao.saveGameState(state.copy(lastSavedTimestamp = System.currentTimeMillis()))
    }

    suspend fun saveMineShaft(shaft: MineShaftEntity) {
        gameDao.saveMineShaft(shaft)
    }

    suspend fun saveAllMineShafts(shafts: List<MineShaftEntity>) {
        gameDao.saveAllMineShafts(shafts)
    }

    suspend fun saveSettings(settings: GameSettingsEntity) {
        gameDao.saveSettings(settings)
    }

    suspend fun updateLeaderboards(entries: List<LeaderboardEntity>) {
        gameDao.saveLeaderboards(entries)
    }

    suspend fun performPrestigeReset(relicsGained: Int) {
        val currentState = gameDao.getGameStateOnce() ?: return
        val newRelics = currentState.relics + relicsGained
        val newPrestigeCount = currentState.prestigeCount + 1
        val newGlobalMult = 1.0 + (newRelics * 0.25) // 25% boost per relic

        val resetState = currentState.copy(
            gold = 100.0, // Fresh start bonus
            relics = newRelics,
            prestigeCount = newPrestigeCount,
            elevatorLevel = 1,
            elevatorSpeed = 1.0,
            elevatorLoad = 50.0,
            elevatorManagerUnlocked = false,
            warehouseLevel = 1,
            warehouseSpeed = 1.0,
            warehouseLoad = 100.0,
            warehouseManagerUnlocked = false,
            globalProductionMultiplier = newGlobalMult,
            lastSavedTimestamp = System.currentTimeMillis()
        )
        gameDao.saveGameState(resetState)

        // Reset mine shafts
        gameDao.clearMineShafts()
        val resetShafts = listOf(
            MineShaftEntity(shaftIndex = 1, isUnlocked = true, minerLevel = 1, unlockCost = 0.0, baseOutput = 5.0, managerName = "Miner Bob"),
            MineShaftEntity(shaftIndex = 2, isUnlocked = false, minerLevel = 1, unlockCost = 250.0, baseOutput = 20.0, managerName = "Gemma Stone"),
            MineShaftEntity(shaftIndex = 3, isUnlocked = false, minerLevel = 1, unlockCost = 1000.0, baseOutput = 100.0, managerName = "Boulder Bill"),
            MineShaftEntity(shaftIndex = 4, isUnlocked = false, minerLevel = 1, unlockCost = 5000.0, baseOutput = 500.0, managerName = "Goldie Rush"),
            MineShaftEntity(shaftIndex = 5, isUnlocked = false, minerLevel = 1, unlockCost = 25000.0, baseOutput = 2500.0, managerName = "Sledge Sam"),
            MineShaftEntity(shaftIndex = 6, isUnlocked = false, minerLevel = 1, unlockCost = 150000.0, baseOutput = 12000.0, managerName = "Diamond Dan"),
            MineShaftEntity(shaftIndex = 7, isUnlocked = false, minerLevel = 1, unlockCost = 1000000.0, baseOutput = 75000.0, managerName = "Obsidian Otto"),
            MineShaftEntity(shaftIndex = 8, isUnlocked = false, minerLevel = 1, unlockCost = 8000000.0, baseOutput = 500000.0, managerName = "Emerald Eva"),
            MineShaftEntity(shaftIndex = 9, isUnlocked = false, minerLevel = 1, unlockCost = 50000000.0, baseOutput = 3500000.0, managerName = "Ruby Rex"),
            MineShaftEntity(shaftIndex = 10, isUnlocked = false, minerLevel = 1, unlockCost = 400000000.0, baseOutput = 25000000.0, managerName = "Cosmic Carl")
        )
        gameDao.saveAllMineShafts(resetShafts)
    }

    suspend fun exportSaveJson(): String {
        val state = gameDao.getGameStateOnce()
        val shafts = gameDao.getAllMineShaftsOnce()
        val json = JSONObject()
        if (state != null) {
            val stateObj = JSONObject().apply {
                put("gold", state.gold)
                put("lifetimeGold", state.lifetimeGold)
                put("relics", state.relics)
                put("prestigeCount", state.prestigeCount)
                put("elevatorLevel", state.elevatorLevel)
                put("warehouseLevel", state.warehouseLevel)
                put("cloudSyncCode", state.cloudSyncCode)
            }
            json.put("state", stateObj)
        }

        val shaftsArr = JSONArray()
        for (s in shafts) {
            val sObj = JSONObject().apply {
                put("index", s.shaftIndex)
                put("unlocked", s.isUnlocked)
                put("level", s.minerLevel)
                put("hasManager", s.hasManager)
            }
            shaftsArr.put(sObj)
        }
        json.put("shafts", shaftsArr)
        return json.toString()
    }

    suspend fun importSaveJson(jsonString: String): Boolean {
        return try {
            val json = JSONObject(jsonString)
            if (json.has("state")) {
                val sObj = json.getJSONObject("state")
                val curState = gameDao.getGameStateOnce() ?: GameStateEntity()
                val newState = curState.copy(
                    gold = sObj.optDouble("gold", curState.gold),
                    lifetimeGold = sObj.optDouble("lifetimeGold", curState.lifetimeGold),
                    relics = sObj.optInt("relics", curState.relics),
                    prestigeCount = sObj.optInt("prestigeCount", curState.prestigeCount),
                    elevatorLevel = sObj.optInt("elevatorLevel", curState.elevatorLevel),
                    warehouseLevel = sObj.optInt("warehouseLevel", curState.warehouseLevel),
                    cloudSyncCode = sObj.optString("cloudSyncCode", curState.cloudSyncCode),
                    lastSavedTimestamp = System.currentTimeMillis()
                )
                gameDao.saveGameState(newState)
            }

            if (json.has("shafts")) {
                val shaftsArr = json.getJSONArray("shafts")
                val currentShafts = gameDao.getAllMineShaftsOnce().associateBy { it.shaftIndex }.toMutableMap()
                for (i in 0 until shaftsArr.length()) {
                    val sObj = shaftsArr.getJSONObject(i)
                    val idx = sObj.getInt("index")
                    val existing = currentShafts[idx]
                    if (existing != null) {
                        currentShafts[idx] = existing.copy(
                            isUnlocked = sObj.optBoolean("unlocked", existing.isUnlocked),
                            minerLevel = sObj.optInt("level", existing.minerLevel),
                            hasManager = sObj.optBoolean("hasManager", existing.hasManager)
                        )
                    }
                }
                gameDao.saveAllMineShafts(currentShafts.values.toList())
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
