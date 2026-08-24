package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyType

data class MatchGroup(
    val positions: Set<BoardPosition>,
    val type: CandyType,
    val isHorizontal: Boolean = false,
    val isVertical: Boolean = false,
    val isTOrLShape: Boolean = false
) {
    val size: Int get() = positions.size
}
