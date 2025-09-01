package com.smirnoffmg.pomodorotimer.notification

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSettings
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        companion object {
            private const val PREFS_NAME = "notification_settings"
            private const val KEY_BREAK_NOTIFICATIONS_ENABLED = "break_notifications_enabled"
            private const val KEY_SESSION_COMPLETE_NOTIFICATIONS_ENABLED = "session_complete_notifications_enabled"
            private const val KEY_MILESTONE_NOTIFICATIONS_ENABLED = "milestone_notifications_enabled"
            private const val KEY_NOTIFICATION_SOUND_ENABLED = "notification_sound_enabled"
            private const val KEY_NOTIFICATION_VIBRATION_ENABLED = "notification_vibration_enabled"
            private const val KEY_PERSISTENT_TIMER_NOTIFICATION_ENABLED = "persistent_timer_notification_enabled"
        }

        private val prefs: SharedPreferences by lazy {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        var isBreakNotificationsEnabled: Boolean
            get() = prefs.getBoolean(KEY_BREAK_NOTIFICATIONS_ENABLED, true)
            set(value) = prefs.edit().putBoolean(KEY_BREAK_NOTIFICATIONS_ENABLED, value).apply()

        var isSessionCompleteNotificationsEnabled: Boolean
            get() = prefs.getBoolean(KEY_SESSION_COMPLETE_NOTIFICATIONS_ENABLED, true)
            set(value) = prefs.edit().putBoolean(KEY_SESSION_COMPLETE_NOTIFICATIONS_ENABLED, value).apply()

        var isMilestoneNotificationsEnabled: Boolean
            get() = prefs.getBoolean(KEY_MILESTONE_NOTIFICATIONS_ENABLED, true)
            set(value) = prefs.edit().putBoolean(KEY_MILESTONE_NOTIFICATIONS_ENABLED, value).apply()

        var isNotificationSoundEnabled: Boolean
            get() = prefs.getBoolean(KEY_NOTIFICATION_SOUND_ENABLED, true)
            set(value) = prefs.edit().putBoolean(KEY_NOTIFICATION_SOUND_ENABLED, value).apply()

        var isNotificationVibrationEnabled: Boolean
            get() = prefs.getBoolean(KEY_NOTIFICATION_VIBRATION_ENABLED, true)
            set(value) = prefs.edit().putBoolean(KEY_NOTIFICATION_VIBRATION_ENABLED, value).apply()

        var isPersistentTimerNotificationEnabled: Boolean
            get() = prefs.getBoolean(KEY_PERSISTENT_TIMER_NOTIFICATION_ENABLED, true)
            set(value) = prefs.edit().putBoolean(KEY_PERSISTENT_TIMER_NOTIFICATION_ENABLED, value).apply()

        data class NotificationPreferences(
            val breakNotificationsEnabled: Boolean,
            val sessionCompleteNotificationsEnabled: Boolean,
            val milestoneNotificationsEnabled: Boolean,
            val soundEnabled: Boolean,
            val vibrationEnabled: Boolean,
            val persistentTimerNotificationEnabled: Boolean,
        )

        fun getAllSettings(): NotificationPreferences =
            NotificationPreferences(
                breakNotificationsEnabled = isBreakNotificationsEnabled,
                sessionCompleteNotificationsEnabled = isSessionCompleteNotificationsEnabled,
                milestoneNotificationsEnabled = isMilestoneNotificationsEnabled,
                soundEnabled = isNotificationSoundEnabled,
                vibrationEnabled = isNotificationVibrationEnabled,
                persistentTimerNotificationEnabled = isPersistentTimerNotificationEnabled,
            )

        fun resetToDefaults() {
            prefs.edit().clear().apply()
        }
    }
