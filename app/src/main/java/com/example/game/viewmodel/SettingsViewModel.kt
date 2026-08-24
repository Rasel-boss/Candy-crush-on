package com.example.game.viewmodel

import androidx.lifecycle.ViewModel
import com.example.game.model.GameSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel managing session-level settings (sound effects and haptic feedback toggles).
 */
class SettingsViewModel : ViewModel() {

    private val _settingsState = MutableStateFlow(GameSettings())
    val settingsState: StateFlow<GameSettings> = _settingsState.asStateFlow()

    fun setSoundEnabled(enabled: Boolean) {
        _settingsState.update { it.copy(soundEnabled = enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _settingsState.update { it.copy(vibrationEnabled = enabled) }
    }

    fun toggleSound() {
        _settingsState.update { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun toggleVibration() {
        _settingsState.update { it.copy(vibrationEnabled = !it.vibrationEnabled) }
    }
}
