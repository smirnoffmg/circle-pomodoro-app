package com.smirnoffmg.pomodorotimer.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smirnoffmg.pomodorotimer.data.local.db.dao.PomodoroSessionDao
import com.smirnoffmg.pomodorotimer.data.local.db.dao.TimerRecordDao
import com.smirnoffmg.pomodorotimer.data.local.db.entity.PomodoroSessionEntity
import com.smirnoffmg.pomodorotimer.data.local.db.entity.TimerRecordEntity

@Database(
    entities = [
        PomodoroSessionEntity::class,
        TimerRecordEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class PomodoroDatabase : RoomDatabase() {
    abstract fun pomodoroSessionDao(): PomodoroSessionDao

    abstract fun timerRecordDao(): TimerRecordDao
}
