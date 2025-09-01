package com.smirnoffmg.pomodorotimer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smirnoffmg.pomodorotimer.domain.model.TimerSettings
import com.smirnoffmg.pomodorotimer.domain.usecase.GetTimerSettingsUseCase
import com.smirnoffmg.pomodorotimer.domain.usecase.ResetTimerSettingsUseCase
import com.smirnoffmg.pomodorotimer.domain.usecase.SaveTimerSettingsUseCase
import com.smirnoffmg.pomodorotimer.service.TimerServiceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerSettingsViewModel
    @Inject
    constructor(
        private val getTimerSettingsUseCase: GetTimerSettingsUseCase,
        private val saveTimerSettingsUseCase: SaveTimerSettingsUseCase,
        private val resetTimerSettingsUseCase: ResetTimerSettingsUseCase,
        private val timerServiceManager: TimerServiceManager,
    ) : ViewModel() {
        private val _settings = MutableStateFlow<TimerSettings?>(null)
        val settings: StateFlow<TimerSettings?> = _settings.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        private val _errorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

        init {
            loadSettings()
        }

        private fun loadSettings() {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    // Load settings once instead of continuous collection
                    val settings = getTimerSettingsUseCase().first()
                    _settings.value = settings ?: TimerSettings.getDefaultSettings()
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to load settings: ${e.message}"
                    _settings.value = TimerSettings.getDefaultSettings()
                } finally {
                    _isLoading.value = false
                }
            }
        }

        fun saveSettings(settings: TimerSettings) {
            viewModelScope.launch {
                try {
                    _errorMessage.value = null
                    val result = saveTimerSettingsUseCase(settings)
                    result.fold(
                        onSuccess = {
                            // Settings saved successfully - notify timer service immediately
                            timerServiceManager.reloadSettings()
                        },
                        onFailure = { exception ->
                            _errorMessage.value = "Failed to save settings: ${exception.message}"
                        },
                    )
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to save settings: ${e.message}"
                }
            }
        }

        fun resetToDefaults() {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    _errorMessage.value = null

                    val result = resetTimerSettingsUseCase()
                    result.fold(
                        onSuccess = {
                            _settings.value = TimerSettings.getDefaultSettings()
                            // Notify timer service of reset settings
                            timerServiceManager.reloadSettings()
                        },
                        onFailure = { exception ->
                            _errorMessage.value = "Failed to reset settings: ${exception.message}"
                        },
                    )
                } finally {
                    _isLoading.value = false
                }
            }
        }

        fun clearError() {
            _errorMessage.value = null
        }
    }
