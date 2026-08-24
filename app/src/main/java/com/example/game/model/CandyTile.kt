package com.example.game.model

data class CandyTile(
    val id: Long,
    val type: CandyType,
    val specialType: SpecialCandyType? = null
) {
    val isSpecial: Boolean
        get() = specialType != null

    val isColorBomb: Boolean
        get() = specialType == SpecialCandyType.COLOR_BOMB
}
