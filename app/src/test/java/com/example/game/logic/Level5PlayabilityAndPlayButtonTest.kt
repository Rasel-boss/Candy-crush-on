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
import org.junit.Test
import kotlin.random.Random

/**
 * Verification test for Level 5 Playability and Campaign progression preservation.
 */
class Level5PlayabilityAndPlayButtonTest {

    @Test
    fun testLevel5ConfigurationIntegrity() {
        val lvl5 = LevelProvider.getLevelConfig(5)
        assertEquals(5, lvl5.levelNumber)
        assertEquals(8, lvl5.rows)
        assertEquals(8, lvl5.columns)
        assertEquals(22, lvl5.startingMoves)
        assertEquals(LevelDifficulty.EXPERT, lvl5.difficulty)
        assertEquals(3, lvl5.objectives.size)

        val redObj = lvl5.objectives.find { it.type == ObjectiveType.COLLECT_CANDY && it.candyType == CandyType.RED }
        assertNotNull(redObj)
        assertEquals(18, redObj?.target)

        val blueObj = lvl5.objectives.find { it.type == ObjectiveType.COLLECT_CANDY && it.candyType == CandyType.BLUE }
        assertNotNull(blueObj)
        assertEquals(18, blueObj?.target)

        val scoreObj = lvl5.objectives.find { it.type == ObjectiveType.TARGET_SCORE }
        assertNotNull(scoreObj)
        assertEquals(1600, scoreObj?.target)
        assertEquals(1600, lvl5.targetScore)
    }

    @Test
    fun testLevel5ObjectivesProgressionAndCompletion() {
        val config = LevelProvider.getLevelConfig(5)
        var objectives = config.objectives

        assertFalse(ObjectiveManager.areAllObjectivesCompleted(objectives))

        // Simulate collecting 18 red candies
        val redTiles = List(18) { index ->
            CandyTile(id = index.toLong(), type = CandyType.RED, row = index % 8, column = index / 8)
        }
        objectives = ObjectiveManager.onCandiesRemoved(objectives, redTiles)

        val redObj = objectives.find { it.candyType == CandyType.RED }
        assertTrue(redObj?.isCompleted == true)
        assertEquals(18, redObj?.currentProgress)

        // Blue and score not completed yet
        assertFalse(ObjectiveManager.areAllObjectivesCompleted(objectives))

        // Simulate collecting 18 blue candies
        val blueTiles = List(18) { index ->
            CandyTile(id = (100 + index).toLong(), type = CandyType.BLUE, row = index % 8, column = index / 8)
        }
        objectives = ObjectiveManager.onCandiesRemoved(objectives, blueTiles)

        val blueObj = objectives.find { it.candyType == CandyType.BLUE }
        assertTrue(blueObj?.isCompleted == true)
        assertEquals(18, blueObj?.currentProgress)

        // Reach target score
        objectives = ObjectiveManager.onScoreChanged(objectives, 1600)
        val scoreObj = objectives.find { it.type == ObjectiveType.TARGET_SCORE }
        assertTrue(scoreObj?.isCompleted == true)

        // All objectives complete
        assertTrue(ObjectiveManager.areAllObjectivesCompleted(objectives))
    }

    @Test
    fun testLevels1To4RegressionPreserved() {
        val lvl1 = LevelProvider.getLevelConfig(1)
        assertEquals(1, lvl1.levelNumber)
        assertEquals(30, lvl1.startingMoves)
        assertEquals(LevelDifficulty.EASY, lvl1.difficulty)
        assertEquals(2, lvl1.objectives.size)
        assertEquals(20, lvl1.objectives.find { it.candyType == CandyType.RED }?.target)
        assertEquals(500, lvl1.targetScore)

        val lvl2 = LevelProvider.getLevelConfig(2)
        assertEquals(2, lvl2.levelNumber)
        assertEquals(30, lvl2.startingMoves)
        assertEquals(LevelDifficulty.NORMAL, lvl2.difficulty)
        assertEquals(2, lvl2.objectives.size)
        assertEquals(25, lvl2.objectives.find { it.candyType == CandyType.BLUE }?.target)
        assertEquals(750, lvl2.targetScore)

        val lvl3 = LevelProvider.getLevelConfig(3)
        assertEquals(3, lvl3.levelNumber)
        assertEquals(28, lvl3.startingMoves)
        assertEquals(LevelDifficulty.NORMAL, lvl3.difficulty)
        assertEquals(3, lvl3.objectives.size)
        assertEquals(20, lvl3.objectives.find { it.candyType == CandyType.GREEN }?.target)
        assertEquals(20, lvl3.objectives.find { it.candyType == CandyType.YELLOW }?.target)
        assertEquals(1000, lvl3.targetScore)

        val lvl4 = LevelProvider.getLevelConfig(4)
        assertEquals(4, lvl4.levelNumber)
        assertEquals(25, lvl4.startingMoves)
        assertEquals(LevelDifficulty.HARD, lvl4.difficulty)
        assertEquals(3, lvl4.objectives.size)
        assertEquals(25, lvl4.objectives.find { it.candyType == CandyType.PURPLE }?.target)
        assertEquals(25, lvl4.objectives.find { it.candyType == CandyType.ORANGE }?.target)
        assertEquals(1500, lvl4.targetScore)
    }

    @Test
    fun testLevel5ViewModelFlow() {
        val viewModel = Match3ViewModel()
        viewModel.stepDelayMs = 0L
        viewModel.startGame(level = 5)

        val initialState = viewModel.gameState.value
        assertEquals(5, initialState.level)
        assertEquals(22, initialState.movesRemaining)
        assertEquals(LevelDifficulty.EXPERT, initialState.levelConfig?.difficulty)
        assertEquals(GameStatus.PLAYING, initialState.status)
        assertFalse(initialState.isLevelCompleted)
        assertFalse(initialState.isGameOver)
    }
}
