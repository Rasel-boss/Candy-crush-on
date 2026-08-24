package com.example.game.logic

import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.LevelObjective
import com.example.game.model.ObjectiveType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObjectiveManagerTest {

    @Test
    fun testCollectCandyObjectiveTracksRemovedTiles() {
        val objective = LevelObjective(
            id = "collect_red",
            type = ObjectiveType.COLLECT_CANDY,
            target = 10,
            candyType = CandyType.RED
        )

        val removedTiles = listOf(
            CandyTile(id = 1L, type = CandyType.RED, row = 0, column = 0),
            CandyTile(id = 2L, type = CandyType.RED, row = 0, column = 1),
            CandyTile(id = 3L, type = CandyType.BLUE, row = 0, column = 2),
            CandyTile(id = 4L, type = CandyType.RED, row = 0, column = 3),
            CandyTile(id = 5L, type = CandyType.GREEN, row = 0, column = 4)
        )

        val updatedObjectives = ObjectiveManager.onCandiesRemoved(
            currentObjectives = listOf(objective),
            removedTiles = removedTiles
        )

        assertEquals(1, updatedObjectives.size)
        assertEquals(3, updatedObjectives[0].currentProgress)
        assertFalse(updatedObjectives[0].isCompleted)
        assertEquals(0.3f, updatedObjectives[0].progressRatio, 0.001f)
    }

    @Test
    fun testTargetScoreObjectiveTracksCurrentScore() {
        val objective = LevelObjective(
            id = "target_score",
            type = ObjectiveType.TARGET_SCORE,
            target = 1000
        )

        val updatedObjectives = ObjectiveManager.onScoreChanged(
            currentObjectives = listOf(objective),
            currentScore = 650
        )

        assertEquals(650, updatedObjectives[0].currentProgress)
        assertFalse(updatedObjectives[0].isCompleted)
        assertEquals(0.65f, updatedObjectives[0].progressRatio, 0.001f)

        val completedObjectives = ObjectiveManager.onScoreChanged(
            currentObjectives = updatedObjectives,
            currentScore = 1200
        )

        assertEquals(1200, completedObjectives[0].currentProgress)
        assertTrue(completedObjectives[0].isCompleted)
        assertEquals(1.0f, completedObjectives[0].progressRatio, 0.001f)
    }

    @Test
    fun testMakeMatchesObjectiveTracksMatchCount() {
        val objective = LevelObjective(
            id = "matches_obj",
            type = ObjectiveType.MAKE_MATCHES,
            target = 5
        )

        val step1 = ObjectiveManager.onMatchesMade(
            currentObjectives = listOf(objective),
            matchesCount = 2
        )

        assertEquals(2, step1[0].currentProgress)
        assertFalse(step1[0].isCompleted)

        val step2 = ObjectiveManager.onMatchesMade(
            currentObjectives = step1,
            matchesCount = 3
        )

        assertEquals(5, step2[0].currentProgress)
        assertTrue(step2[0].isCompleted)
        assertTrue(ObjectiveManager.areAllObjectivesCompleted(step2))
    }

    @Test
    fun testMultipleObjectivesEvaluation() {
        val objectives = listOf(
            LevelObjective(id = "c_red", type = ObjectiveType.COLLECT_CANDY, target = 5, candyType = CandyType.RED),
            LevelObjective(id = "c_blue", type = ObjectiveType.COLLECT_CANDY, target = 5, candyType = CandyType.BLUE)
        )

        val removedTiles = listOf(
            CandyTile(id = 1L, type = CandyType.RED, row = 0, column = 0),
            CandyTile(id = 2L, type = CandyType.RED, row = 0, column = 1),
            CandyTile(id = 3L, type = CandyType.RED, row = 0, column = 2),
            CandyTile(id = 4L, type = CandyType.RED, row = 0, column = 3),
            CandyTile(id = 5L, type = CandyType.RED, row = 0, column = 4) // 5 Reds
        )

        val updated = ObjectiveManager.onCandiesRemoved(
            currentObjectives = objectives,
            removedTiles = removedTiles
        )

        assertTrue(updated[0].isCompleted)
        assertFalse(updated[1].isCompleted)
        assertFalse(ObjectiveManager.areAllObjectivesCompleted(updated))

        val moreRemoved = listOf(
            CandyTile(id = 6L, type = CandyType.BLUE, row = 1, column = 0),
            CandyTile(id = 7L, type = CandyType.BLUE, row = 1, column = 1),
            CandyTile(id = 8L, type = CandyType.BLUE, row = 1, column = 2),
            CandyTile(id = 9L, type = CandyType.BLUE, row = 1, column = 3),
            CandyTile(id = 10L, type = CandyType.BLUE, row = 1, column = 4)
        )

        val fullyCompleted = ObjectiveManager.onCandiesRemoved(
            currentObjectives = updated,
            removedTiles = moreRemoved
        )

        assertTrue(fullyCompleted[0].isCompleted)
        assertTrue(fullyCompleted[1].isCompleted)
        assertTrue(ObjectiveManager.areAllObjectivesCompleted(fullyCompleted))
    }

    @Test
    fun testLevelProviderConfigurations() {
        val level1 = LevelProvider.getLevelConfig(1)
        assertEquals(1, level1.levelNumber)
        assertEquals(30, level1.startingMoves)
        assertTrue(level1.objectives.isNotEmpty())

        val level2 = LevelProvider.getLevelConfig(2)
        assertEquals(2, level2.levelNumber)
        assertEquals(30, level2.startingMoves)

        val level3 = LevelProvider.getLevelConfig(3)
        assertEquals(3, level3.levelNumber)
        assertEquals(28, level3.startingMoves)

        // Dynamic generation for subsequent levels
        val level10 = LevelProvider.getLevelConfig(10)
        assertEquals(10, level10.levelNumber)
        assertTrue(level10.startingMoves in 15..28)
        assertTrue(level10.objectives.isNotEmpty())
    }
}
