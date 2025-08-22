package com.smirnoffmg.pomodorotimer.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerServiceManager
    @Inject
    constructor(
        private val context: Context
    ) {
        private var service: TimerForegroundService? = null
        private var isBound = false
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        // Local state flows that get updated when service is bound
        private val _timerState = MutableStateFlow(TimerState.STOPPED)
        val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
        private val _remainingTime = MutableStateFlow(25 * 60L)
        val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()
    
        private val _progress = MutableStateFlow(1f)
        val progress: StateFlow<Float> = _progress.asStateFlow()
    
        private val _cycleType = MutableStateFlow(TimerForegroundService.CycleType.WORK)
        val cycleType: StateFlow<TimerForegroundService.CycleType> = _cycleType.asStateFlow()

        private val connection =
            object : ServiceConnection {
                override fun onServiceConnected(
                    name: ComponentName?,
                    binder: IBinder?
                ) {
                    val timerBinder = binder as TimerForegroundService.TimerBinder
                    service = timerBinder.getService()
                    isBound = true
                    // Sync local state with service state
                    syncStateWithService()
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    service = null
                    isBound = false
                }
            }

        fun startTimer() {
            val intent =
                Intent(context, TimerForegroundService::class.java).apply {
                    action = TimerForegroundService.ACTION_START_TIMER
                }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            bindService()
        }

        fun pauseTimer() {
            val intent =
                Intent(context, TimerForegroundService::class.java).apply {
                    action = TimerForegroundService.ACTION_PAUSE_TIMER
                }
            context.startService(intent)
        }

        fun stopTimer() {
            val intent =
                Intent(context, TimerForegroundService::class.java).apply {
                    action = TimerForegroundService.ACTION_STOP_TIMER
                }
            context.startService(intent)
            unbindService()
        }

        fun setTimerDuration(durationInSeconds: Long) {
            val intent =
                Intent(context, TimerForegroundService::class.java).apply {
                    action = TimerForegroundService.ACTION_SET_DURATION
                    putExtra(TimerForegroundService.EXTRA_DURATION, durationInSeconds)
                }
            context.startService(intent)
        }

        fun skipBreak() {
            val intent =
                Intent(context, TimerForegroundService::class.java).apply {
                    action = TimerForegroundService.ACTION_SKIP_BREAK
                }
            context.startService(intent)
        }

        fun reloadSettings() {
            service?.let { serviceInstance ->
                // Call the service's reloadSettings method
                serviceInstance.reloadSettings()
            }
        }

        fun isServiceRunning(): Boolean = isBound && service != null

        private fun bindService() {
            if (!isBound) {
                val intent = Intent(context, TimerForegroundService::class.java)
                context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }

        private fun unbindService() {
            if (isBound) {
                context.unbindService(connection)
                isBound = false
            }
        }

        fun cleanup() {
            unbindService()
        }

        private fun syncStateWithService() {
            service?.let { serviceInstance ->
                // Update local state flows with current service state
                _timerState.value = serviceInstance.timerState.value
                _remainingTime.value = serviceInstance.remainingTime.value
                _progress.value = serviceInstance.progress.value
                _cycleType.value = serviceInstance.cycleType.value
            
                // Set up continuous sync
                serviceInstance.timerState.onEach { _timerState.value = it }.launchIn(scope)
                serviceInstance.remainingTime.onEach { _remainingTime.value = it }.launchIn(scope)
                serviceInstance.progress.onEach { _progress.value = it }.launchIn(scope)
                serviceInstance.cycleType.onEach { _cycleType.value = it }.launchIn(scope)
            }
        }
    }
