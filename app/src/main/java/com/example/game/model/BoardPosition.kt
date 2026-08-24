package com.example.game.model

import kotlin.math.abs

/**
 * Represents a 2D coordinate position (row, column) on the Match-3 game board.
 * Coordinates are 0-indexed: row 0 is top, column 0 is left.
 */
data class BoardPosition(
    val row: Int,
    val column: Int
) {
    /**
     * Checks if this position is orthogonally adjacent (UP, DOWN, LEFT, RIGHT) to [other].
     * Diagonal positions and identical positions return false.
     */
    fun isAdjacent(other: BoardPosition): Boolean {
        val rowDiff = abs(row - other.row)
        val colDiff = abs(column - other.column)
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)
    }

    override fun toString(): String = "($row, $column)"
}
