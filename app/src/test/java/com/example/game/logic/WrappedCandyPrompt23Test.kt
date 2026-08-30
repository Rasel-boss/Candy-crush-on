package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import com.example.game.model.SpecialCombinationType
import com.example.game.utils.ScoreCalculator
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive Unit Tests for Prompt 23:
 * - Wrapped Candy Creation from T-shape and L-shape matches
 * - Underlying Candy Color Retention
 * - Single Wrapped 3x3 Activation with boundary safety
 * - Double Wrapped (5x5 Area Clear) Combination
 * - Striped + Wrapped (Super Cross 3-row, 3-column Clear) Combination
 * - Color Bomb + Wrapped (Multi-cluster 3x3 Explosions) Combination
 * - Combination Priority Detection
 * - Deterministic Scoring and Cascading
 */
class WrappedCandyPrompt23Test {

    @Before
    fun setUp() {
        SpecialCandyResolver.resetIdCounter(900000L)
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

    @Test
    fun testWrappedCandyCreation_TShapeMatch() {
        // T-shape match representation: 3 horizontal and 3 vertical intersecting at (3, 3)
        val hMatch = SingleMatch(
            type = CandyType.GREEN,
            positions = listOf(
                BoardPosition(3, 2),
                BoardPosition(3, 3),
                BoardPosition(3, 4)
            ),
            isHorizontal = true
        )
        val vMatch = SingleMatch(
            type = CandyType.GREEN,
            positions = listOf(
                BoardPosition(1, 3),
                BoardPosition(2, 3),
                BoardPosition(3, 3)
            ),
            isHorizontal = false
        )

        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(
            matches = listOf(hMatch, vMatch),
            swapPosA = BoardPosition(3, 3)
        )
        assertTrue("Should detect special candy creation for T-shape", creations.isNotEmpty())

        val creation = creations.firstOrNull { it.specialType == SpecialCandyType.WRAPPED }
        assertNotNull("Should create a WRAPPED special candy", creation)
        assertEquals("Wrapped candy should retain GREEN underlying color", CandyType.GREEN, creation?.baseType)
        assertEquals("Should place wrapped candy at swap/intersection position", BoardPosition(3, 3), creation?.position)
    }

    @Test
    fun testWrappedCandyCreation_LShapeMatch() {
        // L-shape match representation: intersecting at (4, 4)
        val hMatch = SingleMatch(
            type = CandyType.BLUE,
            positions = listOf(
                BoardPosition(4, 4),
                BoardPosition(4, 5),
                BoardPosition(4, 6)
            ),
            isHorizontal = true
        )
        val vMatch = SingleMatch(
            type = CandyType.BLUE,
            positions = listOf(
                BoardPosition(2, 4),
                BoardPosition(3, 4),
                BoardPosition(4, 4)
            ),
            isHorizontal = false
        )

        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(
            matches = listOf(hMatch, vMatch),
            swapPosA = BoardPosition(4, 4)
        )
        val wrappedCreation = creations.firstOrNull { it.specialType == SpecialCandyType.WRAPPED }
        assertNotNull("Should create a WRAPPED candy from L-shape", wrappedCreation)
        assertEquals(CandyType.BLUE, wrappedCreation?.baseType)
        assertEquals(BoardPosition(4, 4), wrappedCreation?.position)
    }

    @Test
    fun testSingleWrappedActivation_Center() {
        var board = createUniformBoard(CandyType.RED)
        val wrappedTile = CandyTile(
            id = 50L,
            type = CandyType.RED,
            row = 3,
            column = 3,
            specialCandyType = SpecialCandyType.WRAPPED
        )
        board = board.withTile(wrappedTile)

        val affected = SpecialCandyActivator.calculateSingleEffect(board, wrappedTile)

        // 3x3 blast centered at (3, 3) covers rows 2..4 and cols 2..4 (9 tiles)
        assertEquals(9, affected.size)
        for (r in 2..4) {
            for (c in 2..4) {
                assertTrue("Should clear ($r, $c)", affected.contains(BoardPosition(r, c)))
            }
        }
    }

    @Test
    fun testSingleWrappedActivation_CornerBoundarySafe() {
        var board = createUniformBoard(CandyType.RED)
        val cornerTile = CandyTile(
            id = 51L,
            type = CandyType.YELLOW,
            row = 0,
            column = 0,
            specialCandyType = SpecialCandyType.WRAPPED
        )
        board = board.withTile(cornerTile)

        val affected = SpecialCandyActivator.calculateSingleEffect(board, cornerTile)

        // Corner 3x3 bounded to grid: (0,0), (0,1), (1,0), (1,1) -> exactly 4 tiles
        assertEquals(4, affected.size)
        assertTrue(affected.contains(BoardPosition(0, 0)))
        assertTrue(affected.contains(BoardPosition(0, 1)))
        assertTrue(affected.contains(BoardPosition(1, 0)))
        assertTrue(affected.contains(BoardPosition(1, 1)))
    }

    @Test
    fun testDoubleWrappedCombination_5x5AreaClear() {
        var board = createUniformBoard(CandyType.RED)
        val wrappedA = CandyTile(id = 60L, type = CandyType.RED, row = 3, column = 3, specialCandyType = SpecialCandyType.WRAPPED)
        val wrappedB = CandyTile(id = 61L, type = CandyType.BLUE, row = 3, column = 4, specialCandyType = SpecialCandyType.WRAPPED)
        board = board.withTile(wrappedA).withTile(wrappedB)

        val comboType = SpecialCombinationResolver.detectCombination(wrappedA, wrappedB)
        assertEquals(SpecialCombinationType.WRAPPED_WRAPPED, comboType)

        val comboResult = SpecialCombinationResolver.resolveCombination(
            board = board,
            posA = BoardPosition(3, 3),
            posB = BoardPosition(3, 4)
        )

        // Covers min(3)-2=1 to max(3)+2=5 rows, min(3)-2=1 to max(4)+2=6 columns
        assertTrue("Double wrapped combo should clear a 5x5+ area", comboResult.affectedPositions.size >= 25)
        assertEquals(ScoreCalculator.COMBO_WRAPPED_WRAPPED_POINTS, comboResult.score)
    }

    @Test
    fun testStripedWrappedCombination_SuperCrossClear() {
        var board = createUniformBoard(CandyType.GREEN)
        val striped = CandyTile(id = 70L, type = CandyType.GREEN, row = 4, column = 4, specialCandyType = SpecialCandyType.HORIZONTAL_STRIPED)
        val wrapped = CandyTile(id = 71L, type = CandyType.PURPLE, row = 4, column = 5, specialCandyType = SpecialCandyType.WRAPPED)
        board = board.withTile(striped).withTile(wrapped)

        val comboType = SpecialCombinationResolver.detectCombination(striped, wrapped)
        assertEquals(SpecialCombinationType.STRIPED_WRAPPED, comboType)

        val comboResult = SpecialCombinationResolver.resolveCombination(
            board = board,
            posA = BoardPosition(4, 4),
            posB = BoardPosition(4, 5)
        )

        // Clears 3 rows and 3+ columns -> large super cross
        val rowsCleared = comboResult.affectedPositions.groupBy { it.row }.filter { it.value.size == 8 }.keys
        val colsCleared = comboResult.affectedPositions.groupBy { it.column }.filter { it.value.size == 8 }.keys

        assertTrue("Super cross should clear at least 3 full rows", rowsCleared.size >= 3)
        assertTrue("Super cross should clear at least 3 full columns", colsCleared.size >= 3)
        assertEquals(ScoreCalculator.COMBO_STRIPED_WRAPPED_POINTS, comboResult.score)
    }

    @Test
    fun testColorBombWrappedCombination_ClusterExplosions() {
        var board = createUniformBoard(CandyType.YELLOW)
        // Set some RED candies
        board = board.withTile(CandyTile(10L, CandyType.RED, 1, 1))
            .withTile(CandyTile(11L, CandyType.RED, 5, 5))
            .withTile(CandyTile(12L, CandyType.RED, 6, 2))

        val colorBomb = CandyTile(id = 80L, type = CandyType.EMPTY, row = 2, column = 2, specialCandyType = SpecialCandyType.COLOR_BOMB)
        val wrappedRed = CandyTile(id = 81L, type = CandyType.RED, row = 2, column = 3, specialCandyType = SpecialCandyType.WRAPPED)
        board = board.withTile(colorBomb).withTile(wrappedRed)

        val comboType = SpecialCombinationResolver.detectCombination(colorBomb, wrappedRed)
        assertEquals(SpecialCombinationType.COLOR_BOMB_WRAPPED, comboType)

        val comboResult = SpecialCombinationResolver.resolveCombination(
            board = board,
            posA = BoardPosition(2, 2),
            posB = BoardPosition(2, 3)
        )

        // Multiple 3x3 blasts around every RED tile
        assertTrue("Color Bomb + Wrapped should clear widespread clusters", comboResult.affectedPositions.size >= 15)
        assertEquals(ScoreCalculator.COMBO_COLOR_BOMB_WRAPPED_POINTS, comboResult.score)
    }

    @Test
    fun testCombinationPriorityOrdering() {
        val colorBomb1 = CandyTile(id = 1, type = CandyType.EMPTY, row = 0, column = 0, specialCandyType = SpecialCandyType.COLOR_BOMB)
        val colorBomb2 = CandyTile(id = 2, type = CandyType.EMPTY, row = 0, column = 1, specialCandyType = SpecialCandyType.COLOR_BOMB)
        val wrapped1 = CandyTile(id = 3, type = CandyType.RED, row = 0, column = 0, specialCandyType = SpecialCandyType.WRAPPED)
        val wrapped2 = CandyTile(id = 4, type = CandyType.BLUE, row = 0, column = 1, specialCandyType = SpecialCandyType.WRAPPED)
        val striped1 = CandyTile(id = 5, type = CandyType.GREEN, row = 0, column = 0, specialCandyType = SpecialCandyType.HORIZONTAL_STRIPED)
        val striped2 = CandyTile(id = 6, type = CandyType.YELLOW, row = 0, column = 1, specialCandyType = SpecialCandyType.VERTICAL_STRIPED)

        assertEquals(SpecialCombinationType.COLOR_BOMB_COLOR_BOMB, SpecialCombinationResolver.detectCombination(colorBomb1, colorBomb2))
        assertEquals(SpecialCombinationType.COLOR_BOMB_WRAPPED, SpecialCombinationResolver.detectCombination(colorBomb1, wrapped1))
        assertEquals(SpecialCombinationType.COLOR_BOMB_STRIPED, SpecialCombinationResolver.detectCombination(colorBomb1, striped1))
        assertEquals(SpecialCombinationType.WRAPPED_WRAPPED, SpecialCombinationResolver.detectCombination(wrapped1, wrapped2))
        assertEquals(SpecialCombinationType.STRIPED_WRAPPED, SpecialCombinationResolver.detectCombination(striped1, wrapped1))
        assertEquals(SpecialCombinationType.STRIPED_WRAPPED, SpecialCombinationResolver.detectCombination(wrapped1, striped1))
        assertEquals(SpecialCombinationType.STRIPED_STRIPED, SpecialCombinationResolver.detectCombination(striped1, striped2))
    }

    @Test
    fun testScoreCalculator_WrappedScores() {
        assertEquals(150, ScoreCalculator.calculateSpecialActivationScore(SpecialCandyType.WRAPPED))
        assertEquals(200, ScoreCalculator.calculateCombinationScore(SpecialCombinationType.WRAPPED_WRAPPED))
        assertEquals(250, ScoreCalculator.calculateCombinationScore(SpecialCombinationType.STRIPED_WRAPPED))
        assertEquals(350, ScoreCalculator.calculateCombinationScore(SpecialCombinationType.COLOR_BOMB_WRAPPED))
    }

    @Test
    fun testFullCascadeResolutionWithWrappedCandy() {
        var board = createUniformBoard(CandyType.PURPLE)
        val wrapped = CandyTile(id = 90L, type = CandyType.PURPLE, row = 3, column = 3, specialCandyType = SpecialCandyType.WRAPPED)
        board = board.withTile(wrapped)

        val step = MatchResolver.resolveDirectSpecialSwapStep(
            currentBoard = board,
            posA = BoardPosition(3, 3),
            posB = BoardPosition(3, 4),
            random = Random(42)
        )

        assertTrue("Direct special swap step should clear tiles", step.removedTiles.isNotEmpty())
        assertTrue("Step score should reflect activation", step.stepScore >= 150)
        assertEquals("Board dimensions preserved", 8, step.boardAfterRefill.rows)
        assertEquals("Board dimensions preserved", 8, step.boardAfterRefill.columns)
    }
}
