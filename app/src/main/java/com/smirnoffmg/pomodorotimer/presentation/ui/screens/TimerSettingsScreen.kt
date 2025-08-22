package com.smirnoffmg.pomodorotimer.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smirnoffmg.pomodorotimer.domain.model.TimerSettings
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.TimerSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TimerSettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var workDuration by remember { mutableStateOf(25) }
    var shortBreakDuration by remember { mutableStateOf(5) }
    var longBreakDuration by remember { mutableStateOf(15) }
    var sessionsBeforeLongBreak by remember { mutableStateOf(4) }
           
    // Initialize local state from settings only once
    LaunchedEffect(settings) {
        settings?.let { currentSettings ->
            workDuration = currentSettings.workDurationMinutes
            shortBreakDuration = currentSettings.shortBreakDurationMinutes
            longBreakDuration = currentSettings.longBreakDurationMinutes
            sessionsBeforeLongBreak = currentSettings.sessionsBeforeLongBreak
        }
    }
    
    // Auto-save settings when values change with debouncing
    LaunchedEffect(workDuration, shortBreakDuration, longBreakDuration, sessionsBeforeLongBreak) {
        // Only save if we have loaded settings at least once
        if (settings != null) {
            // Add small delay to debounce rapid changes
            kotlinx.coroutines.delay(300)
            val newSettings =
                TimerSettings(
                    workDurationMinutes = workDuration,
                    shortBreakDurationMinutes = shortBreakDuration,
                    longBreakDurationMinutes = longBreakDuration,
                    sessionsBeforeLongBreak = sessionsBeforeLongBreak
                )
            viewModel.saveSettings(newSettings)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timer Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetToDefaults() }) {
                        Icon(Icons.Default.Restore, contentDescription = "Reset to Defaults")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Work Duration Setting
            DurationSettingCard(
                title = "Work Session Duration",
                subtitle = "How long each focus session lasts",
                value = workDuration,
                onValueChange = { workDuration = it },
                minValue = TimerSettings.MIN_WORK_DURATION,
                maxValue = TimerSettings.MAX_WORK_DURATION,
                unit = "minutes"
            )
            
            // Short Break Duration Setting
            DurationSettingCard(
                title = "Short Break Duration",
                subtitle = "Duration of regular breaks between work sessions",
                value = shortBreakDuration,
                onValueChange = { shortBreakDuration = it },
                minValue = TimerSettings.MIN_BREAK_DURATION,
                maxValue = TimerSettings.MAX_BREAK_DURATION,
                unit = "minutes"
            )
            
            // Long Break Duration Setting
            DurationSettingCard(
                title = "Long Break Duration",
                subtitle = "Duration of extended breaks after multiple work sessions",
                value = longBreakDuration,
                onValueChange = { longBreakDuration = it },
                minValue = TimerSettings.MIN_BREAK_DURATION,
                maxValue = TimerSettings.MAX_BREAK_DURATION,
                unit = "minutes"
            )
            
            // Sessions Before Long Break Setting
            DurationSettingCard(
                title = "Sessions Before Long Break",
                subtitle = "Number of work sessions before taking a long break",
                value = sessionsBeforeLongBreak,
                onValueChange = { sessionsBeforeLongBreak = it },
                minValue = TimerSettings.MIN_SESSIONS_BEFORE_LONG_BREAK,
                maxValue = TimerSettings.MAX_SESSIONS_BEFORE_LONG_BREAK,
                unit = "sessions"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
                   
            // Auto-save indicator and error display
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Settings are saved automatically",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DurationSettingCard(
    title: String,
    subtitle: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int,
    maxValue: Int,
    unit: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$minValue $unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "$value $unit",
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "$maxValue $unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = minValue.toFloat()..maxValue.toFloat(),
                steps = (maxValue - minValue - 1).coerceAtLeast(0)
            )
        }
    }
}
