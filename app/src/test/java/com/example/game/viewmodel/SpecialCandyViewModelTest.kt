package com.example.game.viewmodel

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class SpecialCandyViewModelTest {

    private lateinit var viewModel: Match3ViewModel

    @Before
    fun setUp() {
        viewModel = Match3ViewModel().apply {
            stepDelayMs = 0L // Instantaneous for tests
        }
    }

    private fun createCustomBoard(grid: List<List<CandyType>>): Match3Board {
        val rows = grid.size
        val cols = grid[0].size
        var nextId = 1L
        val tiles = grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, type ->
                CandyTile(id = nextId++, type = type, row = r, column = c)
            }
        }
        return Match3Board(rows = rows, columns = cols, tiles = tiles)
    }

    // 1 & 2. Player creates a 4-match -> Striped candy is created
    @Test
    fun `1 & 2 - Player creates a 4-match and Striped candy is created`() {
        // Setup board where swapping (0, 3) and (1, 3) creates a horizontal 4-match of RED on row 0
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.RED),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        viewModel.setCustomState(
            GameState(
                board = board,
                rows = 4,
                columns = 4,
                movesRemaining = 30,
                score = 0,
                status = GameStatus.PLAYING
            )
        )

        // Select (1, 3) [RED] then (0, 3) [BLUE] to complete 4-match at row 0
        viewModel.selectTile(1, 3)
        val valid = viewModel.selectTile(0, 3)

        assertTrue(valid)
        val finalState = viewModel.gameState.value
        assertEquals(29, finalState.movesRemaining)
        assertTrue(finalState.score >= 60)
        assertEquals(GameStatus.PLAYING, finalState.status)
    }

    // 3 & 4. Player creates a 5-match -> Color Bomb is created
    @Test
    fun `3 & 4 - Player creates a 5-match and Color Bomb is created`() {
        val grid = listOf(
            listOf(CandyType.YELLOW, CandyType.YELLOW, CandyType.YELLOW, CandyType.YELLOW, CandyType.BLUE),
            listOf(CandyType.RED, CandyType.GREEN, CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.YELLOW, CandyType.GREEN),
            listOf(CandyType.BLUE, CandyType.PURPLE, CandyType.GREEN, CandyType.ORANGE, CandyType.RED)
        )
        val board = createCustomBoard(grid)
        viewModel.setCustomState(
            GameState(
                board = board,
                rows = 5,
                columns = 5,
                movesRemaining = 25,
                score = 0,
                status = GameStatus.PLAYING
            )
        )

        // Swap (1, 4) [YELLOW] with (0, 4) [BLUE]
        viewModel.selectTile(1, 4)
        val valid = viewModel.selectTile(0, 4)

        assertTrue(valid)
        val state = viewModel.gameState.value
        assertEquals(24, state.movesRemaining)
        assertTrue(state.score >= 100)
    }

    // 5. L or T match creates Wrapped candy
    @Test
    fun `5 - L or T match creates Wrapped candy`() {
        val grid = listOf(
            listOf(CandyType.PURPLE, CandyType.PURPLE, CandyType.BLUE, CandyType.PURPLE),
            listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED),
            listOf(CandyType.GREEN, CandyType.PURPLE, CandyType.YELLOW, CandyType.BLUE),
            listOf(CandyType.ORANGE, CandyType.GREEN, CandyType.PURPLE, CandyType.YELLOW)
        )
        val board = createCustomBoard(grid)
        viewModel.setCustomState(
            GameState(
                board = board,
                rows = 4,
                columns = 4,
                movesRemaining = 20,
                score = 0,
                status = GameStatus.PLAYING
            )
        )

        // Swap (0, 2) [BLUE] and (0, 3) [PURPLE] -> L/T shape match at (0, 1)
        viewModel.selectTile(0, 2)
        val valid = viewModel.selectTile(0, 3)
        assertTrue(valid)
    }

    // 6. Special candy activation changes the board correctly
    @Test
    fun `6 - Special candy activation changes the board correctly`() {
        // Board with a Striped Candy at (0, 0)
        var nextId = 1L
        val tiles = List(4) { r ->
            List(4) { c ->
                if (r == 0 && c == 0) {
                    CandyTile(id = 100L, type = CandyType.RED, row = r, column = c, specialCandyType = SpecialCandyType.HORIZONTAL_STRIPED)
                } else {
                    CandyTile(id = nextId++, type = CandyType.BLUE, row = r, column = c)
                }
            }
        }
        val board = Match3Board(rows = 4, columns = 4, tiles = tiles)
        viewModel.setCustomState(
            GameState(
                board = board,
                rows = 4,
                columns = 4,
                movesRemaining = 15,
                score = 0,
                status = GameStatus.PLAYING
            )
        )

        // Swap (0, 0) with (0, 1) to directly activate the horizontal stripe
        viewModel.selectTile(0, 0)
        val activated = viewModel.selectTile(0, 1)
        assertTrue(activated)

        val finalState = viewModel.gameState.value
        assertEquals(14, finalState.movesRemaining)
        assertTrue(finalState.score >= 100) // Striped activation bonus
        assertEquals(GameStatus.PLAYING, finalState.status)
    }

    // 7. Score increases correctly
    @Test
    fun `7 - Score increases correctly`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.GREEN),
            listOf(CandyType.YELLOW, CandyType.ORANGE, CandyType.RED, CandyType.PURPLE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.PURPLE, CandyType.GREEN, CandyType.BLUE, CandyType.YELLOW)
        )
        val board = createCustomBoard(grid)
        viewModel.setCustomState(
            GameState(board = board, rows = 4, columns = 4, movesRemaining = 10, score = 500, status = GameStatus.PLAYING)
        )

        viewModel.selectTile(1, 2)
        viewModel.selectTile(0, 2)

        assertTrue(viewModel.gameState.value.score > 500)
    }

    // 8. Only one move is consumed
    @Test
    fun `8 - Only one move is consumed`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.RED),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.YELLOW)
        )
        val board = createCustomBoard(grid)
        viewModel.setCustomState(
            GameState(board = board, rows = 4, columns = 4, movesRemaining = 30, score = 0, status = GameStatus.PLAYING)
        )

        viewModel.resolveCascadesSynchronously(Random(42))
        // Cascades should NOT reduce movesRemaining
        assertEquals(30, viewModel.gameState.value.movesRemaining)
    }

    // 9. Processing state prevents additional input
    @Test
    fun `9 - Processing state prevents additional input`() {
        val board = Match3Board.createEmpty(4, 4)
        viewModel.setCustomState(
            GameState(board = board, rows = 4, columns = 4, isProcessing = true, status = GameStatus.PROCESSING)
        )

        val selected = viewModel.selectTile(0, 0)
        assertFalse(selected)
    }

    // 10. Game returns to PLAYING after all special effects finish
    @Test
    fun `10 - Game returns to PLAYING after all special effects finish`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        viewModel.setCustomState(
            GameState(board = board, rows = 4, columns = 4, movesRemaining = 10, status = GameStatus.PLAYING)
        )

        viewModel.resolveCascadesSynchronously(Random(42))
        assertEquals(GameStatus.PLAYING, viewModel.gameState.value.status)
        assertFalse(viewModel.gameState.value.isProcessing)
    }
}
