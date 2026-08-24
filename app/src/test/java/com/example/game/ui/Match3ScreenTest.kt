package com.example.game.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.Match3Board
import com.example.game.viewmodel.Match3ViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.theme.PuzzleMasterTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Match3ScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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
        val tiles = grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, type ->
                CandyTile(id = (r * 8 + c + 1).toLong(), type = type, row = r, column = c)
            }
        }
        return Match3Board(rows = 8, columns = 8, tiles = tiles)
    }

    @Test
    fun `1 - 8x8 board renders correctly`() {
        val viewModel = Match3ViewModel()
        viewModel.startGame(level = 1, random = Random(42))

        composeTestRule.setContent {
            PuzzleMasterTheme {
                GameScreen(viewModel = viewModel)
            }
        }

        // Verify top bar and 8x8 board container
        composeTestRule.onNodeWithTag("game_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("match3_board").assertIsDisplayed()

        // Verify all 64 tiles render
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                composeTestRule.onNodeWithTag("candy_tile_${r}_$c").assertIsDisplayed()
            }
        }
    }

    @Test
    fun `2 - A tile can be selected and shows visual selection`() {
        val viewModel = Match3ViewModel()
        viewModel.startGame(level = 1, random = Random(42))

        composeTestRule.setContent {
            PuzzleMasterTheme {
                GameScreen(viewModel = viewModel)
            }
        }

        // Tap tile at (2, 3)
        composeTestRule.onNodeWithTag("candy_tile_2_3").performClick()

        // Assert ViewModel selection state
        assertEquals(BoardPosition(2, 3), viewModel.gameState.value.selectedPosition)
        assertNotNull(viewModel.gameState.value.selectedTile)
    }

    @Test
    fun `3 - Non-adjacent tile tap changes selection without swap or move consumption`() {
        val viewModel = Match3ViewModel()
        viewModel.startGame(level = 1, random = Random(42))

        composeTestRule.setContent {
            PuzzleMasterTheme {
                GameScreen(viewModel = viewModel)
            }
        }

        // Select tile at (0, 0)
        composeTestRule.onNodeWithTag("candy_tile_0_0").performClick()
        assertEquals(BoardPosition(0, 0), viewModel.gameState.value.selectedPosition)
        assertEquals(30, viewModel.gameState.value.movesRemaining)

        // Tap non-adjacent tile at (5, 5)
        composeTestRule.onNodeWithTag("candy_tile_5_5").performClick()
        assertEquals(BoardPosition(5, 5), viewModel.gameState.value.selectedPosition)
        assertEquals(30, viewModel.gameState.value.movesRemaining)
    }

    @Test
    fun `4 - Valid adjacent swap updates board, refills, and decreases move counter`() {
        val viewModel = Match3ViewModel()
        viewModel.stepDelayMs = 0L
        val board = createStandardTestBoard()
        viewModel.setCustomState(
            GameState(
                board = board,
                status = GameStatus.PLAYING,
                movesRemaining = 30,
                score = 0
            )
        )

        composeTestRule.setContent {
            PuzzleMasterTheme {
                GameScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("30").assertIsDisplayed()

        // (0, 2) [BLUE] and (1, 2) [RED] are adjacent and create RED, RED, RED on row 0
        composeTestRule.onNodeWithTag("candy_tile_0_2").performClick()
        composeTestRule.onNodeWithTag("candy_tile_1_2").performClick()

        // Moves should decrease by 1 to 29
        assertEquals(29, viewModel.gameState.value.movesRemaining)
        composeTestRule.onNodeWithText("29").assertIsDisplayed()
        assertNull(viewModel.gameState.value.selectedPosition)

        // Score should increase by at least 30
        assertTrue(viewModel.gameState.value.score >= 30)

        // Board should remain 64 playable tiles
        assertEquals(64, viewModel.gameState.value.board.allTiles.count { it.type.isPlayable })
    }

    @Test
    fun `5 - Invalid swap restores original board and preserves move count`() {
        val viewModel = Match3ViewModel()
        val board = createStandardTestBoard()
        viewModel.setCustomState(
            GameState(
                board = board,
                status = GameStatus.PLAYING,
                movesRemaining = 30,
                score = 0
            )
        )

        composeTestRule.setContent {
            PuzzleMasterTheme {
                GameScreen(viewModel = viewModel)
            }
        }

        // (3, 3) and (3, 4) are adjacent but swapping them yields no match
        composeTestRule.onNodeWithTag("candy_tile_3_3").performClick()
        composeTestRule.onNodeWithTag("candy_tile_3_4").performClick()

        // Moves remain 30
        assertEquals(30, viewModel.gameState.value.movesRemaining)
        composeTestRule.onNodeWithText("30").assertIsDisplayed()
        assertNull(viewModel.gameState.value.selectedPosition)

        // Original tiles restored
        assertEquals(CandyType.ORANGE, viewModel.gameState.value.board.getTile(3, 3)?.type)
        assertEquals(CandyType.RED, viewModel.gameState.value.board.getTile(3, 4)?.type)
    }
}
