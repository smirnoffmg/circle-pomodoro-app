package com.smirnoffmg.pomodorotimer.domain.usecase

import com.smirnoffmg.pomodorotimer.data.repository.TimerRepository
import com.smirnoffmg.pomodorotimer.domain.model.TimerRecord
import javax.inject.Inject

class AddTimerRecordUseCase
    @Inject
    constructor(
        private val timerRepository: TimerRepository,
    ) {
        suspend operator fun invoke(record: TimerRecord) {
            timerRepository.insertTimerRecord(record)
        }
    }
