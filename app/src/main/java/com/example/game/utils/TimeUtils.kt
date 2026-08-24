package com.example.game.utils

import java.util.Locale

object TimeUtils {

    fun formatSecondsToMinutes(seconds: Int): String {
        val mins = seconds / 60
        val remainingSecs = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", mins, remainingSecs)
    }
}
