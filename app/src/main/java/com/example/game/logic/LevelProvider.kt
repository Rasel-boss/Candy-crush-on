package com.example.game.logic

import com.example.game.model.CandyType
import com.example.game.model.LevelConfig
import com.example.game.model.LevelObjective
import com.example.game.model.ObjectiveType

object LevelProvider {

    fun getLevelConfig(level: Int): LevelConfig {
        return when (level) {
            1 -> LevelConfig(
                levelNumber = 1,
                maxMoves = 25,
                targetScore = 1200,
                objectives = listOf(
                    LevelObjective(ObjectiveType.COLLECT_CANDY, targetAmount = 15, targetCandyType = CandyType.RED),
                    LevelObjective(ObjectiveType.TARGET_SCORE, targetAmount = 1200)
                ),
                title = "Level 1: Sweet Beginnings"
            )
            2 -> LevelConfig(
                levelNumber = 2,
                maxMoves = 22,
                targetScore = 2000,
                objectives = listOf(
                    LevelObjective(ObjectiveType.COLLECT_CANDY, targetAmount = 18, targetCandyType = CandyType.BLUE),
                    LevelObjective(ObjectiveType.COLLECT_CANDY, targetAmount = 18, targetCandyType = CandyType.GREEN),
                    LevelObjective(ObjectiveType.TARGET_SCORE, targetAmount = 2000)
                ),
                title = "Level 2: Dual Delight"
            )
            3 -> LevelConfig(
                levelNumber = 3,
                maxMoves = 20,
                targetScore = 3000,
                objectives = listOf(
                    LevelObjective(ObjectiveType.COLLECT_CANDY, targetAmount = 20, targetCandyType = CandyType.PURPLE),
                    LevelObjective(ObjectiveType.MAKE_MATCHES, targetAmount = 12),
                    LevelObjective(ObjectiveType.TARGET_SCORE, targetAmount = 3000)
                ),
                title = "Level 3: Sugar Rush"
            )
            else -> {
                val color = CandyType.entries[(level - 1) % CandyType.entries.size]
                val moves = (20 - (level / 2)).coerceAtLeast(12)
                val target = 2500 + (level * 750)
                LevelConfig(
                    levelNumber = level,
                    maxMoves = moves,
                    targetScore = target,
                    objectives = listOf(
                        LevelObjective(ObjectiveType.COLLECT_CANDY, targetAmount = 15 + level * 2, targetCandyType = color),
                        LevelObjective(ObjectiveType.MAKE_MATCHES, targetAmount = 10 + level),
                        LevelObjective(ObjectiveType.TARGET_SCORE, targetAmount = target)
                    ),
                    title = "Level $level: Master Challenge"
                )
            }
        }
    }
}
