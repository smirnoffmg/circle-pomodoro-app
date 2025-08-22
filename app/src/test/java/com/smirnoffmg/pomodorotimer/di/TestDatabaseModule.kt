package com.smirnoffmg.pomodorotimer.di

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smirnoffmg.pomodorotimer.data.local.db.PomodoroDatabase
import com.smirnoffmg.pomodorotimer.data.local.db.dao.PomodoroSessionDao
import com.smirnoffmg.pomodorotimer.data.repository.PomodoroRepositoryImpl
import com.smirnoffmg.pomodorotimer.domain.repository.PomodoroRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Test database module following DRY principle.
 * Provides in-memory database for testing to avoid file system dependencies.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    private const val TEST_DATABASE_NAME = "test_pomodoro_database"

    /**
     * Provides in-memory test database following KISS principle.
     * Uses simple in-memory configuration for fast, isolated tests.
     */
    @Provides
    @Singleton
    fun provideTestDatabase(): PomodoroDatabase = Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            PomodoroDatabase::class.java
        )
        .allowMainThreadQueries() // Allow main thread queries for testing
        .build()

    /**
     * Provides test PomodoroSessionDao following Single Responsibility Principle.
     */
    @Provides
    @Singleton
    fun provideTestPomodoroSessionDao(database: PomodoroDatabase): PomodoroSessionDao =
        database.pomodoroSessionDao()

    /**
     * Provides test PomodoroRepository following Dependency Inversion Principle.
     */
    @Provides
    @Singleton
    fun provideTestPomodoroRepository(dao: PomodoroSessionDao): PomodoroRepository =
        PomodoroRepositoryImpl(dao)
}
