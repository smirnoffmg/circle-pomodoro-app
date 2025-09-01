package com.smirnoffmg.pomodorotimer.notification

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = com.smirnoffmg.pomodorotimer.TestApplication::class)
class NotificationSettingsTest {
    private lateinit var context: Context
    private lateinit var notificationSettings: NotificationSettings

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notificationSettings = NotificationSettings(context)
        // Clear any existing preferences
        notificationSettings.resetToDefaults()
    }

    @Test
    fun `default settings should be enabled`() =
        runTest {
            // When - Get default settings
            val settings = notificationSettings.getAllSettings()

            // Then - All should be enabled by default
            assertTrue("Break notifications should be enabled by default", settings.breakNotificationsEnabled)
            assertTrue("Session complete notifications should be enabled by default", settings.sessionCompleteNotificationsEnabled)
            assertTrue("Milestone notifications should be enabled by default", settings.milestoneNotificationsEnabled)
            assertTrue("Sound should be enabled by default", settings.soundEnabled)
            assertTrue("Vibration should be enabled by default", settings.vibrationEnabled)
            assertTrue("Persistent timer notification should be enabled by default", settings.persistentTimerNotificationEnabled)
        }

    @Test
    fun `break notifications setting should persist`() =
        runTest {
            // Given - Default is true
            assertTrue("Should start enabled", notificationSettings.isBreakNotificationsEnabled)

            // When - Disable break notifications
            notificationSettings.isBreakNotificationsEnabled = false

            // Then - Should be disabled
            assertFalse("Should be disabled", notificationSettings.isBreakNotificationsEnabled)

            // When - Re-enable
            notificationSettings.isBreakNotificationsEnabled = true

            // Then - Should be enabled again
            assertTrue("Should be enabled again", notificationSettings.isBreakNotificationsEnabled)
        }

    @Test
    fun `session complete notifications setting should persist`() =
        runTest {
            // Given - Default is true
            assertTrue("Should start enabled", notificationSettings.isSessionCompleteNotificationsEnabled)

            // When - Disable
            notificationSettings.isSessionCompleteNotificationsEnabled = false

            // Then - Should be disabled
            assertFalse("Should be disabled", notificationSettings.isSessionCompleteNotificationsEnabled)

            // When - Re-enable
            notificationSettings.isSessionCompleteNotificationsEnabled = true

            // Then - Should be enabled again
            assertTrue("Should be enabled again", notificationSettings.isSessionCompleteNotificationsEnabled)
        }

    @Test
    fun `milestone notifications setting should persist`() =
        runTest {
            // Given - Default is true
            assertTrue("Should start enabled", notificationSettings.isMilestoneNotificationsEnabled)

            // When - Disable
            notificationSettings.isMilestoneNotificationsEnabled = false

            // Then - Should be disabled
            assertFalse("Should be disabled", notificationSettings.isMilestoneNotificationsEnabled)

            // When - Re-enable
            notificationSettings.isMilestoneNotificationsEnabled = true

            // Then - Should be enabled again
            assertTrue("Should be enabled again", notificationSettings.isMilestoneNotificationsEnabled)
        }

    @Test
    fun `sound setting should persist`() =
        runTest {
            // Given - Default is true
            assertTrue("Should start enabled", notificationSettings.isNotificationSoundEnabled)

            // When - Disable
            notificationSettings.isNotificationSoundEnabled = false

            // Then - Should be disabled
            assertFalse("Should be disabled", notificationSettings.isNotificationSoundEnabled)

            // When - Re-enable
            notificationSettings.isNotificationSoundEnabled = true

            // Then - Should be enabled again
            assertTrue("Should be enabled again", notificationSettings.isNotificationSoundEnabled)
        }

    @Test
    fun `vibration setting should persist`() =
        runTest {
            // Given - Default is true
            assertTrue("Should start enabled", notificationSettings.isNotificationVibrationEnabled)

            // When - Disable
            notificationSettings.isNotificationVibrationEnabled = false

            // Then - Should be disabled
            assertFalse("Should be disabled", notificationSettings.isNotificationVibrationEnabled)

            // When - Re-enable
            notificationSettings.isNotificationVibrationEnabled = true

            // Then - Should be enabled again
            assertTrue("Should be enabled again", notificationSettings.isNotificationVibrationEnabled)
        }

    @Test
    fun `persistent timer notification setting should persist`() =
        runTest {
            // Given - Default is true
            assertTrue("Should start enabled", notificationSettings.isPersistentTimerNotificationEnabled)

            // When - Disable
            notificationSettings.isPersistentTimerNotificationEnabled = false

            // Then - Should be disabled
            assertFalse("Should be disabled", notificationSettings.isPersistentTimerNotificationEnabled)

            // When - Re-enable
            notificationSettings.isPersistentTimerNotificationEnabled = true

            // Then - Should be enabled again
            assertTrue("Should be enabled again", notificationSettings.isPersistentTimerNotificationEnabled)
        }

    @Test
    fun `getAllSettings should return current state`() =
        runTest {
            // Given - Modify some settings
            notificationSettings.isBreakNotificationsEnabled = false
            notificationSettings.isNotificationSoundEnabled = false
            notificationSettings.isMilestoneNotificationsEnabled = false

            // When - Get all settings
            val settings = notificationSettings.getAllSettings()

            // Then - Should reflect current state
            assertFalse("Break notifications should be disabled", settings.breakNotificationsEnabled)
            assertTrue("Session complete should still be enabled", settings.sessionCompleteNotificationsEnabled)
            assertFalse("Milestone should be disabled", settings.milestoneNotificationsEnabled)
            assertFalse("Sound should be disabled", settings.soundEnabled)
            assertTrue("Vibration should still be enabled", settings.vibrationEnabled)
            assertTrue("Persistent timer should still be enabled", settings.persistentTimerNotificationEnabled)
        }

    @Test
    fun `resetToDefaults should restore all settings to default`() =
        runTest {
            // Given - Modify all settings
            notificationSettings.isBreakNotificationsEnabled = false
            notificationSettings.isSessionCompleteNotificationsEnabled = false
            notificationSettings.isMilestoneNotificationsEnabled = false
            notificationSettings.isNotificationSoundEnabled = false
            notificationSettings.isNotificationVibrationEnabled = false
            notificationSettings.isPersistentTimerNotificationEnabled = false

            // Verify they are disabled
            val modifiedSettings = notificationSettings.getAllSettings()
            assertFalse(
                "All should be disabled",
                modifiedSettings.breakNotificationsEnabled ||
                    modifiedSettings.sessionCompleteNotificationsEnabled ||
                    modifiedSettings.milestoneNotificationsEnabled ||
                    modifiedSettings.soundEnabled ||
                    modifiedSettings.vibrationEnabled ||
                    modifiedSettings.persistentTimerNotificationEnabled,
            )

            // When - Reset to defaults
            notificationSettings.resetToDefaults()

            // Then - All should be enabled again (defaults)
            val resetSettings = notificationSettings.getAllSettings()
            assertTrue("Break notifications should be enabled", resetSettings.breakNotificationsEnabled)
            assertTrue("Session complete should be enabled", resetSettings.sessionCompleteNotificationsEnabled)
            assertTrue("Milestone should be enabled", resetSettings.milestoneNotificationsEnabled)
            assertTrue("Sound should be enabled", resetSettings.soundEnabled)
            assertTrue("Vibration should be enabled", resetSettings.vibrationEnabled)
            assertTrue("Persistent timer should be enabled", resetSettings.persistentTimerNotificationEnabled)
        }

    @Test
    fun `settings should persist across instances`() =
        runTest {
            // Given - Modify settings with first instance
            notificationSettings.isBreakNotificationsEnabled = false
            notificationSettings.isNotificationSoundEnabled = false

            // When - Create new instance
            val newInstance = NotificationSettings(context)

            // Then - Settings should persist
            assertFalse("Break notifications should persist as disabled", newInstance.isBreakNotificationsEnabled)
            assertFalse("Sound should persist as disabled", newInstance.isNotificationSoundEnabled)
            assertTrue("Other settings should remain default", newInstance.isSessionCompleteNotificationsEnabled)
        }

    @Test
    fun `notification preferences data class should have correct properties`() {
        // Given
        val preferences =
            NotificationSettings.NotificationPreferences(
                breakNotificationsEnabled = true,
                sessionCompleteNotificationsEnabled = false,
                milestoneNotificationsEnabled = true,
                soundEnabled = false,
                vibrationEnabled = true,
                persistentTimerNotificationEnabled = false,
            )

        // Then
        assertTrue("Break notifications should be true", preferences.breakNotificationsEnabled)
        assertFalse("Session complete should be false", preferences.sessionCompleteNotificationsEnabled)
        assertTrue("Milestone should be true", preferences.milestoneNotificationsEnabled)
        assertFalse("Sound should be false", preferences.soundEnabled)
        assertTrue("Vibration should be true", preferences.vibrationEnabled)
        assertFalse("Persistent timer should be false", preferences.persistentTimerNotificationEnabled)
    }
}
