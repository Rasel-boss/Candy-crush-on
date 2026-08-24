package com.example.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.game.viewmodel.Match3ViewModel
import com.example.ui.theme.PuzzleMasterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun launchNavHost() {
        composeTestRule.setContent {
            PuzzleMasterTheme {
                val viewModel = Match3ViewModel()
                PuzzleNavHost(match3ViewModel = viewModel)
            }
        }
    }

    @Test
    fun homeScreenIsDisplayedInitially() {
        launchNavHost()

        composeTestRule.onNodeWithTag("main_menu_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("menu_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("play_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("levels_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_button").assertIsDisplayed()
    }

    @Test
    fun playButtonNavigatesToGameScreenAndBackNavigatesToHome() {
        launchNavHost()

        // Tap Play
        composeTestRule.onNodeWithTag("play_button").performClick()

        // Verify Match-3 Game Screen
        composeTestRule.onNodeWithTag("game_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("match3_board").assertIsDisplayed()
        composeTestRule.onNodeWithTag("restart_button").assertIsDisplayed()

        // Tap Back
        composeTestRule.onNodeWithTag("game_back_button").performClick()

        // Verify Home Screen
        composeTestRule.onNodeWithTag("main_menu_screen").assertIsDisplayed()
    }

    @Test
    fun levelsButtonNavigatesToLevelsScreenAndBackNavigatesToHome() {
        launchNavHost()

        // Tap Levels
        composeTestRule.onNodeWithTag("levels_button").performClick()

        // Verify Levels Screen
        composeTestRule.onNodeWithTag("levels_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("levels_screen_title").assertIsDisplayed()

        // Tap Back
        composeTestRule.onNodeWithTag("back_button").performClick()

        // Verify Home Screen
        composeTestRule.onNodeWithTag("main_menu_screen").assertIsDisplayed()
    }

    @Test
    fun settingsButtonNavigatesToSettingsScreenAndBackNavigatesToHome() {
        launchNavHost()

        // Tap Settings
        composeTestRule.onNodeWithTag("settings_button").performClick()

        // Verify Settings Screen
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_screen_title").assertIsDisplayed()

        // Tap Back
        composeTestRule.onNodeWithTag("back_button").performClick()

        // Verify Home Screen
        composeTestRule.onNodeWithTag("main_menu_screen").assertIsDisplayed()
    }
}
