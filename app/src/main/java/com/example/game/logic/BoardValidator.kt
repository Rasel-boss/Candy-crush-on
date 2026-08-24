package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.Match3Board

class BoardValidator(
    private val matchDetector: MatchDetector = MatchDetector(),
    private val combinationResolver: SpecialCombinationResolver = SpecialCombinationResolver()
) {

    fun hasPossibleMoves(board: Match3Board): Boolean {
        for (r in 0 until board.rows) {
            for (c in 0 until board.cols) {
                val currentPos = BoardPosition(r, c)
                val currentTile = board[currentPos] ?: continue

                // Check right swap
                if (c + 1 < board.cols) {
                    val rightPos = BoardPosition(r, c + 1)
                    val rightTile = board[rightPos]
                    if (rightTile != null && isValidSwap(board, currentPos, rightPos)) {
                        return true
                    }
                }

                // Check down swap
                if (r + 1 < board.rows) {
                    val downPos = BoardPosition(r + 1, c)
                    val downTile = board[downPos]
                    if (downTile != null && isValidSwap(board, currentPos, downPos)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun isValidSwap(board: Match3Board, pos1: BoardPosition, pos2: BoardPosition): Boolean {
        if (!pos1.isAdjacentTo(pos2)) return false
        val tile1 = board[pos1] ?: return false
        val tile2 = board[pos2] ?: return false

        // Check special combination
        if (combinationResolver.checkCombination(tile1, tile2) != null) {
            return true
        }

        // Check color bomb swap with any normal candy
        if (tile1.isColorBomb || tile2.isColorBomb) {
            return true
        }

        // Test swap for match
        val swappedBoard = board.swap(pos1, pos2)
        val matches = matchDetector.findMatches(swappedBoard)
        return matches.isNotEmpty()
    }
}
