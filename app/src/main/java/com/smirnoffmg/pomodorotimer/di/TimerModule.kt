package com.smirnoffmg.pomodorotimer.di

import com.smirnoffmg.pomodorotimer.timer.AlarmScheduler
import com.smirnoffmg.pomodorotimer.timer.AlarmSchedulerImpl
import com.smirnoffmg.pomodorotimer.timer.HealthMonitor
import com.smirnoffmg.pomodorotimer.timer.HealthMonitorImpl
import com.smirnoffmg.pomodorotimer.timer.TimerStateManager
import com.smirnoffmg.pomodorotimer.timer.TimerStateManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TimerModule {
    @Binds
    @Singleton
    abstract fun bindTimerStateManager(timerStateManagerImpl: TimerStateManagerImpl): TimerStateManager

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(alarmSchedulerImpl: AlarmSchedulerImpl): AlarmScheduler

    @Binds
    @Singleton
    abstract fun bindHealthMonitor(healthMonitorImpl: HealthMonitorImpl): HealthMonitor
}
