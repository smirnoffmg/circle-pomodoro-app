package com.smirnoffmg.pomodorotimer.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.TimerState
import com.smirnoffmg.pomodorotimer.domain.usecase.GetTimerSettingsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerServiceManager
    @Inject
    constructor(
        private val context: Context,
        private val getTimerSettingsUseCase: GetTimerSettingsUseCase
    ) {
        private var service: TimerForegroundService? = null
        private var isBound = false
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        // Flow collection jobs to manage memory leaks
        private var timerStateJob: Job? = null
        private var remainingTimeJob: Job? = null
        private var progressJob: Job? = null
        private var cycleTypeJob: Job? = null

        // Local state flows that get updated when service is bound
        private val _timerState = MutableStateFlow(TimerState.STOPPED)
        val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
        private val _remainingTime = MutableStateFlow(25 * 60L) // Will be updated with actual settings
        val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()
    
        private val _progress = MutableStateFlow(1f)
        val progress: StateFlow<Float> = _progress.asStateFlow()
    
        private val _cycleType = MutableStateFlow(TimerForegroundService.CycleType.WORK)
        val cycleType: StateFlow<TimerForegroundService.CycleType> = _cycleType.asStateFlow()

        init {
            // Load settings to set proper initial timer duration
            scope.launch {
                loadInitialSettings()
            }
        }

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
            ServiceUtils.startService(
                context = context,
                serviceClass = TimerForegroundService::class.java,
                action = TimerForegroundService.ACTION_START_TIMER
            )
            bindService()
        }

        fun pauseTimer() {
            ServiceUtils.startService(
                context = context,
                serviceClass = TimerForegroundService::class.java,
                action = TimerForegroundService.ACTION_PAUSE_TIMER
            )
        }

        fun stopTimer() {
            ServiceUtils.startService(
                context = context,
                serviceClass = TimerForegroundService::class.java,
                action = TimerForegroundService.ACTION_STOP_TIMER
            )
            unbindService()
        }

        fun setTimerDuration(durationInSeconds: Long) {
            ServiceUtils.startService(
                context = context,
                serviceClass = TimerForegroundService::class.java,
                action = TimerForegroundService.ACTION_SET_DURATION,
                extras = mapOf(TimerForegroundService.EXTRA_DURATION to durationInSeconds)
            )
        }

        fun skipBreak() {
            ServiceUtils.startService(
                context = context,
                serviceClass = TimerForegroundService::class.java,
                action = TimerForegroundService.ACTION_SKIP_BREAK
            )
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
            cancelFlowCollections()
            unbindService()
        }

        private suspend fun loadInitialSettings() {
            try {
                val settings =
                    getTimerSettingsUseCase().first()
                        ?: com.smirnoffmg.pomodorotimer.domain.model.TimerSettings
                            .getDefaultSettings()
                val workDuration = settings.workDurationSeconds
                // Only update if service isn't bound (meaning we're showing placeholder values)
                if (!isBound && _timerState.value == TimerState.STOPPED) {
                    _remainingTime.value = workDuration
                }
            } catch (e: Exception) {
                // Keep default value if loading fails
                android.util.Log.w("TimerServiceManager", "Failed to load initial settings", e)
            }
        }

        private fun syncStateWithService() {
            service?.let { serviceInstance ->
                // Cancel previous flow collections to prevent memory leaks
                cancelFlowCollections()
                
                // Update local state flows with current service state
                _timerState.value = serviceInstance.timerState.value
                _remainingTime.value = serviceInstance.remainingTime.value
                _progress.value = serviceInstance.progress.value
                _cycleType.value = serviceInstance.cycleType.value
            
                // Set up continuous sync with proper job tracking
                timerStateJob = serviceInstance.timerState.onEach { _timerState.value = it }.launchIn(scope)
                remainingTimeJob = serviceInstance.remainingTime.onEach { _remainingTime.value = it }.launchIn(scope)
                progressJob = serviceInstance.progress.onEach { _progress.value = it }.launchIn(scope)
                cycleTypeJob = serviceInstance.cycleType.onEach { _cycleType.value = it }.launchIn(scope)
            }
        }

        private fun cancelFlowCollections() {
            timerStateJob?.cancel()
            remainingTimeJob?.cancel()
            progressJob?.cancel()
            cycleTypeJob?.cancel()
            
            timerStateJob = null
            remainingTimeJob = null
            progressJob = null
            cycleTypeJob = null
        }
    }
