package com.smirnoffmg.pomodorotimer.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smirnoffmg.pomodorotimer.domain.model.TimerRecord

@Entity(tableName = "timer_records")
data class TimerRecordEntity(
    @PrimaryKey
    val id: Int = 0,
    val durationSeconds: Int,
    val startTimestamp: Long,
) {
    fun toDomainModel() =
        TimerRecord(
            id = id,
            durationSeconds = durationSeconds,
            startTimestamp = startTimestamp,
        )
}

fun TimerRecord.toEntity() =
    TimerRecordEntity(
        id = id,
        durationSeconds = durationSeconds,
        startTimestamp = startTimestamp,
    )
