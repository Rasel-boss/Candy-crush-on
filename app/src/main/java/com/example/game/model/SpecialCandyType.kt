package com.example.game.model

/**
 * Types of special power-up abilities a candy tile can possess.
 */
enum class SpecialCandyType {
    NONE,
    HORIZONTAL_STRIPED,
    VERTICAL_STRIPED,
    WRAPPED,
    COLOR_BOMB;

    val isSpecial: Boolean
        get() = this != NONE

    val isStriped: Boolean
        get() = this == HORIZONTAL_STRIPED || this == VERTICAL_STRIPED

    companion object {
        val STRIPED_HORIZONTAL: SpecialCandyType = HORIZONTAL_STRIPED
        val STRIPED_VERTICAL: SpecialCandyType = VERTICAL_STRIPED
    }
}
