package com.smirnoffmg.pomodorotimer.di

import android.content.Context
import androidx.room.Room
import com.smirnoffmg.pomodorotimer.data.local.db.PomodoroDatabase
import com.smirnoffmg.pomodorotimer.data.local.db.dao.PomodoroSessionDao
import com.smirnoffmg.pomodorotimer.data.repository.PomodoroRepositoryImpl
import com.smirnoffmg.pomodorotimer.domain.repository.PomodoroRepository
import com.smirnoffmg.pomodorotimer.service.TimerServiceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database module following Single Responsibility Principle.
 * Only handles database-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "pomodoro_database"

    /**
     * Provides Room database instance following KISS principle.
     * Uses simple configuration with sensible defaults.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): PomodoroDatabase = Room
        .databaseBuilder(
            context,
            PomodoroDatabase::class.java,
            DATABASE_NAME,
        )
        .addMigrations(PomodoroDatabase.MIGRATION_1_2, PomodoroDatabase.MIGRATION_2_3)
        .build()

    /**
     * Provides PomodoroSessionDao following Single Responsibility Principle.
     * Only handles session data access.
     */
    @Provides
    @Singleton
    fun providePomodoroSessionDao(database: PomodoroDatabase): PomodoroSessionDao =
        database.pomodoroSessionDao()

    /**
     * Provides PomodoroRepository following Dependency Inversion Principle.
     * Depends on abstraction (PomodoroSessionDao) rather than concrete implementation.
     */
    @Provides
    @Singleton
    fun providePomodoroRepository(dao: PomodoroSessionDao): PomodoroRepository =
        PomodoroRepositoryImpl(dao)

    /**
     * Provides TimerServiceManager following Single Responsibility Principle.
     * Only handles timer service lifecycle management.
     */
    @Provides
    @Singleton
    fun provideTimerServiceManager(
        @ApplicationContext context: Context
    ): TimerServiceManager = TimerServiceManager(context)
}
