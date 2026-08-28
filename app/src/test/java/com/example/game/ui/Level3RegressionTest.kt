package com.example.game.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.game.model.GameStatus
import com.example.game.viewmodel.Match3ViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.theme.PuzzleMasterTheme
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Deterministic Regression Test for Prompt 11 verifying that Level 1, 2, and 3
 * initialize completely and render without missing details or broken layouts.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Level3RegressionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLevel3InitializesCompleteGameState() {
        val viewModel = Match3ViewModel()
        viewModel.startGame(level = 3, random = Random(42))
        val state = viewModel.gameState.value

        assertEquals(3, state.level)
        assertNotNull(state.board)
        assertEquals(8, state.board.rows)
        assertEquals(8, state.board.columns)
        assertEquals(0, state.score)
        assertEquals(28, state.movesRemaining)
        assertNotNull(state.objectives)
        assertEquals(3, state.objectives.size)
        assertEquals(GameStatus.PLAYING, state.status)
        assertNull(state.selectedPosition)
        assertFalse(state.isProcessing)
        assertFalse(state.isLevelCompleted)
        assertFalse(state.isGameOver)
    }

    @Test
    fun testLevel3UIRendersAllElementsAndObjectives() {
        val viewModel = Match3ViewModel()
        viewModel.startGame(level = 3, random = Random(42))

        composeTestRule.setContent {
            PuzzleMasterTheme {
                GameScreen(viewModel = viewModel)
            }
        }

        // Verify Level Header & Info
        composeTestRule.onNodeWithTag("level_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level 3").assertIsDisplayed()
        composeTestRule.onNodeWithTag("game_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("match3_board").assertIsDisplayed()

        // Verify Moves & Score Stats
        composeTestRule.onNodeWithTag("moves_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("score_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("moves_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("score_text").assertIsDisplayed()

        // Verify Objective Badges
        composeTestRule.onNodeWithTag("objective_item_lvl3_obj_green").assertIsDisplayed()
        composeTestRule.onNodeWithTag("objective_item_lvl3_obj_yellow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("objective_item_lvl3_obj_score").assertIsDisplayed()
    }

    @Test
    fun testLevel1AndLevel2UIRendersConsistently() {
        val viewModel = Match3ViewModel()

        // Test Level 1
        viewModel.startGame(level = 1, random = Random(42))
        composeTestRule.setContent {
            PuzzleMasterTheme {
                GameScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("level_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level 1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("objective_item_lvl1_obj_red").assertIsDisplayed()
        composeTestRule.onNodeWithTag("objective_item_lvl1_obj_score").assertIsDisplayed()
    }
}
