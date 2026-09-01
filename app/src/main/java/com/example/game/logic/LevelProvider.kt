package com.example.game.logic

import com.example.game.model.CandyType
import com.example.game.model.DEFAULT_COLUMNS
import com.example.game.model.DEFAULT_ROWS
import com.example.game.model.LevelConfig
import com.example.game.model.LevelDifficulty
import com.example.game.model.LevelObjective
import com.example.game.model.ObjectiveType

/**
 * Data provider responsible for defining, configuring, and supplying level blueprints.
 * Enables data-driven level generation, difficulties, and future level extensibility.
 */
object LevelProvider {

    /** Maximum hand-crafted campaign levels currently available */
    const val MAX_CAMPAIGN_LEVELS = 10

    /**
     * Retrieves the [LevelConfig] for the requested [levelNumber].
     * If the level number exceeds configured hand-crafted levels, returns a valid
     * procedurally generated level configuration. If any error or invalid state is detected,
     * safely falls back to a guaranteed valid level 1 configuration.
     */
    fun getLevelConfig(levelNumber: Int): LevelConfig {
        val safeLevel = if (levelNumber <= 0) 1 else levelNumber
        val config = when (safeLevel) {
            1 -> createLevel1()
            2 -> createLevel2()
            3 -> createLevel3()
            4 -> createLevel4()
            5 -> createLevel5()
            6 -> createLevel6()
            7 -> createLevel7()
            8 -> createLevel8()
            9 -> createLevel9()
            10 -> createLevel10()
            else -> createDynamicLevel(safeLevel)
        }
        return if (LevelConfigValidator.isValid(config)) {
            config
        } else {
            createLevel1()
        }
    }

    /**
     * Returns a list of all pre-configured campaign levels.
     */
    fun getAllLevelConfigs(): List<LevelConfig> {
        return (1..MAX_CAMPAIGN_LEVELS).map { getLevelConfig(it) }
    }

    /**
     * Whether a next level is available in the campaign.
     */
    fun hasNextLevel(currentLevel: Int): Boolean {
        return currentLevel < MAX_CAMPAIGN_LEVELS
    }

    /**
     * Whether a specific level number is within the available campaign bounds.
     */
    fun hasLevel(levelNumber: Int): Boolean {
        return levelNumber in 1..MAX_CAMPAIGN_LEVELS
    }

    /**
     * Level 1:
     * Difficulty: Easy
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
        targetScore = 500,
        difficulty = LevelDifficulty.EASY
    )

    /**
     * Level 2:
     * Difficulty: Normal
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
        targetScore = 750,
        difficulty = LevelDifficulty.NORMAL
    )

    /**
     * Level 3:
     * Difficulty: Normal
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
        targetScore = 1000,
        difficulty = LevelDifficulty.NORMAL
    )

    /**
     * Level 4:
     * Difficulty: Hard
     * Board: 8x8, Moves: 25
     * Objectives: Collect 25 Purple candies, Collect 25 Orange candies, Reach 1500 points.
     */
    fun createLevel4(): LevelConfig = LevelConfig(
        levelNumber = 4,
        rows = DEFAULT_ROWS,
        columns = DEFAULT_COLUMNS,
        startingMoves = 25,
        objectives = listOf(
            LevelObjective(
                id = "lvl4_obj_purple",
                type = ObjectiveType.COLLECT_CANDY,
                target = 25,
                candyType = CandyType.PURPLE
            ),
            LevelObjective(
                id = "lvl4_obj_orange",
                type = ObjectiveType.COLLECT_CANDY,
                target = 25,
                candyType = CandyType.ORANGE
            ),
            LevelObjective(
                id = "lvl4_obj_score",
                type = ObjectiveType.TARGET_SCORE,
                target = 1500
            )
        ),
        targetScore = 1500,
        difficulty = LevelDifficulty.HARD
    )

    /**
     * Level 5:
     * Difficulty: Expert
     * Board: 8x8, Moves: 22
     * Objectives: Collect 18 Red candies, Collect 18 Blue candies, Reach 1600 points.
     */
    fun createLevel5(): LevelConfig = LevelConfig(
        levelNumber = 5,
        rows = DEFAULT_ROWS,
        columns = DEFAULT_COLUMNS,
        startingMoves = 22,
        objectives = listOf(
            LevelObjective(
                id = "lvl5_obj_red",
                type = ObjectiveType.COLLECT_CANDY,
                target = 18,
                candyType = CandyType.RED
            ),
            LevelObjective(
                id = "lvl5_obj_blue",
                type = ObjectiveType.COLLECT_CANDY,
                target = 18,
                candyType = CandyType.BLUE
            ),
            LevelObjective(
                id = "lvl5_obj_score",
                type = ObjectiveType.TARGET_SCORE,
                target = 1600
            )
        ),
        targetScore = 1600,
        difficulty = LevelDifficulty.EXPERT
    )

    /**
     * Level 6:
     * Difficulty: Easy/Medium (NORMAL)
     * Board: 8x8, Moves: 28
     * Objectives: Collect 22 Green candies, Reach 850 points.
     */
    fun createLevel6(): LevelConfig = LevelConfig(
        levelNumber = 6,
        rows = DEFAULT_ROWS,
        columns = DEFAULT_COLUMNS,
        startingMoves = 28,
        objectives = listOf(
            LevelObjective(
                id = "lvl6_obj_green",
                type = ObjectiveType.COLLECT_CANDY,
                target = 22,
                candyType = CandyType.GREEN
            ),
            LevelObjective(
                id = "lvl6_obj_score",
                type = ObjectiveType.TARGET_SCORE,
                target = 850
            )
        ),
        targetScore = 850,
        difficulty = LevelDifficulty.NORMAL
    )

    /**
     * Level 7:
     * Difficulty: Easy/Medium+ (NORMAL)
     * Board: 8x8, Moves: 28
     * Objectives: Collect 18 Yellow candies, Collect 18 Red candies, Reach 1100 points.
     */
    fun createLevel7(): LevelConfig = LevelConfig(
        levelNumber = 7,
        rows = DEFAULT_ROWS,
        columns = DEFAULT_COLUMNS,
        startingMoves = 28,
        objectives = listOf(
            LevelObjective(
                id = "lvl7_obj_yellow",
                type = ObjectiveType.COLLECT_CANDY,
                target = 18,
                candyType = CandyType.YELLOW
            ),
            LevelObjective(
                id = "lvl7_obj_red",
                type = ObjectiveType.COLLECT_CANDY,
                target = 18,
                candyType = CandyType.RED
            ),
            LevelObjective(
                id = "lvl7_obj_score",
                type = ObjectiveType.TARGET_SCORE,
                target = 1100
            )
        ),
        targetScore = 1100,
        difficulty = LevelDifficulty.NORMAL
    )

    /**
     * Level 8:
     * Difficulty: Medium (NORMAL)
     * Board: 8x8, Moves: 27
     * Objectives: Collect 20 Blue candies, Collect 20 Purple candies, Reach 1300 points.
     */
    fun createLevel8(): LevelConfig = LevelConfig(
        levelNumber = 8,
        rows = DEFAULT_ROWS,
        columns = DEFAULT_COLUMNS,
        startingMoves = 27,
        objectives = listOf(
            LevelObjective(
                id = "lvl8_obj_blue",
                type = ObjectiveType.COLLECT_CANDY,
                target = 20,
                candyType = CandyType.BLUE
            ),
            LevelObjective(
                id = "lvl8_obj_purple",
                type = ObjectiveType.COLLECT_CANDY,
                target = 20,
                candyType = CandyType.PURPLE
            ),
            LevelObjective(
                id = "lvl8_obj_score",
                type = ObjectiveType.TARGET_SCORE,
                target = 1300
            )
        ),
        targetScore = 1300,
        difficulty = LevelDifficulty.NORMAL
    )

    /**
     * Level 9:
     * Difficulty: Medium+ (HARD)
     * Board: 8x8, Moves: 26
     * Objectives: Collect 22 Green candies, Collect 22 Yellow candies, Reach 1500 points.
     */
    fun createLevel9(): LevelConfig = LevelConfig(
        levelNumber = 9,
        rows = DEFAULT_ROWS,
        columns = DEFAULT_COLUMNS,
        startingMoves = 26,
        objectives = listOf(
            LevelObjective(
                id = "lvl9_obj_green",
                type = ObjectiveType.COLLECT_CANDY,
                target = 22,
                candyType = CandyType.GREEN
            ),
            LevelObjective(
                id = "lvl9_obj_yellow",
                type = ObjectiveType.COLLECT_CANDY,
                target = 22,
                candyType = CandyType.YELLOW
            ),
            LevelObjective(
                id = "lvl9_obj_score",
                type = ObjectiveType.TARGET_SCORE,
                target = 1500
            )
        ),
        targetScore = 1500,
        difficulty = LevelDifficulty.HARD
    )

    /**
     * Level 10:
     * Difficulty: Medium/Hard (HARD)
     * Board: 8x8, Moves: 28
     * Objectives: Collect 16 Red candies, Collect 16 Blue candies, Collect 16 Orange candies, Reach 1800 points.
     */
    fun createLevel10(): LevelConfig = LevelConfig(
        levelNumber = 10,
        rows = DEFAULT_ROWS,
        columns = DEFAULT_COLUMNS,
        startingMoves = 28,
        objectives = listOf(
            LevelObjective(
                id = "lvl10_obj_red",
                type = ObjectiveType.COLLECT_CANDY,
                target = 16,
                candyType = CandyType.RED
            ),
            LevelObjective(
                id = "lvl10_obj_blue",
                type = ObjectiveType.COLLECT_CANDY,
                target = 16,
                candyType = CandyType.BLUE
            ),
            LevelObjective(
                id = "lvl10_obj_orange",
                type = ObjectiveType.COLLECT_CANDY,
                target = 16,
                candyType = CandyType.ORANGE
            ),
            LevelObjective(
                id = "lvl10_obj_score",
                type = ObjectiveType.TARGET_SCORE,
                target = 1800
            )
        ),
        targetScore = 1800,
        difficulty = LevelDifficulty.HARD
    )

    /**
     * Fallback and procedural level creator for Level 11+ to support future level expansions seamlessly.
     */
    private fun createDynamicLevel(levelNumber: Int): LevelConfig {
        val playableCandies = CandyType.playableCandies
        val primaryColor = playableCandies[(levelNumber - 1) % playableCandies.size]
        val secondaryColor = playableCandies[levelNumber % playableCandies.size]
        val baseScore = 1000 + (levelNumber - 3) * 250
        val targetCandies = (20 + (levelNumber - 3) * 2).coerceAtMost(40)
        val moves = (28 - (levelNumber - 3)).coerceAtLeast(20)

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
            targetScore = baseScore,
            difficulty = if (levelNumber >= 5) LevelDifficulty.EXPERT else LevelDifficulty.HARD
        )
    }
}
