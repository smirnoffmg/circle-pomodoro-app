package com.smirnoffmg.pomodorotimer.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationChannelManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        companion object {
            const val TIMER_CHANNEL_ID = "timer_channel"
            const val BREAKS_CHANNEL_ID = "breaks_channel"
            const val PROGRESS_CHANNEL_ID = "progress_channel"

            private const val TIMER_CHANNEL_NAME = "Timer Service"
            private const val BREAKS_CHANNEL_NAME = "Break Notifications"
            private const val PROGRESS_CHANNEL_NAME = "Progress Updates"
        }

        private val notificationManager: NotificationManager by lazy {
            context.getSystemService() ?: throw IllegalStateException("NotificationManager not available")
        }

        fun createNotificationChannels() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createTimerChannel()
                createBreaksChannel()
                createProgressChannel()
            }
        }

        private fun createTimerChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel =
                    NotificationChannel(
                        TIMER_CHANNEL_ID,
                        TIMER_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_LOW,
                    ).apply {
                        description = "Persistent timer service notification"
                        setShowBadge(false)
                        setSound(null, null)
                        enableVibration(false)
                        enableLights(false)
                    }
                notificationManager.createNotificationChannel(channel)
            }
        }

        private fun createBreaksChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel =
                    NotificationChannel(
                        BREAKS_CHANNEL_ID,
                        BREAKS_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT,
                    ).apply {
                        description = "Break start and end notifications"
                        setShowBadge(true)
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 300, 200, 300)
                        setSound(
                            android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                            AudioAttributes
                                .Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .build(),
                        )
                    }
                notificationManager.createNotificationChannel(channel)
            }
        }

        private fun createProgressChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel =
                    NotificationChannel(
                        PROGRESS_CHANNEL_ID,
                        PROGRESS_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT,
                    ).apply {
                        description = "Session completion and milestone notifications"
                        setShowBadge(true)
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 100, 100, 100)
                        setSound(
                            android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                            AudioAttributes
                                .Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .build(),
                        )
                    }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
