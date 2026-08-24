package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType

data class SpecialDetonationResult(
    val affectedPositions: Set<BoardPosition>,
    val secondaryActivations: List<Pair<BoardPosition, CandyTile>>
)

class SpecialCandyResolver(
    private val activator: SpecialCandyActivator = SpecialCandyActivator()
) {

    fun resolveDetonations(
        board: Match3Board,
        positionsToDetonate: Set<BoardPosition>,
        preferredTargetColor: CandyType? = null
    ): SpecialDetonationResult {
        val totalAffected = mutableSetOf<BoardPosition>()
        val queue = ArrayDeque<BoardPosition>()
        val processed = mutableSetOf<BoardPosition>()
        val secondary = mutableListOf<Pair<BoardPosition, CandyTile>>()

        queue.addAll(positionsToDetonate)

        while (queue.isNotEmpty()) {
            val currentPos = queue.removeFirst()
            if (currentPos in processed) continue
            processed.add(currentPos)

            val tile = board[currentPos] ?: continue
            totalAffected.add(currentPos)

            if (tile.isSpecial) {
                secondary.add(currentPos to tile)
                val directAffected = activator.getAffectedPositions(
                    board = board,
                    position = currentPos,
                    specialType = tile.specialType!!,
                    targetColor = preferredTargetColor ?: tile.type
                )
                for (pos in directAffected) {
                    totalAffected.add(pos)
                    val impactedTile = board[pos]
                    if (impactedTile != null && impactedTile.isSpecial && pos !in processed) {
                        queue.add(pos)
                    }
                }
            }
        }

        return SpecialDetonationResult(totalAffected, secondary)
    }
}
