package com.example.game.viewmodel

import com.example.game.logic.LevelProvider
import com.example.game.logic.ObjectiveManager
import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameStatus
import com.example.game.model.Match3Board
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Deterministic tests verifying ViewModel level transitions, replay, retry,
 * objective progress derivation, and state reset across levels.
 */
class Match3ViewModelProgressionTest {

    private lateinit var viewModel: Match3ViewModel

    @Before
    fun setUp() {
        viewModel = Match3ViewModel()
        viewModel.stepDelayMs = 0L
    }

    @Test
    fun testStartLevel1InitializesCompleteState() {
        viewModel.startGame(level = 1, random = Random(42))
        val state = viewModel.gameState.value

        assertEquals(1, state.level)
        assertEquals(30, state.movesRemaining)
        assertEquals(0, state.score)
        assertEquals(GameStatus.PLAYING, state.status)
        assertFalse(state.isProcessing)
        assertFalse(state.isLevelCompleted)
        assertFalse(state.isGameOver)
        assertNull(state.selectedPosition)
        assertEquals(8, state.rows)
        assertEquals(8, state.columns)
        assertEquals(2, state.objectives.size)
        assertEquals(0, state.objectives[0].currentProgress)
        assertEquals(0, state.objectives[1].currentProgress)
    }

    @Test
    fun testStartLevel2InitializesCompleteState() {
        viewModel.startGame(level = 2, random = Random(42))
        val state = viewModel.gameState.value

        assertEquals(2, state.level)
        assertEquals(30, state.movesRemaining)
        assertEquals(0, state.score)
        assertEquals(GameStatus.PLAYING, state.status)
        assertEquals(2, state.objectives.size)
        assertEquals(CandyType.BLUE, state.objectives[0].candyType)
    }

    @Test
    fun testStartLevel3InitializesCompleteState() {
        viewModel.startGame(level = 3, random = Random(42))
        val state = viewModel.gameState.value

        assertEquals(3, state.level)
        assertEquals(28, state.movesRemaining)
        assertEquals(0, state.score)
        assertEquals(GameStatus.PLAYING, state.status)
        assertEquals(3, state.objectives.size)
        assertEquals(CandyType.GREEN, state.objectives[0].candyType)
        assertEquals(CandyType.YELLOW, state.objectives[1].candyType)
    }

    @Test
    fun testLevelTransitionSequencing() {
        // Start Level 1
        viewModel.startGame(level = 1, random = Random(42))
        assertEquals(1, viewModel.gameState.value.level)

        // Advance to Level 2
        viewModel.nextLevel(random = Random(42))
        assertEquals(2, viewModel.gameState.value.level)
        assertEquals(30, viewModel.gameState.value.movesRemaining)
        assertEquals(0, viewModel.gameState.value.score)
        assertFalse(viewModel.gameState.value.isLevelCompleted)

        // Advance to Level 3
        viewModel.nextLevel(random = Random(42))
        assertEquals(3, viewModel.gameState.value.level)
        assertEquals(28, viewModel.gameState.value.movesRemaining)
        assertEquals(0, viewModel.gameState.value.score)
        assertFalse(viewModel.gameState.value.isLevelCompleted)

        // Advance to Level 4
        viewModel.nextLevel(random = Random(42))
        assertEquals(4, viewModel.gameState.value.level)
        assertTrue(viewModel.gameState.value.levelConfig.isValid())
    }

    @Test
    fun testReplayLevelRestartsCurrentLevelWithoutResettingToLevel1() {
        // Start Level 3
        viewModel.startGame(level = 3, random = Random(42))
        assertEquals(3, viewModel.gameState.value.level)

        // Simulate some state changes in Level 3
        viewModel.setCustomState(
            viewModel.gameState.value.copy(
                score = 500,
                movesRemaining = 15,
                selectedPosition = BoardPosition(2, 2)
            )
        )
        assertEquals(500, viewModel.gameState.value.score)
        assertEquals(15, viewModel.gameState.value.movesRemaining)

        // Replay Level 3
        viewModel.replayLevel(random = Random(42))

        val resetState = viewModel.gameState.value
        assertEquals("Replay must keep Level 3", 3, resetState.level)
        assertEquals("Moves must reset to starting moves for Level 3", 28, resetState.movesRemaining)
        assertEquals("Score must reset to 0", 0, resetState.score)
        assertNull("Selected position must be cleared", resetState.selectedPosition)
        assertEquals(GameStatus.PLAYING, resetState.status)
    }

    @Test
    fun testLevelCompletionDoesNotResetToLevel1() {
        viewModel.startGame(level = 2, random = Random(42))
        val completedObjectives = viewModel.gameState.value.objectives.map { it.copy(currentProgress = it.target) }

        viewModel.setCustomState(
            viewModel.gameState.value.copy(
                objectives = completedObjectives,
                isLevelCompleted = true,
                status = GameStatus.COMPLETED
            )
        )

        assertTrue(viewModel.gameState.value.isLevelCompleted)
        assertEquals(2, viewModel.gameState.value.level)

        // Next level transitions to 3, NOT 1
        viewModel.nextLevel(random = Random(42))
        assertEquals(3, viewModel.gameState.value.level)
    }
}
