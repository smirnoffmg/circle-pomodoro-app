package com.smirnoffmg.pomodorotimer

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Simple test to verify basic testing infrastructure.
 * Follows KISS principle with minimal complexity.
 */
class SimpleTest {

    @Test
    fun `basic test should pass`() {
        // Given
        val expected = true

        // When
        val actual = true

        // Then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `math test should work`() {
        // Given
        val a = 2
        val b = 3

        // When
        val result = a + b

        // Then
        assertThat(result).isEqualTo(5)
    }
}
