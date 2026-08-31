package com.example.game.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.example.game.model.GameSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel managing persistent user preferences (sound effects, music, and haptic feedback toggles).
 */
class SettingsViewModel : ViewModel() {

    companion object {
        private const val PREFS_NAME = "candy_crush_lite_settings_prefs"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_MUSIC_ENABLED = "music_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"

        private var sharedPreferences: SharedPreferences? = null

        /**
         * Initialize persistence with application context.
         */
        fun init(context: Context) {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPreferences = prefs
        }

        fun getInitialSettings(): GameSettings {
            val prefs = sharedPreferences ?: return GameSettings()
            return GameSettings(
                soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
                musicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true),
                vibrationEnabled = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
            )
        }
    }

    private val _settingsState = MutableStateFlow(getInitialSettings())
    val settingsState: StateFlow<GameSettings> = _settingsState.asStateFlow()

    fun setSoundEnabled(enabled: Boolean) {
        _settingsState.update { it.copy(soundEnabled = enabled) }
        persist()
    }

    fun setMusicEnabled(enabled: Boolean) {
        _settingsState.update { it.copy(musicEnabled = enabled) }
        persist()
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _settingsState.update { it.copy(vibrationEnabled = enabled) }
        persist()
    }

    fun toggleSound() {
        _settingsState.update { it.copy(soundEnabled = !it.soundEnabled) }
        persist()
    }

    fun toggleMusic() {
        _settingsState.update { it.copy(musicEnabled = !it.musicEnabled) }
        persist()
    }

    fun toggleVibration() {
        _settingsState.update { it.copy(vibrationEnabled = !it.vibrationEnabled) }
        persist()
    }

    fun resetSettingsToDefault() {
        _settingsState.value = GameSettings(
            soundEnabled = true,
            musicEnabled = true,
            vibrationEnabled = true
        )
        persist()
    }

    private fun persist() {
        val prefs = sharedPreferences ?: return
        val current = _settingsState.value
        prefs.edit()
            .putBoolean(KEY_SOUND_ENABLED, current.soundEnabled)
            .putBoolean(KEY_MUSIC_ENABLED, current.musicEnabled)
            .putBoolean(KEY_VIBRATION_ENABLED, current.vibrationEnabled)
            .apply()
    }
}
