package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object MainMenu : Screen("main_menu")
    data object Levels : Screen("levels")
    data object Game : Screen("game/{level}") {
        fun createRoute(level: Int) = "game/$level"
    }
    data object Settings : Screen("settings")
}
