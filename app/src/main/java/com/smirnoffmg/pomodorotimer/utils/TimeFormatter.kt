package com.smirnoffmg.pomodorotimer.utils

import java.util.Locale

/**
 * Utility class for time formatting with consistent locale
 * 
 * Follows SOLID principles:
 * - Single Responsibility: Only handles time formatting
 * - Open/Closed: Easy to extend for new formats
 * 
 * Follows DRY principle by centralizing time formatting logic
 * Follows KISS principle with simple, focused functionality
 */
object TimeFormatter {
    /**
     * Format time in seconds to MM:SS format
     * 
     * @param timeInSeconds Time in seconds
     * @return Formatted time string (e.g., "25:00")
     */
    fun formatTime(timeInSeconds: Long): String {
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
    }
}
