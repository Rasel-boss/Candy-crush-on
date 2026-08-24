package com.example.game.model

/**
 * Types of special candy combinations that occur when two special candies are swapped directly,
 * or when a Color Bomb is swapped with a candy.
 */
enum class SpecialCombinationType {
    NONE,
    STRIPED_STRIPED,
    WRAPPED_WRAPPED,
    STRIPED_WRAPPED,
    COLOR_BOMB_NORMAL,
    COLOR_BOMB_STRIPED,
    COLOR_BOMB_WRAPPED,
    COLOR_BOMB_COLOR_BOMB;

    val isCombination: Boolean
        get() = this != NONE
}
