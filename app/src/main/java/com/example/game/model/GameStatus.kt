package com.example.game.model

/**
 * Represents the high-level lifecycle status of a Match-3 puzzle session.
 */
enum class GameStatus {
    /** Initial state before gameplay starts or after reset */
    READY,

    /** Active gameplay state; player interactions are accepted */
    PLAYING,

    /** Transient state when swaps, matches, collapses, or refills are resolving */
    PROCESSING,

    /** Level objective achieved successfully */
    COMPLETED,

    /** Level ended without completing objectives (e.g. moves exhausted) */
    GAME_OVER,

    /** Gameplay is paused */
    PAUSED
}
