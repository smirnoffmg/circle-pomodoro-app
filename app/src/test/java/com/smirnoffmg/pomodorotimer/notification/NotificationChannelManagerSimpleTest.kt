package com.smirnoffmg.pomodorotimer.notification

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(
    sdk = [Build.VERSION_CODES.TIRAMISU],
    application = com.smirnoffmg.pomodorotimer.TestApplication::class,
)
class NotificationChannelManagerSimpleTest {
    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var channelManager: NotificationChannelManager

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
        channelManager = NotificationChannelManager(context)
    }

    @Test
    fun `should create notification channels without throwing exception`() {
        // When
        channelManager.createNotificationChannels()

        // Then - Just verify no exception is thrown
        assertTrue("Should complete without exception", true)
    }

    @Test
    fun `should verify channels exist after creation`() {
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
