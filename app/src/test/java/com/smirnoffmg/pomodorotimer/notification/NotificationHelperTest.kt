package com.smirnoffmg.pomodorotimer.notification

import org.junit.Test

class NotificationHelperTest {
    @Test
    fun `notification channel constants are defined correctly`() {
        assert(NotificationChannelManager.TIMER_CHANNEL_ID == "timer_channel")
        assert(NotificationChannelManager.BREAKS_CHANNEL_ID == "breaks_channel")
        assert(NotificationChannelManager.PROGRESS_CHANNEL_ID == "progress_channel")
    }

    @Test
    fun `notification helper constants are defined correctly`() {
        assert(NotificationHelper.TIMER_NOTIFICATION_ID == 1001)
        assert(NotificationHelper.BREAK_START_NOTIFICATION_ID == 1002)
        assert(NotificationHelper.BREAK_END_NOTIFICATION_ID == 1003)
        assert(NotificationHelper.SESSION_COMPLETE_NOTIFICATION_ID == 1004)
        assert(NotificationHelper.MILESTONE_NOTIFICATION_ID == 1005)
    }
}
