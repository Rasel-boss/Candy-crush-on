package com.example.game.model

import kotlin.math.abs

data class BoardPosition(val row: Int, val col: Int) {
    fun isAdjacentTo(other: BoardPosition): Boolean {
        val rowDiff = abs(row - other.row)
        val colDiff = abs(col - other.col)
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)
    }
}
