package com.smirnoffmg.pomodorotimer.di

import android.content.Context
import androidx.room.Room
import com.smirnoffmg.pomodorotimer.data.local.db.PomodoroDatabase
import com.smirnoffmg.pomodorotimer.data.local.db.dao.PomodoroSessionDao
import com.smirnoffmg.pomodorotimer.data.local.db.dao.TimerRecordDao
import com.smirnoffmg.pomodorotimer.data.repository.PomodoroRepositoryImpl
import com.smirnoffmg.pomodorotimer.data.repository.TimerRepository
import com.smirnoffmg.pomodorotimer.domain.repository.PomodoroRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): PomodoroDatabase =
        Room
            .databaseBuilder(
                context,
                PomodoroDatabase::class.java,
                "pomodoro_database",
            ).build()

    @Provides
    @Singleton
    fun providePomodoroSessionDao(database: PomodoroDatabase) = database.pomodoroSessionDao()

    @Provides
    @Singleton
    fun provideTimerRecordDao(database: PomodoroDatabase) = database.timerRecordDao()

    @Provides
    @Singleton
    fun provideTimerRepository(dao: TimerRecordDao) = TimerRepository(dao)

    @Provides
    @Singleton
    fun providePomodoroRepository(dao: PomodoroSessionDao): PomodoroRepository = PomodoroRepositoryImpl(dao)
}
