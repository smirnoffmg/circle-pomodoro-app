package com.smirnoffmg.pomodorotimer.data.repository

import com.smirnoffmg.pomodorotimer.domain.exception.DatabaseException
import com.smirnoffmg.pomodorotimer.domain.exception.safeSuspendDbCall
import com.smirnoffmg.pomodorotimer.domain.model.DailyStatistics
import com.smirnoffmg.pomodorotimer.domain.model.MonthlyStatistics
import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession
import com.smirnoffmg.pomodorotimer.domain.model.SessionType
import com.smirnoffmg.pomodorotimer.domain.model.SessionTypeStatistics
import com.smirnoffmg.pomodorotimer.domain.model.WeeklyStatistics
import com.smirnoffmg.pomodorotimer.domain.repository.PomodoroRepository
import com.smirnoffmg.pomodorotimer.domain.validation.SessionValidator
import com.smirnoffmg.pomodorotimer.domain.validation.ValidationResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafePomodoroRepository
    @Inject
    constructor(
        private val repository: PomodoroRepository,
    ) {
        suspend fun insertSessionSafe(session: PomodoroSession): Result<Long> {
            // Validate session before insertion
            val validationResult = SessionValidator.validate(session)
            if (validationResult is ValidationResult.Invalid) {
                return Result.failure(
                    DatabaseException.InvalidSessionData(
                        validationResult.errors.joinToString(", ") { it.message },
                    ),
                )
            }

            return safeSuspendDbCall { repository.insertSession(session) }
        }

        suspend fun updateSessionSafe(session: PomodoroSession): Result<Unit> {
            val validationResult = SessionValidator.validate(session)
            if (validationResult is ValidationResult.Invalid) {
                return Result.failure(
                    DatabaseException.InvalidSessionData(
                        validationResult.errors.joinToString(", ") { it.message },
                    ),
                )
            }

            return safeSuspendDbCall { repository.updateSession(session) }
        }

        suspend fun getSessionByIdSafe(sessionId: Long): Result<PomodoroSession> =
            safeSuspendDbCall {
                repository.getSessionById(sessionId)
                    ?: throw DatabaseException.SessionNotFound(sessionId)
            }

        suspend fun completeSessionSafe(sessionId: Long): Result<Unit> =
            safeSuspendDbCall {
                val session =
                    repository.getSessionById(sessionId)
                        ?: throw DatabaseException.SessionNotFound(sessionId)

                if (session.isCompleted) {
                    throw DatabaseException.InvalidSessionData("Session is already completed")
                }

                val endTime = System.currentTimeMillis()
                repository.updateSessionCompletion(sessionId, endTime, true)
            }

        suspend fun deleteSessionSafe(sessionId: Long): Result<Unit> =
            safeSuspendDbCall {
                // Verify session exists before deletion
                repository.getSessionById(sessionId)
                    ?: throw DatabaseException.SessionNotFound(sessionId)

                repository.deleteSession(sessionId)
            }

        suspend fun getDailyStatisticsSafe(date: Long): Result<DailyStatistics> = safeSuspendDbCall { repository.getDailyStatistics(date) }

        suspend fun getWeeklyStatisticsSafe(
            weekStart: Long,
            weekEnd: Long,
        ): Result<WeeklyStatistics> {
            if (weekEnd <= weekStart) {
                return Result.failure(
                    DatabaseException.InvalidSessionData("Week end must be after week start"),
                )
            }

            return safeSuspendDbCall { repository.getWeeklyStatistics(weekStart, weekEnd) }
        }

        suspend fun getMonthlyStatisticsSafe(
            monthStart: Long,
            monthEnd: Long,
        ): Result<MonthlyStatistics> {
            if (monthEnd <= monthStart) {
                return Result.failure(
                    DatabaseException.InvalidSessionData("Month end must be after month start"),
                )
            }

            return safeSuspendDbCall { repository.getMonthlyStatistics(monthStart, monthEnd) }
        }

        suspend fun getSessionTypeStatisticsSafe(
            fromDate: Long,
            sessionType: SessionType,
        ): Result<SessionTypeStatistics> =
            safeSuspendDbCall {
                repository.getSessionTypeStatistics(fromDate, sessionType)
            }

        suspend fun getTotalCompletedSessionsCountSafe(fromDate: Long): Result<Int> =
            safeSuspendDbCall {
                repository
                    .getTotalCompletedSessionsCount(fromDate)
            }

        suspend fun getAverageDurationByTypeSafe(
            sessionType: SessionType,
            fromDate: Long,
        ): Result<Double> =
            safeSuspendDbCall {
                repository.getAverageDurationByType(sessionType, fromDate)
            }

        suspend fun deleteSessionsBeforeDateSafe(beforeDate: Long): Result<Unit> {
            val cutoffTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000) // 30 days ago minimum
            if (beforeDate > cutoffTime) {
                return Result.failure(
                    DatabaseException.InvalidSessionData(
                        "Cannot delete sessions newer than 30 days for safety",
                    ),
                )
            }

            return safeSuspendDbCall { repository.deleteSessionsBeforeDate(beforeDate) }
        }

        suspend fun deleteAllSessionsSafe(): Result<Unit> = safeSuspendDbCall { repository.deleteAllSessions() }

        // Flow methods (these handle errors in the flow itself)
        fun getAllSessionsFlow(): Flow<List<PomodoroSession>> = repository.getAllSessions()

        fun getSessionsByDateFlow(date: Long): Flow<List<PomodoroSession>> = repository.getSessionsByDateFlow(date)

        fun getSessionsByWeekFlow(
            weekStart: Long,
            weekEnd: Long,
        ): Flow<List<PomodoroSession>> = repository.getSessionsByWeekFlow(weekStart, weekEnd)

        fun getSessionsByMonthFlow(
            monthStart: Long,
            monthEnd: Long,
        ): Flow<List<PomodoroSession>> = repository.getSessionsByMonthFlow(monthStart, monthEnd)
    }
