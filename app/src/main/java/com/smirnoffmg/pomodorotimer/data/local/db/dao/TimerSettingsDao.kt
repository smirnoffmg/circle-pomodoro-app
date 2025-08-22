package com.smirnoffmg.pomodorotimer.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smirnoffmg.pomodorotimer.data.local.db.entity.TimerSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerSettingsDao {
    @Query("SELECT * FROM timer_settings WHERE id = 1")
    fun getSettings(): Flow<TimerSettingsEntity?>

    @Query("SELECT * FROM timer_settings WHERE id = 1")
    suspend fun getSettingsSync(): TimerSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: TimerSettingsEntity)

    @Update
    suspend fun updateSettings(settings: TimerSettingsEntity)

    @Query("UPDATE timer_settings SET workDurationMinutes = :workDuration, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateWorkDuration(
        workDuration: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE timer_settings SET shortBreakDurationMinutes = :shortBreakDuration, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateShortBreakDuration(
        shortBreakDuration: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE timer_settings SET longBreakDurationMinutes = :longBreakDuration, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateLongBreakDuration(
        longBreakDuration: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE timer_settings SET sessionsBeforeLongBreak = :sessions, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateSessionsBeforeLongBreak(
        sessions: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM timer_settings WHERE id = 1")
    suspend fun deleteSettings()
}
