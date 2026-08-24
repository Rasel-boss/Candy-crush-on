package com.example.game.utils

import java.util.Locale

/**
 * Utility functions for formatting and manipulating game time representations.
 */
object TimeUtils {

    /**
     * Formats elapsed seconds into standard "MM:SS" format or "HH:MM:SS" if duration >= 1 hour.
     * Negative values are clamped safely to 0 ("00:00").
     *
     * @param seconds Total elapsed seconds.
     * @return Formatted string (e.g. "00:00", "01:25", "01:05:30").
     */
    fun formatTime(seconds: Long): String {
        val safeSeconds = maxOf(0L, seconds)
        val hours = safeSeconds / 3600
        val remainingAfterHours = safeSeconds % 3600
        val minutes = remainingAfterHours / 60
        val secs = remainingAfterHours % 60

        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, secs)
        }
    }
}
