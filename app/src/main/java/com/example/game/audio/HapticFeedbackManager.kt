package com.example.game.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticFeedbackManager(private val context: Context?) {

    private val vibrator: Vibrator? by lazy {
        if (context == null) return@lazy null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun performMatchHaptic() {
        vibrate(30)
    }

    fun performSpecialExplosionHaptic() {
        vibrate(80)
    }

    fun performVictoryHaptic() {
        vibrate(150)
    }

    private fun vibrate(durationMs: Long) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(durationMs)
        }
    }
}
