package com.smirnoffmg.pomodorotimer.domain.usecase

import com.smirnoffmg.pomodorotimer.data.db.TimerRecord
import com.smirnoffmg.pomodorotimer.data.repository.TimerRepository
import javax.inject.Inject

class AddTimerRecordUseCase @Inject constructor(
    private val timerRepository: TimerRepository
) {
    suspend operator fun invoke(record: TimerRecord) {
        timerRepository.insertTimerRecord(record)
    }
}
