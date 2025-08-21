package com.smirnoffmg.pomodorotimer.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.smirnoffmg.pomodorotimer.R
import com.smirnoffmg.pomodorotimer.presentation.MainActivity
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class TimerForegroundService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "timer_channel"
        private const val CHANNEL_NAME = "Timer Service"
        private const val WAKE_LOCK_TAG = "CircleTimer::WakeLock"
        private const val ALARM_REQUEST_CODE = 1002
        
        const val ACTION_START_TIMER = "com.smirnoffmg.pomodorotimer.START_TIMER"
        const val ACTION_PAUSE_TIMER = "com.smirnoffmg.pomodorotimer.PAUSE_TIMER"
        const val ACTION_STOP_TIMER = "com.smirnoffmg.pomodorotimer.STOP_TIMER"
        const val ACTION_SET_DURATION = "com.smirnoffmg.pomodorotimer.SET_DURATION"
        const val EXTRA_DURATION = "extra_duration"
        
        // Circle concept: Default intervals for immediate value delivery
        private const val DEFAULT_WORK_DURATION = 25 * 60L // 25 minutes
        private const val DEFAULT_BREAK_DURATION = 5 * 60L // 5 minutes
        private const val DEFAULT_LONG_BREAK_DURATION = 15 * 60L // 15 minutes
        private const val SESSIONS_BEFORE_LONG_BREAK = 4
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var alarmManager: AlarmManager? = null
    private var alarmPendingIntent: PendingIntent? = null

    private val _timerState = MutableStateFlow(TimerState.STOPPED)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _remainingTime = MutableStateFlow(DEFAULT_WORK_DURATION)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()

    private val _progress = MutableStateFlow(1f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    // Circle concept: Automatic cycle management
    private var currentCycleType = CycleType.WORK
    private var completedSessions = 0
    private var initialDuration: Long = DEFAULT_WORK_DURATION

    private val binder = TimerBinder()

    enum class CycleType {
        WORK, BREAK, LONG_BREAK
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> startTimer()
            ACTION_PAUSE_TIMER -> pauseTimer()
            ACTION_STOP_TIMER -> stopTimer()
            ACTION_SET_DURATION -> {
                val duration = intent?.getLongExtra(EXTRA_DURATION, DEFAULT_WORK_DURATION) ?: DEFAULT_WORK_DURATION
                setTimerDuration(duration)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
        cancelAlarm()
        timerJob?.cancel()
    }

    fun startTimer() {
        if (_timerState.value == TimerState.RUNNING) return
        
        _timerState.value = TimerState.RUNNING
        startForeground(NOTIFICATION_ID, createNotification())
        startCountdown()
        scheduleAlarmBackup()
    }

    fun pauseTimer() {
        if (_timerState.value != TimerState.RUNNING) return
        
        _timerState.value = TimerState.PAUSED
        timerJob?.cancel()
        cancelAlarm()
        updateNotification()
    }

    fun stopTimer() {
        _timerState.value = TimerState.STOPPED
        timerJob?.cancel()
        cancelAlarm()
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

    private fun startCountdown() {
        timerJob = serviceScope.launch {
            while (_remainingTime.value > 0 && _timerState.value == TimerState.RUNNING) {
                delay(1000)
                val currentTime = _remainingTime.value
                if (currentTime > 0) {
                    _remainingTime.value = currentTime - 1
                    updateProgress()
                    updateNotification()
                }
            }
            if (_remainingTime.value <= 0) {
                onTimerComplete()
            }
        }
    }

    private fun updateProgress() {
        val currentTime = _remainingTime.value
        val progressValue = if (initialDuration > 0) {
            currentTime.toFloat() / initialDuration.toFloat()
        } else {
            0f
        }
        _progress.value = progressValue.coerceIn(0f, 1f)
    }

    private fun onTimerComplete() {
        // Circle concept: Automatic cycle transitions
        when (currentCycleType) {
            CycleType.WORK -> {
                completedSessions++
                if (completedSessions % SESSIONS_BEFORE_LONG_BREAK == 0) {
                    startLongBreak()
                } else {
                    startBreak()
                }
            }
            CycleType.BREAK, CycleType.LONG_BREAK -> {
                startWork()
            }
        }
    }

    private fun startWork() {
        currentCycleType = CycleType.WORK
        initialDuration = DEFAULT_WORK_DURATION
        _remainingTime.value = initialDuration
        _progress.value = 1f
        _timerState.value = TimerState.RUNNING
        updateNotification()
        scheduleAlarmBackup()
        startCountdown()
    }

    private fun startBreak() {
        currentCycleType = CycleType.BREAK
        initialDuration = DEFAULT_BREAK_DURATION
        _remainingTime.value = initialDuration
        _progress.value = 1f
        _timerState.value = TimerState.RUNNING
        updateNotification()
        scheduleAlarmBackup()
        startCountdown()
    }

    private fun startLongBreak() {
        currentCycleType = CycleType.LONG_BREAK
        initialDuration = DEFAULT_LONG_BREAK_DURATION
        _remainingTime.value = initialDuration
        _progress.value = 1f
        _timerState.value = TimerState.RUNNING
        updateNotification()
        scheduleAlarmBackup()
        startCountdown()
    }

    private fun resetToWorkCycle() {
        currentCycleType = CycleType.WORK
        completedSessions = 0
        initialDuration = DEFAULT_WORK_DURATION
        _remainingTime.value = initialDuration
        _progress.value = 1f
    }

    // Circle concept: AlarmManager backup for bulletproof reliability
    private fun scheduleAlarmBackup() {
        alarmManager?.let { alarm ->
            val intent = Intent(this, TimerForegroundService::class.java).apply {
                action = ACTION_STOP_TIMER
            }
            
            alarmPendingIntent = PendingIntent.getService(
                this,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerTime = System.currentTimeMillis() + (_remainingTime.value * 1000)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarm.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    alarmPendingIntent!!
                )
            } else {
                alarm.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    alarmPendingIntent!!
                )
            }
        }
    }

    private fun cancelAlarm() {
        alarmPendingIntent?.let { pendingIntent ->
            alarmManager?.cancel(pendingIntent)
            alarmPendingIntent = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Circle timer service notification"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeText = formatTime(_remainingTime.value)
        val cycleText = when (currentCycleType) {
            CycleType.WORK -> "Focus"
            CycleType.BREAK -> "Break"
            CycleType.LONG_BREAK -> "Long Break"
        }
        val stateText = when (_timerState.value) {
            TimerState.RUNNING -> cycleText
            TimerState.PAUSED -> "Paused"
            TimerState.STOPPED -> "Stopped"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Circle Timer")
            .setContentText("$timeText - $stateText")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
        
        // Update widget (simplified)
        com.smirnoffmg.pomodorotimer.widget.CircleTimerWidget.updateTimerDisplay(
            this,
            _remainingTime.value,
            _timerState.value == TimerState.RUNNING
        )
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
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

    private fun formatTime(timeInSeconds: Long): String {
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
