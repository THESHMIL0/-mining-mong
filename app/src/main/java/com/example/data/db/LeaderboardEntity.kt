package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntity(
    @PrimaryKey val id: String,
    val username: String,
    val totalGold: Double,
    val prestigeLevel: Int,
    val isUser: Boolean = false,
    val badgeTitle: String = "Novice Prospector",
    val avatarColorHex: String = "#FFD700"
)
