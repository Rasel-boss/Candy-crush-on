package com.example.game.model

/**
 * Types of level objectives available in Match-3 puzzle levels.
 * Extensible for future level requirements.
 */
enum class ObjectiveType {
    /**
     * Collect a specific quantity of a given [CandyType] (e.g. 20 Red candies).
     */
    COLLECT_CANDY,

    /**
     * Reach or exceed a specified score threshold (e.g. 500 points).
     */
    TARGET_SCORE,

    /**
     * Reach or exceed a specified score threshold (Synonym/Alias for TARGET_SCORE).
     */
    SCORE_TARGET,

    /**
     * Complete a specified count of valid Match-3 linear or cascade matches (e.g. 10 matches).
     */
    MAKE_MATCHES,

    /**
     * Clear special blocker tiles on the board.
     */
    CLEAR_BLOCKERS;

    /**
     * True if this objective type tracks accumulated score points.
     */
    val isScoreObjective: Boolean
        get() = this == TARGET_SCORE || this == SCORE_TARGET
}
