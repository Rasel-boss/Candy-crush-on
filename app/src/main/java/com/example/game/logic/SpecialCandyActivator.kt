package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import com.example.game.utils.ScoreCalculator

/**
 * Pure, deterministic engine responsible for:
 * 1. Calculating single special candy blast zones (Row clear, Column clear, 3x3 Wrapped explosion, Color clear).
 * 2. Evaluating direct special candy swap resolutions.
 * 3. Chaining secondary special candy activations safely with visited-ID sets to eliminate infinite loops.
 */
object SpecialCandyActivator {

    /**
     * Calculates the immediate blast area for a single [specialCandy] at its board position.
     *
     * @param board The current game board.
     * @param specialCandy The special candy tile being activated.
     * @param targetColor For [SpecialCandyType.COLOR_BOMB], the specific color to eliminate.
     */
    fun calculateSingleEffect(
        board: Match3Board,
        specialCandy: CandyTile,
        targetColor: CandyType? = null
    ): Set<BoardPosition> {
        val row = specialCandy.row
        val col = specialCandy.column
        val affected = mutableSetOf<BoardPosition>()

        when (specialCandy.specialCandyType) {
            SpecialCandyType.HORIZONTAL_STRIPED -> {
                // Remove entire row
                for (c in 0 until board.columns) {
                    affected.add(BoardPosition(row, c))
                }
            }
            SpecialCandyType.VERTICAL_STRIPED -> {
                // Remove entire column
                for (r in 0 until board.rows) {
                    affected.add(BoardPosition(r, col))
                }
            }
            SpecialCandyType.WRAPPED -> {
                // 3x3 explosion centered on wrapped candy with boundary bounds check
                for (r in (row - 1)..(row + 1)) {
                    for (c in (col - 1)..(col + 1)) {
                        if (BoardValidator.isValidPosition(BoardPosition(r, c), board.rows, board.columns)) {
                            affected.add(BoardPosition(r, c))
                        }
                    }
                }
            }
            SpecialCandyType.COLOR_BOMB -> {
                affected.add(specialCandy.position)
                val colorToClear = targetColor ?: specialCandy.type
                if (colorToClear.isPlayable && colorToClear != CandyType.EMPTY) {
                    for (tile in board.allTiles) {
                        if (tile.type == colorToClear && tile.isPlayable) {
                            affected.add(tile.position)
                        }
                    }
                }
            }
            SpecialCandyType.NONE -> {
                affected.add(specialCandy.position)
            }
        }

        return affected
    }

    /**
     * Evaluates chained special candy activations from an initial set of affected positions.
     * Iteratively gathers all additional affected tiles until no unactivated specials remain in the blast area.
     * Prevents infinite activation loops by checking and tracking [alreadyActivatedIds].
     */
    fun resolveChainedSpecials(
        board: Match3Board,
        currentlyAffected: Set<BoardPosition>,
        alreadyActivatedIds: MutableSet<Long>,
        activatedSpecials: MutableList<CandyTile> = mutableListOf()
    ): SpecialResolutionResult {
        val allAffected = currentlyAffected.toMutableSet()
        var bonusScore = 0
        var foundNewSpecials = true

        while (foundNewSpecials) {
            foundNewSpecials = false
            val unvisitedSpecials = mutableListOf<CandyTile>()

            for (pos in allAffected) {
                val tile = board.getTile(pos) ?: continue
                if (tile.isSpecial && !alreadyActivatedIds.contains(tile.id)) {
                    unvisitedSpecials.add(tile)
                }
            }

            for (specialTile in unvisitedSpecials) {
                alreadyActivatedIds.add(specialTile.id)
                activatedSpecials.add(specialTile)
                bonusScore += ScoreCalculator.calculateSpecialActivationScore(specialTile.specialCandyType)

                val blastArea = calculateSingleEffect(board, specialTile)
                val isNew = allAffected.addAll(blastArea)
                if (isNew) {
                    foundNewSpecials = true
                }
            }
        }

        return SpecialResolutionResult(
            affectedPositions = allAffected,
            activatedSpecials = activatedSpecials,
            bonusScore = bonusScore
        )
    }

    /**
     * Resolves a direct player swap involving at least one special candy.
     */
    fun resolveDirectSpecialSwap(
        board: Match3Board,
        posA: BoardPosition,
        posB: BoardPosition,
        alreadyActivatedIds: MutableSet<Long> = mutableSetOf()
    ): SpecialResolutionResult {
        val tileA = board.getTile(posA) ?: return SpecialResolutionResult(emptySet(), emptyList(), 0)
        val tileB = board.getTile(posB) ?: return SpecialResolutionResult(emptySet(), emptyList(), 0)

        // Check if this swap forms a dedicated special combination
        if (SpecialCombinationResolver.canCombine(board, posA, posB)) {
            val comboResult = SpecialCombinationResolver.resolveCombination(board, posA, posB, alreadyActivatedIds)
            return SpecialResolutionResult(
                affectedPositions = comboResult.affectedPositions,
                activatedSpecials = comboResult.activatedSpecials,
                bonusScore = comboResult.score
            )
        }

        val initialAffected = mutableSetOf<BoardPosition>()
        val activatedSpecials = mutableListOf<CandyTile>()
        var bonusScore = 0

        // Check if tileA is special
        if (tileA.isSpecial) {
            val targetColor = if (tileA.specialCandyType == SpecialCandyType.COLOR_BOMB) tileB.type else null
            val affected = calculateSingleEffect(board, tileA, targetColor)
            initialAffected.addAll(affected)
            activatedSpecials.add(tileA)
            alreadyActivatedIds.add(tileA.id)
            bonusScore += ScoreCalculator.calculateSpecialActivationScore(tileA.specialCandyType)
        }

        // Check if tileB is special
        if (tileB.isSpecial) {
            val targetColor = if (tileB.specialCandyType == SpecialCandyType.COLOR_BOMB) tileA.type else null
            val affected = calculateSingleEffect(board, tileB, targetColor)
            initialAffected.addAll(affected)
            activatedSpecials.add(tileB)
            alreadyActivatedIds.add(tileB.id)
            bonusScore += ScoreCalculator.calculateSpecialActivationScore(tileB.specialCandyType)
        }

        // Include the swapped positions themselves
        initialAffected.add(posA)
        initialAffected.add(posB)

        // Resolve chained special activations safely
        val chainResult = resolveChainedSpecials(
            board = board,
            currentlyAffected = initialAffected,
            alreadyActivatedIds = alreadyActivatedIds,
            activatedSpecials = activatedSpecials
        )

        return SpecialResolutionResult(
            affectedPositions = chainResult.affectedPositions,
            activatedSpecials = chainResult.activatedSpecials,
            bonusScore = bonusScore + chainResult.bonusScore
        )
    }
}
