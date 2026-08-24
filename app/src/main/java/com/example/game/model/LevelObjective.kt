package com.example.game.model

/**
 * Immutable model representing a single level objective and its current completion progress.
 *
 * @property id Unique identifier for this objective within the level.
 * @property type The classification of objective (candy collection, target score, match count).
 * @property target The goal value required to satisfy the objective.
 * @property currentProgress Current progress achieved by the player.
 * @property candyType The specific candy color required if [type] is [ObjectiveType.COLLECT_CANDY].
 */
data class LevelObjective(
    val id: String,
    val type: ObjectiveType,
    val target: Int,
    val currentProgress: Int = 0,
    val candyType: CandyType? = null
) {
    /**
     * Whether this individual objective has reached or exceeded its target goal.
     */
    val isCompleted: Boolean
        get() = target > 0 && currentProgress >= target

    /**
     * Normalized completion ratio bounded between 0.0f and 1.0f for progress bar animations.
     */
    val progressRatio: Float
        get() = if (target > 0) (currentProgress.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f

    /**
     * Human-readable title or label for accessibility and UI display.
     */
    val displayTitle: String
        get() = when (type) {
            ObjectiveType.COLLECT_CANDY -> "Collect ${candyType?.displayName ?: "Candies"}"
            ObjectiveType.TARGET_SCORE -> "Target Score"
            ObjectiveType.MAKE_MATCHES -> "Make Matches"
        }

    /**
     * Clear accessibility description detailing type, progress, target, and completion state.
     */
    val accessibilityDescription: String
        get() = when (type) {
            ObjectiveType.COLLECT_CANDY -> {
                val color = candyType?.displayName ?: "candies"
                "Collect $color candies: $currentProgress of $target${if (isCompleted) ", completed" else ""}"
            }
            ObjectiveType.TARGET_SCORE -> {
                "Target score: $currentProgress of $target${if (isCompleted) ", completed" else ""}"
            }
            ObjectiveType.MAKE_MATCHES -> {
                "Make matches: $currentProgress of $target${if (isCompleted) ", completed" else ""}"
            }
        }
}
