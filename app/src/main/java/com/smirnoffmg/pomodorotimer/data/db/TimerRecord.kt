package com.smirnoffmg.pomodorotimer.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_records")
data class TimerRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val duration: Long
)
