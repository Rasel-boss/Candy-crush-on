package com.example.game.logic

import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.LevelConfig
import com.example.game.model.LevelDifficulty
import com.example.game.model.LevelObjective
import com.example.game.model.Match3Board
import com.example.game.model.ObjectiveProgress
import com.example.game.model.ObjectiveType
import com.example.game.model.SpecialCandyType
import com.example.game.viewmodel.Match3ViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Comprehensive Unit Test Suite for Prompt 17:
 * - Advanced Level Objectives
 * - Multi-objective tracking & validation
 * - Level Progression & Unlocking (Level N clears -> Level N+1 unlocks)
 * - Best score retention (higher score updates, lower score does not replace)
 * - Victory & Game Over handling
 */
class LevelProgressionPrompt17Test {

    @Before
    fun setUp() {
        LevelProgressionManager.resetProgression()
    }

    @Test
    fun testLevel1To5ConfigurationsAndDifficulties() {
        val lvl1 = LevelProvider.getLevelConfig(1)
        assertEquals(1, lvl1.levelNumber)
        assertEquals(LevelDifficulty.EASY, lvl1.difficulty)
        assertEquals(30, lvl1.startingMoves)
        assertEquals(2, lvl1.objectives.size)

        val lvl2 = LevelProvider.getLevelConfig(2)
        assertEquals(2, lvl2.levelNumber)
        assertEquals(LevelDifficulty.NORMAL, lvl2.difficulty)
        assertEquals(30, lvl2.startingMoves)
        assertEquals(2, lvl2.objectives.size)

        val lvl3 = LevelProvider.getLevelConfig(3)
        assertEquals(3, lvl3.levelNumber)
        assertEquals(LevelDifficulty.NORMAL, lvl3.difficulty)
        assertEquals(28, lvl3.startingMoves)
        assertEquals(3, lvl3.objectives.size)

        val lvl4 = LevelProvider.getLevelConfig(4)
        assertEquals(4, lvl4.levelNumber)
        assertEquals(LevelDifficulty.HARD, lvl4.difficulty)
        assertEquals(25, lvl4.startingMoves)
        assertEquals(3, lvl4.objectives.size)

        val lvl5 = LevelProvider.getLevelConfig(5)
        assertEquals(5, lvl5.levelNumber)
        assertEquals(LevelDifficulty.EXPERT, lvl5.difficulty)
        assertEquals(22, lvl5.startingMoves)
        assertEquals(3, lvl5.objectives.size)
    }

    @Test
    fun testLevelConfigValidator() {
        val validConfig = LevelProvider.createLevel1()
        assertTrue(LevelConfigValidator.isValid(validConfig))

        // Null config
        assertFalse(LevelConfigValidator.isValid(null))

        // Invalid moves
        val zeroMoves = validConfig.copy(startingMoves = 0)
        assertFalse(LevelConfigValidator.isValid(zeroMoves))

        // Empty objectives
        val emptyObjectives = validConfig.copy(objectives = emptyList())
        assertFalse(LevelConfigValidator.isValid(emptyObjectives))

        // Negative target
        val invalidTarget = validConfig.copy(
            objectives = listOf(
                LevelObjective(
                    id = "invalid_obj",
                    type = ObjectiveType.COLLECT_CANDY,
                    target = -5,
                    candyType = CandyType.RED
                )
            )
        )
        assertFalse(LevelConfigValidator.isValid(invalidTarget))
    }

    @Test
    fun testObjectiveProgressEncapsulation() {
        val progress = ObjectiveProgress(current = 15, target = 20)
        assertEquals(15, progress.current)
        assertEquals(20, progress.target)
        assertEquals(5, progress.target - progress.displayCurrent)
        assertFalse(progress.completed)
        assertEquals(0.75f, progress.progressRatio, 0.001f)
        assertEquals("15 / 20", progress.formattedDisplay)

        // Progress exceeds target
        val overProgress = ObjectiveProgress(current = 25, target = 20)
        assertTrue(overProgress.completed)
        assertEquals(20, overProgress.displayCurrent)
        assertEquals(1.0f, overProgress.progressRatio, 0.001f)
        assertEquals("20 / 20", overProgress.formattedDisplay)
    }

    @Test
    fun testObjectiveManagerCollectCandiesAndScore() {
        val config = LevelProvider.createLevel1()
        var objectives = ObjectiveManager.initializeObjectives(config)

        assertFalse(ObjectiveManager.areAllObjectivesCompleted(objectives))

        // Remove 10 RED candies
        val removedRed = (1..10).map {
            CandyTile(id = it.toLong(), type = CandyType.RED, row = 0, column = 0, specialCandyType = SpecialCandyType.NONE)
        }
        objectives = ObjectiveManager.onCandiesRemoved(objectives, removedRed)
        val redObj = objectives.find { it.type == ObjectiveType.COLLECT_CANDY && it.candyType == CandyType.RED }
        assertEquals(10, redObj?.currentProgress)
        assertFalse(redObj?.isCompleted ?: true)

        // Remove 10 more RED candies
        objectives = ObjectiveManager.onCandiesRemoved(objectives, removedRed)
        val redObjCompleted = objectives.find { it.type == ObjectiveType.COLLECT_CANDY && it.candyType == CandyType.RED }
        assertEquals(20, redObjCompleted?.currentProgress)
        assertTrue(redObjCompleted?.isCompleted ?: false)

        // Score is still 0 so not all completed
        assertFalse(ObjectiveManager.areAllObjectivesCompleted(objectives))

        // Reach target score 500
        objectives = ObjectiveManager.onScoreChanged(objectives, 550)
        val scoreObj = objectives.find { it.type == ObjectiveType.TARGET_SCORE }
        assertTrue(scoreObj?.isCompleted ?: false)

        // Now all objectives are completed
        assertTrue(ObjectiveManager.areAllObjectivesCompleted(objectives))
    }

    @Test
    fun testLevelProgressionUnlockRules() {
        // Initial state: Level 1 unlocked, Level 2 locked
        assertTrue("Level 1 must be unlocked by default", LevelProgressionManager.isLevelUnlocked(1))
        assertFalse("Level 2 must be locked initially", LevelProgressionManager.isLevelUnlocked(2))
        assertFalse("Level 3 must be locked initially", LevelProgressionManager.isLevelUnlocked(3))

        assertEquals(LevelStatus.UNLOCKED, LevelProgressionManager.getLevelStatus(1))
        assertEquals(LevelStatus.LOCKED, LevelProgressionManager.getLevelStatus(2))

        // Complete Level 1 with score 800
        val isNewBest = LevelProgressionManager.recordLevelCompletion(1, 800)
        assertTrue("First completion establishes new best", isNewBest)
        assertTrue("Level 1 is now completed", LevelProgressionManager.isLevelCompleted(1))
        assertEquals(800, LevelProgressionManager.getBestScore(1))

        // Level 2 should now be unlocked!
        assertTrue("Level 2 must be unlocked after clearing Level 1", LevelProgressionManager.isLevelUnlocked(2))
        assertEquals(LevelStatus.UNLOCKED, LevelProgressionManager.getLevelStatus(2))

        // Complete Level 2 with score 1200
        LevelProgressionManager.recordLevelCompletion(2, 1200)
        assertTrue("Level 2 is now completed", LevelProgressionManager.isLevelCompleted(2))
        assertTrue("Level 3 must be unlocked after clearing Level 2", LevelProgressionManager.isLevelUnlocked(3))
    }

    @Test
    fun testBestScoreUpdateAndPreservation() {
        // Clear Level 1 with 1000 pts
        LevelProgressionManager.recordLevelCompletion(1, 1000)
        assertEquals(1000, LevelProgressionManager.getBestScore(1))

        // Replay Level 1 with a higher score: 1500 pts
        val higher = LevelProgressionManager.recordLevelCompletion(1, 1500)
        assertTrue("Higher score must establish new best", higher)
        assertEquals(1500, LevelProgressionManager.getBestScore(1))

        // Replay Level 1 with a lower score: 900 pts
        val lower = LevelProgressionManager.recordLevelCompletion(1, 900)
        assertFalse("Lower score must NOT replace best score", lower)
        assertEquals("Best score must remain 1500", 1500, LevelProgressionManager.getBestScore(1))
    }

    @Test
    fun testLockedLevelCannotStartInViewModel() {
        val vm = Match3ViewModel()

        // Attempting to start locked level 3 should be rejected
        val started = vm.startLevelIfUnlocked(level = 3)
        assertFalse("Locked level 3 cannot start", started)

        // Starting unlocked level 1 should succeed
        val startedLevel1 = vm.startLevelIfUnlocked(level = 1)
        assertTrue("Unlocked level 1 starts successfully", startedLevel1)
        assertEquals(1, vm.gameState.value.level)
        assertEquals(GameStatus.PLAYING, vm.gameState.value.status)
    }

    @Test
    fun testViewModelFullLevelProgressionLifecycle() {
        val vm = Match3ViewModel()
        vm.startGame(level = 1)

        // Artificially satisfy level 1 objectives: Red candies = 20, score = 500
        var objectives = vm.gameState.value.objectives
        val twentyReds = (1..20).map {
            CandyTile(id = it.toLong(), type = CandyType.RED, row = 0, column = 0, specialCandyType = SpecialCandyType.NONE)
        }
        objectives = ObjectiveManager.onCandiesRemoved(objectives, twentyReds)
        objectives = ObjectiveManager.onScoreChanged(objectives, 600)

        assertTrue(ObjectiveManager.areAllObjectivesCompleted(objectives))

        // Set custom state with satisfied objectives
        val completedState = vm.gameState.value.copy(
            score = 600,
            objectives = objectives,
            movesRemaining = 10
        )
        vm.setCustomState(completedState)

        // Run synchronous resolution to trigger victory completion
        vm.resolveCascadesSynchronously(Random(42))

        assertEquals(GameStatus.COMPLETED, vm.gameState.value.status)
        assertTrue(vm.gameState.value.isLevelCompleted)

        // LevelProgressionManager should now reflect Level 1 completed and Level 2 unlocked
        assertTrue(LevelProgressionManager.isLevelCompleted(1))
        assertTrue(LevelProgressionManager.isLevelUnlocked(2))

        // Advancing to next level via ViewModel nextLevel() should start Level 2
        val nextStarted = vm.nextLevel()
        assertTrue(nextStarted)
        assertEquals(2, vm.gameState.value.level)
        assertEquals(GameStatus.PLAYING, vm.gameState.value.status)
    }

    @Test
    fun testGameOverWhenMovesDepletedWithoutObjectivesMet() {
        val vm = Match3ViewModel()
        vm.startGame(level = 1)

        // State with 0 moves and incomplete objectives
        val zeroMovesState = vm.gameState.value.copy(
            movesRemaining = 0,
            score = 100
        )
        vm.setCustomState(zeroMovesState)
        vm.resolveCascadesSynchronously(Random(42))

        assertEquals(GameStatus.GAME_OVER, vm.gameState.value.status)
        assertTrue(vm.gameState.value.isGameOver)
        assertFalse(vm.gameState.value.isLevelCompleted)
        assertFalse(LevelProgressionManager.isLevelCompleted(1))
    }
}
