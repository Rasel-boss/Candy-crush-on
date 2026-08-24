package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import com.example.game.utils.ScoreCalculator

/**
 * Data class representing the creation of a special candy resulting from a match.
 *
 * @property position The coordinate where the special candy is placed.
 * @property specialType The type of special candy created.
 * @property baseType The base color/type of the candy.
 */
data class SpecialCandyCreation(
    val position: BoardPosition,
    val specialType: SpecialCandyType,
    val baseType: CandyType
)

/**
 * Result bundle after evaluating special candy activations and chains.
 *
 * @property affectedPositions All board positions to be removed by regular matches and special effects.
 * @property activatedSpecials List of special candy tiles that were triggered during this resolution.
 * @property bonusScore Points awarded specifically for the special candy activations.
 */
data class SpecialResolutionResult(
    val affectedPositions: Set<BoardPosition>,
    val activatedSpecials: List<CandyTile>,
    val bonusScore: Int
)

/**
 * Pure, deterministic engine responsible for:
 * 1. Detecting special candy creation opportunities from player matches (4-match, 5-match, L/T shapes).
 * 2. Evaluating direct special candy swap activations (Color Bomb + Color, Striped, Wrapped).
 * 3. Calculating affected blast areas (row, column, 3x3, color clear).
 * 4. Resolving chained special activations safely without infinite loops.
 */
object SpecialCandyResolver {

    /**
     * Resets internal ID generator for deterministic test fixtures if needed.
     */
    fun resetIdCounter(start: Long = 700000L) {
        SpecialCandyCreator.resetIdCounter(start)
    }

    /**
     * Determines whether the given matches formed by a player swap create any special candies.
     */
    fun determineCreatedSpecialCandies(
        matches: List<SingleMatch>,
        swapPosA: BoardPosition? = null,
        swapPosB: BoardPosition? = null
    ): List<SpecialCandyCreation> {
        return SpecialCandyCreator.createSpecialCandiesFromMatches(matches, swapPosA, swapPosB)
    }

    /**
     * Checks if swapping [posA] and [posB] constitutes a direct special candy activation.
     */
    fun isDirectSpecialSwap(board: Match3Board, posA: BoardPosition, posB: BoardPosition): Boolean {
        val tileA = board.getTile(posA) ?: return false
        val tileB = board.getTile(posB) ?: return false
        if (!tileA.isPlayable || !tileB.isPlayable) return false

        return tileA.isSpecial || tileB.isSpecial
    }

    /**
     * Calculates the immediate blast area for a single [specialCandy] at [position].
     */
    fun calculateSingleSpecialEffect(
        board: Match3Board,
        specialCandy: CandyTile,
        targetColor: CandyType? = null
    ): Set<BoardPosition> {
        return SpecialCandyActivator.calculateSingleEffect(board, specialCandy, targetColor)
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
        return SpecialCandyActivator.resolveDirectSpecialSwap(board, posA, posB, alreadyActivatedIds)
    }

    /**
     * Evaluates chained special candy activations from an initial set of affected positions.
     */
    fun resolveChainedSpecials(
        board: Match3Board,
        currentlyAffected: Set<BoardPosition>,
        alreadyActivatedIds: MutableSet<Long>,
        activatedSpecials: MutableList<CandyTile> = mutableListOf()
    ): SpecialResolutionResult {
        return SpecialCandyActivator.resolveChainedSpecials(board, currentlyAffected, alreadyActivatedIds, activatedSpecials)
    }

    /**
     * Creates a new [CandyTile] with the given [specialType] and [baseType] at [position].
     */
    fun createSpecialTile(
        position: BoardPosition,
        specialType: SpecialCandyType,
        baseType: CandyType
    ): CandyTile {
        return SpecialCandyCreator.createTile(position, specialType, baseType)
    }
}
