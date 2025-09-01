package com.smirnoffmg.pomodorotimer.testing

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner for Hilt integration tests.
 * Follows Single Responsibility Principle by only handling Hilt test setup.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        name: String?,
        context: Context?,
    ): Application =
        super
            .newApplication(cl, HiltTestApplication::class.java.name, context)
}
