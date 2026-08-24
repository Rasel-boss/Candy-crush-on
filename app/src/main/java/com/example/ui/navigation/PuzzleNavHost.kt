package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.game.viewmodel.Match3ViewModel
import com.example.game.viewmodel.SettingsViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LevelsScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.SettingsScreen

@Composable
fun PuzzleNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    match3ViewModel: Match3ViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainMenu.route,
        modifier = modifier
    ) {
        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                onPlayClick = {
                    navController.navigate(Screen.Game.createRoute(1))
                },
                onLevelsClick = {
                    navController.navigate(Screen.Levels.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Levels.route) {
            LevelsScreen(
                onLevelSelected = { level ->
                    navController.navigate(Screen.Game.createRoute(level))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Game.route,
            arguments = listOf(navArgument("level") { type = NavType.IntType; defaultValue = 1 })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            GameScreen(
                level = level,
                viewModel = match3ViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
