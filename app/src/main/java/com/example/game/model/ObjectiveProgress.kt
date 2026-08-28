package com.example.game.model

/**
 * Clean data representation of level objective progress.
 *
 * @property current Current raw progress achieved.
 * @property target The goal required to complete the objective.
 * @property completed Whether the objective has satisfied or exceeded the target goal.
 */
data class ObjectiveProgress(
    val current: Int,
    val target: Int,
    val completed: Boolean = target > 0 && current >= target
) {
    /**
     * Displayed current value clamped between 0 and target so progress never exceeds target when displayed.
     * E.g. "1250 / 5000", "5000 / 5000".
     */
    val displayCurrent: Int
        get() = current.coerceIn(0, target)

    /**
     * Human-readable formatted progress text (e.g. "1,250 / 5,000" or "12 / 20").
     */
    val formattedDisplay: String
        get() = "$displayCurrent / $target"

    /**
     * Normalized completion ratio bounded between 0.0f and 1.0f for progress bar animations.
     */
    val progressRatio: Float
        get() = if (target > 0) (current.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
}
