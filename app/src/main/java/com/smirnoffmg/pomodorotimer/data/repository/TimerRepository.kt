package com.smirnoffmg.pomodorotimer.data.repository

import com.smirnoffmg.pomodorotimer.data.db.TimerRecord
import com.smirnoffmg.pomodorotimer.data.db.TimerRecordDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TimerRepository @Inject constructor(
    private val timerRecordDao: TimerRecordDao
) {
    fun getAllTimerRecords(): Flow<List<TimerRecord>> = timerRecordDao.getAll()

    suspend fun insertTimerRecord(record: TimerRecord) {
        timerRecordDao.insert(record)
    }
}
