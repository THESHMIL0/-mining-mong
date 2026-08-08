package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query("SELECT * FROM game_state WHERE id = 1")
    fun getGameState(): Flow<GameStateEntity?>

    @Query("SELECT * FROM game_state WHERE id = 1")
    suspend fun getGameStateOnce(): GameStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameState(state: GameStateEntity)

    @Query("SELECT * FROM mine_shafts ORDER BY shaftIndex ASC")
    fun getAllMineShafts(): Flow<List<MineShaftEntity>>

    @Query("SELECT * FROM mine_shafts ORDER BY shaftIndex ASC")
    suspend fun getAllMineShaftsOnce(): List<MineShaftEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMineShaft(shaft: MineShaftEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAllMineShafts(shafts: List<MineShaftEntity>)

    @Query("SELECT * FROM leaderboard_entries ORDER BY totalGold DESC")
    fun getLeaderboards(): Flow<List<LeaderboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLeaderboards(entries: List<LeaderboardEntity>)

    @Query("SELECT * FROM game_settings WHERE id = 1")
    fun getSettings(): Flow<GameSettingsEntity?>

    @Query("SELECT * FROM game_settings WHERE id = 1")
    suspend fun getSettingsOnce(): GameSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: GameSettingsEntity)

    @Query("DELETE FROM mine_shafts")
    suspend fun clearMineShafts()
}
