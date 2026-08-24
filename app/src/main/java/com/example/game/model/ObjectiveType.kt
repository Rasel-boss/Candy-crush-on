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
     * Complete a specified count of valid Match-3 linear or cascade matches (e.g. 10 matches).
     */
    MAKE_MATCHES
}
