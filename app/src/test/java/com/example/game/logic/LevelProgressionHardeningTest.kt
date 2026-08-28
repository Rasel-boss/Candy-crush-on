package com.example.game.logic

import com.example.game.model.CandyType
import com.example.game.model.LevelConfig
import com.example.game.model.LevelObjective
import com.example.game.model.ObjectiveType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Deterministic regression test suite for Prompt 11 verifying Level 1-3 configuration,
 * validator stability, objective definitions, and level progression rules.
 */
class LevelProgressionHardeningTest {

    @Test
    fun testLevel1ConfigurationExistsAndIsValid() {
        val config = LevelProvider.getLevelConfig(1)
        assertNotNull("Level 1 config must not be null", config)
        assertEquals(1, config.levelNumber)
        assertTrue("Level 1 config must be valid", config.isValid())
        assertTrue(LevelConfigValidator.isValid(config))
    }

    @Test
    fun testLevel2ConfigurationExistsAndIsValid() {
        val config = LevelProvider.getLevelConfig(2)
        assertNotNull("Level 2 config must not be null", config)
        assertEquals(2, config.levelNumber)
        assertTrue("Level 2 config must be valid", config.isValid())
        assertTrue(LevelConfigValidator.isValid(config))
    }

    @Test
    fun testLevel3ConfigurationExistsAndIsValid() {
        val config = LevelProvider.getLevelConfig(3)
        assertNotNull("Level 3 config must not be null", config)
        assertEquals(3, config.levelNumber)
        assertTrue("Level 3 config must be valid", config.isValid())
        assertTrue(LevelConfigValidator.isValid(config))
    }

    @Test
    fun testLevel1HasValidObjectiveAndMoves() {
        val config = LevelProvider.createLevel1()
        assertTrue("Level 1 moves must be > 0", config.startingMoves > 0)
        assertEquals(30, config.startingMoves)
        assertFalse("Level 1 objectives must not be empty", config.objectives.isEmpty())
        val redObj = config.objectives.find { it.type == ObjectiveType.COLLECT_CANDY }
        assertNotNull("Level 1 must have a COLLECT_CANDY objective", redObj)
        assertEquals(CandyType.RED, redObj?.candyType)
        assertEquals(20, redObj?.target)
        val scoreObj = config.objectives.find { it.type == ObjectiveType.TARGET_SCORE }
        assertNotNull("Level 1 must have a TARGET_SCORE objective", scoreObj)
        assertEquals(500, scoreObj?.target)
    }

    @Test
    fun testLevel2HasValidObjectiveAndMoves() {
        val config = LevelProvider.createLevel2()
        assertTrue("Level 2 moves must be > 0", config.startingMoves > 0)
        assertEquals(30, config.startingMoves)
        assertFalse("Level 2 objectives must not be empty", config.objectives.isEmpty())
        val blueObj = config.objectives.find { it.type == ObjectiveType.COLLECT_CANDY }
        assertNotNull("Level 2 must have a COLLECT_CANDY objective", blueObj)
        assertEquals(CandyType.BLUE, blueObj?.candyType)
        assertEquals(25, blueObj?.target)
        val scoreObj = config.objectives.find { it.type == ObjectiveType.TARGET_SCORE }
        assertNotNull("Level 2 must have a TARGET_SCORE objective", scoreObj)
        assertEquals(750, scoreObj?.target)
    }

    @Test
    fun testLevel3HasValidObjectiveAndMoves() {
        val config = LevelProvider.createLevel3()
        assertTrue("Level 3 moves must be > 0", config.startingMoves > 0)
        assertEquals(28, config.startingMoves)
        assertEquals("Level 3 must have 3 objectives", 3, config.objectives.size)
        val greenObj = config.objectives.find { it.candyType == CandyType.GREEN }
        assertNotNull("Level 3 must have green candy objective", greenObj)
        assertEquals(20, greenObj?.target)
        val yellowObj = config.objectives.find { it.candyType == CandyType.YELLOW }
        assertNotNull("Level 3 must have yellow candy objective", yellowObj)
        assertEquals(20, yellowObj?.target)
        val scoreObj = config.objectives.find { it.type == ObjectiveType.TARGET_SCORE }
        assertNotNull("Level 3 must have TARGET_SCORE objective", scoreObj)
        assertEquals(1000, scoreObj?.target)
    }

    @Test
    fun testLevelTransitionConfigurations() {
        val lvl1 = LevelProvider.getLevelConfig(1)
        val lvl2 = LevelProvider.getLevelConfig(lvl1.levelNumber + 1)
        val lvl3 = LevelProvider.getLevelConfig(lvl2.levelNumber + 1)
        val lvl4 = LevelProvider.getLevelConfig(lvl3.levelNumber + 1)

        assertEquals(1, lvl1.levelNumber)
        assertEquals(2, lvl2.levelNumber)
        assertEquals(3, lvl3.levelNumber)
        assertEquals(4, lvl4.levelNumber)
        assertTrue(lvl4.isValid())
    }

    @Test
    fun testLevelConfigValidatorDetectsInvalidConfigurations() {
        val nullConfigResult = LevelConfigValidator.validate(null)
        assertTrue(nullConfigResult is ValidationResult.Invalid)

        val invalidMoves = LevelConfig(
            levelNumber = 1,
            startingMoves = 0,
            objectives = listOf(LevelObjective("obj", ObjectiveType.TARGET_SCORE, 100))
        )
        assertFalse(LevelConfigValidator.isValid(invalidMoves))

        val emptyObjectives = LevelConfig(
            levelNumber = 1,
            startingMoves = 20,
            objectives = emptyList()
        )
        assertFalse(LevelConfigValidator.isValid(emptyObjectives))

        val unplayableCandy = LevelConfig(
            levelNumber = 1,
            startingMoves = 20,
            objectives = listOf(
                LevelObjective("obj", ObjectiveType.COLLECT_CANDY, 10, candyType = CandyType.EMPTY)
            )
        )
        assertFalse(LevelConfigValidator.isValid(unplayableCandy))

        val zeroTargetObjective = LevelConfig(
            levelNumber = 1,
            startingMoves = 20,
            objectives = listOf(
                LevelObjective("obj", ObjectiveType.TARGET_SCORE, 0)
            )
        )
        assertFalse(LevelConfigValidator.isValid(zeroTargetObjective))
    }

    @Test
    fun testFallbackForInvalidOrNegativeLevelNumbers() {
        val negativeConfig = LevelProvider.getLevelConfig(-5)
        assertEquals("Negative level must safely fallback to Level 1", 1, negativeConfig.levelNumber)
        assertTrue(negativeConfig.isValid())

        val zeroConfig = LevelProvider.getLevelConfig(0)
        assertEquals("Zero level must safely fallback to Level 1", 1, zeroConfig.levelNumber)
        assertTrue(zeroConfig.isValid())
    }
}
