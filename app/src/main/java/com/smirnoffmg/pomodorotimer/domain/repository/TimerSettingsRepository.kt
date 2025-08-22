package com.smirnoffmg.pomodorotimer.domain.repository

import com.smirnoffmg.pomodorotimer.domain.model.TimerSettings
import kotlinx.coroutines.flow.Flow

interface TimerSettingsRepository {
    fun getSettings(): Flow<TimerSettings?>

    suspend fun getSettingsSync(): TimerSettings?

    suspend fun saveSettings(settings: TimerSettings)

    suspend fun updateWorkDuration(minutes: Int)

    suspend fun updateShortBreakDuration(minutes: Int)

    suspend fun updateLongBreakDuration(minutes: Int)

    suspend fun updateSessionsBeforeLongBreak(sessions: Int)

    suspend fun resetToDefaults()
}
