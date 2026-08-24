package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.game.audio.HapticFeedbackManager
import com.example.game.audio.IHapticFeedbackManager
import com.example.game.audio.ISoundManager
import com.example.game.audio.SoundManager
import com.example.game.viewmodel.Match3ViewModel
import com.example.game.viewmodel.SettingsViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LevelsScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.SettingsScreen

/**
 * Top-level Navigation Host managing app screen transitions, settings state,
 * game initialization, and audio/haptic feedback integration.
 */
@Composable
fun PuzzleNavHost(
    match3ViewModel: Match3ViewModel,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(),
    soundManager: ISoundManager? = null,
    hapticFeedbackManager: IHapticFeedbackManager? = null,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    val resolvedSoundManager = soundManager ?: remember(settingsViewModel) {
        SoundManager(
            soundEnabledSupplier = { settingsViewModel.settingsState.value.soundEnabled }
        )
    }

    val resolvedHapticManager = hapticFeedbackManager ?: remember(context, settingsViewModel) {
        HapticFeedbackManager(
            context = context,
            vibrationEnabledSupplier = { settingsViewModel.settingsState.value.vibrationEnabled }
        )
    }

    // Release audio resources when Navigation Host leaves composition
    DisposableEffect(resolvedSoundManager) {
        onDispose {
            resolvedSoundManager.release()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            MainMenuScreen(
                onPlayClick = {
                    resolvedSoundManager.playButtonClick()
                    resolvedHapticManager.performButtonClickFeedback()
                    match3ViewModel.startGame(level = 1)
                    navController.navigate(Screen.Game.route)
                },
                onLevelsClick = {
                    resolvedSoundManager.playButtonClick()
                    resolvedHapticManager.performButtonClickFeedback()
                    navController.navigate(Screen.Levels.route)
                },
                onSettingsClick = {
                    resolvedSoundManager.playButtonClick()
                    resolvedHapticManager.performButtonClickFeedback()
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                viewModel = match3ViewModel,
                soundManager = resolvedSoundManager,
                hapticFeedbackManager = resolvedHapticManager,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Levels.route) {
            LevelsScreen(
                onSelectLevel = { level ->
                    resolvedSoundManager.playButtonClick()
                    resolvedHapticManager.performButtonClickFeedback()
                    match3ViewModel.startGame(level = level)
                    navController.navigate(Screen.Game.route)
                },
                onBackClick = {
                    resolvedSoundManager.playButtonClick()
                    resolvedHapticManager.performButtonClickFeedback()
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onBackClick = {
                    resolvedSoundManager.playButtonClick()
                    resolvedHapticManager.performButtonClickFeedback()
                    navController.popBackStack()
                }
            )
        }
    }
}
