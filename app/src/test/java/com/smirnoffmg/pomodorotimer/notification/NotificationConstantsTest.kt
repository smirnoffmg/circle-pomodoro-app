package com.smirnoffmg.pomodorotimer.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationConstantsTest {
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
    fun `channel constants should be immutable`() {
        assertEquals("timer_channel", NotificationChannelManager.TIMER_CHANNEL_ID)
        assertEquals("breaks_channel", NotificationChannelManager.BREAKS_CHANNEL_ID)
        assertEquals("progress_channel", NotificationChannelManager.PROGRESS_CHANNEL_ID)

        // These should be compile-time constants
        assertTrue("Timer channel ID should not be empty", NotificationChannelManager.TIMER_CHANNEL_ID.isNotEmpty())
        assertTrue("Breaks channel ID should not be empty", NotificationChannelManager.BREAKS_CHANNEL_ID.isNotEmpty())
        assertTrue("Progress channel ID should not be empty", NotificationChannelManager.PROGRESS_CHANNEL_ID.isNotEmpty())
    }

    @Test
    fun `channels should have unique IDs`() {
        val channelIds =
            setOf(
                NotificationChannelManager.TIMER_CHANNEL_ID,
                NotificationChannelManager.BREAKS_CHANNEL_ID,
                NotificationChannelManager.PROGRESS_CHANNEL_ID,
            )

        assertEquals("All channel IDs should be unique", 3, channelIds.size)
    }
}
