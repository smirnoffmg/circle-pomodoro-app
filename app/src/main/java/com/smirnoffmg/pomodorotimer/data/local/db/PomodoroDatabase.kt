package com.smirnoffmg.pomodorotimer.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smirnoffmg.pomodorotimer.data.local.db.dao.PomodoroSessionDao
import com.smirnoffmg.pomodorotimer.data.local.db.entity.PomodoroSessionEntity

@Database(
    entities = [
        PomodoroSessionEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class PomodoroDatabase : RoomDatabase() {
    abstract fun pomodoroSessionDao(): PomodoroSessionDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to pomodoro_sessions table
                db.execSQL("ALTER TABLE pomodoro_sessions ADD COLUMN createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                db.execSQL("ALTER TABLE pomodoro_sessions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                
                // Create indexes for better query performance
                db.execSQL("CREATE INDEX index_pomodoro_sessions_startTime ON pomodoro_sessions(startTime)")
                db.execSQL("CREATE INDEX index_pomodoro_sessions_type ON pomodoro_sessions(type)")
                db.execSQL("CREATE INDEX index_pomodoro_sessions_isCompleted ON pomodoro_sessions(isCompleted)")
                db.execSQL("CREATE INDEX index_pomodoro_sessions_startTime_type ON pomodoro_sessions(startTime, type)")
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Remove timer_records table as it's being consolidated into pomodoro_sessions
                db.execSQL("DROP TABLE IF EXISTS timer_records")
            }
        }
    }
}
