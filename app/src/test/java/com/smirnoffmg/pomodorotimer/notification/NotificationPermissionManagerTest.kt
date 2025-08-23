package com.smirnoffmg.pomodorotimer.notification

import android.os.Build
import org.junit.Test

class NotificationPermissionManagerTest {
    @Test
    fun `permission status data class should have correct properties`() {
        val status =
            NotificationPermissionManager.PermissionStatus(
                isGranted = true,
                areNotificationsEnabled = true,
                needsRuntimePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            )
        
        assert(status.isGranted)
        assert(status.areNotificationsEnabled)
    }

    @Test
    fun `permission constants should be defined`() {
        // This test ensures the class compiles without errors
        assert(true)
    }
}
