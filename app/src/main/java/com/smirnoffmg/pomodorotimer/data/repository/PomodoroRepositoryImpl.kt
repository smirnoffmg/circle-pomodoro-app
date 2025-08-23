package com.smirnoffmg.pomodorotimer.data.repository

import com.smirnoffmg.pomodorotimer.data.local.db.dao.PomodoroSessionDao
import com.smirnoffmg.pomodorotimer.data.mapper.toDomain
import com.smirnoffmg.pomodorotimer.data.mapper.toEntity
import com.smirnoffmg.pomodorotimer.domain.model.DailyStatistics
import com.smirnoffmg.pomodorotimer.domain.model.MonthlyStatistics
import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession
import com.smirnoffmg.pomodorotimer.domain.model.SessionType
import com.smirnoffmg.pomodorotimer.domain.model.SessionTypeStatistics
import com.smirnoffmg.pomodorotimer.domain.model.WeeklyStatistics
import com.smirnoffmg.pomodorotimer.domain.repository.PomodoroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PomodoroRepositoryImpl
    @Inject
    constructor(
        private val pomodoroSessionDao: PomodoroSessionDao,
    ) : PomodoroRepository {
        // Basic CRUD operations
        override fun getAllSessions(): Flow<List<PomodoroSession>> =
            pomodoroSessionDao.getAllSessions().map { sessions ->
                sessions.map { it.toDomain() }
            }

        override suspend fun getSessionById(sessionId: Long): PomodoroSession? = pomodoroSessionDao.getSessionById(sessionId)?.toDomain()

        override suspend fun insertSession(session: PomodoroSession): Long = pomodoroSessionDao.insertSession(session.toEntity())

        override suspend fun updateSession(session: PomodoroSession) {
            val updatedSession = session.copy(updatedAt = System.currentTimeMillis())
            pomodoroSessionDao.updateSession(updatedSession.toEntity())
        }

        override suspend fun updateSessionCompletion(
            sessionId: Long,
            endTime: Long,
            isCompleted: Boolean,
        ) = pomodoroSessionDao.updateSessionCompletion(sessionId, endTime, isCompleted)

        override suspend fun deleteSession(sessionId: Long) = pomodoroSessionDao.deleteSessionById(sessionId)

        override suspend fun deleteAllSessions() = pomodoroSessionDao.deleteAllSessions()

        // Daily aggregation
        override suspend fun getSessionsByDate(date: Long): List<PomodoroSession> =
            pomodoroSessionDao.getSessionsByDate(date).map { it.toDomain() }

        override fun getSessionsByDateFlow(date: Long): Flow<List<PomodoroSession>> =
            pomodoroSessionDao.getSessionsByDateFlow(date).map { sessions ->
                sessions.map { it.toDomain() }
            }

        override suspend fun getDailyStatistics(date: Long): DailyStatistics {
            val sessions = getSessionsByDate(date)
            val completedSessions = sessions.filter { it.isCompleted }
            
            val workSessions = completedSessions.count { it.type == SessionType.WORK }
            val shortBreakSessions = completedSessions.count { it.type == SessionType.SHORT_BREAK }
            val longBreakSessions = completedSessions.count { it.type == SessionType.LONG_BREAK }
            
            val totalWorkDuration =
                completedSessions
                    .filter { it.type == SessionType.WORK }
                    .sumOf { it.duration }
            val totalBreakDuration =
                completedSessions
                    .filter { it.type == SessionType.SHORT_BREAK || it.type == SessionType.LONG_BREAK }
                    .sumOf { it.duration }

            return DailyStatistics(
                date = date,
                totalSessions = sessions.size,
                completedSessions = completedSessions.size,
                workSessions = workSessions,
                shortBreakSessions = shortBreakSessions,
                longBreakSessions = longBreakSessions,
                totalWorkDuration = totalWorkDuration,
                totalBreakDuration = totalBreakDuration
            )
        }

        // Weekly aggregation
        override suspend fun getSessionsByWeek(
            weekStart: Long,
            weekEnd: Long
        ): List<PomodoroSession> = pomodoroSessionDao.getSessionsByWeek(weekStart, weekEnd).map { it.toDomain() }

        override fun getSessionsByWeekFlow(
            weekStart: Long,
            weekEnd: Long
        ): Flow<List<PomodoroSession>> =
            pomodoroSessionDao.getSessionsByWeekFlow(weekStart, weekEnd).map { sessions ->
                sessions.map { it.toDomain() }
            }

        override suspend fun getWeeklyStatistics(
            weekStart: Long,
            weekEnd: Long
        ): WeeklyStatistics {
            val sessions = getSessionsByWeek(weekStart, weekEnd)
            val completedSessions = sessions.filter { it.isCompleted }
            
            val workSessions = completedSessions.count { it.type == SessionType.WORK }
            val shortBreakSessions = completedSessions.count { it.type == SessionType.SHORT_BREAK }
            val longBreakSessions = completedSessions.count { it.type == SessionType.LONG_BREAK }
            
            val totalWorkDuration =
                completedSessions
                    .filter { it.type == SessionType.WORK }
                    .sumOf { it.duration }
            val totalBreakDuration =
                completedSessions
                    .filter { it.type == SessionType.SHORT_BREAK || it.type == SessionType.LONG_BREAK }
                    .sumOf { it.duration }

            val weekDurationInDays = ((weekEnd - weekStart) / (24 * 60 * 60 * 1000L)).coerceAtLeast(1).toInt()
            val averageDailyWorkTime = if (weekDurationInDays > 0) totalWorkDuration / weekDurationInDays else 0L

            return WeeklyStatistics(
                weekStart = weekStart,
                weekEnd = weekEnd,
                totalSessions = sessions.size,
                completedSessions = completedSessions.size,
                workSessions = workSessions,
                shortBreakSessions = shortBreakSessions,
                longBreakSessions = longBreakSessions,
                totalWorkDuration = totalWorkDuration,
                totalBreakDuration = totalBreakDuration,
                averageDailyWorkTime = averageDailyWorkTime
            )
        }

        // Monthly aggregation
        override suspend fun getSessionsByMonth(
            monthStart: Long,
            monthEnd: Long
        ): List<PomodoroSession> = pomodoroSessionDao.getSessionsByMonth(monthStart, monthEnd).map { it.toDomain() }

        override fun getSessionsByMonthFlow(
            monthStart: Long,
            monthEnd: Long
        ): Flow<List<PomodoroSession>> =
            pomodoroSessionDao.getSessionsByMonthFlow(monthStart, monthEnd).map { sessions ->
                sessions.map { it.toDomain() }
            }

        override suspend fun getMonthlyStatistics(
            monthStart: Long,
            monthEnd: Long
        ): MonthlyStatistics {
            val sessions = getSessionsByMonth(monthStart, monthEnd)
            val completedSessions = sessions.filter { it.isCompleted }
            
            val workSessions = completedSessions.count { it.type == SessionType.WORK }
            val shortBreakSessions = completedSessions.count { it.type == SessionType.SHORT_BREAK }
            val longBreakSessions = completedSessions.count { it.type == SessionType.LONG_BREAK }
            
            val totalWorkDuration =
                completedSessions
                    .filter { it.type == SessionType.WORK }
                    .sumOf { it.duration }
            val totalBreakDuration =
                completedSessions
                    .filter { it.type == SessionType.SHORT_BREAK || it.type == SessionType.LONG_BREAK }
                    .sumOf { it.duration }

            val monthDurationInDays = ((monthEnd - monthStart) / (24 * 60 * 60 * 1000L)).coerceAtLeast(1).toInt()
            val averageDailyWorkTime = if (monthDurationInDays > 0) totalWorkDuration / monthDurationInDays else 0L

            return MonthlyStatistics(
                monthStart = monthStart,
                monthEnd = monthEnd,
                totalSessions = sessions.size,
                completedSessions = completedSessions.size,
                workSessions = workSessions,
                shortBreakSessions = shortBreakSessions,
                longBreakSessions = longBreakSessions,
                totalWorkDuration = totalWorkDuration,
                totalBreakDuration = totalBreakDuration,
                averageDailyWorkTime = averageDailyWorkTime
            )
        }

        // Statistics and analytics
        override suspend fun getSessionTypeStatistics(
            fromDate: Long,
            sessionType: SessionType
        ): SessionTypeStatistics {
            val sessionTypeString =
                when (sessionType) {
                    SessionType.WORK -> "WORK"
                    SessionType.SHORT_BREAK -> "SHORT_BREAK"
                    SessionType.LONG_BREAK -> "LONG_BREAK"
                }
            
            val totalCount =
                pomodoroSessionDao.getCompletedSessionsCountByWeekAndType(
                    fromDate, Long.MAX_VALUE, sessionTypeString
                )
            val totalDuration =
                pomodoroSessionDao.getTotalDurationByWeekAndType(
                    fromDate, Long.MAX_VALUE, sessionTypeString
                ) ?: 0L
            val averageDuration = pomodoroSessionDao.getAverageDurationByType(sessionTypeString, fromDate) ?: 0.0

            return SessionTypeStatistics(
                type = sessionType,
                totalCount = totalCount,
                completedCount = totalCount, // All counted sessions are completed
                totalDuration = totalDuration,
                averageDuration = averageDuration
            )
        }

        override suspend fun getTotalCompletedSessionsCount(fromDate: Long): Int =
            pomodoroSessionDao.getTotalCompletedSessionsCount(fromDate)

        override suspend fun getAverageDurationByType(
            sessionType: SessionType,
            fromDate: Long
        ): Double {
            val sessionTypeString =
                when (sessionType) {
                    SessionType.WORK -> "WORK"
                    SessionType.SHORT_BREAK -> "SHORT_BREAK"
                    SessionType.LONG_BREAK -> "LONG_BREAK"
                }
            return pomodoroSessionDao.getAverageDurationByType(sessionTypeString, fromDate) ?: 0.0
        }

        // Data management
        override suspend fun deleteSessionsBeforeDate(beforeDate: Long) = pomodoroSessionDao.deleteSessionsBeforeDate(beforeDate)
    }
