package com.example.game.model

/**
 * Immutable model representing a single level objective and its current completion progress.
 *
 * @property id Unique identifier for this objective within the level.
 * @property type The classification of objective (candy collection, target score, match count, blockers).
 * @property target The goal value required to satisfy the objective.
 * @property currentProgress Current progress achieved by the player.
 * @property candyType The specific candy color required if [type] is [ObjectiveType.COLLECT_CANDY].
 */
data class LevelObjective(
    val id: String = "",
    val type: ObjectiveType,
    val target: Int,
    val currentProgress: Int = 0,
    val candyType: CandyType? = null
) {
    /**
     * Unique or auto-generated identifier.
     */
    val resolvedId: String
        get() = if (id.isNotEmpty()) id else "${type.name.lowercase()}_${candyType?.name?.lowercase() ?: "goal"}_$target"

    /**
     * Whether this individual objective has reached or exceeded its target goal.
     */
    val isCompleted: Boolean
        get() = target > 0 && currentProgress >= target

    /**
     * Synonym for [isCompleted].
     */
    val completed: Boolean
        get() = isCompleted

    /**
     * Structured [ObjectiveProgress] snapshot.
     */
    val progress: ObjectiveProgress
        get() = ObjectiveProgress(
            current = currentProgress,
            target = target,
            completed = isCompleted
        )

    /**
     * Displayed current progress, clamped so it never exceeds target (e.g. 1250/5000).
     */
    val displayCurrent: Int
        get() = currentProgress.coerceIn(0, target)

    /**
     * Formatted string (e.g. "12 / 20" or "1,250 / 5,000").
     */
    val displayFormatted: String
        get() = "$displayCurrent / $target"

    /**
     * Normalized completion ratio bounded between 0.0f and 1.0f for progress bar animations.
     */
    val progressRatio: Float
        get() = if (target > 0) (currentProgress.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f

    /**
     * Concise label for compact dashboard cards and HUD badges.
     */
    val shortDisplayName: String
        get() = when (type) {
            ObjectiveType.COLLECT_CANDY -> when (candyType) {
                CandyType.RED -> "Red"
                CandyType.BLUE -> "Blue"
                CandyType.GREEN -> "Green"
                CandyType.YELLOW -> "Yellow"
                CandyType.PURPLE -> "Purple"
                CandyType.ORANGE -> "Orange"
                else -> candyType?.displayName?.substringBefore(" ") ?: "Candy"
            }
            ObjectiveType.TARGET_SCORE,
            ObjectiveType.SCORE_TARGET -> "Score"
            ObjectiveType.MAKE_MATCHES -> "Matches"
            ObjectiveType.CLEAR_BLOCKERS -> "Blockers"
        }

    /**
     * Human-readable title or label for accessibility and UI display.
     */
    val displayTitle: String
        get() = when (type) {
            ObjectiveType.COLLECT_CANDY -> "Collect ${candyType?.displayName ?: "Candies"}"
            ObjectiveType.TARGET_SCORE,
            ObjectiveType.SCORE_TARGET -> "Target Score"
            ObjectiveType.MAKE_MATCHES -> "Make Matches"
            ObjectiveType.CLEAR_BLOCKERS -> "Clear Blockers"
        }

    /**
     * Clear accessibility description detailing type, progress, target, and completion state.
     */
    val accessibilityDescription: String
        get() = when (type) {
            ObjectiveType.COLLECT_CANDY -> {
                val color = candyType?.displayName ?: "candies"
                "Collect $color candies: $displayCurrent of $target${if (isCompleted) ", completed" else ""}"
            }
            ObjectiveType.TARGET_SCORE,
            ObjectiveType.SCORE_TARGET -> {
                "Target score: $displayCurrent of $target${if (isCompleted) ", completed" else ""}"
            }
            ObjectiveType.MAKE_MATCHES -> {
                "Make matches: $displayCurrent of $target${if (isCompleted) ", completed" else ""}"
            }
            ObjectiveType.CLEAR_BLOCKERS -> {
                "Clear blockers: $displayCurrent of $target${if (isCompleted) ", completed" else ""}"
            }
        }
}
