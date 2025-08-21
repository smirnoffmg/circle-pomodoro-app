package com.smirnoffmg.pomodorotimer.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smirnoffmg.pomodorotimer.data.local.db.entity.PomodoroSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSessionEntity): Long

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): PomodoroSessionEntity?

    @Query("UPDATE pomodoro_sessions SET endTime = :endTime, isCompleted = :isCompleted WHERE id = :sessionId")
    suspend fun updateSessionCompletion(
        sessionId: Long,
        endTime: Long,
        isCompleted: Boolean,
    )
}
