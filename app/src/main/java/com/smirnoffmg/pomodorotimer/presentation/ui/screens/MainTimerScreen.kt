package com.smirnoffmg.pomodorotimer.presentation.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.TimerState
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.MainTimerViewModel
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Circle - Minimalistic Pomodoro Timer Screen
 * 
 * Single-focus design with subtle visual feedback for discoverable interaction.
 * Follows Circle concept principles: zero cognitive overhead, immediate value delivery.
 */
@Composable
fun MainTimerScreen(
    modifier: Modifier = Modifier,
    viewModel: MainTimerViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val progress by viewModel.progress.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Primary Circular Timer Display with Subtle Feedback
            CircularTimerDisplay(
                remainingTime = remainingTime,
                progress = progress,
                timerState = timerState,
                onTimerClick = {
                    when (timerState) {
                        TimerState.RUNNING -> viewModel.pauseTimer()
                        else -> viewModel.startTimer()
                    }
                }
            )

            // Secondary Stop Control (minimal, positioned at bottom)
            TimerSecondaryControl(
                timerState = timerState,
                onStopClick = { viewModel.stopTimer() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            )
        }
    }
}

/**
 * Primary circular timer display with subtle visual feedback.
 * Large, interactive circular progress indicator with clean design.
 */
@Composable
private fun CircularTimerDisplay(
    remainingTime: Long,
    progress: Float,
    timerState: TimerState,
    onTimerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Box(
        modifier = modifier
            .size(320.dp)
            .testTag("circular_timer_display"),
        contentAlignment = Alignment.Center
    ) {
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(300),
            label = "progress_animation"
        )

        // Large Circular Progress Indicator with Subtle Visual Feedback
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .size(320.dp)
                .scale(if (isPressed) 0.98f else 1f)
                .testTag("timer_progress"),
            strokeWidth = 16.dp,
            strokeCap = StrokeCap.Round,
            color = getTimerColor(timerState),
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )

        // Time Display (dominant text)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.testTag("time_display")
        ) {
            Text(
                text = formatTime(remainingTime),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Light
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = getTimerStateText(timerState),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Clickable Area with Subtle Visual Feedback
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null // No ripple for clean aesthetic
                ) { onTimerClick() }
                .testTag("timer_click_area"),
            contentAlignment = Alignment.Center
        ) {
            // Invisible clickable area
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .testTag("timer_click_target")
            )
        }
    }
}

/**
 * Secondary control - minimal, non-intrusive.
 * Only essential stop functionality to maintain single-focus design.
 */
@Composable
private fun TimerSecondaryControl(
    timerState: TimerState,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (timerState != TimerState.STOPPED) {
        FloatingActionButton(
            onClick = onStopClick,
            modifier = modifier.testTag("stop_button"),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop Timer",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Get timer color based on state - follows Circle's color psychology.
 * Calming blues/greens for focus, warm tones for breaks.
 */
@Composable
private fun getTimerColor(timerState: TimerState) = when (timerState) {
    TimerState.RUNNING -> MaterialTheme.colorScheme.primary
    TimerState.PAUSED -> MaterialTheme.colorScheme.secondary
    TimerState.STOPPED -> MaterialTheme.colorScheme.outline
}

/**
 * Format time in MM:SS format - clean, minimal display.
 */
private fun formatTime(timeInSeconds: Long): String {
    val minutes = timeInSeconds / 60
    val seconds = timeInSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

/**
 * Get timer state text - minimal, clear indication.
 */
private fun getTimerStateText(timerState: TimerState): String = when (timerState) {
    TimerState.RUNNING -> "Focus"
    TimerState.PAUSED -> "Paused"
    TimerState.STOPPED -> "Ready"
}

@Preview(showBackground = true)
@Composable
private fun MainTimerScreenPreview() {
    MainTimerScreen()
}
