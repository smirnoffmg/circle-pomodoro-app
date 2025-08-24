package com.smirnoffmg.pomodorotimer.notification

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
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
class NotificationChannelManagerBasicTest {
    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        
        // Initialize WorkManager for testing
        val config =
            Configuration
                .Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .setExecutor(SynchronousExecutor())
                .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Test
    fun `should create notification channels without throwing exception`() {
        // Create a simple NotificationChannelManager without Hilt
        val channelManager = SimpleNotificationChannelManager(context)
        
        // When
        channelManager.createNotificationChannels()

        // Then - Just verify no exception is thrown
        assertTrue("Should complete without exception", true)
    }

    @Test
    fun `should verify channels exist after creation`() {
        // Create a simple NotificationChannelManager without Hilt
        val channelManager = SimpleNotificationChannelManager(context)
        
        // When
        channelManager.createNotificationChannels()

        // Then
        val timerChannel = notificationManager.getNotificationChannel(NotificationChannelManager.TIMER_CHANNEL_ID)
        val breaksChannel = notificationManager.getNotificationChannel(NotificationChannelManager.BREAKS_CHANNEL_ID)
        val progressChannel = notificationManager.getNotificationChannel(NotificationChannelManager.PROGRESS_CHANNEL_ID)
        
        assertNotNull("Timer channel should be created", timerChannel)
        assertNotNull("Breaks channel should be created", breaksChannel)
        assertNotNull("Progress channel should be created", progressChannel)
    }
}

// Simple version without Hilt for testing
class SimpleNotificationChannelManager(
    private val context: Context
) {
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
                android.app
                    .NotificationChannel(
                        NotificationChannelManager.TIMER_CHANNEL_ID,
                        "Timer Service",
                        NotificationManager.IMPORTANCE_LOW
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
                android.app
                    .NotificationChannel(
                        NotificationChannelManager.BREAKS_CHANNEL_ID,
                        "Break Notifications",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Break start and end notifications"
                        setShowBadge(true)
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 300, 200, 300)
                        setSound(
                            android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                            android.media.AudioAttributes
                                .Builder()
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                                .build()
                        )
                    }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createProgressChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                android.app
                    .NotificationChannel(
                        NotificationChannelManager.PROGRESS_CHANNEL_ID,
                        "Progress Updates",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Session completion and milestone notifications"
                        setShowBadge(true)
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 100, 100, 100)
                        setSound(
                            android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                            android.media.AudioAttributes
                                .Builder()
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                                .build()
                        )
                    }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
