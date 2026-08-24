package com.example.game.model

enum class SpecialCandyType(val displayName: String, val description: String) {
    STRIPED_HORIZONTAL("Striped Horizontal", "Clears the entire horizontal row"),
    STRIPED_VERTICAL("Striped Vertical", "Clears the entire vertical column"),
    WRAPPED("Wrapped Candy", "Explodes twice in a 3x3 radius"),
    COLOR_BOMB("Color Bomb", "Clears all candies of a chosen color")
}
