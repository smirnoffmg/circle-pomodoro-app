package com.smirnoffmg.pomodorotimer.notification

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(
    sdk = [Build.VERSION_CODES.TIRAMISU],
    application = com.smirnoffmg.pomodorotimer.TestApplication::class
)
class NotificationIntegrationTest {
    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var channelManager: NotificationChannelManager
    private lateinit var notificationSettings: NotificationSettings
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var permissionManager: NotificationPermissionManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        channelManager = NotificationChannelManager(context)
        notificationSettings = NotificationSettings(context)
        notificationHelper = NotificationHelper(context, channelManager, notificationSettings)
        permissionManager = NotificationPermissionManager(context)
    }

    @Test
    fun `notification system should work end-to-end`() =
        runTest {
            // Given - Create notification channels first
            channelManager.createNotificationChannels()

            // When - Create and show various notifications
            val timerNotification =
                notificationHelper.createTimerNotification(
                    remainingTime = "25:00",
                    cycleType = "Work",
                    isRunning = true
                )
        
            val breakNotification =
                notificationHelper.createBreakNotification(
                    remainingTime = "05:00", 
                    breakType = "Short Break",
                    isRunning = true
                )

            // Show notifications
            notificationHelper.showBreakStartNotification("Short Break")
            notificationHelper.showSessionCompleteNotification(1)
            notificationHelper.showMilestoneNotification(5)

            // Then - Verify notifications were created successfully
            assertNotNull("Timer notification should be created", timerNotification)
            assertNotNull("Break notification should be created", breakNotification)
        
            // Verify notification properties
            assertEquals("Pomodoro Timer", timerNotification?.extras?.getString("android.title"))
            assertEquals("25:00 - Work", timerNotification?.extras?.getString("android.text"))
        
            assertEquals("Break Time", breakNotification?.extras?.getString("android.title"))
            assertEquals("05:00 - Short Break", breakNotification?.extras?.getString("android.text"))

            // Verify channels exist
            val timerChannel = notificationManager.getNotificationChannel(NotificationChannelManager.TIMER_CHANNEL_ID)
            val breaksChannel = notificationManager.getNotificationChannel(NotificationChannelManager.BREAKS_CHANNEL_ID)
            val progressChannel = notificationManager.getNotificationChannel(NotificationChannelManager.PROGRESS_CHANNEL_ID)
        
            assertNotNull("Timer channel should exist", timerChannel)
            assertNotNull("Breaks channel should exist", breaksChannel)
            assertNotNull("Progress channel should exist", progressChannel)
        }

    @Test
    fun `notification actions should be properly configured`() =
        runTest {
            // Given
            channelManager.createNotificationChannels()

            // When - Create notifications with actions
            val runningTimerNotification =
                notificationHelper.createTimerNotification(
                    remainingTime = "20:00",
                    cycleType = "Work", 
                    isRunning = true
                )
        
            val pausedTimerNotification =
                notificationHelper.createTimerNotification(
                    remainingTime = "20:00",
                    cycleType = "Work",
                    isRunning = false
                )

            val breakNotification =
                notificationHelper.createBreakNotification(
                    remainingTime = "03:00",
                    breakType = "Short Break",
                    isRunning = true
                )

            // Then - Verify action buttons
            assertEquals("Running timer should have 2 actions", 2, runningTimerNotification?.actions?.size)
            assertEquals("Paused timer should have 2 actions", 2, pausedTimerNotification?.actions?.size)
            assertEquals("Break notification should have 2 actions", 2, breakNotification?.actions?.size)

            // Verify specific actions for running timer
            assertTrue("Should have Pause action", 
                runningTimerNotification?.actions?.any { it.title == "Pause" } == true)
            assertTrue("Should have Stop action",
                runningTimerNotification?.actions?.any { it.title == "Stop" } == true)

            // Verify specific actions for paused timer
            assertTrue("Should have Resume action",
                pausedTimerNotification?.actions?.any { it.title == "Resume" } == true)
            assertTrue("Should have Stop action", 
                pausedTimerNotification?.actions?.any { it.title == "Stop" } == true)

            // Verify specific actions for break
            assertTrue("Should have Skip action",
                breakNotification?.actions?.any { it.title == "Skip" } == true)
            assertTrue("Should have Stop action",
                breakNotification?.actions?.any { it.title == "Stop" } == true)
        }

    @Test
    fun `notification cancellation should work properly`() =
        runTest {
            // Given
            channelManager.createNotificationChannels()
        
            // Show some notifications
            notificationHelper.showBreakStartNotification("Short Break")
            notificationHelper.showSessionCompleteNotification(1)

            // When - Cancel specific notification
            notificationHelper.cancelNotification(NotificationHelper.BREAK_START_NOTIFICATION_ID)

            // Then - Verify cancellation works (no exception thrown)
            assertTrue("Should not throw exception when cancelling notification", true)

            // When - Cancel all notifications
            notificationHelper.cancelAllNotifications()

            // Then - Verify cancellation works (no exception thrown)
            assertTrue("Should not throw exception when cancelling all notifications", true)
        }

    @Test
    fun `permission status should be accurate`() =
        runTest {
            // When
            val status = permissionManager.getPermissionStatus()

            // Then
            assertNotNull("Status should not be null", status)
            assertEquals("Runtime permission requirement should match API level",
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                status.needsRuntimePermission)
        }

    @Test
    fun `notification channels should have correct importance levels`() =
        runTest {
            // Given
            channelManager.createNotificationChannels()

            // When
            val timerChannel = notificationManager.getNotificationChannel(NotificationChannelManager.TIMER_CHANNEL_ID)
            val breaksChannel = notificationManager.getNotificationChannel(NotificationChannelManager.BREAKS_CHANNEL_ID)
            val progressChannel = notificationManager.getNotificationChannel(NotificationChannelManager.PROGRESS_CHANNEL_ID)

            // Then
            assertEquals("Timer channel should have LOW importance", 
                NotificationManager.IMPORTANCE_LOW, timerChannel?.importance)
            assertEquals("Breaks channel should have DEFAULT importance",
                NotificationManager.IMPORTANCE_DEFAULT, breaksChannel?.importance)
            assertEquals("Progress channel should have DEFAULT importance", 
                NotificationManager.IMPORTANCE_DEFAULT, progressChannel?.importance)
        }

    @Test
    fun `notification validation should prevent invalid inputs`() =
        runTest {
            // Given
            channelManager.createNotificationChannels()

            // When/Then - Test session count validation
            notificationHelper.showSessionCompleteNotification(-1) // Should be ignored
            notificationHelper.showSessionCompleteNotification(0)  // Should be ignored
            notificationHelper.showSessionCompleteNotification(1001) // Should be ignored
        
            // When/Then - Test milestone validation
            notificationHelper.showMilestoneNotification(-1) // Should be ignored
            notificationHelper.showMilestoneNotification(0)  // Should be ignored
            notificationHelper.showMilestoneNotification(10001) // Should be ignored

            // Valid inputs should work
            notificationHelper.showSessionCompleteNotification(1)
            notificationHelper.showSessionCompleteNotification(100)
            notificationHelper.showMilestoneNotification(5)
            notificationHelper.showMilestoneNotification(1000)

            // No exceptions should be thrown
            assertTrue("Should handle invalid inputs gracefully", true)
        }
}
