package com.example.game.model

data class LevelObjective(
    val type: ObjectiveType,
    val targetAmount: Int,
    val currentAmount: Int = 0,
    val targetCandyType: CandyType? = null
) {
    val isCompleted: Boolean
        get() = currentAmount >= targetAmount

    val progressRatio: Float
        get() = if (targetAmount > 0) (currentAmount.toFloat() / targetAmount).coerceIn(0f, 1f) else 1f

    val description: String
        get() = when (type) {
            ObjectiveType.COLLECT_CANDY -> "Collect $targetAmount ${targetCandyType?.displayName ?: "Candies"}"
            ObjectiveType.TARGET_SCORE -> "Reach $targetAmount Points"
            ObjectiveType.MAKE_MATCHES -> "Make $targetAmount Matches"
        }
}
