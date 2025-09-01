package com.smirnoffmg.pomodorotimer.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smirnoffmg.pomodorotimer.data.local.db.dao.PomodoroSessionDao
import com.smirnoffmg.pomodorotimer.data.local.db.dao.TimerSettingsDao
import com.smirnoffmg.pomodorotimer.data.local.db.entity.PomodoroSessionEntity
import com.smirnoffmg.pomodorotimer.data.local.db.entity.TimerSettingsEntity

@Database(
    entities = [
        PomodoroSessionEntity::class,
        TimerSettingsEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class PomodoroDatabase : RoomDatabase() {
    abstract fun pomodoroSessionDao(): PomodoroSessionDao

    abstract fun timerSettingsDao(): TimerSettingsDao

    companion object {
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    val currentTime = System.currentTimeMillis()

                    // Add new columns to pomodoro_sessions table
                    db.execSQL("ALTER TABLE pomodoro_sessions ADD COLUMN createdAt INTEGER NOT NULL DEFAULT $currentTime")
                    db.execSQL("ALTER TABLE pomodoro_sessions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT $currentTime")

                    // Create indexes for better query performance
                    db.execSQL("CREATE INDEX index_pomodoro_sessions_startTime ON pomodoro_sessions(startTime)")
                    db.execSQL("CREATE INDEX index_pomodoro_sessions_type ON pomodoro_sessions(type)")
                    db.execSQL("CREATE INDEX index_pomodoro_sessions_isCompleted ON pomodoro_sessions(isCompleted)")
                    db.execSQL("CREATE INDEX index_pomodoro_sessions_startTime_type ON pomodoro_sessions(startTime, type)")
                }
            }

        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Remove timer_records table as it's being consolidated into pomodoro_sessions
                    db.execSQL("DROP TABLE IF EXISTS timer_records")
                }
            }

        val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    val currentTime = System.currentTimeMillis()

                    // Create timer_settings table
                    db.execSQL(
                        """
                    CREATE TABLE timer_settings (
                        id INTEGER PRIMARY KEY NOT NULL,
                        workDurationMinutes INTEGER NOT NULL DEFAULT 25,
                        shortBreakDurationMinutes INTEGER NOT NULL DEFAULT 5,
                        longBreakDurationMinutes INTEGER NOT NULL DEFAULT 15,
                        sessionsBeforeLongBreak INTEGER NOT NULL DEFAULT 4,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                """,
                    )

                    // Insert default settings
                    db.execSQL(
                        """
                    INSERT INTO timer_settings (
                        id, workDurationMinutes, shortBreakDurationMinutes, 
                        longBreakDurationMinutes, sessionsBeforeLongBreak, 
                        createdAt, updatedAt
                    ) VALUES (1, 25, 5, 15, 4, $currentTime, $currentTime)
                """,
                    )
                }
            }
    }
}
