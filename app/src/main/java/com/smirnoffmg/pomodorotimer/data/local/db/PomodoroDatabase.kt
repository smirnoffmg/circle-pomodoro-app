package com.smirnoffmg.pomodorotimer.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smirnoffmg.pomodorotimer.data.local.db.dao.PomodoroSessionDao
import com.smirnoffmg.pomodorotimer.data.local.db.dao.TimerRecordDao
import com.smirnoffmg.pomodorotimer.data.local.db.entity.PomodoroSessionEntity
import com.smirnoffmg.pomodorotimer.data.local.db.entity.TimerRecordEntity

@Database(
    entities = [
        PomodoroSessionEntity::class,
        TimerRecordEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class PomodoroDatabase : RoomDatabase() {
    abstract fun pomodoroSessionDao(): PomodoroSessionDao

    abstract fun timerRecordDao(): TimerRecordDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to pomodoro_sessions table
                database.execSQL("ALTER TABLE pomodoro_sessions ADD COLUMN createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                database.execSQL("ALTER TABLE pomodoro_sessions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                
                // Create indexes for better query performance
                database.execSQL("CREATE INDEX index_pomodoro_sessions_startTime ON pomodoro_sessions(startTime)")
                database.execSQL("CREATE INDEX index_pomodoro_sessions_type ON pomodoro_sessions(type)")
                database.execSQL("CREATE INDEX index_pomodoro_sessions_isCompleted ON pomodoro_sessions(isCompleted)")
                database.execSQL("CREATE INDEX index_pomodoro_sessions_startTime_type ON pomodoro_sessions(startTime, type)")
            }
        }
    }
}
