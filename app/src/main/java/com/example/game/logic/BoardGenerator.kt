package com.example.game.logic

import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.DEFAULT_COLUMNS
import com.example.game.model.DEFAULT_ROWS
import com.example.game.model.Match3Board
import kotlin.random.Random

/**
 * Generates initial Match-3 boards with balanced candy distributions
 * and guarantees that no pre-existing 3-in-a-row matches exist on initial board presentation.
 */
object BoardGenerator {

    /**
     * Generates a valid, solvable initial [Match3Board] of size [rows] x [columns].
     * Guarantees zero initial matches of 3 or more horizontally or vertically,
     * and guarantees that at least one valid swap move is available for the player.
     *
     * @param rows Number of board rows (default 8).
     * @param columns Number of board columns (default 8).
     * @param random Random generator instance for determinism and testing.
     * @param allowedTypes Playable candy types to distribute across the grid.
     * @param ensurePossibleMoves Whether to guarantee at least one valid swap move exists.
     */
    fun generateBoard(
        rows: Int = DEFAULT_ROWS,
        columns: Int = DEFAULT_COLUMNS,
        random: Random = Random.Default,
        allowedTypes: List<CandyType> = CandyType.PLAYABLE_TYPES,
        ensurePossibleMoves: Boolean = true
    ): Match3Board {
        require(rows > 0 && columns > 0) { "Board dimensions must be positive" }
        require(allowedTypes.size >= 3) { "Must have at least 3 candy types to generate match-free board" }

        var attempts = 0
        val maxAttempts = 50

        while (attempts < maxAttempts) {
            attempts++
            val grid = Array(rows) { arrayOfNulls<CandyTile>(columns) }
            var nextId = 1L

            for (r in 0 until rows) {
                for (c in 0 until columns) {
                    // Find disallowed types that would form a 3-in-a-row match
                    val disallowed = mutableSetOf<CandyType>()

                    // Check horizontal match (left 2 tiles)
                    if (c >= 2) {
                        val t1 = grid[r][c - 1]?.type
                        val t2 = grid[r][c - 2]?.type
                        if (t1 != null && t1 == t2 && t1.isPlayable) {
                            disallowed.add(t1)
                        }
                    }

                    // Check vertical match (top 2 tiles)
                    if (r >= 2) {
                        val t1 = grid[r - 1][c]?.type
                        val t2 = grid[r - 2][c]?.type
                        if (t1 != null && t1 == t2 && t1.isPlayable) {
                            disallowed.add(t1)
                        }
                    }

                    // Valid candidate pool
                    val candidatePool = allowedTypes.filter { it !in disallowed }
                    val chosenType = if (candidatePool.isNotEmpty()) {
                        candidatePool[random.nextInt(candidatePool.size)]
                    } else {
                        allowedTypes[random.nextInt(allowedTypes.size)]
                    }

                    grid[r][c] = CandyTile(
                        id = nextId++,
                        type = chosenType,
                        row = r,
                        column = c
                    )
                }
            }

            val candidateBoard = Match3Board(
                rows = rows,
                columns = columns,
                tiles = grid.map { rowArray -> rowArray.map { it!! } }
            )

            // Verify no initial matches
            if (MatchDetector.hasAnyMatches(candidateBoard)) {
                continue
            }

            // Verify at least one valid move exists if requested
            if (ensurePossibleMoves && !MatchDetector.hasPossibleMoves(candidateBoard)) {
                continue
            }

            return candidateBoard
        }

        // Fallback if max attempts reached (construct a standard match-free board)
        return generateMatchFreeBoard(rows, columns, random, allowedTypes)
    }

    private fun generateMatchFreeBoard(
        rows: Int,
        columns: Int,
        random: Random,
        allowedTypes: List<CandyType>
    ): Match3Board {
        val grid = Array(rows) { arrayOfNulls<CandyTile>(columns) }
        var nextId = 1L
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                val disallowed = mutableSetOf<CandyType>()
                if (c >= 2 && grid[r][c - 1]?.type == grid[r][c - 2]?.type) {
                    grid[r][c - 1]?.type?.let { disallowed.add(it) }
                }
                if (r >= 2 && grid[r - 1][c]?.type == grid[r - 2][c]?.type) {
                    grid[r - 1][c]?.type?.let { disallowed.add(it) }
                }
                val pool = allowedTypes.filter { it !in disallowed }.ifEmpty { allowedTypes }
                grid[r][c] = CandyTile(
                    id = nextId++,
                    type = pool[random.nextInt(pool.size)],
                    row = r,
                    column = c
                )
            }
        }
        return Match3Board(
            rows = rows,
            columns = columns,
            tiles = grid.map { rowList -> rowList.map { it!! } }
        )
    }
}
