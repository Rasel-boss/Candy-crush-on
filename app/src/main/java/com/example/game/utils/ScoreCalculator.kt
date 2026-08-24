package com.example.game.utils

import com.example.game.logic.SingleMatch
import com.example.game.model.SpecialCandyType
import com.example.game.model.SpecialCombinationType
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

/**
 * Deterministic utility calculating and formatting Match-3 game scores based on matched tiles,
 * special candy activations, special candy combinations, match runs, and remaining moves bonus.
 */
object ScoreCalculator {

    const val MATCH_3_POINTS = 30
    const val MATCH_4_POINTS = 60
    const val MATCH_5_POINTS = 100

    const val STRIPED_ACTIVATION_POINTS = 100
    const val WRAPPED_ACTIVATION_POINTS = 150
    const val COLOR_BOMB_ACTIVATION_POINTS = 200

    // Special Combination Bonus Points
    const val COMBO_STRIPED_STRIPED_POINTS = 150
    const val COMBO_WRAPPED_WRAPPED_POINTS = 200
    const val COMBO_STRIPED_WRAPPED_POINTS = 250
    const val COMBO_COLOR_BOMB_NORMAL_POINTS = 200
    const val COMBO_COLOR_BOMB_STRIPED_POINTS = 300
    const val COMBO_COLOR_BOMB_WRAPPED_POINTS = 350
    const val COMBO_COLOR_BOMB_COLOR_BOMB_POINTS = 500

    const val REMAINING_MOVE_BONUS = 50

    /**
     * Calculates deterministic points earned for a match of length [matchLength].
     * - 3-match: +30 points
     * - 4-match: +60 points
     * - 5+ match: +100 points
     */
    fun calculateMatchScore(matchLength: Int): Int {
        return when {
            matchLength < 3 -> 0
            matchLength == 3 -> MATCH_3_POINTS
            matchLength == 4 -> MATCH_4_POINTS
            else -> MATCH_5_POINTS
        }
    }

    /**
     * Calculates total points earned for a list of linear matches.
     */
    fun calculateMatchesScore(matches: List<SingleMatch>): Int {
        return matches.sumOf { calculateMatchScore(it.length) }
    }

    /**
     * Calculates bonus points awarded for activating a special candy of [specialType].
     */
    fun calculateSpecialActivationScore(specialType: SpecialCandyType): Int {
        return when (specialType) {
            SpecialCandyType.HORIZONTAL_STRIPED,
            SpecialCandyType.VERTICAL_STRIPED -> STRIPED_ACTIVATION_POINTS
            SpecialCandyType.WRAPPED -> WRAPPED_ACTIVATION_POINTS
            SpecialCandyType.COLOR_BOMB -> COLOR_BOMB_ACTIVATION_POINTS
            SpecialCandyType.NONE -> 0
        }
    }

    /**
     * Calculates bonus points awarded for a special combination of [comboType].
     */
    fun calculateCombinationScore(comboType: SpecialCombinationType): Int {
        return when (comboType) {
            SpecialCombinationType.STRIPED_STRIPED -> COMBO_STRIPED_STRIPED_POINTS
            SpecialCombinationType.WRAPPED_WRAPPED -> COMBO_WRAPPED_WRAPPED_POINTS
            SpecialCombinationType.STRIPED_WRAPPED -> COMBO_STRIPED_WRAPPED_POINTS
            SpecialCombinationType.COLOR_BOMB_NORMAL -> COMBO_COLOR_BOMB_NORMAL_POINTS
            SpecialCombinationType.COLOR_BOMB_STRIPED -> COMBO_COLOR_BOMB_STRIPED_POINTS
            SpecialCombinationType.COLOR_BOMB_WRAPPED -> COMBO_COLOR_BOMB_WRAPPED_POINTS
            SpecialCombinationType.COLOR_BOMB_COLOR_BOMB -> COMBO_COLOR_BOMB_COLOR_BOMB_POINTS
            SpecialCombinationType.NONE -> 0
        }
    }

    /**
     * Calculates the end-of-level bonus points for remaining moves.
     */
    fun calculateRemainingMovesBonus(movesRemaining: Int): Int {
        return max(0, movesRemaining) * REMAINING_MOVE_BONUS
    }

    /**
     * Formats an integer score into a readable string with thousand separators (e.g. 1,550).
     */
    fun formatScore(score: Int): String {
        return NumberFormat.getNumberInstance(Locale.US).format(score.coerceAtLeast(0))
    }
}

