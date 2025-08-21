package com.smirnoffmg.pomodorotimer.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerRecordDao {
    @Insert
    suspend fun insert(record: TimerRecord)

    @Query("SELECT * FROM timer_records ORDER BY timestamp DESC")
    fun getAll(): Flow<List<TimerRecord>>
}
