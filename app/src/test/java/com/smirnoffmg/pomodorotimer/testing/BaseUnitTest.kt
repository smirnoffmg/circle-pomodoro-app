package com.smirnoffmg.pomodorotimer.testing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Base class for all unit tests following DRY principle.
 * Provides common setup and teardown functionality.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseUnitTest {
    
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    
    protected val testScope = TestScope(UnconfinedTestDispatcher())
    
    @Before
    open fun setUp() {
        // Common setup for all unit tests
        TestDispatchers.setMainDispatcher()
    }
}

/**
 * Test rule for coroutine testing.
 * Follows Single Responsibility Principle by only handling coroutine test setup.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TestCoroutineRule : TestWatcher() {
    override fun starting(description: Description?) {
        super.starting(description)
        TestDispatchers.setMainDispatcher()
    }

    override fun finished(description: Description?) {
        super.finished(description)
        TestDispatchers.resetMainDispatcher()
    }
}
