package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.DEFAULT_COLUMNS
import com.example.game.model.DEFAULT_ROWS
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType

/**
 * Reusable validation utilities for Match-3 board geometry, bounds, adjacency, and data integrity.
 */
object BoardValidator {

    /**
     * Checks if coordinates ([row], [column]) fall within the valid board dimensions.
     */
    fun isInsideBoard(
        row: Int,
        column: Int,
        rows: Int = DEFAULT_ROWS,
        columns: Int = DEFAULT_COLUMNS
    ): Boolean {
        return row in 0 until rows && column in 0 until columns
    }

    /**
     * Checks if [position] falls within the valid board boundaries.
     */
    fun isValidPosition(
        position: BoardPosition,
        rows: Int = DEFAULT_ROWS,
        columns: Int = DEFAULT_COLUMNS
    ): Boolean {
        return isInsideBoard(position.row, position.column, rows, columns)
    }

    /**
     * Checks whether [posA] and [posB] are orthogonally adjacent.
     */
    fun isAdjacent(posA: BoardPosition, posB: BoardPosition): Boolean {
        return posA.isAdjacent(posB)
    }

    /**
     * Validates an individual candy tile:
     * - Color Bomb: valid when specialCandyType == COLOR_BOMB
     * - Normal/Special playable candy: valid when type.isPlayable
     * - Empty candy: valid if allowEmpty is true and type == EMPTY and special == NONE
     */
    fun isTileValid(tile: CandyTile, allowEmpty: Boolean = false): Boolean {
        if (tile.specialCandyType == com.example.game.model.SpecialCandyType.COLOR_BOMB) {
            return true
        }
        if (tile.type.isPlayable) {
            return true
        }
        if (allowEmpty && tile.isEmpty) {
            return true
        }
        return false
    }

    /**
     * Validates that the [board] meets structural integrity requirements:
     * - Dimensions match expected rows & columns.
     * - Total positions equal rows * columns.
     * - No null tiles or out-of-bound tile coordinates exist.
     * - Every tile contains a valid playable candy (normal, striped, wrapped, or Color Bomb).
     * - If [allowEmpty] is true, EMPTY tiles during cascade transitions are accepted.
     */
    fun isBoardValid(
        board: Match3Board,
        expectedRows: Int = DEFAULT_ROWS,
        expectedColumns: Int = DEFAULT_COLUMNS,
        allowEmpty: Boolean = false
    ): Boolean {
        if (board.rows != expectedRows || board.columns != expectedColumns) return false
        if (board.tiles.size != expectedRows) return false

        for (r in 0 until expectedRows) {
            val rowList = board.tiles.getOrNull(r) ?: return false
            if (rowList.size != expectedColumns) return false

            for (c in 0 until expectedColumns) {
                val tile = rowList.getOrNull(c) ?: return false
                if (tile.row != r || tile.column != c) return false
                if (!isTileValid(tile, allowEmpty)) return false
            }
        }
        return true
    }

    /**
     * Checks if the board currently contains any 3-or-more in-a-row matches.
     */
    fun containsInitialMatches(board: Match3Board): Boolean {
        return MatchDetector.hasAnyMatches(board)
    }

    /**
     * Checks if the board has at least one possible valid move remaining.
     */
    fun hasPossibleMoves(board: Match3Board): Boolean {
        return MatchDetector.hasPossibleMoves(board)
    }
}
