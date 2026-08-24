package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.SpecialCandyType

data class CreatedSpecialCandy(
    val position: BoardPosition,
    val tile: CandyTile
)

class SpecialCandyCreator {

    private var idCounter = 100000L

    fun checkAndCreateSpecial(
        matchGroup: MatchGroup,
        triggerPosition: BoardPosition? = null
    ): CreatedSpecialCandy? {
        val count = matchGroup.positions.size
        val targetPos = if (triggerPosition != null && triggerPosition in matchGroup.positions) {
            triggerPosition
        } else {
            matchGroup.positions.first()
        }

        return when {
            count >= 5 && !matchGroup.isTOrLShape -> {
                CreatedSpecialCandy(
                    position = targetPos,
                    tile = CandyTile(
                        id = ++idCounter,
                        type = matchGroup.type,
                        specialType = SpecialCandyType.COLOR_BOMB
                    )
                )
            }
            matchGroup.isTOrLShape || (count >= 5 && matchGroup.isHorizontal && matchGroup.isVertical) -> {
                CreatedSpecialCandy(
                    position = targetPos,
                    tile = CandyTile(
                        id = ++idCounter,
                        type = matchGroup.type,
                        specialType = SpecialCandyType.WRAPPED
                    )
                )
            }
            count == 4 -> {
                val specialType = if (matchGroup.isHorizontal) {
                    SpecialCandyType.STRIPED_VERTICAL
                } else {
                    SpecialCandyType.STRIPED_HORIZONTAL
                }
                CreatedSpecialCandy(
                    position = targetPos,
                    tile = CandyTile(
                        id = ++idCounter,
                        type = matchGroup.type,
                        specialType = specialType
                    )
                )
            }
            else -> null
        }
    }
}
