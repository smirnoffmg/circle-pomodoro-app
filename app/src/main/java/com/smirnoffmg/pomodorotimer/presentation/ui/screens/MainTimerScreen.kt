package com.smirnoffmg.pomodorotimer.presentation.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import com.smirnoffmg.pomodorotimer.R
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.TimerState
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.MainTimerViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

/**
 * Main timer screen following Material Design 3 principles.
 * Provides a clean, accessible interface for the Pomodoro timer.
 */
@Composable
fun MainTimerScreen(
    modifier: Modifier = Modifier,
    viewModel: MainTimerViewModel = hiltViewModel()
) {
    // Get timer state from ViewModel
    val timerState by viewModel.timerState.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val progress by viewModel.progress.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Timer Display Card
            TimerDisplayCard(
                remainingTime = remainingTime,
                progress = progress,
                timerState = timerState,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))

                        // Timer Controls
            TimerControls(
                timerState = timerState,
                onStartClick = {
                    viewModel.startTimer()
                },
                onPauseClick = { 
                    viewModel.pauseTimer()
                },
                onStopClick = { 
                    viewModel.stopTimer()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Timer display card with circular progress indicator and time display.
 * Follows Material Design 3 card design principles.
 */
@Composable
private fun TimerDisplayCard(
    remainingTime: Long,
    progress: Float,
    timerState: TimerState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Progress Indicator
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(300),
                    label = "progress_animation"
                )

                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .size(280.dp)
                        .testTag("timer_progress"),
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round,
                    color = when (timerState) {
                        TimerState.RUNNING -> MaterialTheme.colorScheme.primary
                        TimerState.PAUSED -> MaterialTheme.colorScheme.secondary
                        TimerState.STOPPED -> MaterialTheme.colorScheme.outline
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                // Time Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatTime(remainingTime),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("timer_display")
                    )

                    Text(
                        text = getTimerStateText(timerState),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("timer_state")
                    )
                }
            }
        }
    }
}

/**
 * Timer control buttons following Material Design 3 button hierarchy.
 * Provides accessible controls for timer operations.
 */
@Composable
private fun TimerControls(
    timerState: TimerState,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stop Button (always visible)
        OutlinedButton(
            onClick = onStopClick,
            modifier = Modifier
                .weight(1f)
                .testTag("stop_button"),
            enabled = timerState != TimerState.STOPPED
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = stringResource(R.string.stop_timer),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(text = stringResource(R.string.stop))
        }

        // Start/Pause Button
        Button(
            onClick = if (timerState == TimerState.RUNNING) onPauseClick else onStartClick,
            modifier = Modifier
                .weight(1f)
                .testTag("start_pause_button"),
            enabled = true // Always enabled since it handles both start and pause
        ) {
            Icon(
                imageVector = if (timerState == TimerState.RUNNING) {
                    Icons.Default.Pause
                } else {
                    Icons.Default.PlayArrow
                },
                contentDescription = if (timerState == TimerState.RUNNING) {
                    stringResource(R.string.pause_timer)
                } else {
                    stringResource(R.string.start_timer)
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = if (timerState == TimerState.RUNNING) {
                    stringResource(R.string.pause)
                } else {
                    stringResource(R.string.start)
                }
            )
        }
    }
}

/**
 * Formats time in MM:SS format for display.
 * Follows KISS principle with simple formatting logic.
 */
private fun formatTime(timeInSeconds: Long): String {
    val minutes = timeInSeconds / 60
    val seconds = timeInSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

/**
 * Gets the appropriate text for the current timer state.
 * Provides clear state indication to users.
 */
private fun getTimerStateText(timerState: TimerState): String {
    return when (timerState) {
        TimerState.RUNNING -> "Focus Time"
        TimerState.PAUSED -> "Paused"
        TimerState.STOPPED -> "Ready to Start"
    }
}

@Preview(showBackground = true)
@Composable
private fun MainTimerScreenPreview() {
    MainTimerScreen()
}
