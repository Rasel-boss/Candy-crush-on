package com.example.game.model

import androidx.compose.ui.graphics.Color

/**
 * Enumeration of candy types in the Match-3 puzzle game.
 * Includes 6 normal playable candy types with distinct visual identities,
 * and an EMPTY sentinel type for internal board processing only.
 */
enum class CandyType(
    val displayName: String,
    val symbol: String,
    val color: Color,
    val isPlayable: Boolean = true
) {
    RED(
        displayName = "Red Berry",
        symbol = "◆",
        color = Color(0xFFEF4444) // Vibrant Red
    ),
    BLUE(
        displayName = "Blue Drop",
        symbol = "●",
        color = Color(0xFF3B82F6) // Vivid Blue
    ),
    GREEN(
        displayName = "Green Apple",
        symbol = "■",
        color = Color(0xFF10B981) // Emerald Green
    ),
    YELLOW(
        displayName = "Yellow Star",
        symbol = "★",
        color = Color(0xFFF59E0B) // Golden Amber/Yellow
    ),
    PURPLE(
        displayName = "Purple Grape",
        symbol = "▲",
        color = Color(0xFF8B5CF6) // Royal Purple
    ),
    ORANGE(
        displayName = "Orange Citrus",
        symbol = "⬢",
        color = Color(0xFFF97316) // Bright Orange
    ),
    EMPTY(
        displayName = "Empty",
        symbol = "",
        color = Color.Transparent,
        isPlayable = false
    );

    companion object {
        /**
         * The list of normal playable candy types used for board generation and refills.
         */
        val PLAYABLE_TYPES: List<CandyType> = entries.filter { it.isPlayable }
        val playableCandies: List<CandyType> = PLAYABLE_TYPES
    }
}
