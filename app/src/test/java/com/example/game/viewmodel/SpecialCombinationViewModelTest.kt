package com.example.game.viewmodel

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import com.example.game.model.SpecialCombinationType
import com.example.game.utils.ScoreCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SpecialCombinationViewModelTest {

    private lateinit var viewModel: Match3ViewModel

    @Before
    fun setUp() {
        viewModel = Match3ViewModel().apply {
            stepDelayMs = 0L
        }
    }

    private fun createFilledBoard(fillType: CandyType = CandyType.BLUE): Match3Board {
        var idCounter = 1L
        val tiles = List(8) { r ->
            List(8) { c ->
                CandyTile(
                    id = idCounter++,
                    type = fillType,
                    row = r,
                    column = c
                )
            }
        }
        return Match3Board(8, 8, tiles)
    }

    @Test
    fun testDirectCombinationConsumesExactlyOneMove() {
        var customBoard = createFilledBoard(CandyType.BLUE)
        val posA = BoardPosition(3, 3)
        val posB = BoardPosition(3, 4)

        customBoard = customBoard.withTile(
            CandyTile(100L, CandyType.RED, 3, 3, SpecialCandyType.HORIZONTAL_STRIPED)
        )
        customBoard = customBoard.withTile(
            CandyTile(101L, CandyType.GREEN, 3, 4, SpecialCandyType.VERTICAL_STRIPED)
        )

        viewModel.setCustomState(
            GameState(
                board = customBoard,
                movesRemaining = 30,
                score = 0,
                status = GameStatus.PLAYING,
                isGameStarted = true
            )
        )

        // Select first tile
        val selectFirst = viewModel.selectTile(posA)
        assertTrue(selectFirst)
        assertEquals(posA, viewModel.gameState.value.selectedPosition)

        // Select adjacent second tile (triggers combination)
        val selectSecond = viewModel.selectTile(posB)
        assertTrue(selectSecond)

        val finalState = viewModel.gameState.value
        assertEquals(29, finalState.movesRemaining)
        assertTrue(finalState.score >= ScoreCalculator.COMBO_STRIPED_STRIPED_POINTS)
        assertEquals(GameStatus.PLAYING, finalState.status)
        assertFalse(finalState.isProcessing)
    }

    @Test
    fun testColorBombCombinationScoresAndCascades() {
        var customBoard = createFilledBoard(CandyType.YELLOW)
        val posA = BoardPosition(0, 0)
        val posB = BoardPosition(0, 1)

        customBoard = customBoard.withTile(
            CandyTile(100L, CandyType.EMPTY, 0, 0, SpecialCandyType.COLOR_BOMB)
        )
        customBoard = customBoard.withTile(
            CandyTile(101L, CandyType.EMPTY, 0, 1, SpecialCandyType.COLOR_BOMB)
        )

        viewModel.setCustomState(
            GameState(
                board = customBoard,
                movesRemaining = 10,
                score = 0,
                status = GameStatus.PLAYING,
                isGameStarted = true
            )
        )

        viewModel.selectTile(posA)
        viewModel.selectTile(posB)

        val finalState = viewModel.gameState.value
        assertEquals(9, finalState.movesRemaining)
        assertTrue(finalState.score >= ScoreCalculator.COMBO_COLOR_BOMB_COLOR_BOMB_POINTS)

        // Board must be fully refilled and valid
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val tile = finalState.board.getTile(r, c)
                assertTrue("Tile at ($r, $c) must not be empty", tile != null && tile.type != CandyType.EMPTY)
            }
        }
    }
}
