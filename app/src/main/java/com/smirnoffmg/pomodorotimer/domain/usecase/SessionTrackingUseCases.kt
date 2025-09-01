package com.smirnoffmg.pomodorotimer.domain.usecase

import com.smirnoffmg.pomodorotimer.domain.model.DailyStatistics
import com.smirnoffmg.pomodorotimer.domain.model.MonthlyStatistics
import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession
import com.smirnoffmg.pomodorotimer.domain.model.SessionType
import com.smirnoffmg.pomodorotimer.domain.model.WeeklyStatistics
import com.smirnoffmg.pomodorotimer.domain.repository.PomodoroRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

class StartSessionUseCase
    @Inject
    constructor(
        private val repository: PomodoroRepository,
    ) {
        suspend operator fun invoke(
            sessionType: SessionType,
            duration: Long,
        ): Long {
            val currentTime = System.currentTimeMillis()
            val session =
                PomodoroSession(
                    startTime = currentTime,
                    endTime = null,
                    duration = duration,
                    isCompleted = false,
                    type = sessionType,
                    createdAt = currentTime,
                    updatedAt = currentTime,
                )
            return repository.insertSession(session)
        }
    }

class CompleteSessionUseCase
    @Inject
    constructor(
        private val repository: PomodoroRepository,
    ) {
        suspend operator fun invoke(sessionId: Long): Result<Unit> {
            return try {
                val session =
                    repository.getSessionById(sessionId)
                        ?: return Result.failure(IllegalArgumentException("Session not found"))

                if (session.isCompleted) {
                    return Result.failure(IllegalStateException("Session already completed"))
                }

                val endTime = System.currentTimeMillis()
                repository.updateSessionCompletion(sessionId, endTime, true)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

class CancelSessionUseCase
    @Inject
    constructor(
        private val repository: PomodoroRepository,
    ) {
        suspend operator fun invoke(sessionId: Long): Result<Unit> {
            return try {
                val session =
                    repository.getSessionById(sessionId)
                        ?: return Result.failure(IllegalArgumentException("Session not found"))

                if (session.isCompleted) {
                    return Result.failure(IllegalStateException("Cannot cancel completed session"))
                }

                repository.deleteSession(sessionId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

class GetDailyStatisticsUseCase
    @Inject
    constructor(
        private val repository: PomodoroRepository,
    ) {
        suspend operator fun invoke(date: Long = System.currentTimeMillis()): DailyStatistics = repository.getDailyStatistics(date)

        suspend fun getTodayStatistics(): DailyStatistics = invoke(System.currentTimeMillis())
    }

class GetWeeklyStatisticsUseCase
    @Inject
    constructor(
        private val repository: PomodoroRepository,
    ) {
        suspend operator fun invoke(weekStart: Long? = null): WeeklyStatistics {
            val calendar = Calendar.getInstance()

            val start =
                weekStart ?: run {
                    calendar.timeInMillis = System.currentTimeMillis()
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }

            calendar.timeInMillis = start
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            val end = calendar.timeInMillis

            return repository.getWeeklyStatistics(start, end)
        }
    }

class GetMonthlyStatisticsUseCase
    @Inject
    constructor(
        private val repository: PomodoroRepository,
    ) {
        suspend operator fun invoke(monthStart: Long? = null): MonthlyStatistics {
            val calendar = Calendar.getInstance()

            val start =
                monthStart ?: run {
                    calendar.timeInMillis = System.currentTimeMillis()
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }

            calendar.timeInMillis = start
            calendar.add(Calendar.MONTH, 1)
            val end = calendar.timeInMillis

            return repository.getMonthlyStatistics(start, end)
        }
    }

class GetSessionsByDateUseCase
    @Inject
    constructor(
        private val repository: PomodoroRepository,
    ) {
        fun invoke(date: Long): Flow<List<PomodoroSession>> = repository.getSessionsByDateFlow(date)

        fun getTodaySessions(): Flow<List<PomodoroSession>> = invoke(System.currentTimeMillis())
    }

class GetProductivityStreakUseCase
    @Inject
    constructor(
        private val repository: PomodoroRepository,
    ) {
        suspend operator fun invoke(sessionType: SessionType = SessionType.WORK): Int {
            val calendar = Calendar.getInstance()
            var streakDays = 0

            // Check each day backwards until we find a day with no completed sessions
            for (dayOffset in 0..30) { // Check up to 30 days back
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val dayStats = repository.getDailyStatistics(calendar.timeInMillis)
                val hasProductiveDay =
                    when (sessionType) {
                        SessionType.WORK -> dayStats.workSessions > 0
                        SessionType.SHORT_BREAK -> dayStats.shortBreakSessions > 0
                        SessionType.LONG_BREAK -> dayStats.longBreakSessions > 0
                    }

                if (hasProductiveDay) {
                    streakDays++
                } else if (dayOffset > 0) {
                    // If this is not today and no sessions found, break the streak
                    break
                }
            }

            return streakDays
        }
    }

class CleanupOldSessionsUseCase
    @Inject
    constructor(
        private val repository: PomodoroRepository,
    ) {
        suspend operator fun invoke(daysToKeep: Int = 90): Result<Unit> =
            try {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -daysToKeep)
                val cutoffDate = calendar.timeInMillis

                repository.deleteSessionsBeforeDate(cutoffDate)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
