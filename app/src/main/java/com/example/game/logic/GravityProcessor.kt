package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board

/**
 * Pure, deterministic logic for applying gravity to a Match-3 board.
 * Candies fall downward into empty spaces column by column, preserving their relative vertical order.
 */
object GravityProcessor {

    /**
     * Applies downward gravity to the [board].
     *
     * For each column:
     * 1. Collects all non-empty (playable) candies from top to bottom.
     * 2. Places all non-empty candies at the bottom of the column, maintaining their relative order.
     * 3. Fills the remaining spaces at the top of the column with [CandyType.EMPTY] tiles.
     *
     * Does not move candies horizontally.
     */
    fun applyGravity(board: Match3Board): Match3Board {
        val rows = board.rows
        val cols = board.columns

        // Temporary 2D array for the new grid of tiles
        val newGrid = Array(rows) { r ->
            Array(cols) { c ->
                CandyTile(
                    id = (r * cols + c + 1).toLong(),
                    type = CandyType.EMPTY,
                    row = r,
                    column = c
                )
            }
        }

        var nextEmptyId = 100000L

        for (c in 0 until cols) {
            // Collect non-empty tiles in column c in original top-to-bottom order
            val columnCandies = mutableListOf<CandyTile>()
            for (r in 0 until rows) {
                val tile = board.getTile(r, c)
                if (tile != null && tile.isPlayable) {
                    columnCandies.add(tile)
                }
            }

            val nonEmptyCount = columnCandies.size
            val emptyCount = rows - nonEmptyCount

            // 1. Fill top rows with EMPTY
            for (r in 0 until emptyCount) {
                newGrid[r][c] = CandyTile(
                    id = nextEmptyId++,
                    type = CandyType.EMPTY,
                    row = r,
                    column = c
                )
            }

            // 2. Fill bottom rows with original non-empty candies in order
            for (i in 0 until nonEmptyCount) {
                val targetRow = emptyCount + i
                val originalTile = columnCandies[i]
                newGrid[targetRow][c] = originalTile.copy(
                    row = targetRow,
                    column = c
                )
            }
        }

        val updatedTiles = newGrid.map { it.toList() }.toList()
        return board.copy(tiles = updatedTiles)
    }
}
