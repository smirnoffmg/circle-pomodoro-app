package com.smirnoffmg.pomodorotimer.data.repository

import com.smirnoffmg.pomodorotimer.data.local.db.dao.TimerSettingsDao
import com.smirnoffmg.pomodorotimer.data.mapper.toDomainModel
import com.smirnoffmg.pomodorotimer.data.mapper.toEntity
import com.smirnoffmg.pomodorotimer.domain.model.TimerSettings
import com.smirnoffmg.pomodorotimer.domain.repository.TimerSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TimerSettingsRepositoryImpl
    @Inject
    constructor(
        private val timerSettingsDao: TimerSettingsDao
    ) : TimerSettingsRepository {
        override fun getSettings(): Flow<TimerSettings?> =
            timerSettingsDao.getSettings().map { entity ->
                entity?.toDomainModel()
            }

        override suspend fun getSettingsSync(): TimerSettings? = timerSettingsDao.getSettingsSync()?.toDomainModel()

        override suspend fun saveSettings(settings: TimerSettings) {
            timerSettingsDao.insertSettings(settings.toEntity())
        }

        override suspend fun updateWorkDuration(minutes: Int) {
            timerSettingsDao.updateWorkDuration(minutes)
        }

        override suspend fun updateShortBreakDuration(minutes: Int) {
            timerSettingsDao.updateShortBreakDuration(minutes)
        }

        override suspend fun updateLongBreakDuration(minutes: Int) {
            timerSettingsDao.updateLongBreakDuration(minutes)
        }

        override suspend fun updateSessionsBeforeLongBreak(sessions: Int) {
            timerSettingsDao.updateSessionsBeforeLongBreak(sessions)
        }

        override suspend fun resetToDefaults() {
            val defaultSettings = TimerSettings.getDefaultSettings()
            timerSettingsDao.insertSettings(defaultSettings.toEntity())
        }
    }
