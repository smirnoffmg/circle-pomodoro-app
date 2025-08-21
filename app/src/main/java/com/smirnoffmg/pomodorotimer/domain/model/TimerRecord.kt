package com.smirnoffmg.pomodorotimer.domain.model

data class TimerRecord(
    val id: Int = 0,
    val durationSeconds: Int,
    val startTimestamp: Long,
)
