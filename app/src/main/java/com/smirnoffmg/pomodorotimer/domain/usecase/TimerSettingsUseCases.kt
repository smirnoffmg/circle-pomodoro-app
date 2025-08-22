package com.smirnoffmg.pomodorotimer.domain.usecase

import com.smirnoffmg.pomodorotimer.domain.model.TimerSettings
import com.smirnoffmg.pomodorotimer.domain.repository.TimerSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTimerSettingsUseCase
    @Inject
    constructor(
        private val repository: TimerSettingsRepository
    ) {
        operator fun invoke(): Flow<TimerSettings?> = repository.getSettings()
    }

class SaveTimerSettingsUseCase
    @Inject
    constructor(
        private val repository: TimerSettingsRepository
    ) {
        suspend operator fun invoke(settings: TimerSettings): Result<Unit> {
            return try {
                if (!settings.isValid()) {
                    return Result.failure(IllegalArgumentException("Invalid timer settings"))
                }
                repository.saveSettings(settings)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

class UpdateWorkDurationUseCase
    @Inject
    constructor(
        private val repository: TimerSettingsRepository
    ) {
        suspend operator fun invoke(minutes: Int): Result<Unit> {
            return try {
                if (minutes !in TimerSettings.MIN_WORK_DURATION..TimerSettings.MAX_WORK_DURATION) {
                    return Result
                        .failure(IllegalArgumentException("Work duration must be between ${TimerSettings.MIN_WORK_DURATION} and ${TimerSettings.MAX_WORK_DURATION} minutes"))
                }
                repository.updateWorkDuration(minutes)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

class UpdateShortBreakDurationUseCase
    @Inject
    constructor(
        private val repository: TimerSettingsRepository
    ) {
        suspend operator fun invoke(minutes: Int): Result<Unit> {
            return try {
                if (minutes !in TimerSettings.MIN_BREAK_DURATION..TimerSettings.MAX_BREAK_DURATION) {
                    return Result
                        .failure(IllegalArgumentException("Short break duration must be between ${TimerSettings.MIN_BREAK_DURATION} and ${TimerSettings.MAX_BREAK_DURATION} minutes"))
                }
                repository.updateShortBreakDuration(minutes)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

class UpdateLongBreakDurationUseCase
    @Inject
    constructor(
        private val repository: TimerSettingsRepository
    ) {
        suspend operator fun invoke(minutes: Int): Result<Unit> {
            return try {
                if (minutes !in TimerSettings.MIN_BREAK_DURATION..TimerSettings.MAX_BREAK_DURATION) {
                    return Result
                        .failure(IllegalArgumentException("Long break duration must be between ${TimerSettings.MIN_BREAK_DURATION} and ${TimerSettings.MAX_BREAK_DURATION} minutes"))
                }
                repository.updateLongBreakDuration(minutes)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

class UpdateSessionsBeforeLongBreakUseCase
    @Inject
    constructor(
        private val repository: TimerSettingsRepository
    ) {
        suspend operator fun invoke(sessions: Int): Result<Unit> {
            return try {
                if (sessions !in TimerSettings.MIN_SESSIONS_BEFORE_LONG_BREAK..TimerSettings.MAX_SESSIONS_BEFORE_LONG_BREAK) {
                    return Result
                        .failure(IllegalArgumentException("Sessions before long break must be between ${TimerSettings.MIN_SESSIONS_BEFORE_LONG_BREAK} and ${TimerSettings.MAX_SESSIONS_BEFORE_LONG_BREAK}"))
                }
                repository.updateSessionsBeforeLongBreak(sessions)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

class ResetTimerSettingsUseCase
    @Inject
    constructor(
        private val repository: TimerSettingsRepository
    ) {
        suspend operator fun invoke(): Result<Unit> =
            try {
                repository.resetToDefaults()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
