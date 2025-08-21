package com.smirnoffmg.pomodorotimer.service

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
        private const val WAKE_LOCK_TAG = "PomodoroTimer::WakeLock"
        
        const val ACTION_START_TIMER = "com.smirnoffmg.pomodorotimer.START_TIMER"
        const val ACTION_PAUSE_TIMER = "com.smirnoffmg.pomodorotimer.PAUSE_TIMER"
        const val ACTION_STOP_TIMER = "com.smirnoffmg.pomodorotimer.STOP_TIMER"
        const val ACTION_SET_DURATION = "com.smirnoffmg.pomodorotimer.SET_DURATION"
        const val EXTRA_DURATION = "extra_duration"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val _timerState = MutableStateFlow(TimerState.STOPPED)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _remainingTime = MutableStateFlow(25 * 60L)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()

    private val _progress = MutableStateFlow(1f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private var initialDuration: Long = 25 * 60L

    private val binder = TimerBinder()

    inner class TimerBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> startTimer()
            ACTION_PAUSE_TIMER -> pauseTimer()
            ACTION_STOP_TIMER -> stopTimer()
            ACTION_SET_DURATION -> {
                val duration = intent?.getLongExtra(EXTRA_DURATION, 25 * 60L) ?: 25 * 60L
                setTimerDuration(duration)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
        timerJob?.cancel()
    }

    fun startTimer() {
        if (_timerState.value == TimerState.RUNNING) return
        
        _timerState.value = TimerState.RUNNING
        startForeground(NOTIFICATION_ID, createNotification())
        startCountdown()
    }

    fun pauseTimer() {
        if (_timerState.value != TimerState.RUNNING) return
        
        _timerState.value = TimerState.PAUSED
        timerJob?.cancel()
        updateNotification()
    }

    fun stopTimer() {
        _timerState.value = TimerState.STOPPED
        timerJob?.cancel()
        _remainingTime.value = initialDuration
        _progress.value = 1f
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
        _timerState.value = TimerState.STOPPED
        _remainingTime.value = 0
        _progress.value = 0f
        updateNotification()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Timer service notification"
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
        val stateText = when (_timerState.value) {
            TimerState.RUNNING -> "Focus Time"
            TimerState.PAUSED -> "Paused"
            TimerState.STOPPED -> "Timer Stopped"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro Timer")
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
