package com.smirnoffmg.pomodorotimer.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_settings")
data class TimerSettingsEntity(
    @PrimaryKey
    val id: Int = 1, // Single settings record
    // Work session duration in minutes
    val workDurationMinutes: Int = 25,
    // Break durations in minutes
    val shortBreakDurationMinutes: Int = 5,
    val longBreakDurationMinutes: Int = 15,
    // Sessions before long break
    val sessionsBeforeLongBreak: Int = 4,
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
