package com.example.game.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.game.viewmodel.SettingsViewModel
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.PuzzleMasterTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_displaysHeaderAndSettingSwitchesInitiallyOn() {
        val viewModel = SettingsViewModel()

        composeTestRule.setContent {
            PuzzleMasterTheme {
                SettingsScreen(
                    settingsViewModel = viewModel,
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_screen_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sound_setting_item").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sound_switch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sound_switch").assertIsOn()

        composeTestRule.onNodeWithTag("vibration_setting_item").assertIsDisplayed()
        composeTestRule.onNodeWithTag("vibration_switch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("vibration_switch").assertIsOn()

        composeTestRule.onNodeWithTag("settings_back_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun settingsScreen_toggleSoundSwitch_updatesSoundSetting() {
        val viewModel = SettingsViewModel()

        composeTestRule.setContent {
            PuzzleMasterTheme {
                SettingsScreen(
                    settingsViewModel = viewModel,
                    onBackClick = {}
                )
            }
        }

        assertTrue(viewModel.settingsState.value.soundEnabled)

        // Toggle Sound Off
        composeTestRule.onNodeWithTag("sound_switch").performClick()
        assertFalse(viewModel.settingsState.value.soundEnabled)
        composeTestRule.onNodeWithTag("sound_switch").assertIsOff()

        // Toggle Sound On
        composeTestRule.onNodeWithTag("sound_switch").performClick()
        assertTrue(viewModel.settingsState.value.soundEnabled)
        composeTestRule.onNodeWithTag("sound_switch").assertIsOn()
    }

    @Test
    fun settingsScreen_toggleVibrationSwitch_updatesVibrationSetting() {
        val viewModel = SettingsViewModel()

        composeTestRule.setContent {
            PuzzleMasterTheme {
                SettingsScreen(
                    settingsViewModel = viewModel,
                    onBackClick = {}
                )
            }
        }

        assertTrue(viewModel.settingsState.value.vibrationEnabled)

        // Toggle Vibration Off
        composeTestRule.onNodeWithTag("vibration_switch").performClick()
        assertFalse(viewModel.settingsState.value.vibrationEnabled)
        composeTestRule.onNodeWithTag("vibration_switch").assertIsOff()

        // Toggle Vibration On
        composeTestRule.onNodeWithTag("vibration_switch").performClick()
        assertTrue(viewModel.settingsState.value.vibrationEnabled)
        composeTestRule.onNodeWithTag("vibration_switch").assertIsOn()
    }

    @Test
    fun settingsScreen_backButton_triggersCallback() {
        var backClicked = false
        val viewModel = SettingsViewModel()

        composeTestRule.setContent {
            PuzzleMasterTheme {
                SettingsScreen(
                    settingsViewModel = viewModel,
                    onBackClick = { backClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("back_button").performClick()
        assertTrue(backClicked)
    }
}
