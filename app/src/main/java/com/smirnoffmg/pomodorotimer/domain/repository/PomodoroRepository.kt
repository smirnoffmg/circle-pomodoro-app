package com.smirnoffmg.pomodorotimer.domain.repository

import com.smirnoffmg.pomodorotimer.domain.model.DailyStatistics
import com.smirnoffmg.pomodorotimer.domain.model.MonthlyStatistics
import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession
import com.smirnoffmg.pomodorotimer.domain.model.SessionType
import com.smirnoffmg.pomodorotimer.domain.model.SessionTypeStatistics
import com.smirnoffmg.pomodorotimer.domain.model.WeeklyStatistics
import kotlinx.coroutines.flow.Flow

interface PomodoroRepository {
    // Basic CRUD operations
    fun getAllSessions(): Flow<List<PomodoroSession>>

    suspend fun getSessionById(sessionId: Long): PomodoroSession?

    suspend fun insertSession(session: PomodoroSession): Long

    suspend fun updateSession(session: PomodoroSession)

    suspend fun updateSessionCompletion(
        sessionId: Long,
        endTime: Long,
        isCompleted: Boolean,
    )

    suspend fun deleteSession(sessionId: Long)

    suspend fun deleteAllSessions()

    // Daily aggregation
    suspend fun getSessionsByDate(date: Long): List<PomodoroSession>

    fun getSessionsByDateFlow(date: Long): Flow<List<PomodoroSession>>

    suspend fun getDailyStatistics(date: Long): DailyStatistics

    // Weekly aggregation  
    suspend fun getSessionsByWeek(weekStart: Long, weekEnd: Long): List<PomodoroSession>

    fun getSessionsByWeekFlow(weekStart: Long, weekEnd: Long): Flow<List<PomodoroSession>>

    suspend fun getWeeklyStatistics(weekStart: Long, weekEnd: Long): WeeklyStatistics

    // Monthly aggregation
    suspend fun getSessionsByMonth(monthStart: Long, monthEnd: Long): List<PomodoroSession>

    fun getSessionsByMonthFlow(monthStart: Long, monthEnd: Long): Flow<List<PomodoroSession>>

    suspend fun getMonthlyStatistics(monthStart: Long, monthEnd: Long): MonthlyStatistics

    // Statistics and analytics
    suspend fun getSessionTypeStatistics(fromDate: Long, sessionType: SessionType): SessionTypeStatistics

    suspend fun getTotalCompletedSessionsCount(fromDate: Long): Int

    suspend fun getAverageDurationByType(sessionType: SessionType, fromDate: Long): Double

    // Data management
    suspend fun deleteSessionsBeforeDate(beforeDate: Long)
}
