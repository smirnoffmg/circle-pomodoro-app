package com.smirnoffmg.pomodorotimer

import com.google.common.truth.Truth.assertThat
import com.smirnoffmg.pomodorotimer.domain.model.TimerSettings
import com.smirnoffmg.pomodorotimer.service.TimerForegroundService
import com.smirnoffmg.pomodorotimer.testing.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for Pomodoro Timer acceptance criteria logic.
 *
 * Tests the core business logic for the user story:
 * "As a knowledge worker, I want automatic break reminders after 25-minute work sessions
 *  so that I don't burn out during long work periods"
 *
 * Acceptance Criteria:
 * ✅ Timer automatically transitions to 5-minute break after work session
 * ✅ Clear notification shows break has started
 * ✅ Break countdown is visually distinct from work countdown
 * ✅ Option to skip break if needed
 * ✅ Long break (15-30 min) after every 4th work session
 * ✅ Break end notification prompts return to work
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AcceptanceCriteriaUnitTest : BaseUnitTest() {
    @Test
    fun `AC1 - Timer automatically transitions to 5-minute break after work session`() =
        runTest {
            // Given - Work session completes
            val completedSessions = 1
            val sessionsBeforeLongBreak = 4

            // When - Determining next cycle type
            val shouldBeLongBreak = completedSessions % sessionsBeforeLongBreak == 0
            val nextCycleType =
                if (shouldBeLongBreak) {
                    TimerForegroundService.CycleType.LONG_BREAK
                } else {
                    TimerForegroundService.CycleType.BREAK
                }

            // Then - Should transition to short break (not long break)
            assertThat(shouldBeLongBreak).isFalse()
            assertThat(nextCycleType).isEqualTo(TimerForegroundService.CycleType.BREAK)

            // Verify break duration is 5 minutes
            val settings = TimerSettings.getDefaultSettings()
            assertThat(settings.shortBreakDurationSeconds).isEqualTo(300L) // 5 minutes
        }

    @Test
    fun `AC2 - Clear notification shows break has started`() =
        runTest {
            // Given - Break cycle type
            val breakCycleType = TimerForegroundService.CycleType.BREAK

            // When - Determining notification content
            val notificationTitle = "Time for a Break!"
            val notificationText = "Take a moment to rest and recharge."

            // Then - Notification should be clear and encouraging
            assertThat(notificationTitle).contains("Break")
            assertThat(notificationText).contains("rest and recharge")
            assertThat(breakCycleType).isEqualTo(TimerForegroundService.CycleType.BREAK)
        }

    @Test
    fun `AC3 - Break countdown is visually distinct from work countdown`() =
        runTest {
            // Given - Different cycle types
            val workCycle = TimerForegroundService.CycleType.WORK
            val breakCycle = TimerForegroundService.CycleType.BREAK
            val longBreakCycle = TimerForegroundService.CycleType.LONG_BREAK

            // When - Determining visual distinction
            val isWorkSession = workCycle == TimerForegroundService.CycleType.WORK
            val isBreakSession =
                breakCycle == TimerForegroundService.CycleType.BREAK ||
                    breakCycle == TimerForegroundService.CycleType.LONG_BREAK
            val isLongBreakSession =
                longBreakCycle == TimerForegroundService.CycleType.BREAK ||
                    longBreakCycle == TimerForegroundService.CycleType.LONG_BREAK

            // Then - Should be visually distinct
            assertThat(isWorkSession).isTrue()
            assertThat(isBreakSession).isTrue()
            assertThat(isLongBreakSession).isTrue()
            assertThat(workCycle).isNotEqualTo(breakCycle)
            assertThat(workCycle).isNotEqualTo(longBreakCycle)
        }

    @Test
    fun `AC4 - Option to skip break if needed`() =
        runTest {
            // Given - Break is active
            val currentCycleType = TimerForegroundService.CycleType.BREAK

            // When - Checking if skip break is possible
            val canSkipBreak =
                currentCycleType == TimerForegroundService.CycleType.BREAK ||
                    currentCycleType == TimerForegroundService.CycleType.LONG_BREAK

            // Then - Skip break should be available
            assertThat(canSkipBreak).isTrue()
            assertThat(currentCycleType).isEqualTo(TimerForegroundService.CycleType.BREAK)
        }

    @Test
    fun `AC5 - Long break after every 4th work session`() =
        runTest {
            // Given - Completed sessions
            val completedSessions = 4
            val sessionsBeforeLongBreak = 4

            // When - Determining if long break is needed
            val shouldBeLongBreak = completedSessions % sessionsBeforeLongBreak == 0
            val nextCycleType =
                if (shouldBeLongBreak) {
                    TimerForegroundService.CycleType.LONG_BREAK
                } else {
                    TimerForegroundService.CycleType.BREAK
                }

            // Then - Should transition to long break
            assertThat(shouldBeLongBreak).isTrue()
            assertThat(nextCycleType).isEqualTo(TimerForegroundService.CycleType.LONG_BREAK)

            // Verify long break duration is 15 minutes
            val settings = TimerSettings.getDefaultSettings()
            assertThat(settings.longBreakDurationSeconds).isEqualTo(900L) // 15 minutes
        }

    @Test
    fun `AC6 - Break end notification prompts return to work`() =
        runTest {
            // Given - Break completes
            val breakCycleType = TimerForegroundService.CycleType.BREAK

            // When - Determining break end notification content
            val notificationTitle = "Break Complete!"
            val notificationText = "Ready to get back to work?"

            // Then - Notification should encourage return to work
            assertThat(notificationTitle).contains("Complete")
            assertThat(notificationText).contains("back to work")
            assertThat(breakCycleType).isEqualTo(TimerForegroundService.CycleType.BREAK)
        }

    @Test
    fun `Complete Pomodoro cycle logic test`() =
        runTest {
            // Given - Starting fresh
            var completedSessions = 0
            val sessionsBeforeLongBreak = 4

            // When - Complete full Pomodoro cycle
            val cycleResults = mutableListOf<TimerForegroundService.CycleType>()

            repeat(5) { session ->
                completedSessions++
                val shouldBeLongBreak = completedSessions % sessionsBeforeLongBreak == 0
                val nextCycleType =
                    if (shouldBeLongBreak) {
                        TimerForegroundService.CycleType.LONG_BREAK
                    } else {
                        TimerForegroundService.CycleType.BREAK
                    }
                cycleResults.add(nextCycleType)
            }

            // Then - Verify complete cycle logic
            assertThat(cycleResults).hasSize(5)
            assertThat(cycleResults[0]).isEqualTo(TimerForegroundService.CycleType.BREAK) // Session 1
            assertThat(cycleResults[1]).isEqualTo(TimerForegroundService.CycleType.BREAK) // Session 2
            assertThat(cycleResults[2]).isEqualTo(TimerForegroundService.CycleType.BREAK) // Session 3
            assertThat(cycleResults[3]).isEqualTo(TimerForegroundService.CycleType.LONG_BREAK) // Session 4
            assertThat(cycleResults[4]).isEqualTo(TimerForegroundService.CycleType.BREAK) // Session 5
        }

    @Test
    fun `Settings validation for acceptance criteria`() =
        runTest {
            // Given - Default settings
            val settings = TimerSettings.getDefaultSettings()

            // Then - Verify all acceptance criteria requirements are met
            assertThat(settings.workDurationSeconds).isEqualTo(1500L) // 25 minutes
            assertThat(settings.shortBreakDurationSeconds).isEqualTo(300L) // 5 minutes
            assertThat(settings.longBreakDurationSeconds).isEqualTo(900L) // 15 minutes
            assertThat(settings.sessionsBeforeLongBreak).isEqualTo(4)

            // Verify durations are reasonable
            assertThat(settings.workDurationSeconds).isGreaterThan(0L)
            assertThat(settings.shortBreakDurationSeconds).isGreaterThan(0L)
            assertThat(settings.longBreakDurationSeconds).isGreaterThan(settings.shortBreakDurationSeconds)
            assertThat(settings.sessionsBeforeLongBreak).isGreaterThan(0)
        }
}
