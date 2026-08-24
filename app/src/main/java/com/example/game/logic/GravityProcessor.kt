package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.Match3Board

data class TileFall(
    val from: BoardPosition,
    val to: BoardPosition,
    val tile: CandyTile
)

data class GravityResult(
    val updatedBoard: Match3Board,
    val falls: List<TileFall>
)

class GravityProcessor {

    fun applyGravity(board: Match3Board): GravityResult {
        var currentBoard = board
        val falls = mutableListOf<TileFall>()

        for (col in 0 until board.cols) {
            var writeRow = board.rows - 1

            for (readRow in (board.rows - 1) downTo 0) {
                val tile = currentBoard[readRow, col]
                if (tile != null) {
                    if (readRow != writeRow) {
                        val from = BoardPosition(readRow, col)
                        val to = BoardPosition(writeRow, col)
                        falls.add(TileFall(from, to, tile))
                        currentBoard = currentBoard.set(to, tile).set(from, null)
                    }
                    writeRow--
                }
            }
        }

        return GravityResult(currentBoard, falls)
    }
}
