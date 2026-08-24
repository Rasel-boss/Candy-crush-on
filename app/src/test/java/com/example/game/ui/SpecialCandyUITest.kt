package com.example.game.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import com.example.game.ui.components.CandyTileView
import com.example.game.viewmodel.Match3ViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.theme.PuzzleMasterTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SpecialCandyUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // 1. Normal candies render correctly
    @Test
    fun `1 - Normal candies render correctly`() {
        val tile = CandyTile(id = 1L, type = CandyType.RED, row = 0, column = 0)
        composeTestRule.setContent {
            PuzzleMasterTheme {
                CandyTileView(tile = tile, isSelected = false, onClick = {})
            }
        }
        composeTestRule.onNodeWithTag("candy_tile_0_0").assertIsDisplayed()
    }

    // 2. Selected candy still highlights correctly
    @Test
    fun `2 - Selected candy still highlights correctly`() {
        val tile = CandyTile(id = 2L, type = CandyType.BLUE, row = 1, column = 1)
        composeTestRule.setContent {
            PuzzleMasterTheme {
                CandyTileView(tile = tile, isSelected = true, onClick = {})
            }
        }
        composeTestRule.onNodeWithTag("candy_tile_1_1").assertIsDisplayed()
    }

    // 3. Striped candy has a visible stripe
    @Test
    fun `3 - Striped candy has a visible stripe`() {
        val horizontalTile = CandyTile(id = 3L, type = CandyType.GREEN, row = 2, column = 2, specialCandyType = SpecialCandyType.HORIZONTAL_STRIPED)
        val verticalTile = CandyTile(id = 4L, type = CandyType.YELLOW, row = 3, column = 3, specialCandyType = SpecialCandyType.VERTICAL_STRIPED)

        composeTestRule.setContent {
            PuzzleMasterTheme {
                CandyTileView(tile = horizontalTile, isSelected = false, onClick = {})
                CandyTileView(tile = verticalTile, isSelected = false, onClick = {})
            }
        }
        composeTestRule.onNodeWithTag("striped_horizontal", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("striped_vertical", useUnmergedTree = true).assertIsDisplayed()
    }

    // 4. Wrapped candy has a visibly different appearance
    @Test
    fun `4 - Wrapped candy has a visibly different appearance`() {
        val wrappedTile = CandyTile(id = 5L, type = CandyType.PURPLE, row = 4, column = 4, specialCandyType = SpecialCandyType.WRAPPED)
        composeTestRule.setContent {
            PuzzleMasterTheme {
                CandyTileView(tile = wrappedTile, isSelected = false, onClick = {})
            }
        }
        composeTestRule.onNodeWithTag("wrapped_candy_badge", useUnmergedTree = true).assertIsDisplayed()
    }

    // 5. Color Bomb is visually distinct
    @Test
    fun `5 - Color Bomb is visually distinct`() {
        val bombTile = CandyTile(id = 6L, type = CandyType.EMPTY, row = 5, column = 5, specialCandyType = SpecialCandyType.COLOR_BOMB)
        composeTestRule.setContent {
            PuzzleMasterTheme {
                CandyTileView(tile = bombTile, isSelected = false, onClick = {})
            }
        }
        composeTestRule.onNodeWithTag("color_bomb_badge", useUnmergedTree = true).assertIsDisplayed()
    }

    // 6. Special activation does not crash the UI
    @Test
    fun `6 - Special activation does not crash the UI`() {
        val viewModel = Match3ViewModel().apply { stepDelayMs = 0L }
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
            GameState(board = board, rows = 4, columns = 4, movesRemaining = 20, score = 0, status = GameStatus.PLAYING)
        )

        composeTestRule.setContent {
            PuzzleMasterTheme {
                GameScreen(viewModel = viewModel)
            }
        }

        // Tap (0, 0) then (0, 1)
        composeTestRule.onNodeWithTag("candy_tile_0_0").performClick()
        composeTestRule.onNodeWithTag("candy_tile_0_1").performClick()

        // Verify score increases and UI stays responsive
        assertTrue(viewModel.gameState.value.score >= 100)
        assertEquals(19, viewModel.gameState.value.movesRemaining)
    }

    // 7. Board returns to normal interaction after effects finish
    @Test
    fun `7 - Board returns to normal interaction after effects finish`() {
        val viewModel = Match3ViewModel().apply { stepDelayMs = 0L }
        viewModel.startGame(level = 1)

        composeTestRule.setContent {
            PuzzleMasterTheme {
                GameScreen(viewModel = viewModel)
            }
        }

        assertFalse(viewModel.gameState.value.isProcessing)
        composeTestRule.onNodeWithTag("candy_tile_2_2").performClick()
        assertEquals(BoardPosition(2, 2), viewModel.gameState.value.selectedPosition)
    }
}
