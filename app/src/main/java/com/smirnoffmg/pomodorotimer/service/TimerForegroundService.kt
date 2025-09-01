package com.smirnoffmg.pomodorotimer.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.smirnoffmg.pomodorotimer.R
import com.smirnoffmg.pomodorotimer.domain.model.SessionType
import com.smirnoffmg.pomodorotimer.domain.usecase.StartSessionUseCase
import com.smirnoffmg.pomodorotimer.domain.usecase.CompleteSessionUseCase
import com.smirnoffmg.pomodorotimer.notification.NotificationHelper
import com.smirnoffmg.pomodorotimer.timer.TimerRedundancyManager
import com.smirnoffmg.pomodorotimer.timer.TimerHealthMonitor
import com.smirnoffmg.pomodorotimer.timer.ServiceLifecycleEvent
import com.smirnoffmg.pomodorotimer.presentation.MainActivity
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.TimerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerForegroundService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val WAKE_LOCK_TAG = "CircleTimer::WakeLock"
        
        const val ACTION_START_TIMER = "com.smirnoffmg.pomodorotimer.START_TIMER"
        const val ACTION_PAUSE_TIMER = "com.smirnoffmg.pomodorotimer.PAUSE_TIMER"
        const val ACTION_STOP_TIMER = "com.smirnoffmg.pomodorotimer.STOP_TIMER"
        const val ACTION_SKIP_BREAK = "com.smirnoffmg.pomodorotimer.SKIP_BREAK"
        const val ACTION_SET_DURATION = "com.smirnoffmg.pomodorotimer.SET_DURATION"
        const val ACTION_TIMER_COMPLETE = "com.smirnoffmg.pomodorotimer.TIMER_COMPLETE"
        const val EXTRA_DURATION = "extra_duration"

        // Circle concept: Default intervals for immediate value delivery
        private const val DEFAULT_WORK_DURATION = 25 * 60L // 25 minutes
        private const val DEFAULT_BREAK_DURATION = 5 * 60L // 5 minutes
        private const val DEFAULT_LONG_BREAK_DURATION = 15 * 60L // 15 minutes
        private const val SESSIONS_BEFORE_LONG_BREAK = 4
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val _timerState = MutableStateFlow(TimerState.STOPPED)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _remainingTime = MutableStateFlow(DEFAULT_WORK_DURATION)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()

    private val _progress = MutableStateFlow(1f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    // Circle concept: Automatic cycle management
    private var currentCycleType = CycleType.WORK
    
    private val _cycleType = MutableStateFlow(CycleType.WORK)
    val cycleType: StateFlow<CycleType> = _cycleType.asStateFlow()
    private var completedSessions = 0 // For cycle management (long break timing)
    private var totalSessionsToday = 0 // For milestone notifications
    private var initialDuration: Long = DEFAULT_WORK_DURATION

    // Custom settings
    private var currentSettings: com.smirnoffmg.pomodorotimer.domain.model.TimerSettings? = null

    // Session tracking
    private var activeSessionId: Long? = null
    private var sessionStartTime: Long = 0L

    // Session tracking dependencies
    @Inject
    lateinit var startSessionUseCase: StartSessionUseCase
    
    @Inject
    lateinit var completeSessionUseCase: CompleteSessionUseCase

    @Inject
    lateinit var getDailyStatisticsUseCase: com.smirnoffmg.pomodorotimer.domain.usecase.GetDailyStatisticsUseCase

    // Settings dependencies
    @Inject
    lateinit var getTimerSettingsUseCase: com.smirnoffmg.pomodorotimer.domain.usecase.GetTimerSettingsUseCase
    
    @Inject
    lateinit var notificationHelper: NotificationHelper
    
    @Inject
    lateinit var redundancyManager: TimerRedundancyManager
    
    @Inject
    lateinit var healthMonitor: TimerHealthMonitor

    private val binder = TimerBinder()

    enum class CycleType {
        WORK,
        BREAK,
        LONG_BREAK
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }

    override fun onCreate() {
        super.onCreate()
        healthMonitor.reportServiceLifecycle(ServiceLifecycleEvent.SERVICE_STARTED)
        acquireWakeLock()
        
        // Load settings and daily session count to ensure they're available immediately
        serviceScope.launch {
            loadSettings()
            loadTodaySessionCount()
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        android.util.Log.d("TimerService", "onStartCommand called with action: ${intent?.action}, flags: $flags, startId: $startId")
        
        when (intent?.action) {
            ACTION_START_TIMER -> {
                val duration = intent.getLongExtra(EXTRA_DURATION, 0L)
                if (duration > 0) {
                    setTimerDuration(duration)
                }
                startTimer()
            }
            ACTION_PAUSE_TIMER -> pauseTimer()
            ACTION_STOP_TIMER -> stopTimer()
            ACTION_SKIP_BREAK -> skipBreak()
            ACTION_TIMER_COMPLETE -> onTimerComplete()
            ACTION_SET_DURATION -> {
                val duration = intent.getLongExtra(EXTRA_DURATION, DEFAULT_WORK_DURATION)
                setTimerDuration(duration)
            }
            null -> {
                android.util.Log.d("TimerService", "Service restarted by system, attempting timer recovery")
                // Fix: Improved service restart logic with better state recovery
                serviceScope.launch {
                    loadSettings()
                    // Check if we should resume a running timer
                    if (_timerState.value == TimerState.RUNNING) {
                        android.util.Log.d("TimerService", "Resuming timer after service restart")
                        // Ensure the timer is properly running and update notification
                        startCountdown()
                        updateNotification()
                        // Restart redundancy system
                        redundancyManager.startTimer(_remainingTime.value * 1000L)
                    } else if (_timerState.value == TimerState.PAUSED) {
                        android.util.Log.d("TimerService", "Service restarted with paused timer, updating notification")
                        updateNotification()
                    }
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        healthMonitor.reportServiceLifecycle(ServiceLifecycleEvent.SERVICE_STOPPED)
        redundancyManager.stopTimer()
        releaseWakeLock()
        timerJob?.cancel()
    }

    fun startTimer() {
        if (_timerState.value == TimerState.RUNNING) return
        
        if (_timerState.value == TimerState.PAUSED) {
            // Resume from paused state
            _timerState.value = TimerState.RUNNING
            redundancyManager.resumeTimer(_remainingTime.value * 1000L)
            updateNotification()
            // Fix: Remove duplicate alarm scheduling - TimerRedundancyManager handles backups
            startCountdown()
        } else {
            // Start new timer - ensure settings are loaded first
            serviceScope.launch {
                // Wait for settings to load before starting timer (fix race condition)
                loadSettings()
                startWork()
            }
        }
    }

    fun pauseTimer() {
        if (_timerState.value != TimerState.RUNNING) return
        
        _timerState.value = TimerState.PAUSED
        timerJob?.cancel()
        
        // Report to redundancy system
        redundancyManager.pauseTimer()
        updateNotification()
    }

    fun stopTimer() {
        _timerState.value = TimerState.STOPPED
        timerJob?.cancel()
        
        // Cancel active session if stopped manually during work
        if (currentCycleType == CycleType.WORK && activeSessionId != null) {
            cancelActiveSession()
        }
        
        redundancyManager.stopTimer()
        resetToWorkCycle()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun setTimerDuration(durationInSeconds: Long) {
        initialDuration = durationInSeconds
        if (_timerState.value == TimerState.STOPPED) {
            _remainingTime.value = durationInSeconds
            _progress.value = 1f
        }
    }

    fun skipBreak() {
        if (currentCycleType == CycleType.BREAK || currentCycleType == CycleType.LONG_BREAK) {
            // Skip break and start work immediately
            startWork()
        }
    }

    private fun startCountdown() {
        // Cancel any existing countdown to prevent double speed
        timerJob?.cancel()
        android.util.Log
            .d("TimerService", "Starting countdown with _remainingTime: ${_remainingTime.value}s, _timerState: ${_timerState.value}")
        timerJob =
            serviceScope.launch {
                var lastHeartbeat = System.currentTimeMillis()
                val initialRemainingTime = _remainingTime.value
                val startTime = System.currentTimeMillis()
                
                while (_remainingTime.value > 0 && _timerState.value == TimerState.RUNNING) {
                    val currentTime = System.currentTimeMillis()
                    val elapsed = (currentTime - startTime) / 1000L
                    val remaining = (initialRemainingTime - elapsed).coerceAtLeast(0L)
                    
                    if (remaining != _remainingTime.value) {
                        _remainingTime.value = remaining
                        updateProgress()
                        updateNotification()
                    }
                    
                    // Report heartbeat to redundancy system every 5 seconds
                    if (currentTime - lastHeartbeat >= 5000L) {
                        redundancyManager.reportPrimaryTimerHeartbeat(remaining * 1000L)
                        lastHeartbeat = currentTime
                    }
                    
                    // Sleep for a shorter interval to be more responsive
                    delay(500)
                }
                if (_remainingTime.value <= 0) {
                    onTimerComplete()
                }
            }
    }

    private fun updateProgress() {
        val currentTime = _remainingTime.value
        val progressValue =
            if (initialDuration > 0) {
                currentTime.toFloat() / initialDuration.toFloat()
            } else {
                0f
            }
        _progress.value = progressValue.coerceIn(0f, 1f)
    }

    private fun onTimerComplete() {
        android.util.Log.d("TimerService", "Timer completed for cycle: $currentCycleType")
        
        // Circle concept: Automatic cycle transitions
        when (currentCycleType) {
            CycleType.WORK -> {
                completedSessions++
                totalSessionsToday++
                android.util.Log.d("TimerService", "Work session completed. Sessions: $completedSessions")
                
                // Notify that a work session was completed
                notifySessionCompleted()
                val sessionsBeforeLongBreak = currentSettings?.sessionsBeforeLongBreak ?: SESSIONS_BEFORE_LONG_BREAK
                
                // Ensure settings are loaded before starting break
                serviceScope.launch {
                    if (currentSettings == null) {
                        android.util.Log.d("TimerService", "Settings not loaded, loading before break")
                        loadSettings()
                    }
                    
                    if (completedSessions % sessionsBeforeLongBreak == 0) {
                        android.util.Log.d("TimerService", "Starting long break")
                        startLongBreak()
                    } else {
                        android.util.Log.d("TimerService", "Starting short break")
                        startBreak()
                    }
                }
            }
            CycleType.BREAK, CycleType.LONG_BREAK -> {
                android.util.Log.d("TimerService", "Break completed, starting work")
                // Show break end notification before starting work
                showBreakEndNotification()
                // Work transition already handles settings loading
                startWork()
            }
        }
    }

    private suspend fun loadSettings() {
        try {
            // Load settings synchronously for immediate use
            currentSettings =
                getTimerSettingsUseCase().first() ?: com.smirnoffmg.pomodorotimer.domain.model.TimerSettings
                    .getDefaultSettings()
            android.util.Log
                .d("TimerService", "Settings loaded: work=${currentSettings?.workDurationMinutes}min, break=${currentSettings?.shortBreakDurationMinutes}min")
            
            // Update initial timer duration when timer is stopped (at app startup)
            if (_timerState.value == TimerState.STOPPED && currentCycleType == CycleType.WORK) {
                val workDuration = currentSettings?.workDurationSeconds ?: DEFAULT_WORK_DURATION
                initialDuration = workDuration
                _remainingTime.value = workDuration
                _progress.value = 1f
                android.util.Log
                    .d("TimerService", "Updated initial timer duration to: ${workDuration}s (${workDuration / 60}min) from settings")
            }
        } catch (e: Exception) {
            // Fall back to default settings if loading fails
            currentSettings =
                com.smirnoffmg.pomodorotimer.domain.model.TimerSettings
                    .getDefaultSettings()
            android.util.Log.e("TimerService", "Failed to load settings, using defaults", e)
            
            // Update initial timer duration even with defaults when timer is stopped
            if (_timerState.value == TimerState.STOPPED && currentCycleType == CycleType.WORK) {
                val workDuration = currentSettings?.workDurationSeconds ?: DEFAULT_WORK_DURATION
                initialDuration = workDuration
                _remainingTime.value = workDuration
                _progress.value = 1f
                android.util.Log.d("TimerService", "Updated initial timer duration to default: ${workDuration}s (${workDuration / 60}min)")
            }
        }
    }

    private suspend fun loadTodaySessionCount() {
        try {
            val todayStats = getDailyStatisticsUseCase.getTodayStatistics()
            totalSessionsToday = todayStats.workSessions
            android.util.Log.d("TimerService", "Loaded today's session count: $totalSessionsToday")
        } catch (e: Exception) {
            android.util.Log.e("TimerService", "Failed to load today's session count", e)
            // Start with 0 if count can't be loaded
            totalSessionsToday = 0
        }
    }

    fun reloadSettings() {
        serviceScope.launch {
            loadSettings()
        }
    }

    private fun createWorkSession() {
        serviceScope.launch {
            try {
                val sessionId =
                    startSessionUseCase(
                        sessionType = SessionType.WORK,
                        duration = initialDuration * 1000L // Convert seconds to milliseconds
                    )
                activeSessionId = sessionId
            } catch (e: Exception) {
                // Log error but don't crash the service
                activeSessionId = null
            }
        }
    }

    private fun cancelActiveSession() {
        // For cancelled sessions, we could either delete them or mark them as incomplete
        // For now, we'll just clear the reference - incomplete sessions remain in DB
        activeSessionId = null
    }

    private fun notifySessionCompleted() {
        // Complete the active work session
        activeSessionId?.let { sessionId ->
            serviceScope.launch {
                try {
                    completeSessionUseCase(sessionId)
                    activeSessionId = null
                    
                    // Show session completion notification
                    notificationHelper.showSessionCompleteNotification(totalSessionsToday)
                    
                    // Check for milestones (every 5, 10, 25, 50 sessions)
                    when (totalSessionsToday) {
                        5, 10, 25, 50 -> {
                            notificationHelper.showMilestoneNotification(totalSessionsToday)
                        }
                        in 1..Int.MAX_VALUE -> {
                            if (totalSessionsToday % 100 == 0) {
                                notificationHelper.showMilestoneNotification(totalSessionsToday)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Log error but don't crash the service
                }
            }
        }
    }

    private fun startWork() {
        currentCycleType = CycleType.WORK
        _cycleType.value = CycleType.WORK
        // Use current settings or fallback to defaults - never wait for loading
        initialDuration = currentSettings?.workDurationSeconds ?: DEFAULT_WORK_DURATION
        android.util.Log.d("TimerService", "Starting work timer with duration: ${initialDuration}s (${initialDuration / 60}min)")
        _remainingTime.value = initialDuration
        _progress.value = 1f
        _timerState.value = TimerState.RUNNING
        
        // Create session record for work sessions
        sessionStartTime = System.currentTimeMillis()
        createWorkSession()
        
        // Start redundancy system
        redundancyManager.startTimer(initialDuration * 1000L)
        
        updateNotification()
        // Fix: Remove duplicate alarm scheduling - TimerRedundancyManager handles backups
        startCountdown()
    }

    private fun startBreak() {
        currentCycleType = CycleType.BREAK
        _cycleType.value = CycleType.BREAK
        
        val settingsValue = currentSettings?.shortBreakDurationSeconds
        initialDuration = settingsValue ?: DEFAULT_BREAK_DURATION
        android.util.Log
            .d("TimerService", "Starting short break - settings value: $settingsValue, final duration: ${initialDuration}s (${initialDuration / 60}min)")
        android.util.Log.d("TimerService", "Current settings object: $currentSettings")
        
        _remainingTime.value = initialDuration
        _progress.value = 1f
        _timerState.value = TimerState.RUNNING
        
        // Show break start notification
        showBreakStartNotification()
        
        // Start redundancy system for break
        redundancyManager.startTimer(initialDuration * 1000L)
        
        updateNotification()
        // Fix: Remove duplicate alarm scheduling - TimerRedundancyManager handles backups
        startCountdown()
    }

    private fun startLongBreak() {
        currentCycleType = CycleType.LONG_BREAK
        _cycleType.value = CycleType.LONG_BREAK
        initialDuration = currentSettings?.longBreakDurationSeconds ?: DEFAULT_LONG_BREAK_DURATION
        android.util.Log.d("TimerService", "Starting long break with duration: ${initialDuration}s (${initialDuration / 60}min)")
        _remainingTime.value = initialDuration
        _progress.value = 1f
        _timerState.value = TimerState.RUNNING
        
        // Show break start notification
        showBreakStartNotification()
        
        // Start redundancy system for long break
        redundancyManager.startTimer(initialDuration * 1000L)
        
        updateNotification()
        // Fix: Remove duplicate alarm scheduling - TimerRedundancyManager handles backups
        startCountdown()
    }

    private fun resetToWorkCycle() {
        currentCycleType = CycleType.WORK
        completedSessions = 0
        initialDuration = DEFAULT_WORK_DURATION
        _remainingTime.value = initialDuration
        _progress.value = 1f
    }

    // Note: AlarmManager backup is now handled by TimerRedundancyManager
    // This prevents dual alarm system conflicts that caused premature timer completion

    private fun createNotification(): Notification? {
        val timeText = formatTime(_remainingTime.value)
        val cycleText =
            when (currentCycleType) {
                CycleType.WORK -> "Focus"
                CycleType.BREAK -> "Break"
                CycleType.LONG_BREAK -> "Long Break"
            }
        
        return if (currentCycleType == CycleType.WORK) {
            notificationHelper.createTimerNotification(
                remainingTime = timeText,
                cycleType = cycleText,
                isRunning = _timerState.value == TimerState.RUNNING
            )
        } else {
            notificationHelper.createBreakNotification(
                remainingTime = timeText,
                breakType = cycleText,
                isRunning = _timerState.value == TimerState.RUNNING
            )
        }
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationId = com.smirnoffmg.pomodorotimer.notification.NotificationHelper.TIMER_NOTIFICATION_ID
        
        if (notification != null) {
            // Start as foreground service if running, otherwise just update notification
            if (_timerState.value == TimerState.RUNNING) {
                startForeground(notificationId, notification)
            } else {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(notificationId, notification)
            }
        } else {
            // If notification is null (disabled by settings), stop foreground service
            if (_timerState.value == TimerState.RUNNING) {
                stopForeground(true)
            }
        }
    }

    private fun showBreakStartNotification() {
        val breakType =
            when (currentCycleType) {
                CycleType.BREAK -> "Break"
                CycleType.LONG_BREAK -> "Long Break"
                else -> "Break"
            }
        
        notificationHelper.showBreakStartNotification(breakType)
    }

    private fun showBreakEndNotification() {
        notificationHelper.showBreakEndNotification()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock =
            powerManager
                .newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    WAKE_LOCK_TAG
                ).apply {
                    acquire(10 * 60 * 1000L) // 10 minutes timeout
                }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    private fun formatTime(timeInSeconds: Long): String =
        com.smirnoffmg.pomodorotimer.utils.TimeFormatter
            .formatTime(timeInSeconds)

    // Fix: Method to get current timer state for better UI synchronization
    fun getCurrentTimerState(): Map<String, Any> {
        return mapOf(
            "timerState" to _timerState.value,
            "remainingTime" to _remainingTime.value,
            "progress" to _progress.value,
            "cycleType" to _cycleType.value,
            "initialDuration" to initialDuration,
            "completedSessions" to completedSessions
        )
    }
}
