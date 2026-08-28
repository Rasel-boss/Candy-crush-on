package com.example.game.model

/**
 * Represents an individual candy tile on the Match-3 game grid.
 *
 * @property id Unique persistent identifier for this tile instance.
 * @property type The candy type (color/flavor).
 * @property row The current 0-indexed row position.
 * @property column The current 0-indexed column position.
 * @property specialCandyType The special ability type of this candy (default NONE).
 */
data class CandyTile(
    val id: Long,
    val type: CandyType,
    val row: Int,
    val column: Int,
    val specialCandyType: SpecialCandyType = SpecialCandyType.NONE
) {
    val position: BoardPosition
        get() = BoardPosition(row, column)

    val isPlayable: Boolean
        get() = type.isPlayable || specialCandyType == SpecialCandyType.COLOR_BOMB

    val isSpecial: Boolean
        get() = specialCandyType.isSpecial

    val isStriped: Boolean
        get() = specialCandyType.isStriped

    val isEmpty: Boolean
        get() = type == CandyType.EMPTY && specialCandyType == SpecialCandyType.NONE
}
