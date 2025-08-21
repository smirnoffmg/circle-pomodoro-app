package com.smirnoffmg.pomodorotimer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TimerRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timerRecordDao(): TimerRecordDao
}
