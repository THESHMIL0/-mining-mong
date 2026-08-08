package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_settings")
data class GameSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isDarkMode: Boolean = true,
    val isSfxEnabled: Boolean = true,
    val isMusicEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val particleQuality: String = "HIGH", // HIGH, BALANCED, LOW
    val menuTransitionSpeed: Float = 1.0f,
    val highRefreshRateMode: Boolean = true,
    val doubleTapBoost: Boolean = true,
    val retroCrtTheme: Boolean = false
)
