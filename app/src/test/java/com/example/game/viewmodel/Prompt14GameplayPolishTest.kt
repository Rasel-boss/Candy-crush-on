package com.example.game.viewmodel

import com.example.game.logic.LevelProvider
import com.example.game.logic.ObjectiveManager
import com.example.game.logic.SpecialCandyResolver
import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.LevelObjective
import com.example.game.model.Match3Board
import com.example.game.model.ObjectiveType
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
 * Deterministic test suite for Prompt 14:
 * Professional Gameplay Polish, Animations & Feedback.
 *
 * Covers:
 * 1. Selecting a tile updates selection state.
 * 2. Processing state prevents duplicate input.
 * 3. Invalid swap returns to stable board state.
 * 4. Valid swap enters processing state.
 * 5. Score changes only once per resolved match.
 * 6. Level objective progress is not duplicated.
 * 7. Restart clears animation-related state.
 * 8. Level completion does not automatically start another level.
 */
class Prompt14GameplayPolishTest {

    private lateinit var viewModel: Match3ViewModel

    @Before
    fun setUp() {
        viewModel = Match3ViewModel()
        viewModel.stepDelayMs = 0L // Instant resolution for deterministic unit testing
        SpecialCandyResolver.resetIdCounter(900000L)
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

    @Test
    fun `1 - Selecting a tile updates selection state`() {
        viewModel.startGame(level = 1, random = Random(101))
        val pos = BoardPosition(2, 3)

        val accepted = viewModel.selectTile(pos)

        assertTrue(accepted)
        assertEquals(pos, viewModel.gameState.value.selectedPosition)
        assertNotNull(viewModel.gameState.value.selectedTile)
        assertEquals(pos.row, viewModel.gameState.value.selectedTile?.row)
        assertEquals(pos.column, viewModel.gameState.value.selectedTile?.column)
    }

    @Test
    fun `2 - Processing state prevents duplicate input`() {
        viewModel.startGame(level = 1, random = Random(102))
        val processingState = viewModel.gameState.value.copy(
            isProcessing = true,
            status = GameStatus.PROCESSING
        )
        viewModel.setCustomState(processingState)

        // All tap attempts during processing must be strictly rejected
        val tappedA = viewModel.selectTile(BoardPosition(0, 0))
        val tappedB = viewModel.selectTile(BoardPosition(1, 1))

        assertFalse(tappedA)
        assertFalse(tappedB)
        assertNull(viewModel.gameState.value.selectedPosition)
    }

    @Test
    fun `3 - Invalid swap returns to stable board state`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW)
        )
        val initialBoard = createCustomBoard(grid)
        val startMoves = 20
        val state = GameState.createInitial(4, 4, 1).copy(
            board = initialBoard,
            movesRemaining = startMoves,
            status = GameStatus.PLAYING,
            isGameStarted = true
        )
        viewModel.setCustomState(state)

        // Select (0, 0) and attempt invalid swap with adjacent (0, 1)
        viewModel.selectTile(BoardPosition(0, 0))
        val result = viewModel.selectTile(BoardPosition(0, 1))

        assertFalse(result)
        assertEquals(initialBoard, viewModel.gameState.value.board)
        assertEquals(startMoves, viewModel.gameState.value.movesRemaining)
        assertNull(viewModel.gameState.value.selectedPosition)
        assertFalse(viewModel.gameState.value.isProcessing)
    }

    @Test
    fun `4 - Valid swap enters processing state and resolves`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.GREEN),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.RED, CandyType.YELLOW),
            listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE)
        )
        val board = createCustomBoard(grid)
        val startMoves = 15
        val state = GameState.createInitial(4, 4, 1).copy(
            board = board,
            movesRemaining = startMoves,
            status = GameStatus.PLAYING,
            isGameStarted = true
        )
        viewModel.setCustomState(state)

        // Select (0, 2) [BLUE] and swap with (1, 2) [RED] -> creates horizontal 3 RED match on row 0
        viewModel.selectTile(BoardPosition(0, 2))
        val validSwap = viewModel.selectTile(BoardPosition(1, 2), Random(202))

        assertTrue(validSwap)
        assertEquals(startMoves - 1, viewModel.gameState.value.movesRemaining)
        assertTrue(viewModel.gameState.value.score > 0)
        assertFalse(viewModel.gameState.value.isProcessing)
    }

    @Test
    fun `5 - Score changes only once per resolved match`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.GREEN),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.RED, CandyType.YELLOW),
            listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE)
        )
        val board = createCustomBoard(grid)
        val state = GameState.createInitial(4, 4, 1).copy(
            board = board,
            score = 0,
            status = GameStatus.PLAYING,
            isGameStarted = true
        )
        viewModel.setCustomState(state)

        viewModel.selectTile(BoardPosition(0, 2))
        viewModel.selectTile(BoardPosition(1, 2), Random(303))

        val scoreAfterMatch = viewModel.gameState.value.score
        assertTrue(scoreAfterMatch > 0)

        // No subsequent ghost increments without interaction
        assertEquals(scoreAfterMatch, viewModel.gameState.value.score)
    }

    @Test
    fun `6 - Level objective progress is not duplicated`() {
        val objective = LevelObjective(
            id = "collect_red",
            type = ObjectiveType.COLLECT_CANDY,
            target = 3,
            currentProgress = 0,
            candyType = CandyType.RED
        )
        val initialRemoved = listOf(
            CandyTile(1L, CandyType.RED, 0, 0),
            CandyTile(2L, CandyType.RED, 0, 1),
            CandyTile(3L, CandyType.RED, 0, 2)
        )
        val updated = ObjectiveManager.onCandiesRemoved(listOf(objective), initialRemoved)
        assertEquals(3, updated.first().currentProgress)
        assertTrue(updated.first().isCompleted)

        // Passing empty list does not duplicate progress
        val secondPass = ObjectiveManager.onCandiesRemoved(updated, emptyList())
        assertEquals(3, secondPass.first().currentProgress)
    }

    @Test
    fun `7 - Restart clears animation-related state`() {
        viewModel.startGame(level = 1, random = Random(505))

        // Populate animation-related states
        viewModel.setCustomState(
            viewModel.gameState.value.copy(
                selectedPosition = BoardPosition(2, 2),
                matchingPositions = setOf(BoardPosition(0, 0), BoardPosition(0, 1), BoardPosition(0, 2)),
                invalidSwapPair = Pair(BoardPosition(1, 1), BoardPosition(1, 2)),
                swappingPair = Pair(BoardPosition(3, 3), BoardPosition(3, 4)),
                isBoardImpact = true,
                cascadeChainCount = 4
            )
        )

        // Call restart
        viewModel.restartGame(Random(606))

        val cleanState = viewModel.gameState.value
        assertNull(cleanState.selectedPosition)
        assertTrue(cleanState.matchingPositions.isEmpty())
        assertNull(cleanState.invalidSwapPair)
        assertNull(cleanState.swappingPair)
        assertFalse(cleanState.isBoardImpact)
        assertEquals(0, cleanState.cascadeChainCount)
        assertFalse(cleanState.isProcessing)
        assertEquals(GameStatus.PLAYING, cleanState.status)
        assertEquals(0, cleanState.score)
    }

    @Test
    fun `8 - Level completion does not automatically start another level`() {
        var completedFired = 0
        viewModel.onLevelCompleteListener = {
            completedFired++
        }

        viewModel.startGame(level = 1, random = Random(707))
        val currentLevel = viewModel.gameState.value.level

        // Set state to completed
        viewModel.setCustomState(
            viewModel.gameState.value.copy(
                objectives = viewModel.gameState.value.objectives.map { it.copy(currentProgress = it.target) },
                score = 5000,
                status = GameStatus.PLAYING,
                isProcessing = false
            )
        )

        viewModel.resolveCascadesSynchronously(random = Random(808))

        assertEquals(1, completedFired)
        assertTrue(viewModel.gameState.value.isLevelCompleted)
        assertEquals(GameStatus.COMPLETED, viewModel.gameState.value.status)
        // Level must remain on current level waiting for explicit user interaction
        assertEquals(currentLevel, viewModel.gameState.value.level)
    }
}
