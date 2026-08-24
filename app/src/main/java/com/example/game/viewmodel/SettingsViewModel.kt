package com.example.game.viewmodel

import androidx.lifecycle.ViewModel
import com.example.game.model.GameSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {

    private val _settings = MutableStateFlow(GameSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    fun toggleSound() {
        _settings.update { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun toggleMusic() {
        _settings.update { it.copy(musicEnabled = !it.musicEnabled) }
    }

    fun toggleHaptic() {
        _settings.update { it.copy(hapticEnabled = !it.hapticEnabled) }
    }

    fun toggleParticles() {
        _settings.update { it.copy(particlesEnabled = !it.particlesEnabled) }
    }
}
