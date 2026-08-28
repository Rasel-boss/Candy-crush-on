package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType

/**
 * Represents a single continuous linear match of 3 or more candies of the same type.
 *
 * @property type The candy type that matched.
 * @property positions All board positions participating in this linear match.
 * @property isHorizontal True if horizontal, false if vertical.
 */
data class SingleMatch(
    val type: CandyType,
    val positions: List<BoardPosition>,
    val isHorizontal: Boolean
) {
    val length: Int
        get() = positions.size
}

/**
 * Result bundle containing distinct matched positions and the individual linear matches.
 */
data class MatchResult(
    val matchedPositions: Set<BoardPosition>,
    val matches: List<SingleMatch>
) {
    val hasMatches: Boolean
        get() = matchedPositions.isNotEmpty()
}

/**
 * High-performance, deterministic match detection engine for Match-3 boards.
 * Finds horizontal and vertical runs of 3, 4, 5+ consecutive identical playable candies,
 * and validates whether adjacent tile swaps produce newly formed matches.
 */
object MatchDetector {

    /**
     * Finds all horizontal and vertical matches of 3 or more identical playable candies on the [board].
     */
    fun findMatches(board: Match3Board): List<SingleMatch> {
        val matches = mutableListOf<SingleMatch>()

        // 1. Horizontal match scanning (row by row)
        for (r in 0 until board.rows) {
            var matchStartCol = 0
            var currentType: CandyType = CandyType.EMPTY

            for (c in 0 until board.columns) {
                val tileType = board.getTile(r, c)?.type ?: CandyType.EMPTY

                if (tileType.isPlayable && tileType == currentType) {
                    // Continue current run
                } else {
                    // Check if previous run forms a valid match (>= 3)
                    val runLength = c - matchStartCol
                    if (currentType.isPlayable && runLength >= 3) {
                        val positions = (matchStartCol until c).map { col -> BoardPosition(r, col) }
                        matches.add(
                            SingleMatch(
                                type = currentType,
                                positions = positions,
                                isHorizontal = true
                            )
                        )
                    }
                    // Start new run
                    matchStartCol = c
                    currentType = tileType
                }
            }
            // Check trailing run at the end of the row
            val trailingLength = board.columns - matchStartCol
            if (currentType.isPlayable && trailingLength >= 3) {
                val positions = (matchStartCol until board.columns).map { col -> BoardPosition(r, col) }
                matches.add(
                    SingleMatch(
                        type = currentType,
                        positions = positions,
                        isHorizontal = true
                    )
                )
            }
        }

        // 2. Vertical match scanning (column by column)
        for (c in 0 until board.columns) {
            var matchStartRow = 0
            var currentType: CandyType = CandyType.EMPTY

            for (r in 0 until board.rows) {
                val tileType = board.getTile(r, c)?.type ?: CandyType.EMPTY

                if (tileType.isPlayable && tileType == currentType) {
                    // Continue current run
                } else {
                    // Check if previous run forms a valid match (>= 3)
                    val runLength = r - matchStartRow
                    if (currentType.isPlayable && runLength >= 3) {
                        val positions = (matchStartRow until r).map { row -> BoardPosition(row, c) }
                        matches.add(
                            SingleMatch(
                                type = currentType,
                                positions = positions,
                                isHorizontal = false
                            )
                        )
                    }
                    // Start new run
                    matchStartRow = r
                    currentType = tileType
                }
            }
            // Check trailing run at the end of the column
            val trailingLength = board.rows - matchStartRow
            if (currentType.isPlayable && trailingLength >= 3) {
                val positions = (matchStartRow until board.rows).map { row -> BoardPosition(row, c) }
                matches.add(
                    SingleMatch(
                        type = currentType,
                        positions = positions,
                        isHorizontal = false
                    )
                )
            }
        }

        return matches
    }

    /**
     * Returns the set of all unique [BoardPosition] coordinates participating in at least one match.
     */
    fun findAllMatchedPositions(board: Match3Board): Set<BoardPosition> {
        val matches = findMatches(board)
        return matches.flatMap { it.positions }.toSet()
    }

    /**
     * Groups linear matches into rich [MatchGroup] structures identifying shapes like
     * HORIZONTAL, VERTICAL, L_SHAPE, T_SHAPE, FIVE_IN_A_ROW, and their special candy candidate.
     */
    fun detectMatchGroups(board: Match3Board): List<MatchGroup> {
        val matches = findMatches(board)
        if (matches.isEmpty()) return emptyList()

        val groups = mutableListOf<MatchGroup>()
        val processedMatchIndices = mutableSetOf<Int>()

        val horizontalMatches = matches.mapIndexedNotNull { index, m -> if (m.isHorizontal) index to m else null }
        val verticalMatches = matches.mapIndexedNotNull { index, m -> if (!m.isHorizontal) index to m else null }

        // 1. Check for L or T shape intersections
        for ((hIdx, hMatch) in horizontalMatches) {
            for ((vIdx, vMatch) in verticalMatches) {
                if (hMatch.type == vMatch.type && hMatch.type.isPlayable) {
                    val intersection = hMatch.positions.intersect(vMatch.positions.toSet())
                    if (intersection.isNotEmpty()) {
                        val combinedPositions = (hMatch.positions + vMatch.positions).toSet()
                        if (combinedPositions.size >= 5) {
                            val intersectionPos = intersection.first()
                            val isT = (intersectionPos != hMatch.positions.first() && intersectionPos != hMatch.positions.last()) ||
                                    (intersectionPos != vMatch.positions.first() && intersectionPos != vMatch.positions.last())
                            val formation = if (isT) MatchFormationType.T_SHAPE else MatchFormationType.L_SHAPE

                            groups.add(
                                MatchGroup(
                                    type = hMatch.type,
                                    positions = combinedPositions,
                                    formationType = formation,
                                    specialCandyCandidate = SpecialCandyType.WRAPPED
                                )
                            )
                            processedMatchIndices.add(hIdx)
                            processedMatchIndices.add(vIdx)
                        }
                    }
                }
            }
        }

        // 2. Process remaining linear matches
        for (i in matches.indices) {
            if (processedMatchIndices.contains(i)) continue

            val match = matches[i]
            val positions = match.positions.toSet()

            val formation: MatchFormationType
            val candidate: SpecialCandyType

            when {
                positions.size >= 5 -> {
                    formation = MatchFormationType.FIVE_IN_A_ROW
                    candidate = SpecialCandyType.COLOR_BOMB
                }
                positions.size == 4 -> {
                    if (match.isHorizontal) {
                        formation = MatchFormationType.HORIZONTAL
                        candidate = SpecialCandyType.HORIZONTAL_STRIPED
                    } else {
                        formation = MatchFormationType.VERTICAL
                        candidate = SpecialCandyType.VERTICAL_STRIPED
                    }
                }
                else -> {
                    formation = if (match.isHorizontal) MatchFormationType.HORIZONTAL else MatchFormationType.VERTICAL
                    candidate = SpecialCandyType.NONE
                }
            }

            groups.add(
                MatchGroup(
                    type = match.type,
                    positions = positions,
                    formationType = formation,
                    specialCandyCandidate = candidate
                )
            )
        }

        return groups
    }

    /**
     * Evaluates complete match results with both list of matches and unique position set.
     */
    fun detectMatches(board: Match3Board): MatchResult {
        val matches = findMatches(board)
        val positions = matches.flatMap { it.positions }.toSet()
        return MatchResult(matchedPositions = positions, matches = matches)
    }

    /**
     * Returns true if there is at least one horizontal or vertical match on the board.
     */
    fun hasAnyMatches(board: Match3Board): Boolean {
        // Fast horizontal check
        for (r in 0 until board.rows) {
            for (c in 0 until board.columns - 2) {
                val t1 = board.getTile(r, c)?.type ?: continue
                if (!t1.isPlayable) continue
                val t2 = board.getTile(r, c + 1)?.type ?: continue
                val t3 = board.getTile(r, c + 2)?.type ?: continue
                if (t1 == t2 && t2 == t3) return true
            }
        }
        // Fast vertical check
        for (r in 0 until board.rows - 2) {
            for (c in 0 until board.columns) {
                val t1 = board.getTile(r, c)?.type ?: continue
                if (!t1.isPlayable) continue
                val t2 = board.getTile(r + 1, c)?.type ?: continue
                val t3 = board.getTile(r + 2, c)?.type ?: continue
                if (t1 == t2 && t2 == t3) return true
            }
        }
        return false
    }

    /**
     * Finds all matches created as a direct result of swapping [posA] and [posB].
     * Only returns matches that contain either [posA] or [posB].
     */
    fun findMatchesAfterSwap(
        board: Match3Board,
        posA: BoardPosition,
        posB: BoardPosition
    ): List<SingleMatch> {
        if (!posA.isAdjacent(posB)) return emptyList()
        if (!BoardValidator.isValidPosition(posA, board.rows, board.columns) ||
            !BoardValidator.isValidPosition(posB, board.rows, board.columns)
        ) {
            return emptyList()
        }

        val tileA = board.getTile(posA) ?: return emptyList()
        val tileB = board.getTile(posB) ?: return emptyList()
        if (!tileA.isPlayable || !tileB.isPlayable) return emptyList()

        val swappedBoard = board.swapTiles(posA, posB)
        val allMatches = findMatches(swappedBoard)

        // Filter only matches that involve either swapped position
        return allMatches.filter { match ->
            match.positions.contains(posA) || match.positions.contains(posB)
        }
    }

    /**
     * Checks if swapping tiles at [posA] and [posB] directly creates a valid match of 3 or more
     * that involves either of the swapped positions.
     */
    fun doesSwapCreateMatch(
        board: Match3Board,
        posA: BoardPosition,
        posB: BoardPosition
    ): Boolean {
        return findMatchesAfterSwap(board, posA, posB).isNotEmpty()
    }

    /**
     * Alias for [doesSwapCreateMatch]. Checks if swapping [posA] and [posB] creates a valid match.
     */
    fun wouldSwapCreateMatch(
        board: Match3Board,
        posA: BoardPosition,
        posB: BoardPosition
    ): Boolean {
        return doesSwapCreateMatch(board, posA, posB)
    }

    /**
     * Pure, non-mutating check whether swapping adjacent tiles at [posA] and [posB] produces
     * a valid move (either via standard 3-in-a-row match or special candy activation/combination).
     */
    fun isPotentialValidSwap(
        board: Match3Board,
        posA: BoardPosition,
        posB: BoardPosition
    ): Boolean {
        if (!posA.isAdjacent(posB)) return false
        if (!BoardValidator.isValidPosition(posA, board.rows, board.columns) ||
            !BoardValidator.isValidPosition(posB, board.rows, board.columns)
        ) {
            return false
        }
        val tileA = board.getTile(posA) ?: return false
        val tileB = board.getTile(posB) ?: return false
        if (!tileA.isPlayable || !tileB.isPlayable) return false

        // Check regular match
        if (doesSwapCreateMatch(board, posA, posB)) {
            return true
        }

        // Check special combination or direct special swap
        if (SpecialCombinationResolver.canCombine(board, posA, posB) ||
            SpecialCandyResolver.isDirectSpecialSwap(board, posA, posB)
        ) {
            return true
        }

        return false
    }

    /**
     * Pure, non-mutating inspection of all adjacent horizontal and vertical pairs on [board].
     * Returns true if at least one valid swap exists that produces a match or special effect.
     * Returns false if no valid moves exist (dead board).
     */
    fun hasPossibleMoves(board: Match3Board): Boolean {
        // 1. Inspect horizontal adjacent pairs
        for (r in 0 until board.rows) {
            for (c in 0 until board.columns - 1) {
                val posA = BoardPosition(r, c)
                val posB = BoardPosition(r, c + 1)
                if (isPotentialValidSwap(board, posA, posB)) {
                    return true
                }
            }
        }

        // 2. Inspect vertical adjacent pairs
        for (r in 0 until board.rows - 1) {
            for (c in 0 until board.columns) {
                val posA = BoardPosition(r, c)
                val posB = BoardPosition(r + 1, c)
                if (isPotentialValidSwap(board, posA, posB)) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Returns all unique pairs of adjacent positions that form valid moves on [board].
     */
    fun findPossibleMoves(board: Match3Board): List<Pair<BoardPosition, BoardPosition>> {
        val validMoves = mutableListOf<Pair<BoardPosition, BoardPosition>>()

        for (r in 0 until board.rows) {
            for (c in 0 until board.columns - 1) {
                val posA = BoardPosition(r, c)
                val posB = BoardPosition(r, c + 1)
                if (isPotentialValidSwap(board, posA, posB)) {
                    validMoves.add(posA to posB)
                }
            }
        }

        for (r in 0 until board.rows - 1) {
            for (c in 0 until board.columns) {
                val posA = BoardPosition(r, c)
                val posB = BoardPosition(r + 1, c)
                if (isPotentialValidSwap(board, posA, posB)) {
                    validMoves.add(posA to posB)
                }
            }
        }

        return validMoves
    }
}
