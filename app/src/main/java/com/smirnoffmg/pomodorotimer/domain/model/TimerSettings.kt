package com.smirnoffmg.pomodorotimer.domain.model

data class TimerSettings(
    val id: Int = 1,
    val workDurationMinutes: Int = 25,
    val shortBreakDurationMinutes: Int = 5,
    val longBreakDurationMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun getDefaultSettings() = TimerSettings()

        // Validation constants
        const val MIN_WORK_DURATION = 1
        const val MAX_WORK_DURATION = 90
        const val MIN_BREAK_DURATION = 1
        const val MAX_BREAK_DURATION = 30
        const val MIN_SESSIONS_BEFORE_LONG_BREAK = 2
        const val MAX_SESSIONS_BEFORE_LONG_BREAK = 8
    }

    // Convert to seconds for timer service
    val workDurationSeconds: Long get() = workDurationMinutes * 60L
    val shortBreakDurationSeconds: Long get() = shortBreakDurationMinutes * 60L
    val longBreakDurationSeconds: Long get() = longBreakDurationMinutes * 60L

    // Validation
    fun isValid(): Boolean =
        workDurationMinutes in MIN_WORK_DURATION..MAX_WORK_DURATION &&
            shortBreakDurationMinutes in MIN_BREAK_DURATION..MAX_BREAK_DURATION &&
            longBreakDurationMinutes in MIN_BREAK_DURATION..MAX_BREAK_DURATION &&
            sessionsBeforeLongBreak in MIN_SESSIONS_BEFORE_LONG_BREAK..MAX_SESSIONS_BEFORE_LONG_BREAK
}
