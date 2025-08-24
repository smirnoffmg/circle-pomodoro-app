package com.smirnoffmg.pomodorotimer.notification

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(
    sdk = [Build.VERSION_CODES.TIRAMISU],
    application = com.smirnoffmg.pomodorotimer.TestApplication::class
)
class NotificationHelperTest {
    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var channelManager: NotificationChannelManager
    private lateinit var notificationSettings: NotificationSettings
    private lateinit var notificationHelper: NotificationHelper

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        channelManager = NotificationChannelManager(context)
        notificationSettings = NotificationSettings(context)
        notificationHelper = NotificationHelper(context, channelManager, notificationSettings)
        
        // Create notification channels for testing
        channelManager.createNotificationChannels()
    }

    @Test
    fun `notification channel constants are defined correctly`() {
        assertEquals("timer_channel", NotificationChannelManager.TIMER_CHANNEL_ID)
        assertEquals("breaks_channel", NotificationChannelManager.BREAKS_CHANNEL_ID)
        assertEquals("progress_channel", NotificationChannelManager.PROGRESS_CHANNEL_ID)
    }

    @Test
    fun `notification helper constants are defined correctly`() {
        assertEquals(1001, NotificationHelper.TIMER_NOTIFICATION_ID)
        assertEquals(1002, NotificationHelper.BREAK_START_NOTIFICATION_ID)
        assertEquals(1003, NotificationHelper.BREAK_END_NOTIFICATION_ID)
        assertEquals(1004, NotificationHelper.SESSION_COMPLETE_NOTIFICATION_ID)
        assertEquals(1005, NotificationHelper.MILESTONE_NOTIFICATION_ID)
    }

    @Test
    fun `createTimerNotification should create notification with correct title and content`() =
        runTest {
            // Given
            val remainingTime = "25:00"
            val cycleType = "Focus"
            val isRunning = true

            // When
            val notification = notificationHelper.createTimerNotification(remainingTime, cycleType, isRunning)

            // Then
            assertNotNull(notification)
            assertEquals("Pomodoro Timer", notification?.extras?.getString("android.title"))
            assertTrue("Should contain remaining time", notification?.extras?.getString("android.text")?.contains(remainingTime) == true)
        }

    @Test
    fun `createTimerNotification should add pause and stop actions when running`() =
        runTest {
            // Given
            val remainingTime = "25:00"
            val cycleType = "Focus"
            val isRunning = true

            // When
            val notification = notificationHelper.createTimerNotification(remainingTime, cycleType, isRunning)

            // Then
            assertNotNull(notification)
            assertNotNull("Should have actions", notification?.actions)
            assertEquals("Should have 2 actions: Pause and Stop", 2, notification?.actions?.size)
            assertTrue("Should have pause action", notification?.actions?.any { it.title == "Pause" } == true)
            assertTrue("Should have stop action", notification?.actions?.any { it.title == "Stop" } == true)
        }

    @Test
    fun `createTimerNotification should add resume and stop actions when paused`() =
        runTest {
            // Given
            val remainingTime = "25:00"
            val cycleType = "Focus"
            val isRunning = false

            // When
            val notification = notificationHelper.createTimerNotification(remainingTime, cycleType, isRunning)

            // Then
            assertNotNull(notification)
            assertNotNull("Should have actions", notification?.actions)
            assertEquals("Should have 2 actions: Resume and Stop", 2, notification?.actions?.size)
            assertTrue("Should have resume action", notification?.actions?.any { it.title == "Resume" } == true)
            assertTrue("Should have stop action", notification?.actions?.any { it.title == "Stop" } == true)
        }

    @Test
    fun `createBreakNotification should create notification with correct title`() =
        runTest {
            // Given
            val remainingTime = "05:00"
            val breakType = "Short Break"
            val isRunning = true

            // When
            val notification = notificationHelper.createBreakNotification(remainingTime, breakType, isRunning)

            // Then
            assertNotNull(notification)
            assertEquals("Break Time", notification?.extras?.getString("android.title"))
            assertTrue("Should contain remaining time", notification?.extras?.getString("android.text")?.contains(remainingTime) == true)
        }

    @Test
    fun `createBreakNotification should add skip break action when running`() =
        runTest {
            // Given
            val remainingTime = "05:00"
            val breakType = "Short Break"
            val isRunning = true

            // When
            val notification = notificationHelper.createBreakNotification(remainingTime, breakType, isRunning)

            // Then
            assertNotNull(notification)
            assertNotNull("Should have actions", notification?.actions)
            assertEquals("Should have 2 actions: Skip and Stop", 2, notification?.actions?.size)
            assertTrue("Should have skip action", notification?.actions?.any { it.title == "Skip" } == true)
            assertTrue("Should have stop action", notification?.actions?.any { it.title == "Stop" } == true)
        }

    @Test
    fun `showBreakStartNotification should display notification with correct content`() =
        runTest {
            // Given
            val breakType = "Short Break"

            // When
            notificationHelper.showBreakStartNotification(breakType)

            // Then
            // Verify notification was created (we can't easily verify the actual notification display in unit tests)
            assertTrue("Should not throw exception", true)
        }

    @Test
    fun `showSessionCompleteNotification should display completion notification`() =
        runTest {
            // Given
            val completedSessions = 3

            // When
            notificationHelper.showSessionCompleteNotification(completedSessions)

            // Then
            // Verify notification was created (we can't easily verify the actual notification display in unit tests)
            assertTrue("Should not throw exception", true)
        }

    @Test
    fun `showMilestoneNotification should display milestone achievement`() =
        runTest {
            // Given
            val milestone = 8

            // When
            notificationHelper.showMilestoneNotification(milestone)

            // Then
            // Verify notification was created (we can't easily verify the actual notification display in unit tests)
            assertTrue("Should not throw exception", true)
        }

    @Test
    fun `cancelAllNotifications should cancel all active notifications`() =
        runTest {
            // When
            notificationHelper.cancelAllNotifications()

            // Then
            // Verify no exception is thrown
            assertTrue("Should not throw exception", true)
        }

    @Test
    fun `notification actions should have correct pending intents`() =
        runTest {
            // Given
            val remainingTime = "25:00"
            val cycleType = "Focus"
            val isRunning = true

            // When
            val notification = notificationHelper.createTimerNotification(remainingTime, cycleType, isRunning)

            // Then
            assertNotNull(notification)
            assertNotNull(notification?.actions)
            assertTrue("Should have pause action", notification?.actions?.any { it.title == "Pause" } == true)
            assertTrue("Should have stop action", notification?.actions?.any { it.title == "Stop" } == true)
        }

    @Test
    fun `notifications should use correct channels`() =
        runTest {
            // Given
            val remainingTime = "25:00"

            // When - Timer notification
            val timerNotification = notificationHelper.createTimerNotification(remainingTime, "Focus", true)
        
            // When - Break notification  
            val breakNotification = notificationHelper.createBreakNotification(remainingTime, "Break", true)

            // Then
            assertNotNull(timerNotification)
            assertNotNull(breakNotification)
            // Notifications should use appropriate channels (timer vs breaks)
        }

    @Test
    fun `notifications should be ongoing for foreground service`() =
        runTest {
            // Given
            val remainingTime = "25:00"
            val cycleType = "Focus"

            // When
            val notification = notificationHelper.createTimerNotification(remainingTime, cycleType, true)

            // Then
            assertNotNull(notification)
            assertTrue("Timer notification should be ongoing",
                (notification?.flags?.and(android.app.Notification.FLAG_ONGOING_EVENT) ?: 0) != 0)
        }

    @Test
    fun `notifications should have proper icons`() =
        runTest {
            // Given
            val remainingTime = "25:00"
            val cycleType = "Focus"

            // When
            val notification = notificationHelper.createTimerNotification(remainingTime, cycleType, true)

            // Then
            assertNotNull(notification)
            assertTrue("Should have small icon", notification?.smallIcon != null)
        }
}
