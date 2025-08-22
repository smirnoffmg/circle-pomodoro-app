package com.smirnoffmg.pomodorotimer.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smirnoffmg.pomodorotimer.data.local.db.entity.PomodoroSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSessionEntity): Long

    @Update
    suspend fun updateSession(session: PomodoroSessionEntity)

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): PomodoroSessionEntity?

    @Query("UPDATE pomodoro_sessions SET endTime = :endTime, isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun updateSessionCompletion(
        sessionId: Long,
        endTime: Long,
        isCompleted: Boolean,
        updatedAt: Long = System.currentTimeMillis(),
    )

    // Daily aggregation queries
    @Query("""
        SELECT * FROM pomodoro_sessions 
        WHERE DATE(startTime / 1000, 'unixepoch') = DATE(:date / 1000, 'unixepoch')
        ORDER BY startTime ASC
    """)
    suspend fun getSessionsByDate(date: Long): List<PomodoroSessionEntity>

    @Query("""
        SELECT * FROM pomodoro_sessions 
        WHERE DATE(startTime / 1000, 'unixepoch') = DATE(:date / 1000, 'unixepoch')
        ORDER BY startTime ASC
    """)
    fun getSessionsByDateFlow(date: Long): Flow<List<PomodoroSessionEntity>>

    @Query("""
        SELECT COUNT(*) FROM pomodoro_sessions 
        WHERE DATE(startTime / 1000, 'unixepoch') = DATE(:date / 1000, 'unixepoch')
        AND isCompleted = 1
        AND type = :sessionType
    """)
    suspend fun getCompletedSessionsCountByDateAndType(
        date: Long,
        sessionType: String
    ): Int

    @Query("""
        SELECT SUM(duration) FROM pomodoro_sessions 
        WHERE DATE(startTime / 1000, 'unixepoch') = DATE(:date / 1000, 'unixepoch')
        AND isCompleted = 1
        AND type = :sessionType
    """)
    suspend fun getTotalDurationByDateAndType(
        date: Long,
        sessionType: String
    ): Long?

    // Weekly aggregation queries
    @Query("""
        SELECT * FROM pomodoro_sessions 
        WHERE startTime >= :weekStart AND startTime < :weekEnd
        ORDER BY startTime ASC
    """)
    suspend fun getSessionsByWeek(
        weekStart: Long,
        weekEnd: Long
    ): List<PomodoroSessionEntity>

    @Query("""
        SELECT * FROM pomodoro_sessions 
        WHERE startTime >= :weekStart AND startTime < :weekEnd
        ORDER BY startTime ASC
    """)
    fun getSessionsByWeekFlow(
        weekStart: Long,
        weekEnd: Long
    ): Flow<List<PomodoroSessionEntity>>

    @Query("""
        SELECT COUNT(*) FROM pomodoro_sessions 
        WHERE startTime >= :weekStart AND startTime < :weekEnd
        AND isCompleted = 1
        AND type = :sessionType
    """)
    suspend fun getCompletedSessionsCountByWeekAndType(
        weekStart: Long,
        weekEnd: Long,
        sessionType: String
    ): Int

    @Query("""
        SELECT SUM(duration) FROM pomodoro_sessions 
        WHERE startTime >= :weekStart AND startTime < :weekEnd
        AND isCompleted = 1
        AND type = :sessionType
    """)
    suspend fun getTotalDurationByWeekAndType(
        weekStart: Long,
        weekEnd: Long,
        sessionType: String
    ): Long?

    // Monthly aggregation queries
    @Query("""
        SELECT * FROM pomodoro_sessions 
        WHERE startTime >= :monthStart AND startTime < :monthEnd
        ORDER BY startTime ASC
    """)
    suspend fun getSessionsByMonth(
        monthStart: Long,
        monthEnd: Long
    ): List<PomodoroSessionEntity>

    @Query("""
        SELECT * FROM pomodoro_sessions 
        WHERE startTime >= :monthStart AND startTime < :monthEnd
        ORDER BY startTime ASC
    """)
    fun getSessionsByMonthFlow(
        monthStart: Long,
        monthEnd: Long
    ): Flow<List<PomodoroSessionEntity>>

    // Statistics queries
    @Query("""
        SELECT AVG(duration) FROM pomodoro_sessions 
        WHERE isCompleted = 1 
        AND type = :sessionType
        AND startTime >= :fromDate
    """)
    suspend fun getAverageDurationByType(
        sessionType: String,
        fromDate: Long
    ): Double?

    @Query("""
        SELECT COUNT(*) FROM pomodoro_sessions 
        WHERE isCompleted = 1
        AND startTime >= :fromDate
    """)
    suspend fun getTotalCompletedSessionsCount(fromDate: Long): Int

    @Query("""
        SELECT type, COUNT(*) as count FROM pomodoro_sessions 
        WHERE isCompleted = 1
        AND startTime >= :fromDate
        GROUP BY type
    """)
    suspend fun getSessionCountsByType(fromDate: Long): List<SessionTypeCount>

    // Delete operations
    @Query("DELETE FROM pomodoro_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Query("DELETE FROM pomodoro_sessions WHERE startTime < :beforeDate")
    suspend fun deleteSessionsBeforeDate(beforeDate: Long)

    @Query("DELETE FROM pomodoro_sessions")
    suspend fun deleteAllSessions()
}

data class SessionTypeCount(
    val type: String,
    val count: Int
)
