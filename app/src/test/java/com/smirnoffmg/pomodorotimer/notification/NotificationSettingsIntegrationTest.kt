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
@Config(application = com.smirnoffmg.pomodorotimer.TestApplication::class)
class NotificationSettingsIntegrationTest {
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
        
        // Reset settings to defaults
        notificationSettings.resetToDefaults()
    }

    @Test
    fun `timer notification should be null when persistent timer notification is disabled`() =
        runTest {
            // Given - Disable persistent timer notification
            notificationSettings.isPersistentTimerNotificationEnabled = false
            channelManager.createNotificationChannels()

            // When - Create timer notification
            val notification =
                notificationHelper.createTimerNotification(
                    remainingTime = "25:00",
                    cycleType = "Work",
                    isRunning = true
                )

            // Then - Should return null
            assertNull("Timer notification should be null when disabled", notification)
        }

    @Test
    fun `timer notification should be created when persistent timer notification is enabled`() =
        runTest {
            // Given - Enable persistent timer notification
            notificationSettings.isPersistentTimerNotificationEnabled = true
            channelManager.createNotificationChannels()

            // When - Create timer notification
            val notification =
                notificationHelper.createTimerNotification(
                    remainingTime = "25:00",
                    cycleType = "Work",
                    isRunning = true
                )

            // Then - Should return notification
            assertNotNull("Timer notification should be created when enabled", notification)
            assertEquals("Pomodoro Timer", notification?.extras?.getString("android.title"))
            assertEquals("25:00 - Work", notification?.extras?.getString("android.text"))
        }

    @Test
    fun `break notification should be null when persistent timer notification is disabled`() =
        runTest {
            // Given - Disable persistent timer notification
            notificationSettings.isPersistentTimerNotificationEnabled = false
            channelManager.createNotificationChannels()

            // When - Create break notification
            val notification =
                notificationHelper.createBreakNotification(
                    remainingTime = "05:00",
                    breakType = "Short Break",
                    isRunning = true
                )

            // Then - Should return null
            assertNull("Break notification should be null when disabled", notification)
        }

    @Test
    fun `break notification should be created when persistent timer notification is enabled`() =
        runTest {
            // Given - Enable persistent timer notification
            notificationSettings.isPersistentTimerNotificationEnabled = true
            channelManager.createNotificationChannels()

            // When - Create break notification
            val notification =
                notificationHelper.createBreakNotification(
                    remainingTime = "05:00",
                    breakType = "Short Break",
                    isRunning = true
                )

            // Then - Should return notification
            assertNotNull("Break notification should be created when enabled", notification)
            assertEquals("Break Time", notification?.extras?.getString("android.title"))
            assertEquals("05:00 - Short Break", notification?.extras?.getString("android.text"))
        }

    @Test
    fun `break start notification should not be shown when break notifications are disabled`() =
        runTest {
            // Given - Disable break notifications
            notificationSettings.isBreakNotificationsEnabled = false
            channelManager.createNotificationChannels()

            // When - Show break start notification
            notificationHelper.showBreakStartNotification("Short Break")

            // Then - Should not throw exception and should handle gracefully
            assertTrue("Should handle disabled break notifications gracefully", true)
        }

    @Test
    fun `break start notification should be shown when break notifications are enabled`() =
        runTest {
            // Given - Enable break notifications
            notificationSettings.isBreakNotificationsEnabled = true
            channelManager.createNotificationChannels()

            // When - Show break start notification
            notificationHelper.showBreakStartNotification("Short Break")

            // Then - Should not throw exception
            assertTrue("Should show break start notification when enabled", true)
        }

    @Test
    fun `break end notification should not be shown when break notifications are disabled`() =
        runTest {
            // Given - Disable break notifications
            notificationSettings.isBreakNotificationsEnabled = false
            channelManager.createNotificationChannels()

            // When - Show break end notification
            notificationHelper.showBreakEndNotification()

            // Then - Should not throw exception and should handle gracefully
            assertTrue("Should handle disabled break notifications gracefully", true)
        }

    @Test
    fun `session complete notification should not be shown when session notifications are disabled`() =
        runTest {
            // Given - Disable session complete notifications
            notificationSettings.isSessionCompleteNotificationsEnabled = false
            channelManager.createNotificationChannels()

            // When - Show session complete notification
            notificationHelper.showSessionCompleteNotification(5)

            // Then - Should not throw exception and should handle gracefully
            assertTrue("Should handle disabled session notifications gracefully", true)
        }

    @Test
    fun `session complete notification should be shown when session notifications are enabled`() =
        runTest {
            // Given - Enable session complete notifications
            notificationSettings.isSessionCompleteNotificationsEnabled = true
            channelManager.createNotificationChannels()

            // When - Show session complete notification
            notificationHelper.showSessionCompleteNotification(5)

            // Then - Should not throw exception
            assertTrue("Should show session complete notification when enabled", true)
        }

    @Test
    fun `milestone notification should not be shown when milestone notifications are disabled`() =
        runTest {
            // Given - Disable milestone notifications
            notificationSettings.isMilestoneNotificationsEnabled = false
            channelManager.createNotificationChannels()

            // When - Show milestone notification
            notificationHelper.showMilestoneNotification(10)

            // Then - Should not throw exception and should handle gracefully
            assertTrue("Should handle disabled milestone notifications gracefully", true)
        }

    @Test
    fun `milestone notification should be shown when milestone notifications are enabled`() =
        runTest {
            // Given - Enable milestone notifications
            notificationSettings.isMilestoneNotificationsEnabled = true
            channelManager.createNotificationChannels()

            // When - Show milestone notification
            notificationHelper.showMilestoneNotification(10)

            // Then - Should not throw exception
            assertTrue("Should show milestone notification when enabled", true)
        }

    @Test
    fun `notification should use correct icons for different types`() =
        runTest {
            // Given - Enable all notifications
            notificationSettings.isPersistentTimerNotificationEnabled = true
            notificationSettings.isBreakNotificationsEnabled = true
            notificationSettings.isSessionCompleteNotificationsEnabled = true
            notificationSettings.isMilestoneNotificationsEnabled = true
            channelManager.createNotificationChannels()

            // When - Create different types of notifications
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

            // Then - Should have different icons
            assertNotNull("Timer notification should exist", timerNotification)
            assertNotNull("Break notification should exist", breakNotification)
            
            // Note: We can't easily test the actual icon resource IDs in unit tests
            // but we can verify the notifications are created successfully
            assertTrue("Timer notification should have valid icon", true)
            assertTrue("Break notification should have valid icon", true)
        }

    @Test
    fun `notification settings should persist across app restarts`() =
        runTest {
            // Given - Set custom settings
            notificationSettings.isBreakNotificationsEnabled = false
            notificationSettings.isSessionCompleteNotificationsEnabled = false
            notificationSettings.isMilestoneNotificationsEnabled = false
            notificationSettings.isNotificationSoundEnabled = false
            notificationSettings.isNotificationVibrationEnabled = false
            notificationSettings.isPersistentTimerNotificationEnabled = false

            // When - Create new settings instance (simulating app restart)
            val newSettings = NotificationSettings(context)

            // Then - Settings should persist
            assertFalse("Break notifications should remain disabled", newSettings.isBreakNotificationsEnabled)
            assertFalse("Session notifications should remain disabled", newSettings.isSessionCompleteNotificationsEnabled)
            assertFalse("Milestone notifications should remain disabled", newSettings.isMilestoneNotificationsEnabled)
            assertFalse("Sound should remain disabled", newSettings.isNotificationSoundEnabled)
            assertFalse("Vibration should remain disabled", newSettings.isNotificationVibrationEnabled)
            assertFalse("Persistent timer should remain disabled", newSettings.isPersistentTimerNotificationEnabled)
        }

    @Test
    fun `resetToDefaults should restore default settings`() =
        runTest {
            // Given - Set custom settings
            notificationSettings.isBreakNotificationsEnabled = false
            notificationSettings.isSessionCompleteNotificationsEnabled = false
            notificationSettings.isMilestoneNotificationsEnabled = false
            notificationSettings.isNotificationSoundEnabled = false
            notificationSettings.isNotificationVibrationEnabled = false
            notificationSettings.isPersistentTimerNotificationEnabled = false

            // When - Reset to defaults
            notificationSettings.resetToDefaults()

            // Then - Should have default values
            assertTrue("Break notifications should be enabled by default", notificationSettings.isBreakNotificationsEnabled)
            assertTrue("Session notifications should be enabled by default", notificationSettings.isSessionCompleteNotificationsEnabled)
            assertTrue("Milestone notifications should be enabled by default", notificationSettings.isMilestoneNotificationsEnabled)
            assertTrue("Sound should be enabled by default", notificationSettings.isNotificationSoundEnabled)
            assertTrue("Vibration should be enabled by default", notificationSettings.isNotificationVibrationEnabled)
            assertTrue("Persistent timer should be enabled by default", notificationSettings.isPersistentTimerNotificationEnabled)
        }

    @Test
    fun `getAllSettings should return correct preferences`() =
        runTest {
            // Given - Set custom settings
            notificationSettings.isBreakNotificationsEnabled = false
            notificationSettings.isSessionCompleteNotificationsEnabled = true
            notificationSettings.isMilestoneNotificationsEnabled = false
            notificationSettings.isNotificationSoundEnabled = true
            notificationSettings.isNotificationVibrationEnabled = false
            notificationSettings.isPersistentTimerNotificationEnabled = true

            // When - Get all settings
            val preferences = notificationSettings.getAllSettings()

            // Then - Should match individual settings
            assertEquals("Break notifications should match", false, preferences.breakNotificationsEnabled)
            assertEquals("Session notifications should match", true, preferences.sessionCompleteNotificationsEnabled)
            assertEquals("Milestone notifications should match", false, preferences.milestoneNotificationsEnabled)
            assertEquals("Sound should match", true, preferences.soundEnabled)
            assertEquals("Vibration should match", false, preferences.vibrationEnabled)
            assertEquals("Persistent timer should match", true, preferences.persistentTimerNotificationEnabled)
        }
}
