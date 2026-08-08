package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mine_shafts")
data class MineShaftEntity(
    @PrimaryKey val shaftIndex: Int,
    val isUnlocked: Boolean = false,
    val minerLevel: Int = 1,
    val minerCount: Int = 1,
    val miningSpeed: Double = 1.0,
    val minerCapacity: Double = 20.0,
    val hasManager: Boolean = false,
    val managerName: String = "Miner Jack",
    val oreAccumulated: Double = 0.0,
    val unlockCost: Double = 100.0,
    val baseOutput: Double = 5.0
)
