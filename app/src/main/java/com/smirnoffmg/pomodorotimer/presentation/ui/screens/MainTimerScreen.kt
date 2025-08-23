package com.smirnoffmg.pomodorotimer.presentation.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.TimerState
import com.smirnoffmg.pomodorotimer.presentation.viewmodel.MainTimerViewModel
import com.smirnoffmg.pomodorotimer.service.TimerForegroundService
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
    onNavigateToSettings: () -> Unit = {},
    viewModel: MainTimerViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val dailyStatistics by viewModel.dailyStatistics.collectAsState()
    val showCelebration by viewModel.showCelebration.collectAsState()
    val showSettingsChangedMessage by viewModel.showSettingsChangedMessage.collectAsState()
    
    val cycleType by viewModel.cycleType.collectAsState()
    val isBreakSession =
        cycleType == TimerForegroundService.CycleType.BREAK || 
            cycleType == TimerForegroundService.CycleType.LONG_BREAK

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Daily Session Counter (top of screen)
            DailySessionCounter(
                dailyStatistics = dailyStatistics,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 32.dp, start = 80.dp, end = 80.dp)
            )

            // Settings button (top-right corner with proper spacing)
            FloatingActionButton(
                onClick = onNavigateToSettings,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 32.dp, end = 16.dp)
                        .testTag("settings_button"),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Primary Circular Timer Display with Subtle Feedback
            CircularTimerDisplay(
                remainingTime = remainingTime,
                progress = progress,
                timerState = timerState,
                cycleType = cycleType,
                onTimerClick = {
                    when (timerState) {
                        TimerState.RUNNING -> viewModel.pauseTimer()
                        else -> viewModel.startTimer()
                    }
                }
            )

            // Secondary Controls (minimal, positioned at bottom)
            TimerSecondaryControls(
                timerState = timerState,
                isBreakSession = isBreakSession,
                onStopClick = { viewModel.stopTimer() },
                onSkipBreakClick = { viewModel.skipBreak() },
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
            )

            // Celebration Overlay
            if (showCelebration) {
                CelebrationOverlay(
                    onDismiss = { viewModel.dismissCelebration() }
                )
            }

            // Settings Changed Message
            if (showSettingsChangedMessage) {
                SettingsChangedMessage(
                    onDismiss = { viewModel.dismissSettingsChangedMessage() }
                )
            }
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
    cycleType: TimerForegroundService.CycleType,
    onTimerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Box(
        modifier =
            modifier
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
            modifier =
                Modifier
                    .size(320.dp)
                    .scale(if (isPressed) 0.98f else 1f)
                    .testTag("timer_progress"),
            strokeWidth = 16.dp,
            strokeCap = StrokeCap.Round,
            color = getTimerColor(timerState, cycleType),
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )

        // Time Display (dominant text)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.testTag("time_display")
        ) {
            Text(
                text = formatTime(remainingTime),
                style =
                    MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Light
                    ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = getTimerStateText(timerState, cycleType),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Clickable Area with Subtle Visual Feedback
        Box(
            modifier =
                Modifier
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
                modifier =
                    Modifier
                        .size(280.dp)
                        .testTag("timer_click_target")
            )
        }
    }
}

/**
 * Daily session counter with progress indicator.
 * Shows completed pomodoros for the current day with visual progress.
 */
@Composable
private fun DailySessionCounter(
    dailyStatistics: com.smirnoffmg.pomodorotimer.domain.model.DailyStatistics,
    modifier: Modifier = Modifier
) {
    val dailyGoal = 8 // Default daily goal
    val completedSessions = dailyStatistics.workSessions
    val progress = (completedSessions.toFloat() / dailyGoal).coerceIn(0f, 1f)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .testTag("daily_session_counter"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Session counter text
        Text(
            text = "$completedSessions/$dailyGoal",
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Progress indicator
        LinearProgressIndicator(
            progress = { progress },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .testTag("daily_progress_indicator"),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Session type breakdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SessionTypeChip(
                label = "Work",
                count = dailyStatistics.workSessions,
                color = MaterialTheme.colorScheme.primary
            )
            SessionTypeChip(
                label = "Break",
                count = dailyStatistics.shortBreakSessions + dailyStatistics.longBreakSessions,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

/**
 * Session type chip showing count for a specific session type.
 */
@Composable
private fun SessionTypeChip(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Secondary controls - minimal, non-intrusive.
 * Shows stop button and skip break button when appropriate.
 */
@Composable
private fun TimerSecondaryControls(
    timerState: TimerState,
    isBreakSession: Boolean,
    onStopClick: () -> Unit,
    onSkipBreakClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (timerState != TimerState.STOPPED) {
            // During break sessions, show skip break button instead of stop button
            if (isBreakSession && timerState == TimerState.RUNNING) {
                FloatingActionButton(
                    onClick = onSkipBreakClick,
                    modifier = Modifier.testTag("skip_break_button"),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Skip Break",
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                // Show stop button for work sessions or paused states
                FloatingActionButton(
                    onClick = onStopClick,
                    modifier = Modifier.testTag("stop_button"),
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
    }
}

/**
 * Celebration overlay for when daily goals are reached.
 * Simple, joyful animation to motivate users.
 */
@Composable
private fun CelebrationOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(500),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
        label = "celebration_scale"
    )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("celebration_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Semi-transparent background
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clickable { onDismiss() }
        )

        // Celebration content
        Column(
            modifier =
                Modifier
                    .scale(scale)
                    .testTag("celebration_content"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Goal Achieved",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "🎉 Daily Goal Achieved! 🎉",
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You've completed 8 pomodoros today!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Get timer color based on state and cycle type - follows Circle's color psychology.
 * Calming blues/greens for focus, warm tones for breaks.
 */
@Composable
private fun getTimerColor(
    timerState: TimerState,
    cycleType: TimerForegroundService.CycleType
) = when {
    timerState == TimerState.RUNNING && cycleType == TimerForegroundService.CycleType.WORK -> 
        MaterialTheme.colorScheme.primary
    timerState == TimerState.RUNNING &&
        (cycleType == TimerForegroundService.CycleType.BREAK || cycleType == TimerForegroundService.CycleType.LONG_BREAK) -> 
        MaterialTheme.colorScheme.secondary
    timerState == TimerState.PAUSED -> 
        MaterialTheme.colorScheme.secondary
    else -> 
        MaterialTheme.colorScheme.outline
}

/**
 * Format time in MM:SS format - clean, minimal display.
 */
private fun formatTime(timeInSeconds: Long): String =
    com.smirnoffmg.pomodorotimer.utils.TimeFormatter
        .formatTime(timeInSeconds)

/**
 * Settings changed message overlay - informs user that settings won't affect current timer.
 */
@Composable
private fun SettingsChangedMessage(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("settings_changed_message"),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = "Settings updated. New timers will use these settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Get timer state text - minimal, clear indication.
 */
private fun getTimerStateText(
    timerState: TimerState,
    cycleType: TimerForegroundService.CycleType
): String =
    when (timerState) {
        TimerState.RUNNING ->
            when (cycleType) {
                TimerForegroundService.CycleType.WORK -> "Focus"
                TimerForegroundService.CycleType.BREAK -> "Break"
                TimerForegroundService.CycleType.LONG_BREAK -> "Long Break"
            }
        TimerState.PAUSED ->
            when (cycleType) {
                TimerForegroundService.CycleType.WORK -> "Focus Paused"
                TimerForegroundService.CycleType.BREAK -> "Break Paused" 
                TimerForegroundService.CycleType.LONG_BREAK -> "Long Break Paused"
            }
        TimerState.STOPPED -> "Ready"
    }

@Preview(showBackground = true)
@Composable
private fun MainTimerScreenPreview() {
    MainTimerScreen()
}
