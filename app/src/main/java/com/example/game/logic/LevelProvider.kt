package com.example.game.logic

import com.example.game.model.CandyType
import com.example.game.model.DEFAULT_COLUMNS
import com.example.game.model.DEFAULT_ROWS
import com.example.game.model.LevelConfig
import com.example.game.model.LevelObjective
import com.example.game.model.ObjectiveType

/**
 * Data provider responsible for defining, configuring, and supplying level blueprints.
 * Enables data-driven level generation and future level extensibility.
 */
object LevelProvider {

    /**
     * Retrieves the [LevelConfig] for the requested [levelNumber].
     * If the level number exceeds configured hand-crafted levels, returns a valid
     * procedurally generated level configuration.
     */
    fun getLevelConfig(levelNumber: Int): LevelConfig {
        val safeLevel = if (levelNumber <= 0) 1 else levelNumber
        return when (safeLevel) {
            1 -> createLevel1()
            2 -> createLevel2()
            3 -> createLevel3()
            else -> createDynamicLevel(safeLevel)
        }
    }

    /**
     * Level 1:
     * Board: 8x8, Moves: 30
     * Objectives: Collect 20 Red candies, Reach 500 points.
     */
    fun createLevel1(): LevelConfig = LevelConfig(
        levelNumber = 1,
        rows = DEFAULT_ROWS,
        columns = DEFAULT_COLUMNS,
        startingMoves = 30,
        objectives = listOf(
            LevelObjective(
                id = "lvl1_obj_red",
                type = ObjectiveType.COLLECT_CANDY,
                target = 20,
                candyType = CandyType.RED
            ),
            LevelObjective(
                id = "lvl1_obj_score",
                type = ObjectiveType.TARGET_SCORE,
                target = 500
            )
        ),
        targetScore = 500
    )

    /**
     * Level 2:
     * Board: 8x8, Moves: 30
     * Objectives: Collect 25 Blue candies, Reach 750 points.
     */
    fun createLevel2(): LevelConfig = LevelConfig(
        levelNumber = 2,
        rows = DEFAULT_ROWS,
        columns = DEFAULT_COLUMNS,
        startingMoves = 30,
        objectives = listOf(
            LevelObjective(
                id = "lvl2_obj_blue",
                type = ObjectiveType.COLLECT_CANDY,
                target = 25,
                candyType = CandyType.BLUE
            ),
            LevelObjective(
                id = "lvl2_obj_score",
                type = ObjectiveType.TARGET_SCORE,
                target = 750
            )
        ),
        targetScore = 750
    )

    /**
     * Level 3:
     * Board: 8x8, Moves: 28
     * Objectives: Collect 20 Green candies, Collect 20 Yellow candies, Reach 1000 points.
     */
    fun createLevel3(): LevelConfig = LevelConfig(
        levelNumber = 3,
        rows = DEFAULT_ROWS,
        columns = DEFAULT_COLUMNS,
        startingMoves = 28,
        objectives = listOf(
            LevelObjective(
                id = "lvl3_obj_green",
                type = ObjectiveType.COLLECT_CANDY,
                target = 20,
                candyType = CandyType.GREEN
            ),
            LevelObjective(
                id = "lvl3_obj_yellow",
                type = ObjectiveType.COLLECT_CANDY,
                target = 20,
                candyType = CandyType.YELLOW
            ),
            LevelObjective(
                id = "lvl3_obj_score",
                type = ObjectiveType.TARGET_SCORE,
                target = 1000
            )
        ),
        targetScore = 1000
    )

    /**
     * Fallback and procedural level creator for Level 4+ to support future level expansions seamlessly.
     */
    private fun createDynamicLevel(levelNumber: Int): LevelConfig {
        val playableCandies = CandyType.playableCandies
        val primaryColor = playableCandies[(levelNumber - 1) % playableCandies.size]
        val secondaryColor = playableCandies[levelNumber % playableCandies.size]
        val baseScore = 1000 + (levelNumber - 3) * 250
        val targetCandies = (20 + (levelNumber - 3) * 2).coerceAtMost(40)
        val moves = (28 - (levelNumber - 3)).coerceAtLeast(22)

        val objectives = mutableListOf<LevelObjective>()
        objectives.add(
            LevelObjective(
                id = "lvl${levelNumber}_obj_primary",
                type = ObjectiveType.COLLECT_CANDY,
                target = targetCandies,
                candyType = primaryColor
            )
        )
        if (levelNumber % 2 == 0) {
            objectives.add(
                LevelObjective(
                    id = "lvl${levelNumber}_obj_secondary",
                    type = ObjectiveType.COLLECT_CANDY,
                    target = targetCandies,
                    candyType = secondaryColor
                )
            )
        }
        objectives.add(
            LevelObjective(
                id = "lvl${levelNumber}_obj_score",
                type = ObjectiveType.TARGET_SCORE,
                target = baseScore
            )
        )

        return LevelConfig(
            levelNumber = levelNumber,
            rows = DEFAULT_ROWS,
            columns = DEFAULT_COLUMNS,
            startingMoves = moves,
            objectives = objectives,
            targetScore = baseScore
        )
    }
}
