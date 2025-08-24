package com.smirnoffmg.pomodorotimer

import android.app.Application

/**
 * Test application that doesn't initialize WorkManager or other complex dependencies
 */
class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Don't initialize WorkManager or other complex dependencies in tests
    }
}
