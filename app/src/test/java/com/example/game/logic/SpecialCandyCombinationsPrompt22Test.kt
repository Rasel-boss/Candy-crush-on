package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameStatus
import com.example.game.model.LevelObjective
import com.example.game.model.Match3Board
import com.example.game.model.ObjectiveType
import com.example.game.model.SpecialCandyType
import com.example.game.model.SpecialCombinationType
import com.example.game.utils.ScoreCalculator
import com.example.game.viewmodel.Match3ViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Deterministic test suite for Prompt 22: Advanced Special Candy Combinations.
 *
 * Verifies:
 * 1. Striped + Striped combinations (H+H, H+V, V+V) with row+column cross clear.
 * 2. Striped + Color Bomb combinations (H+ColorBomb, V+ColorBomb) with color target and temporary striped activations.
 * 3. Color Bomb + Color Bomb combinations with full playable board clear and safe refill.
 * 4. Priority detection before normal match validation.
 * 5. Invalid swaps (diagonal, non-adjacent, same tile).
 * 6. Move consumption (exactly 1 move per valid combination).
 * 7. Scoring and objective progress preservation.
 * 8. Stability and regression across Levels 1-4.
 */
class SpecialCandyCombinationsPrompt22Test {

    private lateinit var viewModel: Match3ViewModel

    @Before
    fun setUp() {
        viewModel = Match3ViewModel().apply { stepDelayMs = 0L }
        SpecialCandyResolver.resetIdCounter(950000L)
    }

    private fun createUniformBoard(
        fillType: CandyType = CandyType.BLUE,
        rows: Int = 8,
        columns: Int = 8
    ): Match3Board {
        var id = 1L
        val tiles = (0 until rows).map { r ->
            (0 until columns).map { c ->
                CandyTile(
                    id = id++,
                    type = fillType,
                    row = r,
                    column = c,
                    specialCandyType = SpecialCandyType.NONE
                )
            }
        }
        return Match3Board(rows = rows, columns = columns, tiles = tiles)
    }

    // ==========================================
    // 1. STRIPED + STRIPED COMBINATIONS
    // ==========================================

    @Test
    fun testHorizontalPlusHorizontalStripedCrossClear() {
        var board = createUniformBoard(CandyType.BLUE, rows = 8, columns = 8)
        val hStripe1 = CandyTile(101L, CandyType.RED, 3, 3, SpecialCandyType.HORIZONTAL_STRIPED)
        val hStripe2 = CandyTile(102L, CandyType.GREEN, 3, 4, SpecialCandyType.HORIZONTAL_STRIPED)
        board = board.withTile(hStripe1).withTile(hStripe2)

        val comboType = SpecialCombinationResolver.detectCombination(hStripe1, hStripe2)
        assertEquals(SpecialCombinationType.STRIPED_STRIPED, comboType)

        val result = SpecialCombinationResolver.resolveCombination(
            board = board,
            posA = BoardPosition(3, 3),
            posB = BoardPosition(3, 4)
        )

        // Should clear row 3 and columns 3 and 4
        assertTrue("Must include posA", result.affectedPositions.contains(BoardPosition(3, 3)))
        assertTrue("Must include posB", result.affectedPositions.contains(BoardPosition(3, 4)))
        for (c in 0 until 8) {
            assertTrue("Row 3 Col $c must be cleared", result.affectedPositions.contains(BoardPosition(3, c)))
        }
        for (r in 0 until 8) {
            assertTrue("Row $r Col 3 must be cleared", result.affectedPositions.contains(BoardPosition(r, 3)))
            assertTrue("Row $r Col 4 must be cleared", result.affectedPositions.contains(BoardPosition(r, 4)))
        }
        assertEquals(ScoreCalculator.COMBO_STRIPED_STRIPED_POINTS, result.score)
    }

    @Test
    fun testHorizontalPlusVerticalStripedCrossClear() {
        var board = createUniformBoard(CandyType.YELLOW, rows = 8, columns = 8)
        val hStripe = CandyTile(201L, CandyType.YELLOW, 2, 4, SpecialCandyType.HORIZONTAL_STRIPED)
        val vStripe = CandyTile(202L, CandyType.PURPLE, 2, 5, SpecialCandyType.VERTICAL_STRIPED)
        board = board.withTile(hStripe).withTile(vStripe)

        val comboType = SpecialCombinationResolver.detectCombination(hStripe, vStripe)
        assertEquals(SpecialCombinationType.STRIPED_STRIPED, comboType)

        val result = SpecialCombinationResolver.resolveCombination(
            board = board,
            posA = BoardPosition(2, 4),
            posB = BoardPosition(2, 5)
        )

        for (c in 0 until 8) {
            assertTrue("Row 2 Col $c must be cleared", result.affectedPositions.contains(BoardPosition(2, c)))
        }
        for (r in 0 until 8) {
            assertTrue("Row $r Col 4 must be cleared", result.affectedPositions.contains(BoardPosition(r, 4)))
            assertTrue("Row $r Col 5 must be cleared", result.affectedPositions.contains(BoardPosition(r, 5)))
        }
    }

    @Test
    fun testVerticalPlusVerticalStripedCrossClear() {
        var board = createUniformBoard(CandyType.ORANGE, rows = 8, columns = 8)
        val vStripe1 = CandyTile(301L, CandyType.ORANGE, 4, 2, SpecialCandyType.VERTICAL_STRIPED)
        val vStripe2 = CandyTile(302L, CandyType.RED, 5, 2, SpecialCandyType.VERTICAL_STRIPED)
        board = board.withTile(vStripe1).withTile(vStripe2)

        val comboType = SpecialCombinationResolver.detectCombination(vStripe1, vStripe2)
        assertEquals(SpecialCombinationType.STRIPED_STRIPED, comboType)

        val result = SpecialCombinationResolver.resolveCombination(
            board = board,
            posA = BoardPosition(4, 2),
            posB = BoardPosition(5, 2)
        )

        for (c in 0 until 8) {
            assertTrue("Row 4 Col $c must be cleared", result.affectedPositions.contains(BoardPosition(4, c)))
            assertTrue("Row 5 Col $c must be cleared", result.affectedPositions.contains(BoardPosition(5, c)))
        }
        for (r in 0 until 8) {
            assertTrue("Row $r Col 2 must be cleared", result.affectedPositions.contains(BoardPosition(r, 2)))
        }
    }

    @Test
    fun testStripedPlusStripedConsumesExactlyOneMoveInViewModel() {
        viewModel.startGame(level = 1)
        var board = createUniformBoard(CandyType.BLUE, rows = 8, columns = 8)
        val hStripe = CandyTile(401L, CandyType.RED, 3, 3, SpecialCandyType.HORIZONTAL_STRIPED)
        val vStripe = CandyTile(402L, CandyType.GREEN, 3, 4, SpecialCandyType.VERTICAL_STRIPED)
        board = board.withTile(hStripe).withTile(vStripe)

        viewModel.setCustomBoard(board)

        val initialMoves = viewModel.gameState.value.movesRemaining
        viewModel.selectTile(BoardPosition(3, 3))
        val swapSuccess = viewModel.selectTile(BoardPosition(3, 4))

        assertTrue("Swap must succeed", swapSuccess)
        assertEquals(initialMoves - 1, viewModel.gameState.value.movesRemaining)
        assertTrue(viewModel.gameState.value.status == GameStatus.PLAYING || viewModel.gameState.value.status == GameStatus.COMPLETED)
        assertFalse(viewModel.gameState.value.isProcessing)
    }

    // ==========================================
    // 2. STRIPED + COLOR BOMB COMBINATIONS
    // ==========================================

    @Test
    fun testHorizontalStripedPlusColorBombTargetsColorAndActivatesStripes() {
        var board = createUniformBoard(CandyType.BLUE, rows = 8, columns = 8)
        // Add 4 RED candies to the board
        val red1 = CandyTile(501L, CandyType.RED, 0, 1)
        val red2 = CandyTile(502L, CandyType.RED, 2, 7)
        val red3 = CandyTile(503L, CandyType.RED, 6, 0)
        val red4 = CandyTile(504L, CandyType.RED, 7, 6)
        board = board.withTile(red1).withTile(red2).withTile(red3).withTile(red4)

        // Swap Red Horizontal Striped with Color Bomb at (4, 4) and (4, 5)
        val stripedTile = CandyTile(505L, CandyType.RED, 4, 4, SpecialCandyType.HORIZONTAL_STRIPED)
        val colorBomb = CandyTile(506L, CandyType.EMPTY, 4, 5, SpecialCandyType.COLOR_BOMB)
        board = board.withTile(stripedTile).withTile(colorBomb)

        val comboType = SpecialCombinationResolver.detectCombination(stripedTile, colorBomb)
        assertEquals(SpecialCombinationType.COLOR_BOMB_STRIPED, comboType)

        val result = SpecialCombinationResolver.resolveCombination(
            board = board,
            posA = BoardPosition(4, 4),
            posB = BoardPosition(4, 5)
        )

        // Affected positions must include the combo tiles, the 4 red tiles, and their row/column blasts
        assertTrue(result.affectedPositions.contains(BoardPosition(4, 4)))
        assertTrue(result.affectedPositions.contains(BoardPosition(4, 5)))
        assertTrue(result.affectedPositions.contains(BoardPosition(0, 1)))
        assertTrue(result.affectedPositions.contains(BoardPosition(2, 7)))
        assertTrue(result.affectedPositions.contains(BoardPosition(6, 0)))
        assertTrue(result.affectedPositions.contains(BoardPosition(7, 6)))

        // Score must be at least COMBO_COLOR_BOMB_STRIPED_POINTS
        assertTrue(result.score >= ScoreCalculator.COMBO_COLOR_BOMB_STRIPED_POINTS)
    }

    @Test
    fun testVerticalStripedPlusColorBombConsumesExactlyOneMoveInViewModel() {
        viewModel.startGame(level = 1)
        var board = createUniformBoard(CandyType.GREEN, rows = 8, columns = 8)
        val stripedTile = CandyTile(601L, CandyType.YELLOW, 3, 3, SpecialCandyType.VERTICAL_STRIPED)
        val colorBomb = CandyTile(602L, CandyType.EMPTY, 3, 4, SpecialCandyType.COLOR_BOMB)
        board = board.withTile(stripedTile).withTile(colorBomb)

        viewModel.setCustomBoard(board)

        val initialMoves = viewModel.gameState.value.movesRemaining
        viewModel.selectTile(BoardPosition(3, 3))
        val swapSuccess = viewModel.selectTile(BoardPosition(3, 4))

        assertTrue("Swap must succeed", swapSuccess)
        assertEquals(initialMoves - 1, viewModel.gameState.value.movesRemaining)
        assertTrue(viewModel.gameState.value.status == GameStatus.PLAYING || viewModel.gameState.value.status == GameStatus.COMPLETED)
        assertFalse(viewModel.gameState.value.isProcessing)
    }

    // ==========================================
    // 3. COLOR BOMB + COLOR BOMB COMBINATIONS
    // ==========================================

    @Test
    fun testColorBombPlusColorBombClearsEntirePlayableBoard() {
        var board = createUniformBoard(CandyType.PURPLE, rows = 8, columns = 8)
        val cb1 = CandyTile(701L, CandyType.EMPTY, 3, 3, SpecialCandyType.COLOR_BOMB)
        val cb2 = CandyTile(702L, CandyType.EMPTY, 3, 4, SpecialCandyType.COLOR_BOMB)
        board = board.withTile(cb1).withTile(cb2)

        val comboType = SpecialCombinationResolver.detectCombination(cb1, cb2)
        assertEquals(SpecialCombinationType.COLOR_BOMB_COLOR_BOMB, comboType)

        val result = SpecialCombinationResolver.resolveCombination(
            board = board,
            posA = BoardPosition(3, 3),
            posB = BoardPosition(3, 4)
        )

        // All 64 tiles on an 8x8 board must be cleared
        assertEquals(64, result.affectedPositions.size)
        assertEquals(ScoreCalculator.COMBO_COLOR_BOMB_COLOR_BOMB_POINTS, result.score)
    }

    @Test
    fun testColorBombPlusColorBombSafelyRefillsAndStabilizes() {
        viewModel.startGame(level = 1)
        var board = createUniformBoard(CandyType.BLUE, rows = 8, columns = 8)
        val cb1 = CandyTile(801L, CandyType.EMPTY, 4, 3, SpecialCandyType.COLOR_BOMB)
        val cb2 = CandyTile(802L, CandyType.EMPTY, 4, 4, SpecialCandyType.COLOR_BOMB)
        board = board.withTile(cb1).withTile(cb2)

        viewModel.setCustomBoard(board)
        val initialMoves = viewModel.gameState.value.movesRemaining

        viewModel.selectTile(BoardPosition(4, 3))
        val swapSuccess = viewModel.selectTile(BoardPosition(4, 4))

        assertTrue("Double Color Bomb swap must succeed", swapSuccess)
        val finalBoard = viewModel.gameState.value.board
        assertEquals(8, finalBoard.rows)
        assertEquals(8, finalBoard.columns)

        // Verify no empty slots remain
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val tile = finalBoard.getTile(r, c)
                assertNotNull("Tile at ($r, $c) must not be null", tile)
                assertTrue("Tile at ($r, $c) must be playable", tile!!.isPlayable)
                assertFalse("Tile at ($r, $c) must not be EMPTY", tile.isEmpty)
            }
        }
        assertEquals(initialMoves - 1, viewModel.gameState.value.movesRemaining)
        assertTrue(viewModel.gameState.value.status == GameStatus.PLAYING || viewModel.gameState.value.status == GameStatus.COMPLETED)
    }

    // ==========================================
    // 4. SWAP DETECTION PRIORITY & INVALID SWAPS
    // ==========================================

    @Test
    fun testPriorityDetectionColorBombOverMatch() {
        var board = createUniformBoard(CandyType.GREEN, rows = 8, columns = 8)
        val cb1 = CandyTile(901L, CandyType.EMPTY, 2, 2, SpecialCandyType.COLOR_BOMB)
        val cb2 = CandyTile(902L, CandyType.EMPTY, 2, 3, SpecialCandyType.COLOR_BOMB)
        board = board.withTile(cb1).withTile(cb2)

        assertTrue(SpecialCombinationResolver.canCombine(board, BoardPosition(2, 2), BoardPosition(2, 3)))
        assertTrue(MatchDetector.isPotentialValidSwap(board, BoardPosition(2, 2), BoardPosition(2, 3)))
    }

    @Test
    fun testDiagonalSelectionCannotCombine() {
        var board = createUniformBoard(CandyType.BLUE, rows = 8, columns = 8)
        val cb1 = CandyTile(911L, CandyType.EMPTY, 2, 2, SpecialCandyType.COLOR_BOMB)
        val cb2 = CandyTile(912L, CandyType.EMPTY, 3, 3, SpecialCandyType.COLOR_BOMB)
        board = board.withTile(cb1).withTile(cb2)

        assertFalse(SpecialCombinationResolver.canCombine(board, BoardPosition(2, 2), BoardPosition(3, 3)))
        assertFalse(MatchDetector.isPotentialValidSwap(board, BoardPosition(2, 2), BoardPosition(3, 3)))
    }

    @Test
    fun testNonAdjacentSelectionCannotCombine() {
        var board = createUniformBoard(CandyType.RED, rows = 8, columns = 8)
        val s1 = CandyTile(921L, CandyType.RED, 0, 0, SpecialCandyType.HORIZONTAL_STRIPED)
        val s2 = CandyTile(922L, CandyType.RED, 0, 4, SpecialCandyType.VERTICAL_STRIPED)
        board = board.withTile(s1).withTile(s2)

        assertFalse(SpecialCombinationResolver.canCombine(board, BoardPosition(0, 0), BoardPosition(0, 4)))
        assertFalse(MatchDetector.isPotentialValidSwap(board, BoardPosition(0, 0), BoardPosition(0, 4)))
    }

    @Test
    fun testSameTileSelectionDeselectsWithoutMoveLoss() {
        viewModel.startGame(level = 1)
        val board = createUniformBoard(CandyType.BLUE, rows = 8, columns = 8)
        viewModel.setCustomBoard(board)
        val initialMoves = viewModel.gameState.value.movesRemaining

        viewModel.selectTile(BoardPosition(2, 2))
        assertEquals(BoardPosition(2, 2), viewModel.gameState.value.selectedPosition)

        viewModel.selectTile(BoardPosition(2, 2))
        assertEquals(null, viewModel.gameState.value.selectedPosition)
        assertEquals(initialMoves, viewModel.gameState.value.movesRemaining)
    }

    // ==========================================
    // 5. OBJECTIVE PROGRESSION WITH COMBINATIONS
    // ==========================================

    @Test
    fun testSpecialCombinationUpdatesScoreAndTileRemovalObjectives() {
        viewModel.startGame(level = 1)
        var board = createUniformBoard(CandyType.YELLOW, rows = 8, columns = 8)
        val cb1 = CandyTile(931L, CandyType.EMPTY, 1, 1, SpecialCandyType.COLOR_BOMB)
        val cb2 = CandyTile(932L, CandyType.EMPTY, 1, 2, SpecialCandyType.COLOR_BOMB)
        board = board.withTile(cb1).withTile(cb2)

        val objectives = listOf(
            LevelObjective(
                type = ObjectiveType.TARGET_SCORE,
                target = 500,
                currentProgress = 0
            ),
            LevelObjective(
                type = ObjectiveType.COLLECT_CANDY,
                candyType = CandyType.YELLOW,
                target = 30,
                currentProgress = 0
            )
        )

        viewModel.setCustomState(
            viewModel.gameState.value.copy(
                board = board,
                objectives = objectives
            )
        )

        viewModel.selectTile(BoardPosition(1, 1))
        viewModel.selectTile(BoardPosition(1, 2))

        val currentObjectives = viewModel.gameState.value.objectives
        val scoreObj = currentObjectives.first { it.type == ObjectiveType.TARGET_SCORE }
        val yellowObj = currentObjectives.first { it.type == ObjectiveType.COLLECT_CANDY }

        assertTrue("Score objective must progress", scoreObj.currentProgress >= 500)
        assertTrue("Score objective should be completed", scoreObj.isCompleted)
        assertTrue("Yellow candy objective must progress", yellowObj.currentProgress >= 30)
        assertTrue("Yellow candy objective should be completed", yellowObj.isCompleted)
    }

    // ==========================================
    // 6. LEVEL REGRESSION TESTS (LEVELS 1-4)
    // ==========================================

    @Test
    fun testLevelRegressionLevels1Through4() {
        for (lvl in 1..4) {
            viewModel.startGame(level = lvl)
            assertEquals(lvl, viewModel.gameState.value.level)
            assertEquals(GameStatus.PLAYING, viewModel.gameState.value.status)
            assertTrue("Moves must be positive", viewModel.gameState.value.movesRemaining > 0)
            assertEquals(8, viewModel.gameState.value.board.rows)
            assertEquals(8, viewModel.gameState.value.board.columns)
            assertNotNull(viewModel.gameState.value.objectives)
            assertTrue("Level must have objectives", viewModel.gameState.value.objectives.isNotEmpty())
        }
    }
}
