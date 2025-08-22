package com.smirnoffmg.pomodorotimer.domain.model

data class DailyStatistics(
    val date: Long,
    val totalSessions: Int,
    val completedSessions: Int,
    val workSessions: Int,
    val shortBreakSessions: Int,
    val longBreakSessions: Int,
    val totalWorkDuration: Long,
    val totalBreakDuration: Long,
    val completionRate: Float = if (totalSessions > 0) completedSessions.toFloat() / totalSessions else 0f
)

data class WeeklyStatistics(
    val weekStart: Long,
    val weekEnd: Long,
    val totalSessions: Int,
    val completedSessions: Int,
    val workSessions: Int,
    val shortBreakSessions: Int,
    val longBreakSessions: Int,
    val totalWorkDuration: Long,
    val totalBreakDuration: Long,
    val averageDailyWorkTime: Long,
    val completionRate: Float = if (totalSessions > 0) completedSessions.toFloat() / totalSessions else 0f
)

data class MonthlyStatistics(
    val monthStart: Long,
    val monthEnd: Long,
    val totalSessions: Int,
    val completedSessions: Int,
    val workSessions: Int,
    val shortBreakSessions: Int,
    val longBreakSessions: Int,
    val totalWorkDuration: Long,
    val totalBreakDuration: Long,
    val averageDailyWorkTime: Long,
    val completionRate: Float = if (totalSessions > 0) completedSessions.toFloat() / totalSessions else 0f
)

data class SessionTypeStatistics(
    val type: SessionType,
    val totalCount: Int,
    val completedCount: Int,
    val totalDuration: Long,
    val averageDuration: Double,
    val completionRate: Float = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
)

data class StreakStatistics(
    val currentStreak: Int,
    val longestStreak: Int,
    val streakType: SessionType
)