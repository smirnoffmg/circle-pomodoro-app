package com.smirnoffmg.pomodorotimer.data.repository

import com.smirnoffmg.pomodorotimer.data.local.db.dao.TimerRecordDao
import com.smirnoffmg.pomodorotimer.data.local.db.entity.TimerRecordEntity
import com.smirnoffmg.pomodorotimer.data.local.db.entity.toEntity
import com.smirnoffmg.pomodorotimer.domain.model.TimerRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TimerRepository @Inject constructor(
    private val timerRecordDao: TimerRecordDao,
) {
    fun getAllTimerRecords(): Flow<List<TimerRecord>> =
        timerRecordDao.getAll().map { entities: List<TimerRecordEntity> ->
            entities.map { entity -> entity.toDomainModel() }
        }

    suspend fun insertTimerRecord(record: TimerRecord) {
        timerRecordDao.insert(record.toEntity())
    }
}
