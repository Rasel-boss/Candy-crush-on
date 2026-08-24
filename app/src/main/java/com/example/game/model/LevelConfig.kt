package com.example.game.model

/**
 * Configuration blueprint specifying the parameters, constraints, and objectives for a Match-3 level.
 *
 * @property levelNumber The 1-based index of this level.
 * @property rows Grid row count (default 8).
 * @property columns Grid column count (default 8).
 * @property startingMoves Number of allowed moves available to the player.
 * @property objectives List of requirements the player must complete to clear the level.
 * @property targetScore Optional target score threshold (convenience reference if included in objectives).
 */
data class LevelConfig(
    val levelNumber: Int,
    val rows: Int = DEFAULT_ROWS,
    val columns: Int = DEFAULT_COLUMNS,
    val startingMoves: Int = DEFAULT_MOVES,
    val objectives: List<LevelObjective> = emptyList(),
    val targetScore: Int? = null
) {
    /**
     * Validates whether this level configuration complies with all safety and gameplay constraints.
     * Returns true if valid, false if any parameter is malformed or invalid.
     */
    fun isValid(): Boolean {
        if (levelNumber <= 0) return false
        if (rows <= 0 || columns <= 0) return false
        if (startingMoves <= 0) return false
        if (objectives.isEmpty()) return false

        for (objective in objectives) {
            if (objective.target <= 0) return false
            when (objective.type) {
                ObjectiveType.COLLECT_CANDY -> {
                    if (objective.candyType == null || !objective.candyType.isPlayable) {
                        return false
                    }
                }
                ObjectiveType.TARGET_SCORE -> {
                    if (objective.target <= 0) return false
                }
                ObjectiveType.MAKE_MATCHES -> {
                    if (objective.target <= 0) return false
                }
            }
        }
        return true
    }
}
