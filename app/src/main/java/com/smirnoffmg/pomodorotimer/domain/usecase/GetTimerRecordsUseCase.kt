package com.smirnoffmg.pomodorotimer.domain.usecase

import com.smirnoffmg.pomodorotimer.data.db.TimerRecord
import com.smirnoffmg.pomodorotimer.data.repository.TimerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTimerRecordsUseCase @Inject constructor(
    private val timerRepository: TimerRepository
) {
    operator fun invoke(): Flow<List<TimerRecord>> = timerRepository.getAllTimerRecords()
}
