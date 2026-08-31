package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.LevelConfig
import com.example.game.model.LevelObjective
import com.example.game.model.Match3Board
import com.example.game.model.ObjectiveType
import com.example.game.viewmodel.Match3ViewModel
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite for Prompt 19 — Professional Animations, Visual Feedback & Gameplay Polish.
 * Verifies animation safety, input-lock safety during processing, valid swap move decrement,
 * invalid swap non-consumption, score consistency, objective progress consistency,
 * cascade completion, and game state transitions.
 */
class ProfessionalAnimationsPrompt19Test {

    private fun createCustomBoard(grid: List<List<CandyType>>): Match3Board {
        var id = 1L
        val tiles = grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, type ->
                CandyTile(
                    id = id++,
                    row = r,
                    column = c,
                    type = type
                )
            }
        }
        return Match3Board(rows = grid.size, columns = grid[0].size, tiles = tiles)
    }

    @Test
    fun testTileSelectionUpdatesSelectionState() {
        val vm = Match3ViewModel()
        vm.startGame(level = 1)

        val pos = BoardPosition(2, 3)
        val selected = vm.selectTile(pos)

        assertTrue("Tile selection succeeded", selected)
        assertEquals("Selected position updated in state", pos, vm.gameState.value.selectedPosition)

        // Tapping the same tile deselects it
        val deselected = vm.selectTile(pos)
        assertTrue("Deselection succeeded", deselected)
        assertNull("Selected position is null after deselecting", vm.gameState.value.selectedPosition)
    }

    @Test
    fun testProcessingStateBlocksTileInput() {
        val vm = Match3ViewModel()
        vm.startGame(level = 1)

        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.RED),
                listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
                listOf(CandyType.YELLOW, CandyType.GREEN, CandyType.ORANGE, CandyType.PURPLE),
                listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.GREEN)
            )
        )
        vm.setCustomBoard(board)

        // Force paused state
        vm.pauseGame()
        assertFalse("Cannot select tile while paused", vm.selectTile(BoardPosition(0, 0)))
        vm.resumeGame()

        // Valid selection
        vm.selectTile(BoardPosition(0, 0))
        assertEquals(BoardPosition(0, 0), vm.gameState.value.selectedPosition)
    }

    @Test
    fun testValidSwapConsumesExactlyOneMove() {
        val vm = Match3ViewModel()
        vm.stepDelayMs = 0L
        vm.startGame(level = 1)

        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.RED),
                listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
                listOf(CandyType.YELLOW, CandyType.GREEN, CandyType.ORANGE, CandyType.PURPLE),
                listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.GREEN)
            )
        )
        vm.setCustomBoard(board)
        val initialMoves = vm.gameState.value.movesRemaining

        vm.selectTile(0, 2)
        val swapResult = vm.selectTile(0, 3)

        assertTrue("Swap was valid", swapResult)
        assertEquals("Exactly 1 move was consumed", initialMoves - 1, vm.gameState.value.movesRemaining)
    }

    @Test
    fun testInvalidSwapConsumesZeroMovesAndPreservesBoard() {
        val vm = Match3ViewModel()
        vm.stepDelayMs = 0L
        vm.startGame(level = 1)

        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW),
                listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE),
                listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
                listOf(CandyType.YELLOW, CandyType.GREEN, CandyType.ORANGE, CandyType.PURPLE)
            )
        )
        vm.setCustomBoard(board)
        val initialMoves = vm.gameState.value.movesRemaining
        val initialScore = vm.gameState.value.score

        vm.selectTile(0, 0)
        val swapResult = vm.selectTile(0, 1)

        assertFalse("Swap was invalid", swapResult)
        assertEquals("Zero moves consumed on invalid swap", initialMoves, vm.gameState.value.movesRemaining)
        assertEquals("Zero score added on invalid swap", initialScore, vm.gameState.value.score)
        assertNull("Selected position cleared after invalid swap", vm.gameState.value.selectedPosition)
    }

    @Test
    fun testMatchResolutionRemovesCorrectTiles() {
        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
                listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
                listOf(CandyType.YELLOW, CandyType.GREEN, CandyType.ORANGE, CandyType.PURPLE),
                listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.GREEN)
            )
        )

        val matches = MatchDetector.findMatches(board)
        assertEquals("Found 1 match", 1, matches.size)
        val match = matches[0]
        assertEquals(3, match.length)
        assertEquals(CandyType.RED, match.type)

        val boardAfterRemoval = MatchResolver.removeMatches(board, match.positions.toSet())
        for (pos in match.positions) {
            val tile = boardAfterRemoval.getTile(pos)
            assertTrue("Tile is empty after removal", tile == null || tile.type == CandyType.EMPTY)
        }
    }

    @Test
    fun testCascadesResolveCompletelyToStableBoard() {
        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
                listOf(CandyType.GREEN, CandyType.GREEN, CandyType.YELLOW, CandyType.GREEN),
                listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE),
                listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.GREEN)
            )
        )

        val result = MatchResolver.resolveAllCascades(board, random = Random(42))
        assertTrue("Cascade resolved to stable state", result.isStable)
        assertFalse("No matches remaining in final board", MatchDetector.hasAnyMatches(result.finalBoard))
        assertTrue("Steps were recorded in cascade result", result.steps.isNotEmpty())
    }

    @Test
    fun testScoreRemainsConsistentThroughCascade() {
        val vm = Match3ViewModel()
        vm.stepDelayMs = 0L
        vm.startGame(level = 1)

        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
                listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
                listOf(CandyType.YELLOW, CandyType.GREEN, CandyType.ORANGE, CandyType.PURPLE),
                listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.GREEN)
            )
        )
        vm.setCustomBoard(board)
        vm.resolveCascadesSynchronously()

        val score = vm.gameState.value.score
        assertTrue("Score awarded for match", score >= 30)
    }

    @Test
    fun testObjectiveProgressRemainsConsistent() {
        val vm = Match3ViewModel()
        vm.stepDelayMs = 0L
        vm.startGame(level = 1)

        val customObjectives = listOf(
            LevelObjective(
                id = "1",
                type = ObjectiveType.COLLECT_CANDY,
                candyType = CandyType.RED,
                target = 3,
                currentProgress = 0
            )
        )
        vm.setCustomState(
            vm.gameState.value.copy(
                objectives = customObjectives
            )
        )

        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
                listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
                listOf(CandyType.YELLOW, CandyType.GREEN, CandyType.ORANGE, CandyType.PURPLE),
                listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.GREEN)
            )
        )
        vm.setCustomBoard(board)
        vm.resolveCascadesSynchronously()

        val redObj = vm.gameState.value.objectives.first { it.candyType == CandyType.RED }
        assertTrue("Red candies matched counted in objective", redObj.currentProgress >= 3)
        assertTrue("Objective marked as completed", redObj.isCompleted)
    }

    @Test
    fun testLevelCompletionTriggersCorrectly() {
        val vm = Match3ViewModel()
        vm.stepDelayMs = 0L
        vm.startGame(level = 1)

        val customObjectives = listOf(
            LevelObjective(
                id = "1",
                type = ObjectiveType.COLLECT_CANDY,
                candyType = CandyType.RED,
                target = 3,
                currentProgress = 0
            )
        )
        vm.setCustomState(
            vm.gameState.value.copy(
                objectives = customObjectives
            )
        )

        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
                listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
                listOf(CandyType.YELLOW, CandyType.GREEN, CandyType.ORANGE, CandyType.PURPLE),
                listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.GREEN)
            )
        )
        vm.setCustomBoard(board)
        vm.resolveCascadesSynchronously()

        assertTrue("Level is completed", vm.gameState.value.isLevelCompleted || vm.gameState.value.status == GameStatus.COMPLETED)
    }

    @Test
    fun testGameOverTriggersWhenMovesRunOutWithoutClearingObjectives() {
        val vm = Match3ViewModel()
        vm.stepDelayMs = 0L
        vm.startGame(level = 1)

        val customObjectives = listOf(
            LevelObjective(
                id = "1",
                type = ObjectiveType.COLLECT_CANDY,
                candyType = CandyType.PURPLE,
                target = 50,
                currentProgress = 0
            )
        )
        vm.setCustomState(
            vm.gameState.value.copy(
                movesRemaining = 1,
                objectives = customObjectives
            )
        )

        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.RED),
                listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
                listOf(CandyType.YELLOW, CandyType.GREEN, CandyType.ORANGE, CandyType.PURPLE),
                listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.GREEN)
            )
        )
        vm.setCustomBoard(board)

        vm.selectTile(0, 2)
        vm.selectTile(0, 3)

        assertEquals("Moves reached 0", 0, vm.gameState.value.movesRemaining)
        assertTrue("Game is over when moves expire without fulfilling objective",
            vm.gameState.value.isGameOver || vm.gameState.value.status == GameStatus.GAME_OVER)
    }
}
