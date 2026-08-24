package com.example.game.logic

import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import kotlin.random.Random

/**
 * Pure, deterministic logic for refilling empty spaces at the top of the Match-3 board with new playable candies.
 */
object BoardRefiller {

    /**
     * Refills all [CandyType.EMPTY] or non-playable slots on the [board] with newly generated playable candies.
     * Existing playable candies in their post-gravity positions remain completely untouched.
     *
     * @param board The board to refill.
     * @param random Deterministic or default random generator.
     * @param allowedTypes Playable candy types to choose from (default: all 6 playable types).
     */
    fun refillBoard(
        board: Match3Board,
        random: Random = Random.Default,
        allowedTypes: List<CandyType> = CandyType.playableCandies
    ): Match3Board {
        var nextTileId = 200000L

        val updatedTiles = board.tiles.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, tile ->
                if (tile.type.isPlayable && tile.type != CandyType.EMPTY) {
                    // Keep existing playable tile
                    tile
                } else {
                    // Spawn a new playable candy
                    val newType = allowedTypes[random.nextInt(allowedTypes.size)]
                    CandyTile(
                        id = nextTileId++,
                        type = newType,
                        row = r,
                        column = c
                    )
                }
            }
        }

        return board.copy(tiles = updatedTiles)
    }
}
