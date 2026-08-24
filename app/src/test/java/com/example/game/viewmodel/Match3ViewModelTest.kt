package com.example.game.viewmodel

import com.example.game.logic.MatchDetector
import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.DEFAULT_MOVES
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.Match3Board
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class Match3ViewModelTest {

    private lateinit var viewModel: Match3ViewModel

    @Before
    fun setUp() {
        viewModel = Match3ViewModel()
        viewModel.stepDelayMs = 0L // Instant resolution for tests
    }

    /**
     * Helper to create a deterministic board from a 2D list of CandyTypes.
     */
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
     * Helper to create an 8x8 deterministic board with no initial matches.
     * Swapping (0, 2) [BLUE] and (1, 2) [RED] creates a horizontal 3-match on Row 0.
     */
    private fun createStandardTestBoard(): Match3Board {
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

    // ==========================================
    // 1. SELECTION TESTS
    // ==========================================

    @Test
    fun `1 - First tap selects a tile`() {
        viewModel.startGame(level = 1, random = Random(42))
        val pos = BoardPosition(2, 3)

        val success = viewModel.selectTile(pos)

        assertTrue(success)
        assertEquals(pos, viewModel.gameState.value.selectedPosition)
        assertEquals(pos, viewModel.gameState.value.selectedTile?.position)
    }

    @Test
    fun `2 - Tapping the same tile clears selection`() {
        viewModel.startGame(level = 1, random = Random(42))
        val pos = BoardPosition(2, 3)

        viewModel.selectTile(pos)
        assertEquals(pos, viewModel.gameState.value.selectedPosition)

        val deselectSuccess = viewModel.selectTile(pos)
        assertTrue(deselectSuccess)
        assertNull(viewModel.gameState.value.selectedPosition)
        assertNull(viewModel.gameState.value.selectedTile)
    }

    @Test
    fun `3 - Tapping a non-adjacent tile selects the new tile`() {
        viewModel.startGame(level = 1, random = Random(42))
        val pos1 = BoardPosition(1, 1)
        val pos2 = BoardPosition(5, 6) // Non-adjacent

        viewModel.selectTile(pos1)
        assertEquals(pos1, viewModel.gameState.value.selectedPosition)

        viewModel.selectTile(pos2)
        assertEquals(pos2, viewModel.gameState.value.selectedPosition)
    }

    @Test
    fun `4 - Selection does not consume a move`() {
        viewModel.startGame(level = 1, random = Random(42))
        val initialMoves = viewModel.gameState.value.movesRemaining
        assertEquals(30, initialMoves)

        // Select tile
        viewModel.selectTile(BoardPosition(0, 0))
        assertEquals(initialMoves, viewModel.gameState.value.movesRemaining)

        // Deselect tile
        viewModel.selectTile(BoardPosition(0, 0))
        assertEquals(initialMoves, viewModel.gameState.value.movesRemaining)

        // Select another non-adjacent tile
        viewModel.selectTile(BoardPosition(4, 4))
        assertEquals(initialMoves, viewModel.gameState.value.movesRemaining)
    }

    @Test
    fun `10 - Next level advances level index and loads new level objectives`() {
        viewModel.startGame(level = 1, random = Random(42))
        assertEquals(1, viewModel.gameState.value.level)
        assertEquals(30, viewModel.gameState.value.movesRemaining)
        assertTrue(viewModel.gameState.value.objectives.isNotEmpty())

        viewModel.nextLevel(Random(42))
        assertEquals(2, viewModel.gameState.value.level)
        assertEquals(30, viewModel.gameState.value.movesRemaining)
        assertEquals(GameStatus.PLAYING, viewModel.gameState.value.status)
        assertTrue(viewModel.gameState.value.objectives.isNotEmpty())
    }

    // ==========================================
    // 2. VALID SWAP & RESOLUTION TESTS
    // ==========================================

    @Test
    fun `5 - Valid swap resolves matches, updates score, and consumes one move`() {
        val board = createStandardTestBoard()
        viewModel.setCustomState(
            GameState(
                board = board,
                status = GameStatus.PLAYING,
                movesRemaining = 30,
                score = 0
            )
        )

        // (0, 2) is BLUE, (1, 2) is RED. Swapping them makes row 0: RED, RED, RED
        viewModel.selectTile(BoardPosition(0, 2))
        val swapSuccess = viewModel.selectTile(BoardPosition(1, 2), Random(42))

        assertTrue(swapSuccess)
        // Moves decreased by 1
        assertEquals(29, viewModel.gameState.value.movesRemaining)
        // Score increased by at least 30
        assertTrue(viewModel.gameState.value.score >= 30)
        // Selection cleared
        assertNull(viewModel.gameState.value.selectedPosition)
        // Board has 64 playable tiles and no EMPTY
        assertEquals(64, viewModel.gameState.value.board.allTiles.count { it.type.isPlayable })
    }

    @Test
    fun `6 - Valid swap resolves cascades synchronously and returns to PLAYING`() {
        val board = createStandardTestBoard()
        viewModel.setCustomState(
            GameState(
                board = board,
                status = GameStatus.PLAYING,
                movesRemaining = 30,
                score = 0
            )
        )

        viewModel.selectTile(BoardPosition(0, 2))
        viewModel.selectTile(BoardPosition(1, 2), Random(42))
        viewModel.resolveCascadesSynchronously(Random(42))

        assertEquals(GameStatus.PLAYING, viewModel.gameState.value.status)
        assertFalse(viewModel.gameState.value.isProcessing)
        assertFalse(MatchDetector.hasAnyMatches(viewModel.gameState.value.board))
    }

    @Test
    fun `7 - Invalid swap restores original board and preserves move count and score`() {
        val board = createStandardTestBoard()
        viewModel.setCustomState(
            GameState(
                board = board,
                status = GameStatus.PLAYING,
                movesRemaining = 30,
                score = 0
            )
        )

        val originalTileA = board.getTile(3, 3)!!.type
        val originalTileB = board.getTile(3, 4)!!.type

        viewModel.selectTile(BoardPosition(3, 3))
        val swapSuccess = viewModel.selectTile(BoardPosition(3, 4))

        assertFalse(swapSuccess)
        assertEquals(30, viewModel.gameState.value.movesRemaining)
        assertEquals(0, viewModel.gameState.value.score)
        assertNull(viewModel.gameState.value.selectedPosition)
        assertEquals(originalTileA, viewModel.gameState.value.board.getTile(3, 3)?.type)
        assertEquals(originalTileB, viewModel.gameState.value.board.getTile(3, 4)?.type)
    }

    @Test
    fun `8 - User cannot initiate another swap during PROCESSING`() {
        val board = createStandardTestBoard()
        viewModel.setCustomState(
            GameState(
                board = board,
                status = GameStatus.PLAYING,
                isProcessing = true,
                movesRemaining = 30,
                score = 0
            )
        )

        val result = viewModel.selectTile(BoardPosition(0, 2))
        assertFalse(result)
        assertNull(viewModel.gameState.value.selectedPosition)
        assertEquals(30, viewModel.gameState.value.movesRemaining)
    }

    @Test
    fun `9 - Game over triggered when moves reach 0 after resolution`() {
        val board = createStandardTestBoard()
        viewModel.setCustomState(
            GameState(
                board = board,
                status = GameStatus.PLAYING,
                movesRemaining = 1,
                score = 0
            )
        )

        viewModel.selectTile(BoardPosition(0, 2))
        viewModel.selectTile(BoardPosition(1, 2), Random(42))
        viewModel.resolveCascadesSynchronously(Random(42))

        assertEquals(0, viewModel.gameState.value.movesRemaining)
        assertTrue(viewModel.gameState.value.isGameOver)
        assertEquals(GameStatus.GAME_OVER, viewModel.gameState.value.status)
    }
}
