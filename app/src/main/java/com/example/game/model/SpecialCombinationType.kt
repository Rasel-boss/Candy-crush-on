package com.example.game.model

enum class SpecialCombinationType(val displayName: String, val description: String) {
    STRIPED_PLUS_STRIPED("Striped + Striped", "Cross clearance: clears both the entire row and column"),
    STRIPED_PLUS_WRAPPED("Striped + Wrapped", "Mega beam: clears a 3-row by 3-column cross section"),
    WRAPPED_PLUS_WRAPPED("Wrapped + Wrapped", "Mega blast: detonates a massive 5x5 explosion"),
    COLOR_BOMB_PLUS_STRIPED("Color Bomb + Striped", "Transforms all candies of that color into striped candies and detonates them"),
    COLOR_BOMB_PLUS_WRAPPED("Color Bomb + Wrapped", "Double wave: clears matched color, then transforms the next prominent color into wrapped"),
    COLOR_BOMB_PLUS_COLOR_BOMB("Color Bomb + Color Bomb", "Ultimate board wipe: clears every single tile on the entire board")
}
