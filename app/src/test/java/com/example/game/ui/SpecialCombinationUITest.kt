package com.example.game.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCombinationType
import com.example.game.ui.components.ComboEffectOverlay
import com.example.game.ui.components.Match3BoardView
import com.example.ui.theme.PuzzleMasterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SpecialCombinationUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testComboEffectOverlayDisplaysStripedStriped() {
        composeTestRule.setContent {
            PuzzleMasterTheme {
                ComboEffectOverlay(
                    comboType = SpecialCombinationType.STRIPED_STRIPED,
                    modifier = Modifier.size(400.dp)
                )
            }
        }

        composeTestRule.onNodeWithTag("combo_overlay", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("combo_striped_striped", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("CROSS BEAM!", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testComboEffectOverlayDisplaysWrappedWrapped() {
        composeTestRule.setContent {
            PuzzleMasterTheme {
                ComboEffectOverlay(
                    comboType = SpecialCombinationType.WRAPPED_WRAPPED,
                    modifier = Modifier.size(400.dp)
                )
            }
        }

        composeTestRule.onNodeWithTag("combo_overlay", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("combo_wrapped_wrapped", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("MEGA BLAST!", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testComboEffectOverlayDisplaysStripedWrapped() {
        composeTestRule.setContent {
            PuzzleMasterTheme {
                ComboEffectOverlay(
                    comboType = SpecialCombinationType.STRIPED_WRAPPED,
                    modifier = Modifier.size(400.dp)
                )
            }
        }

        composeTestRule.onNodeWithTag("combo_overlay", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("combo_striped_wrapped", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("SUPER CROSS!", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testComboEffectOverlayDisplaysColorBombCombinations() {
        composeTestRule.setContent {
            PuzzleMasterTheme {
                ComboEffectOverlay(
                    comboType = SpecialCombinationType.COLOR_BOMB_COLOR_BOMB,
                    modifier = Modifier.size(400.dp)
                )
            }
        }

        composeTestRule.onNodeWithTag("combo_color_bomb_color_bomb", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("COSMIC CLEAR!", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testMatch3BoardRendersWithActiveCombo() {
        val board = Match3Board.createEmpty(8, 8)
        composeTestRule.setContent {
            PuzzleMasterTheme {
                Box(modifier = Modifier.size(400.dp)) {
                    Match3BoardView(
                        board = board,
                        selectedPosition = null,
                        onTileClick = {},
                        activeComboType = SpecialCombinationType.COLOR_BOMB_STRIPED,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("match3_board", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("combo_color_bomb_striped", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("STRIPE STORM!", useUnmergedTree = true).assertIsDisplayed()
    }
}
