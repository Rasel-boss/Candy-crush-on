package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import com.example.game.model.SpecialCombinationType

data class CombinationResult(
    val combinationType: SpecialCombinationType,
    val affectedPositions: Set<BoardPosition>,
    val transformedTiles: Map<BoardPosition, CandyTile> = emptyMap(),
    val scoreMultiplier: Int = 2
)

class SpecialCombinationResolver(
    private val activator: SpecialCandyActivator = SpecialCandyActivator()
) {

    fun checkCombination(tile1: CandyTile, tile2: CandyTile): SpecialCombinationType? {
        val s1 = tile1.specialType ?: return null
        val s2 = tile2.specialType ?: return null

        val isStriped1 = s1 == SpecialCandyType.STRIPED_HORIZONTAL || s1 == SpecialCandyType.STRIPED_VERTICAL
        val isStriped2 = s2 == SpecialCandyType.STRIPED_HORIZONTAL || s2 == SpecialCandyType.STRIPED_VERTICAL

        return when {
            s1 == SpecialCandyType.COLOR_BOMB && s2 == SpecialCandyType.COLOR_BOMB ->
                SpecialCombinationType.COLOR_BOMB_PLUS_COLOR_BOMB

            (s1 == SpecialCandyType.COLOR_BOMB && isStriped2) || (s2 == SpecialCandyType.COLOR_BOMB && isStriped1) ->
                SpecialCombinationType.COLOR_BOMB_PLUS_STRIPED

            (s1 == SpecialCandyType.COLOR_BOMB && s2 == SpecialCandyType.WRAPPED) || (s2 == SpecialCandyType.COLOR_BOMB && s1 == SpecialCandyType.WRAPPED) ->
                SpecialCombinationType.COLOR_BOMB_PLUS_WRAPPED

            isStriped1 && isStriped2 ->
                SpecialCombinationType.STRIPED_PLUS_STRIPED

            (isStriped1 && s2 == SpecialCandyType.WRAPPED) || (s2 == SpecialCandyType.WRAPPED && isStriped1) ->
                SpecialCombinationType.STRIPED_PLUS_WRAPPED

            s1 == SpecialCandyType.WRAPPED && s2 == SpecialCandyType.WRAPPED ->
                SpecialCombinationType.WRAPPED_PLUS_WRAPPED

            else -> null
        }
    }

    fun resolveCombination(
        board: Match3Board,
        pos1: BoardPosition,
        pos2: BoardPosition,
        tile1: CandyTile,
        tile2: CandyTile
    ): CombinationResult? {
        val comboType = checkCombination(tile1, tile2) ?: return null
        val center = pos2
        val affected = mutableSetOf<BoardPosition>()
        affected.add(pos1)
        affected.add(pos2)

        when (comboType) {
            SpecialCombinationType.COLOR_BOMB_PLUS_COLOR_BOMB -> {
                affected.addAll(board.allPositions())
                return CombinationResult(comboType, affected, scoreMultiplier = 5)
            }
            SpecialCombinationType.STRIPED_PLUS_STRIPED -> {
                for (c in 0 until board.cols) affected.add(BoardPosition(center.row, c))
                for (r in 0 until board.rows) affected.add(BoardPosition(r, center.col))
                return CombinationResult(comboType, affected, scoreMultiplier = 2)
            }
            SpecialCombinationType.STRIPED_PLUS_WRAPPED -> {
                for (rOffset in -1..1) {
                    val r = center.row + rOffset
                    if (r in 0 until board.rows) {
                        for (c in 0 until board.cols) affected.add(BoardPosition(r, c))
                    }
                }
                for (cOffset in -1..1) {
                    val c = center.col + cOffset
                    if (c in 0 until board.cols) {
                        for (r in 0 until board.rows) affected.add(BoardPosition(r, c))
                    }
                }
                return CombinationResult(comboType, affected, scoreMultiplier = 3)
            }
            SpecialCombinationType.WRAPPED_PLUS_WRAPPED -> {
                for (r in (center.row - 2)..(center.row + 2)) {
                    for (c in (center.col - 2)..(center.col + 2)) {
                        val p = BoardPosition(r, c)
                        if (board.isValidPosition(p)) affected.add(p)
                    }
                }
                return CombinationResult(comboType, affected, scoreMultiplier = 3)
            }
            SpecialCombinationType.COLOR_BOMB_PLUS_STRIPED -> {
                val normalCandy = if (tile1.specialType == SpecialCandyType.COLOR_BOMB) tile2 else tile1
                val targetColor = normalCandy.type
                val transformed = mutableMapOf<BoardPosition, CandyTile>()
                var id = 800000L

                for (r in 0 until board.rows) {
                    for (c in 0 until board.cols) {
                        val p = BoardPosition(r, c)
                        val t = board[p]
                        if (t != null && t.type == targetColor) {
                            val st = if ((r + c) % 2 == 0) SpecialCandyType.STRIPED_HORIZONTAL else SpecialCandyType.STRIPED_VERTICAL
                            val newTile = CandyTile(++id, targetColor, st)
                            transformed[p] = newTile
                            val lineAffected = activator.getAffectedPositions(board, p, st)
                            affected.addAll(lineAffected)
                        }
                    }
                }
                return CombinationResult(comboType, affected, transformed, scoreMultiplier = 4)
            }
            SpecialCombinationType.COLOR_BOMB_PLUS_WRAPPED -> {
                val normalCandy = if (tile1.specialType == SpecialCandyType.COLOR_BOMB) tile2 else tile1
                val targetColor = normalCandy.type
                for (r in 0 until board.rows) {
                    for (c in 0 until board.cols) {
                        val p = BoardPosition(r, c)
                        val t = board[p]
                        if (t != null && t.type == targetColor) {
                            val wrappedAffected = activator.getAffectedPositions(board, p, SpecialCandyType.WRAPPED)
                            affected.addAll(wrappedAffected)
                        }
                    }
                }
                return CombinationResult(comboType, affected, scoreMultiplier = 4)
            }
        }
    }
}
