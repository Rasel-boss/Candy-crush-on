package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyType
import com.example.game.model.SpecialCandyType

/**
 * Categorizes the geometric formation type of a matched group of candies.
 */
enum class MatchFormationType {
    NONE,
    HORIZONTAL,
    VERTICAL,
    L_SHAPE,
    T_SHAPE,
    FIVE_IN_A_ROW
}

/**
 * Rich data bundle describing a grouped match event on the board.
 *
 * @property type The candy type that matched.
 * @property positions All board positions participating in this match group.
 * @property formationType Geometric shape formation (HORIZONTAL, VERTICAL, L_SHAPE, T_SHAPE, FIVE_IN_A_ROW).
 * @property specialCandyCandidate Potential special candy to spawn from this match.
 */
data class MatchGroup(
    val type: CandyType,
    val positions: Set<BoardPosition>,
    val formationType: MatchFormationType,
    val specialCandyCandidate: SpecialCandyType = SpecialCandyType.NONE
) {
    val size: Int
        get() = positions.size
}
