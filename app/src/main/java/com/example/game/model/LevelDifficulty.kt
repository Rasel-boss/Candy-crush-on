package com.example.game.model

/**
 * Difficulty classification for Match-3 levels.
 */
enum class LevelDifficulty(val displayName: String) {
    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard"),
    EXPERT("Expert");

    companion object {
        fun fromString(value: String): LevelDifficulty {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: NORMAL
        }
    }
}
