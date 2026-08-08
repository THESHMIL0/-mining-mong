package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_state")
data class GameStateEntity(
    @PrimaryKey val id: Int = 1,
    val gold: Double = 50.0,
    val lifetimeGold: Double = 50.0,
    val relics: Int = 0,
    val prestigeCount: Int = 0,
    val elevatorLevel: Int = 1,
    val elevatorSpeed: Double = 1.0,
    val elevatorLoad: Double = 50.0,
    val elevatorManagerUnlocked: Boolean = false,
    val warehouseLevel: Int = 1,
    val warehouseSpeed: Double = 1.0,
    val warehouseLoad: Double = 100.0,
    val warehouseManagerUnlocked: Boolean = false,
    val lastSavedTimestamp: Long = System.currentTimeMillis(),
    val cloudSyncCode: String = "MINE" + (1000..9999).random(),
    val cloudSyncStatus: String = "Synced",
    val tapPowerMultiplier: Double = 1.0,
    val globalProductionMultiplier: Double = 1.0
)
