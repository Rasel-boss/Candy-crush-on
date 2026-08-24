package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType

class SpecialCandyActivator {

    fun getAffectedPositions(
        board: Match3Board,
        position: BoardPosition,
        specialType: SpecialCandyType,
        targetColor: CandyType? = null
    ): Set<BoardPosition> {
        val affected = mutableSetOf<BoardPosition>()
        affected.add(position)

        when (specialType) {
            SpecialCandyType.STRIPED_HORIZONTAL -> {
                for (col in 0 until board.cols) {
                    affected.add(BoardPosition(position.row, col))
                }
            }
            SpecialCandyType.STRIPED_VERTICAL -> {
                for (row in 0 until board.rows) {
                    affected.add(BoardPosition(row, position.col))
                }
            }
            SpecialCandyType.WRAPPED -> {
                for (r in (position.row - 1)..(position.row + 1)) {
                    for (c in (position.col - 1)..(position.col + 1)) {
                        val pos = BoardPosition(r, c)
                        if (board.isValidPosition(pos)) {
                            affected.add(pos)
                        }
                    }
                }
            }
            SpecialCandyType.COLOR_BOMB -> {
                val colorToClear = targetColor ?: board[position]?.type
                if (colorToClear != null) {
                    for (r in 0 until board.rows) {
                        for (c in 0 until board.cols) {
                            val pos = BoardPosition(r, c)
                            val tile = board[pos]
                            if (tile != null && tile.type == colorToClear) {
                                affected.add(pos)
                            }
                        }
                    }
                }
            }
        }
        return affected
    }
}
