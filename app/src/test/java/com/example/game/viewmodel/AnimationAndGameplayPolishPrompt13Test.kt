package com.example.game.viewmodel

import com.example.game.logic.LevelProvider
import com.example.game.logic.SpecialCandyResolver
import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import com.example.game.model.SpecialCombinationType
import com.example.game.model.TileAnimationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Deterministic test suite for Prompt 13:
 * Professional Animations, Cascade Feedback & Gameplay Polish.
 *
 * Verifies animation safety, input locks, move stability, cascade non-penalty,
 * and level transition idempotency.
 */
class AnimationAndGameplayPolishPrompt13Test {

    private lateinit var viewModel: Match3ViewModel

    @Before
    fun setUp() {
        viewModel = Match3ViewModel()
        viewModel.stepDelayMs = 0L // Instant resolution for deterministic testing
        SpecialCandyResolver.resetIdCounter(800000L)
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

    // ==========================================
    // 24. ANIMATION SAFETY & POLISH REQUIREMENTS
    // ==========================================

    @Test
    fun `1 - Selecting a tile sets selected state`() {
        viewModel.startGame(level = 1, random = Random(42))
        val pos = BoardPosition(3, 3)

        val result = viewModel.selectTile(pos)
        assertTrue(result)
        assertEquals(pos, viewModel.gameState.value.selectedPosition)
        assertNotNull(viewModel.gameState.value.selectedTile)
    }

    @Test
    fun `2 - Processing locks board input`() {
        viewModel.startGame(level = 1, random = Random(42))
        val lockedState = viewModel.gameState.value.copy(
            isProcessing = true,
            status = GameStatus.PROCESSING
        )
        viewModel.setCustomState(lockedState)

        // Attempting to select or tap any tile during processing must be strictly rejected
        val tapped = viewModel.selectTile(BoardPosition(2, 2))
        assertFalse(tapped)
        assertNull(viewModel.gameState.value.selectedPosition)
    }

    @Test
    fun `3 - Processing eventually returns to false and board reaches stable state`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.GREEN),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.RED, CandyType.YELLOW),
            listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE)
        )
        val board = createCustomBoard(grid)
        val state = GameState.createInitial(4, 4, 1).copy(
            board = board,
            status = GameStatus.PLAYING,
            isGameStarted = true
        )
        viewModel.setCustomState(state)

        // Swap (0, 2) [BLUE] and (1, 2) [RED] -> forms horizontal 3 RED match on Row 0
        val swapAccepted = viewModel.selectTile(BoardPosition(0, 2))
        assertTrue(swapAccepted)
        val swapResolved = viewModel.selectTile(BoardPosition(1, 2), Random(123))
        assertTrue(swapResolved)

        assertFalse(viewModel.gameState.value.isProcessing)
        assertEquals(GameStatus.PLAYING, viewModel.gameState.value.status)
    }

    @Test
    fun `4 - Valid swap changes board correctly`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.GREEN),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.RED, CandyType.YELLOW),
            listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE)
        )
        val board = createCustomBoard(grid)
        val initialMoves = 25
        val state = GameState.createInitial(4, 4, 1).copy(
            board = board,
            movesRemaining = initialMoves,
            status = GameStatus.PLAYING,
            isGameStarted = true
        )
        viewModel.setCustomState(state)

        viewModel.selectTile(BoardPosition(0, 2))
        viewModel.selectTile(BoardPosition(1, 2), Random(123))

        assertTrue(viewModel.gameState.value.score > 0)
        assertEquals(initialMoves - 1, viewModel.gameState.value.movesRemaining)
    }

    @Test
    fun `5 - Invalid swap restores board and does not decrement moves`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW)
        )
        val board = createCustomBoard(grid)
        val initialMoves = 30
        val state = GameState.createInitial(4, 4, 1).copy(
            board = board,
            movesRemaining = initialMoves,
            status = GameStatus.PLAYING,
            isGameStarted = true
        )
        viewModel.setCustomState(state)

        viewModel.selectTile(BoardPosition(0, 0))
        val swapResult = viewModel.selectTile(BoardPosition(0, 1))

        assertFalse(swapResult)
        assertEquals(initialMoves, viewModel.gameState.value.movesRemaining)
        assertEquals(0, viewModel.gameState.value.score)
        assertNull(viewModel.gameState.value.selectedPosition)
        assertEquals(board, viewModel.gameState.value.board)
    }

    @Test
    fun `6 - One valid swap consumes exactly one move`() {
        viewModel.startGame(level = 1, random = Random(42))
        val initialMoves = viewModel.gameState.value.movesRemaining

        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.GREEN),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.RED, CandyType.YELLOW),
            listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE)
        )
        viewModel.setCustomState(viewModel.gameState.value.copy(board = createCustomBoard(grid), status = GameStatus.PLAYING))

        viewModel.selectTile(BoardPosition(0, 2))
        viewModel.selectTile(BoardPosition(1, 2), Random(99))

        assertEquals(initialMoves - 1, viewModel.gameState.value.movesRemaining)
    }

    @Test
    fun `7 - Animation processing does not consume extra moves`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.GREEN),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.RED, CandyType.YELLOW),
            listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE)
        )
        val startMoves = 15
        viewModel.setCustomState(
            GameState.createInitial(4, 4, 1).copy(
                board = createCustomBoard(grid),
                movesRemaining = startMoves,
                status = GameStatus.PLAYING,
                isGameStarted = true
            )
        )

        viewModel.selectTile(BoardPosition(0, 2))
        viewModel.selectTile(BoardPosition(1, 2), Random(77))

        assertEquals(startMoves - 1, viewModel.gameState.value.movesRemaining)
    }

    @Test
    fun `8 - Cascade does not consume extra moves`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.GREEN),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.RED, CandyType.YELLOW),
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.PURPLE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE)
        )
        val startMoves = 20
        viewModel.setCustomState(
            GameState.createInitial(4, 4, 1).copy(
                board = createCustomBoard(grid),
                movesRemaining = startMoves,
                status = GameStatus.PLAYING,
                isGameStarted = true
            )
        )

        viewModel.resolveCascadesSynchronously(random = Random(123))

        // Cascade automatic resolution must not consume player moves
        assertEquals(startMoves, viewModel.gameState.value.movesRemaining)
        assertTrue(viewModel.gameState.value.score > 0)
    }

    @Test
    fun `9 - Special activation does not consume extra moves`() {
        val tiles = List(5) { r ->
            List(5) { c ->
                if (r == 2 && c == 2) {
                    CandyTile(100L, CandyType.RED, r, c, SpecialCandyType.HORIZONTAL_STRIPED)
                } else {
                    CandyTile((r * 5 + c + 1).toLong(), CandyType.BLUE, r, c)
                }
            }
        }
        val board = Match3Board(5, 5, tiles)
        val startMoves = 12
        viewModel.setCustomState(
            GameState.createInitial(5, 5, 1).copy(
                board = board,
                movesRemaining = startMoves,
                status = GameStatus.PLAYING,
                isGameStarted = true
            )
        )

        // Swapping adjacent striped candy with blue
        viewModel.selectTile(BoardPosition(2, 2))
        viewModel.selectTile(BoardPosition(2, 3), Random(42))

        assertEquals(startMoves - 1, viewModel.gameState.value.movesRemaining)
    }

    @Test
    fun `10 - Level completion does not trigger twice`() {
        var completeCallCount = 0
        viewModel.onLevelCompleteListener = {
            completeCallCount++
        }

        viewModel.startGame(level = 1, random = Random(42))
        val target = viewModel.gameState.value.levelConfig.targetScore ?: 1000

        // Fulfill objective
        viewModel.setCustomState(
            viewModel.gameState.value.copy(
                score = target + 500,
                objectives = viewModel.gameState.value.objectives.map { it.copy(currentProgress = it.target) },
                status = GameStatus.PLAYING,
                isProcessing = false
            )
        )

        viewModel.resolveCascadesSynchronously(random = Random(42))
        assertEquals(1, completeCallCount)
        assertTrue(viewModel.gameState.value.isLevelCompleted)
        assertEquals(GameStatus.COMPLETED, viewModel.gameState.value.status)

        // Calling again on completed state should not trigger redundant completion
        viewModel.resolveCascadesSynchronously(random = Random(42))
        assertEquals(1, completeCallCount)
    }

    @Test
    fun `11 - Rapid tapping is guarded by isProcessing lock`() {
        viewModel.startGame(level = 1, random = Random(42))
        viewModel.setCustomState(viewModel.gameState.value.copy(isProcessing = true))

        // Rapid tap attempts
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                val accepted = viewModel.selectTile(BoardPosition(r, c))
                assertFalse(accepted)
            }
        }
    }

    @Test
    fun `12 - TileAnimationState interactive property`() {
        assertTrue(TileAnimationState.IDLE.isInteractive)
        assertTrue(TileAnimationState.SELECTED.isInteractive)
        assertFalse(TileAnimationState.SWAPPING.isInteractive)
        assertFalse(TileAnimationState.MATCHED.isInteractive)
        assertFalse(TileAnimationState.DISAPPEARING.isInteractive)
        assertFalse(TileAnimationState.FALLING.isInteractive)
        assertFalse(TileAnimationState.SPAWNING.isInteractive)
        assertFalse(TileAnimationState.ACTIVATING_SPECIAL.isInteractive)
    }
}
