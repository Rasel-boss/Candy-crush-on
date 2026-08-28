package com.example.game.model

/**
 * Animation states representing the visual lifecycle and feedback phase of a candy tile.
 */
enum class TileAnimationState {
    IDLE,
    SELECTED,
    SWAPPING,
    MATCHED,
    DISAPPEARING,
    FALLING,
    SPAWNING,
    ACTIVATING_SPECIAL;

    val isInteractive: Boolean
        get() = this == IDLE || this == SELECTED
}
