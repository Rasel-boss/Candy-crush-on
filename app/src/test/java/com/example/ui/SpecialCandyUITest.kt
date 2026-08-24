package com.example.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import com.example.game.ui.components.CandyTileView
import com.example.game.ui.components.Match3BoardView
import org.junit.Assert.assertEquals
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

    @Test
    fun testStripedCandyRendersWithAccessibilityDescription() {
        val stripedTile = CandyTile(
            id = 101L,
            type = CandyType.RED,
            row = 2,
            column = 3,
            specialCandyType = SpecialCandyType.HORIZONTAL_STRIPED
        )

        composeTestRule.setContent {
            CandyTileView(
                tile = stripedTile,
                isSelected = false,
                onClick = {}
            )
        }

        composeTestRule.onNodeWithTag("candy_tile_2_3").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Red Berry candy, horizontal striped, row 3, column 4").assertIsDisplayed()
    }

    @Test
    fun testWrappedCandyRendersWithAccessibilityDescription() {
        val wrappedTile = CandyTile(
            id = 102L,
            type = CandyType.BLUE,
            row = 1,
            column = 1,
            specialCandyType = SpecialCandyType.WRAPPED
        )

        composeTestRule.setContent {
            CandyTileView(
                tile = wrappedTile,
                isSelected = false,
                onClick = {}
            )
        }

        composeTestRule.onNodeWithTag("candy_tile_1_1").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Blue Drop candy, wrapped, row 2, column 2").assertIsDisplayed()
    }

    @Test
    fun testColorBombRendersWithAccessibilityDescription() {
        val bombTile = CandyTile(
            id = 103L,
            type = CandyType.EMPTY,
            row = 0,
            column = 0,
            specialCandyType = SpecialCandyType.COLOR_BOMB
        )

        composeTestRule.setContent {
            CandyTileView(
                tile = bombTile,
                isSelected = false,
                onClick = {}
            )
        }

        composeTestRule.onNodeWithTag("candy_tile_0_0").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Color Bomb, row 1, column 1").assertIsDisplayed()
    }

    @Test
    fun testSpecialCandyCanBeSelectedAndClicked() {
        var clicked = false
        val stripedTile = CandyTile(
            id = 104L,
            type = CandyType.YELLOW,
            row = 4,
            column = 4,
            specialCandyType = SpecialCandyType.VERTICAL_STRIPED
        )

        composeTestRule.setContent {
            CandyTileView(
                tile = stripedTile,
                isSelected = true,
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithTag("candy_tile_4_4").performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun testBoardGridRendersAndPassesClicks() {
        var clickedPosition: BoardPosition? = null
        val tiles = List(4) { r ->
            List(4) { c ->
                CandyTile(id = (r * 4 + c + 1).toLong(), type = CandyType.RED, row = r, column = c)
            }
        }
        val board = Match3Board(4, 4, tiles)

        composeTestRule.setContent {
            Match3BoardView(
                board = board,
                selectedPosition = null,
                onTileClick = { pos -> clickedPosition = pos }
            )
        }

        composeTestRule.onNodeWithTag("candy_tile_0_0").performClick()
        assertEquals(BoardPosition(0, 0), clickedPosition)
    }
}

