package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.Match3Board
import com.example.game.viewmodel.Match3ViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Deterministic unit tests covering all 21 core Match-3 gameplay requirements:
 * Swapping, validation, match detection, match removal, gravity collapse,
 * refill, cascading, scoring, move tracking, and game over transitions.
 */
class CoreMatch3GameplayTest {

    private lateinit var viewModel: Match3ViewModel

    @Before
    fun setUp() {
        viewModel = Match3ViewModel()
        viewModel.stepDelayMs = 0L // Instantaneous synchronous execution for deterministic tests
    }

    private fun createCustomBoard(grid: List<List<CandyType>>): Match3Board {
        val rows = grid.size
        val cols = grid[0].size
        var nextId = 1L
        val tiles = grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, type ->
                CandyTile(
                    id = nextId++,
                    type = type,
                    row = r,
                    column = c
                )
            }
        }
        return Match3Board(rows = rows, columns = cols, tiles = tiles)
    }

    /**
     * Standard 8x8 match-free test board.
     * Swapping (0, 2) [BLUE] and (1, 2) [RED] produces a horizontal match on row 0 (RED, RED, RED).
     */
    private fun createStandard8x8Board(): Match3Board {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.RED, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.ORANGE, CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED),
            listOf(CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE, CandyType.GREEN)
        )
        return createCustomBoard(grid)
    }

    // =========================================================================
    // 1. Adjacent tiles can be swapped
    // =========================================================================
    @Test
    fun `01 - Adjacent tiles can be swapped`() {
        val posA = BoardPosition(2, 2)
        val posB = BoardPosition(2, 3)
        assertTrue(posA.isAdjacent(posB))
        assertTrue(posB.isAdjacent(posA))
    }

    // =========================================================================
    // 2. Diagonal tiles cannot be swapped
    // =========================================================================
    @Test
    fun `02 - Diagonal tiles cannot be swapped`() {
        val posA = BoardPosition(2, 2)
        val posDiag = BoardPosition(3, 3)
        assertFalse(posA.isAdjacent(posDiag))

        viewModel.startGame(level = 1, random = Random(42))
        viewModel.onTileTapped(posA)
        assertEquals(posA, viewModel.gameState.value.selectedPosition)

        // Tapping diagonal changes selection instead of swapping
        viewModel.onTileTapped(posDiag)
        assertEquals(posDiag, viewModel.gameState.value.selectedPosition)
    }

    // =========================================================================
    // 3. Valid swap creates a match
    // =========================================================================
    @Test
    fun `03 - Valid swap creates a match`() {
        val board = createStandard8x8Board()
        val posA = BoardPosition(0, 2) // BLUE
        val posB = BoardPosition(1, 2) // RED

        assertTrue(MatchDetector.doesSwapCreateMatch(board, posA, posB))
    }

    // =========================================================================
    // 4. Invalid swap is rejected
    // =========================================================================
    @Test
    fun `04 - Invalid swap is rejected`() {
        val board = createStandard8x8Board()
        viewModel.setCustomState(
            GameState(board = board, status = GameStatus.PLAYING, movesRemaining = 30, score = 0)
        )

        // (3, 3) and (3, 4) produce no match
        viewModel.onTileTapped(BoardPosition(3, 3))
        val result = viewModel.onTileTapped(BoardPosition(3, 4))

        assertFalse(result)
        assertNull(viewModel.gameState.value.selectedPosition)
    }

    // =========================================================================
    // 5. Invalid swap does not reduce moves
    // =========================================================================
    @Test
    fun `05 - Invalid swap does not reduce moves`() {
        val board = createStandard8x8Board()
        viewModel.setCustomState(
            GameState(board = board, status = GameStatus.PLAYING, movesRemaining = 30, score = 0)
        )

        viewModel.onTileTapped(BoardPosition(3, 3))
        viewModel.onTileTapped(BoardPosition(3, 4))

        assertEquals(30, viewModel.gameState.value.movesRemaining)
        assertEquals(0, viewModel.gameState.value.score)
    }

    // =========================================================================
    // 6. Valid swap reduces moves by exactly 1
    // =========================================================================
    @Test
    fun `06 - Valid swap reduces moves by exactly 1`() {
        val board = createStandard8x8Board()
        viewModel.setCustomState(
            GameState(board = board, status = GameStatus.PLAYING, movesRemaining = 30, score = 0)
        )

        viewModel.onTileTapped(BoardPosition(0, 2))
        val success = viewModel.onTileTapped(BoardPosition(1, 2), Random(42))

        assertTrue(success)
        assertEquals(29, viewModel.gameState.value.movesRemaining)
    }

    // =========================================================================
    // 7. Three matching candies are removed
    // =========================================================================
    @Test
    fun `07 - Three matching candies are removed`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val step = MatchResolver.resolveSingleStep(board, Random(42))

        assertNotNull(step)
        assertEquals(3, step!!.matchedPositions.size)
        // Check that matched positions became EMPTY after removal
        for (c in 0 until 3) {
            assertEquals(CandyType.EMPTY, step.boardAfterRemoval.getTile(0, c)?.type)
        }
    }

    // =========================================================================
    // 8. Four matching candies are removed
    // =========================================================================
    @Test
    fun `08 - Four matching candies are removed`() {
        val grid = listOf(
            listOf(CandyType.BLUE, CandyType.BLUE, CandyType.BLUE, CandyType.BLUE),
            listOf(CandyType.RED, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.YELLOW)
        )
        val board = createCustomBoard(grid)
        val step = MatchResolver.resolveSingleStep(board, Random(42))

        assertNotNull(step)
        assertEquals(4, step!!.matchedPositions.size)
    }

    // =========================================================================
    // 9. Vertical matches are removed
    // =========================================================================
    @Test
    fun `09 - Vertical matches are removed`() {
        val grid = listOf(
            listOf(CandyType.GREEN, CandyType.RED, CandyType.BLUE, CandyType.YELLOW),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.GREEN, CandyType.BLUE, CandyType.RED, CandyType.PURPLE),
            listOf(CandyType.ORANGE, CandyType.PURPLE, CandyType.YELLOW, CandyType.RED)
        )
        val board = createCustomBoard(grid)
        val step = MatchResolver.resolveSingleStep(board, Random(42))

        assertNotNull(step)
        assertTrue(step!!.matchedPositions.contains(BoardPosition(0, 0)))
        assertTrue(step.matchedPositions.contains(BoardPosition(1, 0)))
        assertTrue(step.matchedPositions.contains(BoardPosition(2, 0)))
    }

    // =========================================================================
    // 10. Horizontal matches are removed
    // =========================================================================
    @Test
    fun `10 - Horizontal matches are removed`() {
        val grid = listOf(
            listOf(CandyType.PURPLE, CandyType.PURPLE, CandyType.PURPLE, CandyType.YELLOW),
            listOf(CandyType.RED, CandyType.GREEN, CandyType.BLUE, CandyType.ORANGE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.BLUE),
            listOf(CandyType.BLUE, CandyType.ORANGE, CandyType.RED, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val step = MatchResolver.resolveSingleStep(board, Random(42))

        assertNotNull(step)
        assertTrue(step!!.matchedPositions.contains(BoardPosition(0, 0)))
        assertTrue(step.matchedPositions.contains(BoardPosition(0, 1)))
        assertTrue(step.matchedPositions.contains(BoardPosition(0, 2)))
    }

    // =========================================================================
    // 11. Multiple simultaneous matches are removed
    // =========================================================================
    @Test
    fun `11 - Multiple simultaneous matches are removed`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.GREEN),
            listOf(CandyType.BLUE, CandyType.BLUE, CandyType.BLUE, CandyType.ORANGE),
            listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.PURPLE, CandyType.YELLOW, CandyType.PURPLE, CandyType.YELLOW)
        )
        val board = createCustomBoard(grid)
        val step = MatchResolver.resolveSingleStep(board, Random(42))

        assertNotNull(step)
        // Both RED and BLUE horizontal matches resolved simultaneously
        assertEquals(6, step!!.matchedPositions.size)
        assertTrue(step.stepScore >= 60)
    }

    // =========================================================================
    // 12. Gravity moves candies downward
    // =========================================================================
    @Test
    fun `12 - Gravity moves candies downward`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.BLUE),
            listOf(CandyType.EMPTY, CandyType.GREEN),
            listOf(CandyType.YELLOW, CandyType.EMPTY),
            listOf(CandyType.PURPLE, CandyType.ORANGE)
        )
        val board = createCustomBoard(grid)
        val collapsed = GravityProcessor.applyGravity(board)

        // Column 0: Top becomes EMPTY, [RED, YELLOW, PURPLE] shifted down
        assertEquals(CandyType.EMPTY, collapsed.getTile(0, 0)?.type)
        assertEquals(CandyType.RED, collapsed.getTile(1, 0)?.type)
        assertEquals(CandyType.YELLOW, collapsed.getTile(2, 0)?.type)
        assertEquals(CandyType.PURPLE, collapsed.getTile(3, 0)?.type)

        // Column 1: Top becomes EMPTY, [BLUE, GREEN, ORANGE] shifted down
        assertEquals(CandyType.EMPTY, collapsed.getTile(0, 1)?.type)
        assertEquals(CandyType.BLUE, collapsed.getTile(1, 1)?.type)
        assertEquals(CandyType.GREEN, collapsed.getTile(2, 1)?.type)
        assertEquals(CandyType.ORANGE, collapsed.getTile(3, 1)?.type)
    }

    // =========================================================================
    // 13. EMPTY positions are refilled
    // =========================================================================
    @Test
    fun `13 - EMPTY positions are refilled`() {
        val grid = listOf(
            listOf(CandyType.EMPTY, CandyType.EMPTY),
            listOf(CandyType.RED, CandyType.BLUE)
        )
        val board = createCustomBoard(grid)
        val refilled = BoardRefiller.refillBoard(board, Random(42))

        assertTrue(refilled.getTile(0, 0)!!.isPlayable)
        assertTrue(refilled.getTile(0, 1)!!.isPlayable)
        assertEquals(CandyType.RED, refilled.getTile(1, 0)?.type)
        assertEquals(CandyType.BLUE, refilled.getTile(1, 1)?.type)
    }

    // =========================================================================
    // 14. Board contains no EMPTY tiles after refill
    // =========================================================================
    @Test
    fun `14 - Board contains no EMPTY tiles after refill`() {
        val emptyBoard = Match3Board.createEmpty(8, 8)
        val refilled = BoardRefiller.refillBoard(emptyBoard, Random(42))

        assertEquals(0, refilled.allTiles.count { it.type == CandyType.EMPTY })
        assertEquals(64, refilled.allTiles.count { it.isPlayable })
    }

    // =========================================================================
    // 15. Score increases after a valid match
    // =========================================================================
    @Test
    fun `15 - Score increases after a valid match`() {
        val board = createStandard8x8Board()
        viewModel.setCustomState(
            GameState(board = board, status = GameStatus.PLAYING, movesRemaining = 30, score = 0)
        )

        viewModel.onTileTapped(BoardPosition(0, 2))
        viewModel.onTileTapped(BoardPosition(1, 2), Random(42))

        assertTrue(viewModel.gameState.value.score >= 30)
    }

    // =========================================================================
    // 16. Cascade detection works
    // =========================================================================
    @Test
    fun `16 - Cascade detection works`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val cascadeResult = MatchResolver.resolveAllCascades(board, Random(42))

        assertTrue(cascadeResult.steps.isNotEmpty())
        assertTrue(cascadeResult.totalScoreGained >= 30)
    }

    // =========================================================================
    // 17. Cascade continues until board is stable
    // =========================================================================
    @Test
    fun `17 - Cascade continues until board is stable`() {
        val board = createStandard8x8Board()
        val cascadeResult = MatchResolver.resolveAllCascades(board, Random(42))

        assertTrue(cascadeResult.isStable)
        assertFalse(MatchDetector.hasAnyMatches(cascadeResult.finalBoard))
    }

    // =========================================================================
    // 18. Cascades do not consume extra moves
    // =========================================================================
    @Test
    fun `18 - Cascades do not consume extra moves`() {
        val board = createStandard8x8Board()
        viewModel.setCustomState(
            GameState(board = board, status = GameStatus.PLAYING, movesRemaining = 30, score = 0)
        )

        viewModel.onTileTapped(BoardPosition(0, 2))
        viewModel.onTileTapped(BoardPosition(1, 2), Random(42))

        // Multiple cascades may happen, but moves remaining is exactly 29
        assertEquals(29, viewModel.gameState.value.movesRemaining)
    }

    // =========================================================================
    // 19. Processing state prevents additional swaps
    // =========================================================================
    @Test
    fun `19 - Processing state prevents additional swaps`() {
        val board = createStandard8x8Board()
        viewModel.setCustomState(
            GameState(board = board, status = GameStatus.PLAYING, isProcessing = true, movesRemaining = 30, score = 0)
        )

        val result = viewModel.onTileTapped(BoardPosition(0, 2))
        assertFalse(result)
        assertNull(viewModel.gameState.value.selectedPosition)
        assertEquals(30, viewModel.gameState.value.movesRemaining)
    }

    // =========================================================================
    // 20. Moves reaching zero eventually produce GAME_OVER
    // =========================================================================
    @Test
    fun `20 - Moves reaching zero eventually produce GAME_OVER`() {
        val board = createStandard8x8Board()
        viewModel.setCustomState(
            GameState(board = board, status = GameStatus.PLAYING, movesRemaining = 1, score = 0)
        )

        viewModel.onTileTapped(BoardPosition(0, 2))
        viewModel.onTileTapped(BoardPosition(1, 2), Random(42))

        assertEquals(0, viewModel.gameState.value.movesRemaining)
        assertTrue(viewModel.gameState.value.isGameOver)
        assertEquals(GameStatus.GAME_OVER, viewModel.gameState.value.status)
    }

    // =========================================================================
    // 21. Invalid swaps restore the original board
    // =========================================================================
    @Test
    fun `21 - Invalid swaps restore the original board`() {
        val board = createStandard8x8Board()
        val originalTileA = board.getTile(3, 3)!!.type
        val originalTileB = board.getTile(3, 4)!!.type

        viewModel.setCustomState(
            GameState(board = board, status = GameStatus.PLAYING, movesRemaining = 30, score = 0)
        )

        viewModel.onTileTapped(BoardPosition(3, 3))
        viewModel.onTileTapped(BoardPosition(3, 4))

        assertEquals(30, viewModel.gameState.value.movesRemaining)
        assertEquals(0, viewModel.gameState.value.score)
        assertEquals(originalTileA, viewModel.gameState.value.board.getTile(3, 3)?.type)
        assertEquals(originalTileB, viewModel.gameState.value.board.getTile(3, 4)?.type)
        assertEquals(GameStatus.PLAYING, viewModel.gameState.value.status)
        assertFalse(viewModel.gameState.value.isProcessing)
    }
}
