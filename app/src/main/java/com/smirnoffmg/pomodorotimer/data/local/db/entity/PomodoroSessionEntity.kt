package com.smirnoffmg.pomodorotimer.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pomodoro_sessions",
    indices = [
        Index(value = ["startTime"]),
        Index(value = ["type"]),
        Index(value = ["isCompleted"]),
        Index(value = ["startTime", "type"])
    ]
)
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val duration: Long,
    val isCompleted: Boolean,
    val type: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
