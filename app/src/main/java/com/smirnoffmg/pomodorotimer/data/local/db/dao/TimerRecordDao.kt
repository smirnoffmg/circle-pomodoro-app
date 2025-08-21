package com.smirnoffmg.pomodorotimer.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smirnoffmg.pomodorotimer.data.local.db.entity.TimerRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TimerRecordEntity)

    @Query("SELECT * FROM timer_records ORDER BY startTimestamp DESC")
    fun getAll(): Flow<List<TimerRecordEntity>>
}
