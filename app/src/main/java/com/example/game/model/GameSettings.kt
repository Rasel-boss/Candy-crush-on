package com.example.game.model

/**
 * Immutable configuration representing session-level game settings.
 *
 * @property soundEnabled Whether sound effects (clicks, moves, victory) are enabled.
 * @property musicEnabled Whether background music is enabled.
 * @property vibrationEnabled Whether haptic feedback (tactile vibration) is enabled.
 */
data class GameSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)
