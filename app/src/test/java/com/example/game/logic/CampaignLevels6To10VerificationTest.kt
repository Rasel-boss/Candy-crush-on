package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameStatus
import com.example.game.model.LevelDifficulty
import com.example.game.model.ObjectiveType
import com.example.game.viewmodel.Match3ViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Verification test suite for Campaign Levels 6 to 10.
 *
 * Covers:
 * - Level 6 configuration, moves, objectives, difficulty
 * - Level 7 configuration, moves, objectives, difficulty
 * - Level 8 configuration, moves, objectives, difficulty
 * - Level 9 configuration, moves, objectives, difficulty
 * - Level 10 configuration, moves, objectives, difficulty
 * - Multi-objective validity and solvability
 * - Level unlocking progression (1 -> ... -> 10)
 * - Level completion and failure handling
 * - Level selection integration (all 10 levels available)
 * - Levels 1–5 regression protection (including Level 5 playability)
 */
class CampaignLevels6To10VerificationTest {

    private lateinit var viewModel: Match3ViewModel

    @Before
    fun setUp() {
        LevelProgressionManager.resetProgression()
        viewModel = Match3ViewModel()
        viewModel.stepDelayMs = 0L
    }

    @Test
    fun testLevel6Configuration() {
        val config = LevelProvider.getLevelConfig(6)
        assertEquals(6, config.levelNumber)
        assertEquals(8, config.rows)
        assertEquals(8, config.columns)
        assertEquals(28, config.startingMoves)
        assertEquals(LevelDifficulty.NORMAL, config.difficulty)
        assertEquals(2, config.objectives.size)
        assertEquals(850, config.targetScore)
        assertTrue(LevelConfigValidator.isValid(config))

        val greenObj = config.objectives.find { it.type == ObjectiveType.COLLECT_CANDY && it.candyType == CandyType.GREEN }
        assertNotNull("Green candy objective must exist", greenObj)
        assertEquals(22, greenObj?.target)

        val scoreObj = config.objectives.find { it.type == ObjectiveType.TARGET_SCORE }
        assertNotNull("Score objective must exist", scoreObj)
        assertEquals(850, scoreObj?.target)
    }

    @Test
    fun testLevel7Configuration() {
        val config = LevelProvider.getLevelConfig(7)
        assertEquals(7, config.levelNumber)
        assertEquals(8, config.rows)
        assertEquals(8, config.columns)
        assertEquals(28, config.startingMoves)
        assertEquals(LevelDifficulty.NORMAL, config.difficulty)
        assertEquals(3, config.objectives.size)
        assertEquals(1100, config.targetScore)
        assertTrue(LevelConfigValidator.isValid(config))

        val yellowObj = config.objectives.find { it.type == ObjectiveType.COLLECT_CANDY && it.candyType == CandyType.YELLOW }
        assertNotNull("Yellow candy objective must exist", yellowObj)
        assertEquals(18, yellowObj?.target)

        val redObj = config.objectives.find { it.type == ObjectiveType.COLLECT_CANDY && it.candyType == CandyType.RED }
        assertNotNull("Red candy objective must exist", redObj)
        assertEquals(18, redObj?.target)

        val scoreObj = config.objectives.find { it.type == ObjectiveType.TARGET_SCORE }
        assertNotNull("Score objective must exist", scoreObj)
        assertEquals(1100, scoreObj?.target)
    }

    @Test
    fun testLevel8Configuration() {
        val config = LevelProvider.getLevelConfig(8)
        assertEquals(8, config.levelNumber)
        assertEquals(8, config.rows)
        assertEquals(8, config.columns)
        assertEquals(27, config.startingMoves)
        assertEquals(LevelDifficulty.NORMAL, config.difficulty)
        assertEquals(3, config.objectives.size)
        assertEquals(1300, config.targetScore)
        assertTrue(LevelConfigValidator.isValid(config))

        val blueObj = config.objectives.find { it.type == ObjectiveType.COLLECT_CANDY && it.candyType == CandyType.BLUE }
        assertNotNull("Blue candy objective must exist", blueObj)
        assertEquals(20, blueObj?.target)

        val purpleObj = config.objectives.find { it.type == ObjectiveType.COLLECT_CANDY && it.candyType == CandyType.PURPLE }
        assertNotNull("Purple candy objective must exist", purpleObj)
        assertEquals(20, purpleObj?.target)

        val scoreObj = config.objectives.find { it.type == ObjectiveType.TARGET_SCORE }
        assertNotNull("Score objective must exist", scoreObj)
        assertEquals(1300, scoreObj?.target)
    }

    @Test
    fun testLevel9Configuration() {
        val config = LevelProvider.getLevelConfig(9)
        assertEquals(9, config.levelNumber)
        assertEquals(8, config.rows)
        assertEquals(8, config.columns)
        assertEquals(26, config.startingMoves)
        assertEquals(LevelDifficulty.HARD, config.difficulty)
        assertEquals(3, config.objectives.size)
        assertEquals(1500, config.targetScore)
        assertTrue(LevelConfigValidator.isValid(config))

        val greenObj = config.objectives.find { it.type == ObjectiveType.COLLECT_CANDY && it.candyType == CandyType.GREEN }
        assertNotNull("Green candy objective must exist", greenObj)
        assertEquals(22, greenObj?.target)

        val yellowObj = config.objectives.find { it.type == ObjectiveType.COLLECT_CANDY && it.candyType == CandyType.YELLOW }
        assertNotNull("Yellow candy objective must exist", yellowObj)
        assertEquals(22, yellowObj?.target)

        val scoreObj = config.objectives.find { it.type == ObjectiveType.TARGET_SCORE }
        assertNotNull("Score objective must exist", scoreObj)
        assertEquals(1500, scoreObj?.target)
    }

    @Test
    fun testLevel10Configuration() {
        val config = LevelProvider.getLevelConfig(10)
        assertEquals(10, config.levelNumber)
        assertEquals(8, config.rows)
        assertEquals(8, config.columns)
        assertEquals(28, config.startingMoves)
        assertEquals(LevelDifficulty.HARD, config.difficulty)
        assertEquals(4, config.objectives.size)
        assertEquals(1800, config.targetScore)
        assertTrue(LevelConfigValidator.isValid(config))

        val redObj = config.objectives.find { it.type == ObjectiveType.COLLECT_CANDY && it.candyType == CandyType.RED }
        assertNotNull("Red candy objective must exist", redObj)
        assertEquals(16, redObj?.target)

        val blueObj = config.objectives.find { it.type == ObjectiveType.COLLECT_CANDY && it.candyType == CandyType.BLUE }
        assertNotNull("Blue candy objective must exist", blueObj)
        assertEquals(16, blueObj?.target)

        val orangeObj = config.objectives.find { it.type == ObjectiveType.COLLECT_CANDY && it.candyType == CandyType.ORANGE }
        assertNotNull("Orange candy objective must exist", orangeObj)
        assertEquals(16, orangeObj?.target)

        val scoreObj = config.objectives.find { it.type == ObjectiveType.TARGET_SCORE }
        assertNotNull("Score objective must exist", scoreObj)
        assertEquals(1800, scoreObj?.target)
    }

    @Test
    fun testAllTenCampaignLevelsAvailable() {
        val allConfigs = LevelProvider.getAllLevelConfigs()
        assertEquals(10, allConfigs.size)

        for (i in 1..10) {
            val config = allConfigs[i - 1]
            assertEquals(i, config.levelNumber)
            assertTrue("Level $i must be valid", LevelConfigValidator.isValid(config))
            assertTrue("Level $i starting moves must be in 20..30", config.startingMoves in 20..30)
            assertTrue("Level $i must have at least 2 objectives", config.objectives.size >= 2)
            assertTrue("Level $i must be recognized within bounds", LevelProvider.hasLevel(i))
        }

        assertTrue(LevelProvider.hasNextLevel(1))
        assertTrue(LevelProvider.hasNextLevel(5))
        assertTrue(LevelProvider.hasNextLevel(9))
        assertFalse(LevelProvider.hasNextLevel(10))
    }

    @Test
    fun testSequentialUnlockingFromLevel1To10() {
        LevelProgressionManager.resetProgression()

        // Initially only level 1 is unlocked
        assertTrue(LevelProgressionManager.isLevelUnlocked(1))
        assertFalse(LevelProgressionManager.isLevelUnlocked(2))
        assertFalse(LevelProgressionManager.isLevelUnlocked(6))
        assertFalse(LevelProgressionManager.isLevelUnlocked(10))

        // Sequentially complete levels 1 through 9 and verify next level unlocks
        for (lvl in 1..9) {
            assertFalse("Level ${lvl + 1} should be locked before completing level $lvl", LevelProgressionManager.isLevelUnlocked(lvl + 1))
            LevelProgressionManager.recordLevelCompletion(lvl, 1000 * lvl)
            assertTrue("Level $lvl should be marked completed", LevelProgressionManager.isLevelCompleted(lvl))
            assertTrue("Level ${lvl + 1} should be unlocked after completing level $lvl", LevelProgressionManager.isLevelUnlocked(lvl + 1))
        }

        // Complete level 10
        LevelProgressionManager.recordLevelCompletion(10, 15000)
        assertTrue(LevelProgressionManager.isLevelCompleted(10))
        assertEquals(10, LevelProgressionManager.getCompletedLevels().size)
    }

    @Test
    fun testLevel10CompletionFlowInObjectiveManager() {
        val config = LevelProvider.getLevelConfig(10)
        var objectives = config.objectives

        assertFalse(ObjectiveManager.areAllObjectivesCompleted(objectives))

        // Collect 16 Red candies
        val redTiles = List(16) { CandyTile(id = it.toLong(), type = CandyType.RED, row = it % 8, column = it / 8) }
        objectives = ObjectiveManager.onCandiesRemoved(objectives, redTiles)
        val redObj = objectives.find { it.candyType == CandyType.RED }
        assertTrue(redObj?.isCompleted == true)
        assertFalse(ObjectiveManager.areAllObjectivesCompleted(objectives))

        // Collect 16 Blue candies
        val blueTiles = List(16) { CandyTile(id = (100 + it).toLong(), type = CandyType.BLUE, row = it % 8, column = it / 8) }
        objectives = ObjectiveManager.onCandiesRemoved(objectives, blueTiles)
        val blueObj = objectives.find { it.candyType == CandyType.BLUE }
        assertTrue(blueObj?.isCompleted == true)
        assertFalse(ObjectiveManager.areAllObjectivesCompleted(objectives))

        // Collect 16 Orange candies
        val orangeTiles = List(16) { CandyTile(id = (200 + it).toLong(), type = CandyType.ORANGE, row = it % 8, column = it / 8) }
        objectives = ObjectiveManager.onCandiesRemoved(objectives, orangeTiles)
        val orangeObj = objectives.find { it.candyType == CandyType.ORANGE }
        assertTrue(orangeObj?.isCompleted == true)
        assertFalse(ObjectiveManager.areAllObjectivesCompleted(objectives))

        // Reach target score 1800
        objectives = ObjectiveManager.onScoreChanged(objectives, 1850)
        val scoreObj = objectives.find { it.type == ObjectiveType.TARGET_SCORE }
        assertTrue(scoreObj?.isCompleted == true)

        // All 4 objectives completed
        assertTrue(ObjectiveManager.areAllObjectivesCompleted(objectives))
    }

    @Test
    fun testViewModelStartsAllLevels6To10Correctly() {
        for (lvl in 6..10) {
            viewModel.startGame(level = lvl, random = Random(lvl * 17))
            val state = viewModel.gameState.value
            assertEquals(lvl, state.level)
            assertEquals(GameStatus.PLAYING, state.status)
            assertEquals(0, state.score)
            assertFalse(state.isGameOver)
            assertFalse(state.isLevelCompleted)
            assertNotNull(state.board)
            assertEquals(8, state.board.rows)
            assertEquals(8, state.board.columns)

            val config = LevelProvider.getLevelConfig(lvl)
            assertEquals(config.startingMoves, state.movesRemaining)
            assertEquals(config.objectives.size, state.objectives.size)
        }
    }

    @Test
    fun testLevels1To5RegressionProtection() {
        // Level 1
        val lvl1 = LevelProvider.getLevelConfig(1)
        assertEquals(1, lvl1.levelNumber)
        assertEquals(30, lvl1.startingMoves)
        assertEquals(LevelDifficulty.EASY, lvl1.difficulty)
        assertEquals(500, lvl1.targetScore)

        // Level 2
        val lvl2 = LevelProvider.getLevelConfig(2)
        assertEquals(2, lvl2.levelNumber)
        assertEquals(30, lvl2.startingMoves)
        assertEquals(LevelDifficulty.NORMAL, lvl2.difficulty)
        assertEquals(750, lvl2.targetScore)

        // Level 3
        val lvl3 = LevelProvider.getLevelConfig(3)
        assertEquals(3, lvl3.levelNumber)
        assertEquals(28, lvl3.startingMoves)
        assertEquals(LevelDifficulty.NORMAL, lvl3.difficulty)
        assertEquals(1000, lvl3.targetScore)

        // Level 4
        val lvl4 = LevelProvider.getLevelConfig(4)
        assertEquals(4, lvl4.levelNumber)
        assertEquals(25, lvl4.startingMoves)
        assertEquals(LevelDifficulty.HARD, lvl4.difficulty)
        assertEquals(1500, lvl4.targetScore)

        // Level 5 (Playability preservation)
        val lvl5 = LevelProvider.getLevelConfig(5)
        assertEquals(5, lvl5.levelNumber)
        assertEquals(22, lvl5.startingMoves)
        assertEquals(LevelDifficulty.EXPERT, lvl5.difficulty)
        assertEquals(1600, lvl5.targetScore)
        assertEquals(3, lvl5.objectives.size)
        assertEquals(18, lvl5.objectives.find { it.candyType == CandyType.RED }?.target)
        assertEquals(18, lvl5.objectives.find { it.candyType == CandyType.BLUE }?.target)
    }
}
