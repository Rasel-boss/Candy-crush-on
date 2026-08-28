package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameStatus
import com.example.game.model.LevelConfig
import com.example.game.model.LevelObjective
import com.example.game.model.Match3Board
import com.example.game.model.ObjectiveType
import com.example.game.viewmodel.Match3ViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Deterministic unit test suite verifying Prompt 18: Advanced Gameplay Polish & Reliable Level Gameplay.
 */
class GameplayPolishPrompt18Test {

    private fun createCustomBoard(grid: List<List<CandyType>>): Match3Board {
        var id = 1L
        val tiles = grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, type ->
                CandyTile(
                    id = id++,
                    type = type,
                    row = r,
                    column = c
                )
            }
        }
        return Match3Board(rows = grid.size, columns = grid[0].size, tiles = tiles)
    }

    // ==========================================
    // 1. VALID SWAP TESTS
    // ==========================================

    @Test
    fun testHorizontal3MatchSwapIsValid() {
        // [RED, RED, BLUE, RED] -> Swapping (0,2) with (0,3) creates [RED, RED, RED, BLUE]
        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.RED),
                listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
                listOf(CandyType.YELLOW, CandyType.GREEN, CandyType.ORANGE, CandyType.PURPLE),
                listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.GREEN)
            )
        )
        val posA = BoardPosition(0, 2)
        val posB = BoardPosition(0, 3)

        assertTrue(MatchDetector.doesSwapCreateMatch(board, posA, posB))
        assertTrue(MatchDetector.isPotentialValidSwap(board, posA, posB))
    }

    @Test
    fun testVertical3MatchSwapIsValid() {
        // Col 0: [BLUE, BLUE, GREEN, BLUE] -> Swapping (2,0) with (3,0) creates 3 BLUE vertically
        val board = createCustomBoard(
            listOf(
                listOf(CandyType.BLUE, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
                listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
                listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.ORANGE, CandyType.RED),
                listOf(CandyType.BLUE, CandyType.PURPLE, CandyType.GREEN, CandyType.YELLOW)
            )
        )
        val posA = BoardPosition(2, 0)
        val posB = BoardPosition(3, 0)

        assertTrue(MatchDetector.doesSwapCreateMatch(board, posA, posB))
        assertTrue(MatchDetector.isPotentialValidSwap(board, posA, posB))
    }

    @Test
    fun testNoMatchSwapIsInvalid() {
        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.GREEN, CandyType.BLUE, CandyType.YELLOW),
                listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED),
                listOf(CandyType.BLUE, CandyType.RED, CandyType.GREEN, CandyType.YELLOW),
                listOf(CandyType.ORANGE, CandyType.YELLOW, CandyType.PURPLE, CandyType.GREEN)
            )
        )
        val posA = BoardPosition(0, 0)
        val posB = BoardPosition(0, 1)

        assertFalse(MatchDetector.doesSwapCreateMatch(board, posA, posB))
        assertFalse(MatchDetector.isPotentialValidSwap(board, posA, posB))
    }

    // ==========================================
    // 2. MOVE COUNT CONSISTENCY
    // ==========================================

    @Test
    fun testValidSwapConsumesExactlyOneMove() {
        val vm = Match3ViewModel()
        vm.stepDelayMs = 0L
        vm.startGame(level = 1)
        val initialMoves = vm.gameState.value.movesRemaining

        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.RED),
                listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
                listOf(CandyType.YELLOW, CandyType.GREEN, CandyType.ORANGE, CandyType.PURPLE),
                listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.GREEN)
            )
        )
        vm.setCustomBoard(board)

        // Select and swap
        vm.selectTile(0, 2)
        val success = vm.selectTile(0, 3)

        assertTrue("Valid swap executed", success)
        assertEquals("Moves remaining decremented by exactly 1", initialMoves - 1, vm.gameState.value.movesRemaining)
    }

    @Test
    fun testInvalidSwapConsumesZeroMoves() {
        val vm = Match3ViewModel()
        vm.stepDelayMs = 0L
        vm.startGame(level = 1)
        val initialMoves = vm.gameState.value.movesRemaining
        val initialScore = vm.gameState.value.score

        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.GREEN, CandyType.BLUE, CandyType.YELLOW),
                listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED),
                listOf(CandyType.BLUE, CandyType.RED, CandyType.GREEN, CandyType.YELLOW),
                listOf(CandyType.ORANGE, CandyType.YELLOW, CandyType.PURPLE, CandyType.GREEN)
            )
        )
        vm.setCustomBoard(board)

        // Select and attempt invalid swap
        vm.selectTile(0, 0)
        val success = vm.selectTile(0, 1)

        assertFalse("Invalid swap rejected", success)
        assertEquals("Moves remaining must be unchanged on invalid swap", initialMoves, vm.gameState.value.movesRemaining)
        assertEquals("Score must be unchanged on invalid swap", initialScore, vm.gameState.value.score)
        assertNull("Selected position should reset", vm.gameState.value.selectedPosition)
    }

    @Test
    fun testCascadeDoesNotConsumeAdditionalMoves() {
        val vm = Match3ViewModel()
        vm.stepDelayMs = 0L
        vm.startGame(level = 1)
        val initialMoves = vm.gameState.value.movesRemaining

        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.RED),
                listOf(CandyType.GREEN, CandyType.GREEN, CandyType.YELLOW, CandyType.GREEN),
                listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE),
                listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.GREEN)
            )
        )
        vm.setCustomBoard(board)

        // Valid swap that triggers resolution
        vm.selectTile(0, 2)
        vm.selectTile(0, 3)

        assertEquals("Multi-step cascade consumes exactly 1 player move total", initialMoves - 1, vm.gameState.value.movesRemaining)
    }

    // ==========================================
    // 3. MATCH RESOLUTION & COLLAPSE
    // ==========================================

    @Test
    fun testMatchedTilesAreRemoved() {
        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
                listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE)
            )
        )
        val matchedPositions = setOf(BoardPosition(0, 0), BoardPosition(0, 1), BoardPosition(0, 2))
        val boardAfterRemoval = MatchResolver.removeMatches(board, matchedPositions)

        assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(0, 0)?.type)
        assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(0, 1)?.type)
        assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(0, 2)?.type)
        assertEquals(CandyType.BLUE, boardAfterRemoval.getTile(0, 3)?.type)
    }

    @Test
    fun testTilesAboveCollapseDownward() {
        // Row 0 has RED, Row 1 has EMPTY
        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.BLUE),
                listOf(CandyType.EMPTY, CandyType.YELLOW)
            )
        )
        val collapsed = GravityProcessor.applyGravity(board)

        assertEquals(CandyType.EMPTY, collapsed.getTile(0, 0)?.type)
        assertEquals(CandyType.RED, collapsed.getTile(1, 0)?.type)
        assertEquals(CandyType.BLUE, collapsed.getTile(0, 1)?.type)
        assertEquals(CandyType.YELLOW, collapsed.getTile(1, 1)?.type)
    }

    @Test
    fun testNewTilesFillEmptyPositions() {
        val board = createCustomBoard(
            listOf(
                listOf(CandyType.EMPTY, CandyType.EMPTY),
                listOf(CandyType.RED, CandyType.BLUE)
            )
        )
        val refilled = BoardRefiller.refillBoard(board, Random(42))

        assertTrue(refilled.getTile(0, 0)?.type?.isPlayable == true)
        assertTrue(refilled.getTile(0, 1)?.type?.isPlayable == true)
        assertFalse(refilled.tiles.any { row -> row.any { it.type == CandyType.EMPTY } })
    }

    @Test
    fun testCascadesContinueUntilBoardIsStable() {
        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
                listOf(CandyType.GREEN, CandyType.GREEN, CandyType.YELLOW, CandyType.GREEN),
                listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE),
                listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.GREEN)
            )
        )
        val result = MatchResolver.resolveAllCascades(board, random = Random(100))

        assertTrue(result.isStable)
        assertFalse("Final board should have no matches", MatchDetector.hasAnyMatches(result.finalBoard))
        assertTrue("At least one cascade step resolved", result.steps.isNotEmpty())
    }

    // ==========================================
    // 4. POSSIBLE MOVES DETECTION
    // ==========================================

    @Test
    fun testBoardWithValidSwapReturnsTrue() {
        // [RED, RED, BLUE, RED] has a valid swap at (0,2) and (0,3)
        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.RED),
                listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
                listOf(CandyType.YELLOW, CandyType.GREEN, CandyType.ORANGE, CandyType.PURPLE),
                listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.GREEN)
            )
        )

        assertTrue(MatchDetector.hasPossibleMoves(board))
        assertTrue(BoardValidator.hasPossibleMoves(board))
        val moves = MatchDetector.findPossibleMoves(board)
        assertTrue(moves.isNotEmpty())
    }

    @Test
    fun testBoardWithNoValidSwapReturnsFalse() {
        // Checkerboard pattern with alternating colors where no single adjacent swap forms 3-in-a-row
        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.GREEN, CandyType.RED, CandyType.GREEN),
                listOf(CandyType.BLUE, CandyType.YELLOW, CandyType.BLUE, CandyType.YELLOW),
                listOf(CandyType.RED, CandyType.GREEN, CandyType.RED, CandyType.GREEN),
                listOf(CandyType.BLUE, CandyType.YELLOW, CandyType.BLUE, CandyType.YELLOW)
            )
        )

        assertFalse("Dead board should have no valid possible swaps", MatchDetector.hasPossibleMoves(board))
        assertFalse(BoardValidator.hasPossibleMoves(board))
        assertTrue(MatchDetector.findPossibleMoves(board).isEmpty())
    }

    @Test
    fun testPossibleMoveDetectionDoesNotMutateOriginalBoard() {
        val board = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.RED),
                listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
                listOf(CandyType.YELLOW, CandyType.GREEN, CandyType.ORANGE, CandyType.PURPLE),
                listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.GREEN)
            )
        )
        val originalTiles = board.tiles.map { row -> row.map { it.copy() } }

        MatchDetector.hasPossibleMoves(board)
        MatchDetector.findPossibleMoves(board)

        for (r in 0 until board.rows) {
            for (c in 0 until board.columns) {
                assertEquals(originalTiles[r][c].type, board.getTile(r, c)?.type)
                assertEquals(originalTiles[r][c].id, board.getTile(r, c)?.id)
            }
        }
    }

    // ==========================================
    // 5. AUTOMATIC BOARD RECOVERY
    // ==========================================

    @Test
    fun testRecoveredBoardContainsNoImmediateMatchesAndHasPossibleMoves() {
        val random = Random(12345)
        val generated = BoardGenerator.generateBoard(rows = 8, columns = 8, random = random, ensurePossibleMoves = true)

        assertFalse("Generated board has no initial matches", MatchDetector.hasAnyMatches(generated))
        assertTrue("Generated board has valid moves available", MatchDetector.hasPossibleMoves(generated))
        assertTrue("Board satisfies full structural validation", BoardValidator.isBoardValid(generated))
    }

    @Test
    fun testRecoveryDoesNotResetScoreLevelObjectivesOrMoves() {
        val vm = Match3ViewModel()
        vm.stepDelayMs = 0L
        vm.startGame(level = 2)

        // Inject dead board
        val deadBoard = createCustomBoard(
            listOf(
                listOf(CandyType.RED, CandyType.GREEN, CandyType.RED, CandyType.GREEN),
                listOf(CandyType.BLUE, CandyType.YELLOW, CandyType.BLUE, CandyType.YELLOW),
                listOf(CandyType.RED, CandyType.GREEN, CandyType.RED, CandyType.GREEN),
                listOf(CandyType.BLUE, CandyType.YELLOW, CandyType.BLUE, CandyType.YELLOW)
            )
        )
        vm.setCustomState(
            vm.gameState.value.copy(
                board = deadBoard,
                score = 750,
                movesRemaining = 18,
                level = 2,
                status = GameStatus.PLAYING
            )
        )

        // Trigger automatic dead board recovery
        val recovered = vm.checkAndRecoverDeadBoard(Random(999))

        assertTrue("Dead board was recognized and recovered", recovered)
        assertEquals("Score strictly preserved during recovery", 750, vm.gameState.value.score)
        assertEquals("Moves strictly preserved during recovery", 18, vm.gameState.value.movesRemaining)
        assertEquals("Level strictly preserved during recovery", 2, vm.gameState.value.level)
        assertFalse("Recovered board has no initial matches", MatchDetector.hasAnyMatches(vm.gameState.value.board))
        assertTrue("Recovered board has possible moves", MatchDetector.hasPossibleMoves(vm.gameState.value.board))
    }

    // ==========================================
    // 6. GAME STATE & INPUT LOCKING
    // ==========================================

    @Test
    fun testProcessingStatusBlocksDuplicateInput() {
        val vm = Match3ViewModel()
        vm.startGame(level = 1)
        vm.setCustomState(vm.gameState.value.copy(isProcessing = true, status = GameStatus.PROCESSING))

        val handled = vm.selectTile(0, 0)
        assertFalse("Input during PROCESSING must be locked/ignored", handled)
    }

    @Test
    fun testGameOverStatusBlocksTileInteraction() {
        val vm = Match3ViewModel()
        vm.startGame(level = 1)
        vm.setCustomState(vm.gameState.value.copy(movesRemaining = 0, isGameOver = true, status = GameStatus.GAME_OVER))

        val handled = vm.selectTile(0, 0)
        assertFalse("Input during GAME_OVER must be locked/ignored", handled)
    }

    @Test
    fun testCompletedStatusBlocksTileInteraction() {
        val vm = Match3ViewModel()
        vm.startGame(level = 1)
        vm.setCustomState(vm.gameState.value.copy(isLevelCompleted = true, status = GameStatus.COMPLETED))

        val handled = vm.selectTile(0, 0)
        assertFalse("Input during COMPLETED must be locked/ignored", handled)
    }

    @Test
    fun testScoreRemainsConsistentThroughCascades() {
        val vm = Match3ViewModel()
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

        val score1 = vm.gameState.value.score
        assertTrue("Score awarded for match", score1 >= 30)

        // Check that board recovery or state query does not alter or reset score
        vm.checkAndRecoverDeadBoard()
        assertEquals(score1, vm.gameState.value.score)
    }

    @Test
    fun testObjectiveProgressRemainsConsistentThroughCascades() {
        val vm = Match3ViewModel()
        val config = LevelConfig(
            levelNumber = 1,
            startingMoves = 30,
            targetScore = 100,
            objectives = listOf(LevelObjective(type = ObjectiveType.COLLECT_CANDY, target = 5, candyType = CandyType.RED))
        )
        val initialObj = ObjectiveManager.initializeObjectives(config)
        vm.setCustomState(
            vm.gameState.value.copy(
                level = 1,
                levelConfig = config,
                objectives = initialObj,
                status = GameStatus.PLAYING
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
        assertEquals("Red candies matched counted in objective", 3, redObj.currentProgress)
    }

    @Test
    fun testTileSelectionUX() {
        val vm = Match3ViewModel()
        vm.startGame(level = 1)

        // 1. First tap selects
        val selected1 = vm.selectTile(1, 1)
        assertTrue(selected1)
        assertEquals(BoardPosition(1, 1), vm.gameState.value.selectedPosition)

        // 2. Same tile tap deselects
        val deselected = vm.selectTile(1, 1)
        assertTrue(deselected)
        assertNull(vm.gameState.value.selectedPosition)

        // 3. First tap selects (1, 1)
        vm.selectTile(1, 1)
        // 4. Non-adjacent tap at (3, 3) switches selection to (3, 3)
        val switched = vm.selectTile(3, 3)
        assertTrue(switched)
        assertEquals(BoardPosition(3, 3), vm.gameState.value.selectedPosition)
    }
}
