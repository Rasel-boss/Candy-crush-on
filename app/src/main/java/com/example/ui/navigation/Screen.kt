package com.example.ui.navigation

/**
 * Navigation destination routes for Puzzle Master.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Game : Screen("game")
    data object Levels : Screen("levels")
    data object Settings : Screen("settings")
}
