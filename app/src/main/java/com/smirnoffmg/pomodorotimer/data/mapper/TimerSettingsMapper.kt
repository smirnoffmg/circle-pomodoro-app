package com.smirnoffmg.pomodorotimer.data.mapper

import com.smirnoffmg.pomodorotimer.data.local.db.entity.TimerSettingsEntity
import com.smirnoffmg.pomodorotimer.domain.model.TimerSettings

fun TimerSettingsEntity.toDomainModel(): TimerSettings =
    TimerSettings(
        id = id,
        workDurationMinutes = workDurationMinutes,
        shortBreakDurationMinutes = shortBreakDurationMinutes,
        longBreakDurationMinutes = longBreakDurationMinutes,
        sessionsBeforeLongBreak = sessionsBeforeLongBreak,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun TimerSettings.toEntity(): TimerSettingsEntity =
    TimerSettingsEntity(
        id = id,
        workDurationMinutes = workDurationMinutes,
        shortBreakDurationMinutes = shortBreakDurationMinutes,
        longBreakDurationMinutes = longBreakDurationMinutes,
        sessionsBeforeLongBreak = sessionsBeforeLongBreak,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
