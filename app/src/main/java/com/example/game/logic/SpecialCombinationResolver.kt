package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import com.example.game.model.SpecialCombinationType
import com.example.game.utils.ScoreCalculator
import kotlin.math.max
import kotlin.math.min

/**
 * Result of a special candy combination resolution.
 *
 * @property comboType The specific combination triggered.
 * @property affectedPositions All board positions cleared by this combination and any chained specials.
 * @property activatedSpecials List of special candy tiles that were triggered during this resolution.
 * @property score Points awarded for this combination and any chained special activations.
 */
data class CombinationResolutionResult(
    val comboType: SpecialCombinationType,
    val affectedPositions: Set<BoardPosition>,
    val activatedSpecials: List<CandyTile>,
    val score: Int
)

/**
 * Pure, deterministic engine responsible for:
 * 1. Detecting special candy combinations when two adjacent tiles are swapped.
 * 2. Calculating combined blast areas for all supported combination types.
 * 3. Chaining any secondary specials hit by the combination.
 * 4. Calculating accurate bonus scores.
 */
object SpecialCombinationResolver {

    /**
     * Identifies the [SpecialCombinationType] resulting from swapping [tileA] and [tileB].
     */
    fun detectCombination(tileA: CandyTile, tileB: CandyTile): SpecialCombinationType {
        if (!tileA.isPlayable || !tileB.isPlayable) return SpecialCombinationType.NONE

        val typeA = tileA.specialCandyType
        val typeB = tileB.specialCandyType

        // 1. Color Bomb + Color Bomb
        if (typeA == SpecialCandyType.COLOR_BOMB && typeB == SpecialCandyType.COLOR_BOMB) {
            return SpecialCombinationType.COLOR_BOMB_COLOR_BOMB
        }

        // 2. Color Bomb + Other
        if (typeA == SpecialCandyType.COLOR_BOMB || typeB == SpecialCandyType.COLOR_BOMB) {
            val otherTile = if (typeA == SpecialCandyType.COLOR_BOMB) tileB else tileA
            return when {
                otherTile.specialCandyType == SpecialCandyType.WRAPPED -> SpecialCombinationType.COLOR_BOMB_WRAPPED
                otherTile.specialCandyType.isStriped -> SpecialCombinationType.COLOR_BOMB_STRIPED
                otherTile.type.isPlayable && otherTile.type != CandyType.EMPTY -> SpecialCombinationType.COLOR_BOMB_NORMAL
                else -> SpecialCombinationType.NONE
            }
        }

        // 3. Wrapped + Wrapped
        if (typeA == SpecialCandyType.WRAPPED && typeB == SpecialCandyType.WRAPPED) {
            return SpecialCombinationType.WRAPPED_WRAPPED
        }

        // 4. Striped + Wrapped (either order)
        if ((typeA.isStriped && typeB == SpecialCandyType.WRAPPED) ||
            (typeB.isStriped && typeA == SpecialCandyType.WRAPPED)
        ) {
            return SpecialCombinationType.STRIPED_WRAPPED
        }

        // 5. Striped + Striped
        if (typeA.isStriped && typeB.isStriped) {
            return SpecialCombinationType.STRIPED_STRIPED
        }

        return SpecialCombinationType.NONE
    }

    /**
     * Checks if swapping [posA] and [posB] constitutes a valid special combination.
     */
    fun canCombine(board: Match3Board, posA: BoardPosition, posB: BoardPosition): Boolean {
        if (!posA.isAdjacent(posB)) return false
        val tileA = board.getTile(posA) ?: return false
        val tileB = board.getTile(posB) ?: return false
        return detectCombination(tileA, tileB) != SpecialCombinationType.NONE
    }

    /**
     * Resolves a special combination swap between [posA] and [posB].
     *
     * @param board The current game board.
     * @param posA The first swapped position.
     * @param posB The second swapped position.
     * @param alreadyActivatedIds Mutable set of tile IDs that have already been activated.
     * @return [CombinationResolutionResult] containing all affected positions, activated specials, and score.
     */
    fun resolveCombination(
        board: Match3Board,
        posA: BoardPosition,
        posB: BoardPosition,
        alreadyActivatedIds: MutableSet<Long> = mutableSetOf()
    ): CombinationResolutionResult {
        val tileA = board.getTile(posA) ?: return emptyResult()
        val tileB = board.getTile(posB) ?: return emptyResult()

        val comboType = detectCombination(tileA, tileB)
        if (comboType == SpecialCombinationType.NONE) {
            return emptyResult()
        }

        val initialAffected = mutableSetOf<BoardPosition>()
        val activatedSpecials = mutableListOf<CandyTile>()

        // Mark primary combo participants as activated
        if (tileA.isSpecial) {
            alreadyActivatedIds.add(tileA.id)
            activatedSpecials.add(tileA)
        }
        if (tileB.isSpecial) {
            alreadyActivatedIds.add(tileB.id)
            activatedSpecials.add(tileB)
        }

        initialAffected.add(posA)
        initialAffected.add(posB)

        val comboScore = ScoreCalculator.calculateCombinationScore(comboType)

        when (comboType) {
            SpecialCombinationType.STRIPED_STRIPED -> {
                // Clear row and column of posA, and row and column of posB
                initialAffected.addAll(getCrossPositions(board, posA))
                initialAffected.addAll(getCrossPositions(board, posB))
            }

            SpecialCombinationType.WRAPPED_WRAPPED -> {
                // Dual explosion: 3x3 + 5x5 around the combination center
                val minR = min(posA.row, posB.row)
                val maxR = max(posA.row, posB.row)
                val minC = min(posA.column, posB.column)
                val maxC = max(posA.column, posB.column)

                // 5x5 area expanded around both positions (distance <= 2 from either pos)
                for (r in (minR - 2)..(maxR + 2)) {
                    for (c in (minC - 2)..(maxC + 2)) {
                        val pos = BoardPosition(r, c)
                        if (BoardValidator.isValidPosition(pos, board.rows, board.columns)) {
                            initialAffected.add(pos)
                        }
                    }
                }
            }

            SpecialCombinationType.STRIPED_WRAPPED -> {
                // Giant 3-row + 3-column cross effect centered around the combination
                val centerRow = posA.row
                val centerCol = posA.column
                val otherRow = posB.row
                val otherCol = posB.column

                val startRow = min(centerRow, otherRow) - 1
                val endRow = max(centerRow, otherRow) + 1
                val startCol = min(centerCol, otherCol) - 1
                val endCol = max(centerCol, otherCol) + 1

                for (r in startRow..endRow) {
                    if (r in 0 until board.rows) {
                        for (c in 0 until board.columns) {
                            initialAffected.add(BoardPosition(r, c))
                        }
                    }
                }

                for (c in startCol..endCol) {
                    if (c in 0 until board.columns) {
                        for (r in 0 until board.rows) {
                            initialAffected.add(BoardPosition(r, c))
                        }
                    }
                }
            }

            SpecialCombinationType.COLOR_BOMB_NORMAL -> {
                val normalTile = if (tileA.specialCandyType == SpecialCandyType.COLOR_BOMB) tileB else tileA
                val targetColor = normalTile.type
                for (tile in board.allTiles) {
                    if (tile.type == targetColor && tile.isPlayable) {
                        initialAffected.add(tile.position)
                    }
                }
            }

            SpecialCombinationType.COLOR_BOMB_STRIPED -> {
                val stripedTile = if (tileA.specialCandyType == SpecialCandyType.COLOR_BOMB) tileB else tileA
                val targetColor = stripedTile.type
                val matchingTiles = board.allTiles.filter { it.type == targetColor && it.isPlayable }

                for (tile in matchingTiles) {
                    initialAffected.add(tile.position)
                    if (!alreadyActivatedIds.contains(tile.id)) {
                        alreadyActivatedIds.add(tile.id)
                        activatedSpecials.add(tile)
                    }
                    // Deterministically trigger horizontal or vertical blast based on coordinate
                    if ((tile.row + tile.column) % 2 == 0) {
                        for (c in 0 until board.columns) {
                            initialAffected.add(BoardPosition(tile.row, c))
                        }
                    } else {
                        for (r in 0 until board.rows) {
                            initialAffected.add(BoardPosition(r, tile.column))
                        }
                    }
                }
            }

            SpecialCombinationType.COLOR_BOMB_WRAPPED -> {
                val wrappedTile = if (tileA.specialCandyType == SpecialCandyType.COLOR_BOMB) tileB else tileA
                val targetColor = wrappedTile.type
                val matchingTiles = board.allTiles.filter { it.type == targetColor && it.isPlayable }

                for (tile in matchingTiles) {
                    initialAffected.add(tile.position)
                    if (!alreadyActivatedIds.contains(tile.id)) {
                        alreadyActivatedIds.add(tile.id)
                        activatedSpecials.add(tile)
                    }
                    // 3x3 blast around each converted candy
                    for (r in (tile.row - 1)..(tile.row + 1)) {
                        for (c in (tile.column - 1)..(tile.column + 1)) {
                            val pos = BoardPosition(r, c)
                            if (BoardValidator.isValidPosition(pos, board.rows, board.columns)) {
                                initialAffected.add(pos)
                            }
                        }
                    }
                }
            }

            SpecialCombinationType.COLOR_BOMB_COLOR_BOMB -> {
                // Full board clear: remove all playable tiles
                for (r in 0 until board.rows) {
                    for (c in 0 until board.columns) {
                        val tile = board.getTile(r, c)
                        if (tile != null && tile.isPlayable) {
                            initialAffected.add(BoardPosition(r, c))
                        }
                    }
                }
            }

            SpecialCombinationType.NONE -> { /* No-op */ }
        }

        // Chain any secondary special candies caught in the blast area
        val chainResult = SpecialCandyResolver.resolveChainedSpecials(
            board = board,
            currentlyAffected = initialAffected,
            alreadyActivatedIds = alreadyActivatedIds,
            activatedSpecials = activatedSpecials
        )

        return CombinationResolutionResult(
            comboType = comboType,
            affectedPositions = chainResult.affectedPositions,
            activatedSpecials = chainResult.activatedSpecials,
            score = comboScore + chainResult.bonusScore
        )
    }

    /**
     * Helper to return all positions in the full row and full column of [pos].
     */
    private fun getCrossPositions(board: Match3Board, pos: BoardPosition): Set<BoardPosition> {
        val positions = mutableSetOf<BoardPosition>()
        for (c in 0 until board.columns) {
            positions.add(BoardPosition(pos.row, c))
        }
        for (r in 0 until board.rows) {
            positions.add(BoardPosition(r, pos.column))
        }
        return positions
    }

    private fun emptyResult() = CombinationResolutionResult(
        comboType = SpecialCombinationType.NONE,
        affectedPositions = emptySet(),
        activatedSpecials = emptyList(),
        score = 0
    )
}
