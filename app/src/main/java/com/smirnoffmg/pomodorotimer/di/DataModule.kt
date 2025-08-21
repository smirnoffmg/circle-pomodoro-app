package com.smirnoffmg.pomodorotimer.di

import android.content.Context
import androidx.room.Room
import com.smirnoffmg.pomodorotimer.data.db.AppDatabase
import com.smirnoffmg.pomodorotimer.data.db.TimerRecordDao
import com.smirnoffmg.pomodorotimer.data.repository.TimerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pomodoro.db"
        ).build()
    }

    @Provides
    fun provideTimerRecordDao(appDatabase: AppDatabase): TimerRecordDao {
        return appDatabase.timerRecordDao()
    }

    @Provides
    fun provideTimerRepository(timerRecordDao: TimerRecordDao): TimerRepository {
        return TimerRepository(timerRecordDao)
    }
}
