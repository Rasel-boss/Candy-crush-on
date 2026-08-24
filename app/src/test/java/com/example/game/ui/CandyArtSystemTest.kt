package com.example.game.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.SpecialCandyType
import com.example.game.ui.components.CandyCanvasArtwork
import com.example.game.ui.components.CandyTileView
import com.example.ui.theme.PuzzleMasterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CandyArtSystemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAllSixNormalCandyTypesRenderDirectly() {
        val playableCandies = CandyType.PLAYABLE_TYPES
        composeTestRule.setContent {
            PuzzleMasterTheme {
                androidx.compose.foundation.layout.Column {
                    playableCandies.forEachIndexed { index, candyType ->
                        val tile = CandyTile(id = index.toLong() + 1L, type = candyType, row = index, column = 0)
                        Box(modifier = Modifier.size(60.dp)) {
                            CandyTileView(tile = tile, isSelected = false, onClick = {})
                        }
                    }
                }
            }
        }
        for (i in playableCandies.indices) {
            composeTestRule.onNodeWithTag("candy_tile_${i}_0").assertIsDisplayed()
        }
    }

    @Test
    fun testSelectedCandyDisplaysWithHaloAura() {
        val tile = CandyTile(id = 10L, type = CandyType.RED, row = 1, column = 2)
        composeTestRule.setContent {
            PuzzleMasterTheme {
                Box(modifier = Modifier.size(60.dp)) {
                    CandyTileView(tile = tile, isSelected = true, onClick = {})
                }
            }
        }
        composeTestRule.onNodeWithTag("candy_tile_1_2").assertIsDisplayed()
    }

    @Test
    fun testHorizontalAndVerticalStripedCandiesRender() {
        val horizontalTile = CandyTile(
            id = 20L,
            type = CandyType.BLUE,
            row = 2,
            column = 2,
            specialCandyType = SpecialCandyType.HORIZONTAL_STRIPED
        )
        val verticalTile = CandyTile(
            id = 21L,
            type = CandyType.GREEN,
            row = 3,
            column = 3,
            specialCandyType = SpecialCandyType.VERTICAL_STRIPED
        )

        composeTestRule.setContent {
            PuzzleMasterTheme {
                Box(modifier = Modifier.size(60.dp)) {
                    CandyTileView(tile = horizontalTile, isSelected = false, onClick = {})
                }
                Box(modifier = Modifier.size(60.dp)) {
                    CandyTileView(tile = verticalTile, isSelected = false, onClick = {})
                }
            }
        }
        composeTestRule.onNodeWithTag("striped_horizontal", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("striped_vertical", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testWrappedAndColorBombCandiesRender() {
        val wrappedTile = CandyTile(
            id = 30L,
            type = CandyType.PURPLE,
            row = 4,
            column = 4,
            specialCandyType = SpecialCandyType.WRAPPED
        )
        val colorBombTile = CandyTile(
            id = 31L,
            type = CandyType.EMPTY,
            row = 5,
            column = 5,
            specialCandyType = SpecialCandyType.COLOR_BOMB
        )

        composeTestRule.setContent {
            PuzzleMasterTheme {
                Box(modifier = Modifier.size(60.dp)) {
                    CandyTileView(tile = wrappedTile, isSelected = false, onClick = {})
                }
                Box(modifier = Modifier.size(60.dp)) {
                    CandyTileView(tile = colorBombTile, isSelected = false, onClick = {})
                }
            }
        }
        composeTestRule.onNodeWithTag("wrapped_candy_badge", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("color_bomb_badge", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testDirectCanvasArtworkRenderingForAllSpecialVariants() {
        val specials = SpecialCandyType.entries
        composeTestRule.setContent {
            PuzzleMasterTheme {
                androidx.compose.foundation.layout.Column {
                    specials.forEach { special ->
                        Box(modifier = Modifier.size(60.dp)) {
                            CandyCanvasArtwork(
                                candyType = CandyType.YELLOW,
                                specialType = special,
                                isSelected = false
                            )
                        }
                    }
                }
            }
        }
    }
}
