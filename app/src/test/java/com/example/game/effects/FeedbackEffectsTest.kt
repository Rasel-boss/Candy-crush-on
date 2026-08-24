package com.example.game.effects

import androidx.compose.ui.graphics.Color
import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.DEFAULT_COLUMNS
import com.example.game.model.DEFAULT_ROWS
import com.example.game.model.FloatingScoreEvent
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.Match3Board
import com.example.game.ui.effects.ParticleFactory
import com.example.game.ui.effects.ParticleSystemState
import com.example.game.viewmodel.Match3ViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FeedbackEffectsTest {

    private lateinit var viewModel: Match3ViewModel

    @Before
    fun setUp() {
        viewModel = Match3ViewModel().apply {
            stepDelayMs = 0L // instantaneous resolution for deterministic unit tests
        }
    }

    @Test
    fun testParticleFactory_generatesParticlesCorrectly() {
        val particles = ParticleFactory.createMatchBurst(
            centerX = 100f,
            centerY = 150f,
            count = 8,
            baseColor = Color(0xFFFF5376),
            glowColor = Color.White
        )

        assertEquals(8, particles.size)
        for (p in particles) {
            assertEquals(100f, p.x, 10f)
            assertEquals(150f, p.y, 10f)
            assertEquals(1f, p.alpha, 0.001f)
            assertTrue(p.maxAge > 0f)
        }
    }

    @Test
    fun testParticleSystemState_burstEmissionAndTicking() {
        val state = ParticleSystemState()
        assertFalse(state.particles.isNotEmpty())

        val particles = ParticleFactory.createMatchBurst(50f, 50f, 6, Color.Cyan, Color.White)
        state.emitBurst(particles)
        assertTrue(state.particles.isNotEmpty())

        // Tick simulation step
        state.update(0.1f)
        // Advance beyond max lifetime
        state.update(1.0f)
        assertFalse(state.particles.isNotEmpty())
    }

    @Test
    fun testFloatingScoreEvent_formattingAndProperties() {
        val eventNormal = FloatingScoreEvent(
            id = 1L,
            score = 30,
            centerRow = 2.0f,
            centerColumn = 3.0f,
            cascadeCount = 1
        )
        assertEquals("+30", eventNormal.text)

        val eventCascade = FloatingScoreEvent(
            id = 2L,
            score = 60,
            centerRow = 4.0f,
            centerColumn = 4.0f,
            cascadeCount = 2
        )
        assertEquals("+60\nCHAIN x2", eventCascade.text)
    }

    @Test
    fun testInvalidSwap_triggersFeedbackAndPreservesMoves() {
        var idCounter = 1L
        val tiles = List(DEFAULT_ROWS) { r ->
            List(DEFAULT_COLUMNS) { c ->
                val type = if ((r + c) % 2 == 0) CandyType.RED else CandyType.BLUE
                CandyTile(id = idCounter++, row = r, column = c, type = type)
            }
        }
        val board = Match3Board(DEFAULT_ROWS, DEFAULT_COLUMNS, tiles)
        viewModel.setCustomState(
            GameState(
                board = board,
                movesRemaining = 20,
                score = 0,
                status = GameStatus.PLAYING
            )
        )

        var invalidSwapCallbackCalled = false
        viewModel.onInvalidSwapListener = {
            invalidSwapCallbackCalled = true
        }

        // Tap first tile
        val selected = viewModel.selectTile(BoardPosition(0, 0))
        assertTrue(selected)
        assertEquals(BoardPosition(0, 0), viewModel.gameState.value.selectedPosition)

        // Tap adjacent tile that makes NO match
        val swapped = viewModel.selectTile(BoardPosition(0, 1))
        assertFalse(swapped) // Swap invalid
        assertTrue(invalidSwapCallbackCalled)
        assertEquals(20, viewModel.gameState.value.movesRemaining) // Moves preserved
        assertNull(viewModel.gameState.value.selectedPosition)
    }

    @Test
    fun testValidSwap_triggersSoundAndHapticCallbacks() {
        var idCounter = 100L
        val mutableGrid = MutableList(DEFAULT_ROWS) { r ->
            MutableList(DEFAULT_COLUMNS) { c ->
                CandyTile(id = idCounter++, row = r, column = c, type = CandyType.PURPLE)
            }
        }
        // Place a matchable configuration:
        mutableGrid[0][0] = CandyTile(id = 1L, row = 0, column = 0, type = CandyType.RED)
        mutableGrid[0][1] = CandyTile(id = 2L, row = 0, column = 1, type = CandyType.BLUE)
        mutableGrid[0][2] = CandyTile(id = 3L, row = 0, column = 2, type = CandyType.RED)
        mutableGrid[1][1] = CandyTile(id = 4L, row = 1, column = 1, type = CandyType.RED) // Swapping (1,1) with (0,1) makes 3 REDs at (0,0), (0,1), (0,2)

        val board = Match3Board(DEFAULT_ROWS, DEFAULT_COLUMNS, mutableGrid.map { it.toList() })
        viewModel.setCustomState(
            GameState(
                board = board,
                movesRemaining = 15,
                score = 0,
                status = GameStatus.PLAYING
            )
        )

        var validSwapCalled = false
        var matchResolvedCalled = false
        viewModel.onValidSwapListener = { validSwapCalled = true }
        viewModel.onMatchResolvedListener = { matchResolvedCalled = true }

        viewModel.selectTile(BoardPosition(1, 1))
        viewModel.selectTile(BoardPosition(0, 1))

        assertTrue(validSwapCalled)
        assertEquals(14, viewModel.gameState.value.movesRemaining)
        assertTrue(viewModel.gameState.value.score > 0)
    }

    @Test
    fun testGameOver_triggersWhenMovesReachZero() {
        var idCounter = 200L
        val mutableGrid = MutableList(DEFAULT_ROWS) { r ->
            MutableList(DEFAULT_COLUMNS) { c ->
                CandyTile(id = idCounter++, row = r, column = c, type = CandyType.YELLOW)
            }
        }
        // Match at row 0
        mutableGrid[0][0] = CandyTile(id = 10L, row = 0, column = 0, type = CandyType.GREEN)
        mutableGrid[0][1] = CandyTile(id = 11L, row = 0, column = 1, type = CandyType.BLUE)
        mutableGrid[0][2] = CandyTile(id = 12L, row = 0, column = 2, type = CandyType.GREEN)
        mutableGrid[1][1] = CandyTile(id = 13L, row = 1, column = 1, type = CandyType.GREEN)

        val board = Match3Board(DEFAULT_ROWS, DEFAULT_COLUMNS, mutableGrid.map { it.toList() })
        viewModel.setCustomState(
            GameState(
                board = board,
                movesRemaining = 1, // Only 1 move left
                score = 0,
                status = GameStatus.PLAYING
            )
        )

        var gameOverCalled = false
        viewModel.onGameOverListener = { gameOverCalled = true }

        viewModel.selectTile(BoardPosition(1, 1))
        viewModel.selectTile(BoardPosition(0, 1))

        assertEquals(0, viewModel.gameState.value.movesRemaining)
        assertTrue(viewModel.gameState.value.isGameOver)
        assertEquals(GameStatus.GAME_OVER, viewModel.gameState.value.status)
        assertTrue(gameOverCalled)
    }
}
