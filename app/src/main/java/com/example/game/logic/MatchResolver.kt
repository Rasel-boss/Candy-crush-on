package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.Match3Board
import com.example.game.utils.ScoreCalculator

data class CascadeStep(
    val stepIndex: Int,
    val matchedPositions: Set<BoardPosition>,
    val specialDetonations: Set<BoardPosition>,
    val createdSpecials: List<CreatedSpecialCandy>,
    val removedTiles: List<CandyTile>,
    val gravityResult: GravityResult,
    val refillResult: RefillResult,
    val stepScore: Int,
    val finalBoard: Match3Board
)

data class FullResolutionResult(
    val steps: List<CascadeStep>,
    val totalScore: Int,
    val finalBoard: Match3Board
)

class MatchResolver(
    private val matchDetector: MatchDetector = MatchDetector(),
    private val specialCreator: SpecialCandyCreator = SpecialCandyCreator(),
    private val specialResolver: SpecialCandyResolver = SpecialCandyResolver(),
    private val gravityProcessor: GravityProcessor = GravityProcessor(),
    private val boardRefiller: BoardRefiller = BoardRefiller(),
    private val scoreCalculator: ScoreCalculator = ScoreCalculator()
) {

    fun resolveBoard(
        initialBoard: Match3Board,
        triggerPosition: BoardPosition? = null
    ): FullResolutionResult {
        val steps = mutableListOf<CascadeStep>()
        var currentBoard = initialBoard
        var totalScore = 0
        var cascadeIndex = 0

        while (true) {
            val matches = matchDetector.findMatches(currentBoard)
            if (matches.isEmpty()) break

            val matchedPositions = mutableSetOf<BoardPosition>()
            val createdSpecials = mutableListOf<CreatedSpecialCandy>()

            for (match in matches) {
                matchedPositions.addAll(match.positions)
                val special = specialCreator.checkAndCreateSpecial(match, triggerPosition)
                if (special != null) {
                    createdSpecials.add(special)
                }
            }

            // Resolve any special detonations triggered by match
            val detonationResult = specialResolver.resolveDetonations(currentBoard, matchedPositions)
            val allRemovedPositions = matchedPositions + detonationResult.affectedPositions

            val removedTiles = mutableListOf<CandyTile>()
            for (pos in allRemovedPositions) {
                val tile = currentBoard[pos]
                if (tile != null) {
                    removedTiles.add(tile)
                }
            }

            // Calculate step score
            val stepScore = scoreCalculator.calculateMatchScore(
                matchCount = matches.size,
                totalCandiesCleared = allRemovedPositions.size,
                cascadeIndex = cascadeIndex,
                specialsActivated = detonationResult.secondaryActivations.size
            )
            totalScore += stepScore

            // Clear removed positions
            var boardAfterRemoval = currentBoard
            for (pos in allRemovedPositions) {
                boardAfterRemoval = boardAfterRemoval.set(pos, null)
            }

            // Place created specials
            for (spec in createdSpecials) {
                boardAfterRemoval = boardAfterRemoval.set(spec.position, spec.tile)
            }

            // Apply gravity
            val gravityResult = gravityProcessor.applyGravity(boardAfterRemoval)

            // Refill board
            val refillResult = boardRefiller.refillBoard(gravityResult.updatedBoard)

            val step = CascadeStep(
                stepIndex = cascadeIndex,
                matchedPositions = matchedPositions,
                specialDetonations = detonationResult.affectedPositions,
                createdSpecials = createdSpecials,
                removedTiles = removedTiles,
                gravityResult = gravityResult,
                refillResult = refillResult,
                stepScore = stepScore,
                finalBoard = refillResult.updatedBoard
            )

            steps.add(step)
            currentBoard = refillResult.updatedBoard
            cascadeIndex++
        }

        return FullResolutionResult(steps, totalScore, currentBoard)
    }
}
