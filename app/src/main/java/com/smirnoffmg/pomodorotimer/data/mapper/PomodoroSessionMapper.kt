package com.smirnoffmg.pomodorotimer.data.mapper

import com.smirnoffmg.pomodorotimer.data.local.db.entity.PomodoroSessionEntity
import com.smirnoffmg.pomodorotimer.domain.model.PomodoroSession
import com.smirnoffmg.pomodorotimer.domain.model.SessionType

fun PomodoroSessionEntity.toDomain() =
    PomodoroSession(
        id = id,
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        isCompleted = isCompleted,
        type =
            when (type) {
                "WORK" -> SessionType.WORK
                "SHORT_BREAK" -> SessionType.SHORT_BREAK
                "LONG_BREAK" -> SessionType.LONG_BREAK
                else -> throw IllegalArgumentException("Unknown session type: $type")
            },
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun PomodoroSession.toEntity() =
    PomodoroSessionEntity(
        id = id,
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        isCompleted = isCompleted,
        type =
            when (type) {
                SessionType.WORK -> "WORK"
                SessionType.SHORT_BREAK -> "SHORT_BREAK"
                SessionType.LONG_BREAK -> "LONG_BREAK"
            },
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
