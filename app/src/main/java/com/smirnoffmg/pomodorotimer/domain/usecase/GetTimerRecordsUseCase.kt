package com.smirnoffmg.pomodorotimer.domain.usecase

import com.smirnoffmg.pomodorotimer.data.repository.TimerRepository
import com.smirnoffmg.pomodorotimer.domain.model.TimerRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTimerRecordsUseCase
    @Inject
    constructor(
        private val timerRepository: TimerRepository,
    ) {
        operator fun invoke(): Flow<List<TimerRecord>> = timerRepository.getAllTimerRecords()
    }
