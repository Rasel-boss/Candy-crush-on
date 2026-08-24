package com.example.game.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Interface defining haptic tactile feedback for game interactions.
 */
interface IHapticFeedbackManager {
    fun performTileSelectFeedback()
    fun performTileSwapFeedback()
    fun performInvalidSwapFeedback()
    fun performMatchFeedback(intensity: Int = 1)
    fun performCascadeFeedback(chainStep: Int = 2)
    fun performGameOverFeedback()
    fun performVictoryFeedback()
    fun performLevelCompleteFeedback()
    fun performButtonClickFeedback()
    fun performTileMoveFeedback()
    fun performInvalidMoveFeedback()
}

/**
 * Production haptic feedback manager utilizing Android standard [Vibrator] and [VibrationEffect] (API 26+).
 * Safely handles missing hardware or permission constraints without crashing.
 *
 * @param context Android context to access vibration service.
 * @param vibrationEnabledSupplier Lambda returning whether vibration is currently enabled in settings.
 */
class HapticFeedbackManager(
    context: Context,
    private val vibrationEnabledSupplier: () -> Boolean = { true }
) : IHapticFeedbackManager {

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (e: Exception) {
        Log.w("HapticFeedbackManager", "Failed to get Vibrator service: ${e.message}")
        null
    }

    override fun performTileSelectFeedback() {
        if (!vibrationEnabledSupplier()) return
        vibrate(durationMs = 8, amplitude = 35)
    }

    override fun performTileSwapFeedback() {
        if (!vibrationEnabledSupplier()) return
        vibrate(durationMs = 15, amplitude = 55)
    }

    override fun performInvalidSwapFeedback() {
        if (!vibrationEnabledSupplier()) return
        vibrate(durationMs = 25, amplitude = 75)
    }

    override fun performMatchFeedback(intensity: Int) {
        if (!vibrationEnabledSupplier()) return
        val duration = when {
            intensity >= 5 -> 30L
            intensity == 4 -> 22L
            else -> 15L
        }
        val amplitude = when {
            intensity >= 5 -> 100
            intensity == 4 -> 80
            else -> 60
        }
        vibrate(durationMs = duration, amplitude = amplitude)
    }

    override fun performCascadeFeedback(chainStep: Int) {
        if (!vibrationEnabledSupplier()) return
        val duration = (15L + (chainStep * 5L)).coerceAtMost(40L)
        val amplitude = (60 + (chainStep * 15)).coerceAtMost(120)
        vibrate(durationMs = duration, amplitude = amplitude)
    }

    override fun performGameOverFeedback() {
        if (!vibrationEnabledSupplier()) return
        vibrate(durationMs = 45, amplitude = 80)
    }

    override fun performTileMoveFeedback() {
        performTileSwapFeedback()
    }

    override fun performInvalidMoveFeedback() {
        performInvalidSwapFeedback()
    }

    override fun performVictoryFeedback() {
        if (!vibrationEnabledSupplier()) return
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                val pattern = longArrayOf(0, 100, 60, 150)
                val amplitudes = intArrayOf(0, 80, 0, 120)
                val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
                vibrator.vibrate(effect)
            }
        } catch (e: Exception) {
            vibrate(durationMs = 150, amplitude = 100)
        }
    }

    override fun performLevelCompleteFeedback() {
        performVictoryFeedback()
    }

    override fun performButtonClickFeedback() {
        if (!vibrationEnabledSupplier()) return
        vibrate(durationMs = 10, amplitude = 40)
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                val safeAmplitude = amplitude.coerceIn(1, 255)
                val effect = VibrationEffect.createOneShot(durationMs, safeAmplitude)
                vibrator.vibrate(effect)
            }
        } catch (e: Exception) {
            Log.d("HapticFeedbackManager", "Vibration skipped or unavailable: ${e.message}")
        }
    }
}

/**
 * No-op haptic feedback manager implementation for preview and test modes.
 */
class NoOpHapticFeedbackManager : IHapticFeedbackManager {
    override fun performTileSelectFeedback() {}
    override fun performTileSwapFeedback() {}
    override fun performInvalidSwapFeedback() {}
    override fun performMatchFeedback(intensity: Int) {}
    override fun performCascadeFeedback(chainStep: Int) {}
    override fun performGameOverFeedback() {}
    override fun performVictoryFeedback() {}
    override fun performLevelCompleteFeedback() {}
    override fun performButtonClickFeedback() {}
    override fun performTileMoveFeedback() {}
    override fun performInvalidMoveFeedback() {}
}
