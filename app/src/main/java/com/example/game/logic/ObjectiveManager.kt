package com.example.game.logic

import com.example.game.model.CandyTile
import com.example.game.model.LevelObjective
import com.example.game.model.ObjectiveType

class ObjectiveManager {

    fun updateObjectives(
        objectives: List<LevelObjective>,
        removedTiles: List<CandyTile>,
        matchesFormed: Int,
        currentScore: Int
    ): List<LevelObjective> {
        val colorCounts = mutableMapOf<com.example.game.model.CandyType, Int>()
        for (tile in removedTiles) {
            colorCounts[tile.type] = (colorCounts[tile.type] ?: 0) + 1
        }

        return objectives.map { obj ->
            when (obj.type) {
                ObjectiveType.COLLECT_CANDY -> {
                    val count = if (obj.targetCandyType != null) {
                        colorCounts[obj.targetCandyType] ?: 0
                    } else {
                        removedTiles.size
                    }
                    obj.copy(currentAmount = (obj.currentAmount + count).coerceAtMost(obj.targetAmount))
                }
                ObjectiveType.MAKE_MATCHES -> {
                    obj.copy(currentAmount = (obj.currentAmount + matchesFormed).coerceAtMost(obj.targetAmount))
                }
                ObjectiveType.TARGET_SCORE -> {
                    obj.copy(currentAmount = currentScore.coerceAtMost(obj.targetAmount))
                }
            }
        }
    }
}
