package com.smirnoffmg.pomodorotimer.domain.model

data class PomodoroSession(
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val duration: Long,
    val isCompleted: Boolean,
    val type: SessionType,
)

enum class SessionType {
    WORK,
    SHORT_BREAK,
    LONG_BREAK,
}
