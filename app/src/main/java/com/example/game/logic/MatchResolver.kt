package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import com.example.game.utils.ScoreCalculator
import kotlin.random.Random

/**
 * Snapshot representing a single cascade step during match resolution.
 *
 * @property matches The linear matches detected during this step.
 * @property matchedPositions The distinct set of matched/affected positions.
 * @property stepScore The points awarded for this step's matches and special activations.
 * @property boardAfterRemoval The board after replacing matched positions with EMPTY (and placing created specials).
 * @property boardAfterGravity The board after non-empty candies fall to the bottom.
 * @property boardAfterRefill The board after empty spaces at the top are refilled with new candies.
 * @property createdSpecials Any special candies created during this step.
 * @property activatedSpecials Any special candies triggered and consumed during this step.
 */
data class CascadeStep(
    val matches: List<SingleMatch>,
    val matchedPositions: Set<BoardPosition>,
    val stepScore: Int,
    val boardAfterRemoval: Match3Board,
    val boardAfterGravity: Match3Board,
    val boardAfterRefill: Match3Board,
    val createdSpecials: List<SpecialCandyCreation> = emptyList(),
    val activatedSpecials: List<CandyTile> = emptyList(),
    val removedTiles: List<CandyTile> = emptyList()
)

/**
 * Complete outcome of resolving all consecutive matches and cascades on a board.
 *
 * @property finalBoard The resulting stable board with all matches cleared and all slots filled.
 * @property steps List of all cascade steps executed.
 * @property totalScoreGained Total score accumulated across all steps.
 * @property totalMatchesCount Total number of individual linear matches found and cleared.
 * @property isStable True if no matches remain on the final board.
 */
data class CascadeResolutionResult(
    val finalBoard: Match3Board,
    val steps: List<CascadeStep>,
    val totalScoreGained: Int,
    val totalMatchesCount: Int,
    val isStable: Boolean
)

/**
 * Pure, deterministic engine orchestrating match removal, special candy placement,
 * special activation blasting, gravity collapse, candy refilling, and iterative cascade evaluation.
 */
object MatchResolver {

    const val MAX_CASCADE_ITERATIONS = 100

    /**
     * Removes all candies at [matchedPositions] by converting them into [CandyType.EMPTY] tiles.
     * All non-matching candies retain their positions and properties.
     */
    fun removeMatches(board: Match3Board, matchedPositions: Set<BoardPosition>): Match3Board {
        return removeMatchesAndPlaceSpecials(board, matchedPositions, emptyList())
    }

    /**
     * Removes candies at [positionsToRemove] (replacing them with EMPTY), while placing newly created
     * special candies at their designated positions so they survive the removal.
     */
    fun removeMatchesAndPlaceSpecials(
        board: Match3Board,
        positionsToRemove: Set<BoardPosition>,
        createdSpecials: List<SpecialCandyCreation> = emptyList()
    ): Match3Board {
        if (positionsToRemove.isEmpty() && createdSpecials.isEmpty()) return board

        var nextEmptyId = 500000L
        val specialCreationMap = createdSpecials.associateBy { it.position }

        val updatedTiles = board.tiles.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, tile ->
                val pos = BoardPosition(r, c)
                val specialCreation = specialCreationMap[pos]

                if (specialCreation != null) {
                    SpecialCandyResolver.createSpecialTile(
                        position = pos,
                        specialType = specialCreation.specialType,
                        baseType = specialCreation.baseType
                    )
                } else if (positionsToRemove.contains(pos)) {
                    CandyTile(
                        id = nextEmptyId++,
                        type = CandyType.EMPTY,
                        row = r,
                        column = c,
                        specialCandyType = SpecialCandyType.NONE
                    )
                } else {
                    tile
                }
            }
        }
        return board.copy(tiles = updatedTiles)
    }

    /**
     * Executes a single resolution cycle on [currentBoard]:
     * 1. Detects linear matches and evaluates special activations.
     * 2. Places any newly formed special candies.
     * 3. Calculates step score.
     * 4. Applies downward gravity.
     * 5. Refills empty top spaces with new candies.
     */
    fun resolveSingleStep(
        currentBoard: Match3Board,
        swapPosA: BoardPosition? = null,
        swapPosB: BoardPosition? = null,
        random: Random = Random.Default,
        allowedTypes: List<CandyType> = CandyType.playableCandies,
        alreadyActivatedIds: MutableSet<Long> = mutableSetOf()
    ): CascadeStep? {
        val matches = MatchDetector.findMatches(currentBoard)
        if (matches.isEmpty()) return null

        val matchedPositions = matches.flatMap { it.positions }.toSet()
        val matchScore = ScoreCalculator.calculateMatchesScore(matches)

        // Determine if any special candies are formed from this match (e.g. from player swap or 4+/L/T matches)
        val createdSpecials = SpecialCandyResolver.determineCreatedSpecialCandies(matches, swapPosA, swapPosB)

        // Resolve any special candies hit by the match
        val activatedSpecials = mutableListOf<CandyTile>()
        val chainResult = SpecialCandyResolver.resolveChainedSpecials(
            board = currentBoard,
            currentlyAffected = matchedPositions,
            alreadyActivatedIds = alreadyActivatedIds,
            activatedSpecials = activatedSpecials
        )

        val totalAffectedPositions = chainResult.affectedPositions
        val totalStepScore = matchScore + chainResult.bonusScore

        val boardAfterRemoval = removeMatchesAndPlaceSpecials(
            board = currentBoard,
            positionsToRemove = totalAffectedPositions,
            createdSpecials = createdSpecials
        )
        val boardAfterGravity = GravityProcessor.applyGravity(boardAfterRemoval)
        val boardAfterRefill = BoardRefiller.refillBoard(boardAfterGravity, random, allowedTypes)

        val createdPositions = createdSpecials.map { it.position }.toSet()
        val removedTiles = totalAffectedPositions
            .filter { it !in createdPositions }
            .mapNotNull { currentBoard.getTile(it) }
            .filter { it.type != CandyType.EMPTY }

        return CascadeStep(
            matches = matches,
            matchedPositions = totalAffectedPositions,
            stepScore = totalStepScore,
            boardAfterRemoval = boardAfterRemoval,
            boardAfterGravity = boardAfterGravity,
            boardAfterRefill = boardAfterRefill,
            createdSpecials = createdSpecials,
            activatedSpecials = chainResult.activatedSpecials,
            removedTiles = removedTiles
        )
    }

    /**
     * Overload for resolving a single step with only a custom Random instance.
     */
    fun resolveSingleStep(
        currentBoard: Match3Board,
        random: Random
    ): CascadeStep? = resolveSingleStep(
        currentBoard = currentBoard,
        swapPosA = null,
        swapPosB = null,
        random = random
    )

    /**
     * Resolves a direct player swap where at least one special candy is swapped directly.
     */
    fun resolveDirectSpecialSwapStep(
        currentBoard: Match3Board,
        posA: BoardPosition,
        posB: BoardPosition,
        random: Random = Random.Default,
        allowedTypes: List<CandyType> = CandyType.playableCandies,
        alreadyActivatedIds: MutableSet<Long> = mutableSetOf()
    ): CascadeStep {
        val specialResult = SpecialCandyResolver.resolveDirectSpecialSwap(
            board = currentBoard,
            posA = posA,
            posB = posB,
            alreadyActivatedIds = alreadyActivatedIds
        )
        val boardAfterRemoval = removeMatchesAndPlaceSpecials(
            board = currentBoard,
            positionsToRemove = specialResult.affectedPositions,
            createdSpecials = emptyList()
        )
        val boardAfterGravity = GravityProcessor.applyGravity(boardAfterRemoval)
        val boardAfterRefill = BoardRefiller.refillBoard(boardAfterGravity, random, allowedTypes)

        val removedTiles = specialResult.affectedPositions
            .mapNotNull { currentBoard.getTile(it) }
            .filter { it.type != CandyType.EMPTY }

        return CascadeStep(
            matches = emptyList(),
            matchedPositions = specialResult.affectedPositions,
            stepScore = specialResult.bonusScore,
            boardAfterRemoval = boardAfterRemoval,
            boardAfterGravity = boardAfterGravity,
            boardAfterRefill = boardAfterRefill,
            createdSpecials = emptyList(),
            activatedSpecials = specialResult.activatedSpecials,
            removedTiles = removedTiles
        )
    }

    /**
     * Iteratively resolves all matches and cascades on [initialBoard] until the board is completely stable.
     */
    fun resolveAllCascades(
        initialBoard: Match3Board,
        swapPosA: BoardPosition? = null,
        swapPosB: BoardPosition? = null,
        random: Random = Random.Default,
        allowedTypes: List<CandyType> = CandyType.playableCandies,
        maxIterations: Int = MAX_CASCADE_ITERATIONS
    ): CascadeResolutionResult {
        var currentBoard = initialBoard
        val steps = mutableListOf<CascadeStep>()
        var totalScore = 0
        var totalMatches = 0
        var iterations = 0
        val alreadyActivatedIds = mutableSetOf<Long>()

        // Check if the initial step is a direct special candy swap
        if (swapPosA != null && swapPosB != null && SpecialCandyResolver.isDirectSpecialSwap(initialBoard, swapPosA, swapPosB)) {
            val specialStep = resolveDirectSpecialSwapStep(
                currentBoard = currentBoard,
                posA = swapPosA,
                posB = swapPosB,
                random = random,
                allowedTypes = allowedTypes,
                alreadyActivatedIds = alreadyActivatedIds
            )
            steps.add(specialStep)
            totalScore += specialStep.stepScore
            currentBoard = specialStep.boardAfterRefill
            iterations++
        }

        while (iterations < maxIterations) {
            val isFirstStep = iterations == 0
            val stepSwapA = if (isFirstStep) swapPosA else null
            val stepSwapB = if (isFirstStep) swapPosB else null

            val step = resolveSingleStep(
                currentBoard = currentBoard,
                swapPosA = stepSwapA,
                swapPosB = stepSwapB,
                random = random,
                allowedTypes = allowedTypes,
                alreadyActivatedIds = alreadyActivatedIds
            ) ?: break

            steps.add(step)
            totalScore += step.stepScore
            totalMatches += step.matches.size
            currentBoard = step.boardAfterRefill
            iterations++
        }

        val hasRemainingMatches = MatchDetector.hasAnyMatches(currentBoard)

        return CascadeResolutionResult(
            finalBoard = currentBoard,
            steps = steps,
            totalScoreGained = totalScore,
            totalMatchesCount = totalMatches,
            isStable = !hasRemainingMatches
        )
    }

    /**
     * Overload for resolving all cascades with only a custom Random instance.
     */
    fun resolveAllCascades(
        initialBoard: Match3Board,
        random: Random
    ): CascadeResolutionResult = resolveAllCascades(
        initialBoard = initialBoard,
        swapPosA = null,
        swapPosB = null,
        random = random
    )
}

