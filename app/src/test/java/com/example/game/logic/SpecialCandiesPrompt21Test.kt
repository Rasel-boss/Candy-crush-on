package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import com.example.game.utils.ScoreCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Deterministic test suite for Prompt 21: Special Candies System.
 * Verifies creation, activation, swapping, safety, scoring, and non-infinite cascade loops.
 */
class SpecialCandiesPrompt21Test {

    @Before
    fun setUp() {
        SpecialCandyResolver.resetIdCounter(800000L)
    }

    private fun createEmptyGrid(rows: Int = 8, columns: Int = 8): Match3Board {
        var id = 1L
        val tiles = (0 until rows).map { r ->
            (0 until columns).map { c ->
                CandyTile(
                    id = id++,
                    type = CandyType.EMPTY,
                    row = r,
                    column = c,
                    specialCandyType = SpecialCandyType.NONE
                )
            }
        }
        return Match3Board(rows = rows, columns = columns, tiles = tiles)
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
    // 1. SPECIAL CANDY CREATION TESTS
    // ==========================================

    @Test
    fun testFourHorizontalCandiesCreateHorizontalStripedCandy() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.RED,
                positions = listOf(
                    BoardPosition(2, 1),
                    BoardPosition(2, 2),
                    BoardPosition(2, 3),
                    BoardPosition(2, 4)
                ),
                isHorizontal = true
            )
        )

        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(
            matches = matches,
            swapPosA = BoardPosition(2, 2)
        )

        assertEquals(1, creations.size)
        val creation = creations.first()
        assertEquals(SpecialCandyType.STRIPED_HORIZONTAL, creation.specialType)
        assertEquals(CandyType.RED, creation.baseType)
        assertEquals(BoardPosition(2, 2), creation.position)
    }

    @Test
    fun testFourVerticalCandiesCreateVerticalStripedCandy() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.GREEN,
                positions = listOf(
                    BoardPosition(1, 4),
                    BoardPosition(2, 4),
                    BoardPosition(3, 4),
                    BoardPosition(4, 4)
                ),
                isHorizontal = false
            )
        )

        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(
            matches = matches,
            swapPosA = BoardPosition(3, 4)
        )

        assertEquals(1, creations.size)
        val creation = creations.first()
        assertEquals(SpecialCandyType.STRIPED_VERTICAL, creation.specialType)
        assertEquals(CandyType.GREEN, creation.baseType)
        assertEquals(BoardPosition(3, 4), creation.position)
    }

    @Test
    fun testFiveHorizontalCandiesCreateColorBomb() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.YELLOW,
                positions = listOf(
                    BoardPosition(3, 1),
                    BoardPosition(3, 2),
                    BoardPosition(3, 3),
                    BoardPosition(3, 4),
                    BoardPosition(3, 5)
                ),
                isHorizontal = true
            )
        )

        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(
            matches = matches,
            swapPosA = BoardPosition(3, 3)
        )

        assertEquals(1, creations.size)
        val creation = creations.first()
        assertEquals(SpecialCandyType.COLOR_BOMB, creation.specialType)
        assertEquals(BoardPosition(3, 3), creation.position)
    }

    @Test
    fun testFiveVerticalCandiesCreateColorBomb() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.PURPLE,
                positions = listOf(
                    BoardPosition(1, 2),
                    BoardPosition(2, 2),
                    BoardPosition(3, 2),
                    BoardPosition(4, 2),
                    BoardPosition(5, 2)
                ),
                isHorizontal = false
            )
        )

        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(
            matches = matches,
            swapPosA = BoardPosition(2, 2)
        )

        assertEquals(1, creations.size)
        val creation = creations.first()
        assertEquals(SpecialCandyType.COLOR_BOMB, creation.specialType)
        assertEquals(BoardPosition(2, 2), creation.position)
    }

    // ==========================================
    // 2. SPECIAL CANDY ACTIVATION TESTS
    // ==========================================

    @Test
    fun testHorizontalStripedCandyClearsRow() {
        val board = createUniformBoard(CandyType.BLUE, rows = 8, columns = 8)
        val stripedTile = CandyTile(
            id = 999L,
            type = CandyType.BLUE,
            row = 3,
            column = 4,
            specialCandyType = SpecialCandyType.STRIPED_HORIZONTAL
        )

        val blastArea = SpecialCandyActivator.calculateSingleEffect(board, stripedTile)

        assertEquals(8, blastArea.size)
        for (c in 0 until 8) {
            assertTrue("Row 3 Col $c must be in blast area", blastArea.contains(BoardPosition(3, c)))
        }
    }

    @Test
    fun testVerticalStripedCandyClearsColumn() {
        val board = createUniformBoard(CandyType.BLUE, rows = 8, columns = 8)
        val stripedTile = CandyTile(
            id = 998L,
            type = CandyType.GREEN,
            row = 2,
            column = 5,
            specialCandyType = SpecialCandyType.STRIPED_VERTICAL
        )

        val blastArea = SpecialCandyActivator.calculateSingleEffect(board, stripedTile)

        assertEquals(8, blastArea.size)
        for (r in 0 until 8) {
            assertTrue("Row $r Col 5 must be in blast area", blastArea.contains(BoardPosition(r, 5)))
        }
    }

    @Test
    fun testColorBombRemovesTargetedColor() {
        var board = createUniformBoard(CandyType.YELLOW, rows = 8, columns = 8)
        // Put 5 RED candies on board
        board = board.withTile(CandyTile(101L, CandyType.RED, 0, 0))
        board = board.withTile(CandyTile(102L, CandyType.RED, 1, 1))
        board = board.withTile(CandyTile(103L, CandyType.RED, 2, 2))
        board = board.withTile(CandyTile(104L, CandyType.RED, 3, 3))
        board = board.withTile(CandyTile(105L, CandyType.RED, 4, 4))

        val colorBomb = CandyTile(
            id = 777L,
            type = CandyType.EMPTY,
            row = 7,
            column = 7,
            specialCandyType = SpecialCandyType.COLOR_BOMB
        )
        board = board.withTile(colorBomb)

        val blastArea = SpecialCandyActivator.calculateSingleEffect(board, colorBomb, targetColor = CandyType.RED)

        // Must include all 5 RED tiles plus the Color Bomb position itself
        assertEquals(6, blastArea.size)
        assertTrue(blastArea.contains(BoardPosition(0, 0)))
        assertTrue(blastArea.contains(BoardPosition(1, 1)))
        assertTrue(blastArea.contains(BoardPosition(2, 2)))
        assertTrue(blastArea.contains(BoardPosition(3, 3)))
        assertTrue(blastArea.contains(BoardPosition(4, 4)))
        assertTrue(blastArea.contains(BoardPosition(7, 7)))
    }

    // ==========================================
    // 3. SPECIAL CANDY SAFETY TESTS
    // ==========================================

    @Test
    fun testSpecialCandiesHaveUniqueIdentity() {
        val tile1 = SpecialCandyCreator.createTile(BoardPosition(0, 0), SpecialCandyType.STRIPED_HORIZONTAL, CandyType.RED)
        val tile2 = SpecialCandyCreator.createTile(BoardPosition(0, 1), SpecialCandyType.STRIPED_VERTICAL, CandyType.BLUE)
        val tile3 = SpecialCandyCreator.createTile(BoardPosition(0, 2), SpecialCandyType.COLOR_BOMB, CandyType.EMPTY)

        assertTrue(tile1.id != tile2.id)
        assertTrue(tile2.id != tile3.id)
        assertTrue(tile1.isSpecial)
        assertTrue(tile2.isSpecial)
        assertTrue(tile3.isSpecial)
    }

    @Test
    fun testEmptyTileNeverTreatedAsPlayableSpecialCandy() {
        val emptyTile = CandyTile(
            id = 12L,
            type = CandyType.EMPTY,
            row = 0,
            column = 0,
            specialCandyType = SpecialCandyType.NONE
        )

        assertFalse(emptyTile.isPlayable)
        assertFalse(emptyTile.isSpecial)
        assertTrue(emptyTile.isEmpty)
        assertFalse(BoardValidator.isTileValid(emptyTile, allowEmpty = false))
        assertTrue(BoardValidator.isTileValid(emptyTile, allowEmpty = true))
    }

    @Test
    fun testSpecialCandiesDoNotCreateInfiniteProcessingLoops() {
        var board = createUniformBoard(CandyType.ORANGE, rows = 8, columns = 8)
        // Place two striped candies pointing at each other
        val hStripe = CandyTile(1001L, CandyType.ORANGE, 3, 3, SpecialCandyType.STRIPED_HORIZONTAL)
        val vStripe = CandyTile(1002L, CandyType.ORANGE, 3, 5, SpecialCandyType.STRIPED_VERTICAL)
        board = board.withTile(hStripe)
        board = board.withTile(vStripe)

        val alreadyActivated = mutableSetOf<Long>()
        val activatedSpecials = mutableListOf<CandyTile>()

        val result = SpecialCandyActivator.resolveChainedSpecials(
            board = board,
            currentlyAffected = setOf(BoardPosition(3, 3)),
            alreadyActivatedIds = alreadyActivated,
            activatedSpecials = activatedSpecials
        )

        // Chaining should safely trigger both striped candies and terminate
        assertEquals(2, activatedSpecials.size)
        assertTrue(alreadyActivated.contains(1001L))
        assertTrue(alreadyActivated.contains(1002L))
        assertTrue(result.affectedPositions.size >= 15) // Row 3 (8 tiles) + Col 5 (8 tiles) - intersection
    }

    // ==========================================
    // 4. SWAP TESTS
    // ==========================================

    @Test
    fun testNormalPlusStripedCandySwapIsValid() {
        var board = createUniformBoard(CandyType.BLUE, rows = 8, columns = 8)
        val striped = CandyTile(2001L, CandyType.BLUE, 3, 3, SpecialCandyType.STRIPED_HORIZONTAL)
        val normal = CandyTile(2002L, CandyType.RED, 3, 4, SpecialCandyType.NONE)
        board = board.withTile(striped)
        board = board.withTile(normal)

        assertTrue(SpecialCandyResolver.isDirectSpecialSwap(board, BoardPosition(3, 3), BoardPosition(3, 4)))
        assertTrue(MatchDetector.isPotentialValidSwap(board, BoardPosition(3, 3), BoardPosition(3, 4)))
    }

    @Test
    fun testNormalPlusColorBombSwapIsValid() {
        var board = createUniformBoard(CandyType.GREEN, rows = 8, columns = 8)
        val colorBomb = CandyTile(2003L, CandyType.EMPTY, 4, 4, SpecialCandyType.COLOR_BOMB)
        val normal = CandyTile(2004L, CandyType.YELLOW, 4, 5, SpecialCandyType.NONE)
        board = board.withTile(colorBomb)
        board = board.withTile(normal)

        assertTrue(SpecialCandyResolver.isDirectSpecialSwap(board, BoardPosition(4, 4), BoardPosition(4, 5)))
        assertTrue(MatchDetector.isPotentialValidSwap(board, BoardPosition(4, 4), BoardPosition(4, 5)))
    }

    @Test
    fun testInvalidDiagonalSwapRemainsInvalid() {
        val board = createUniformBoard(CandyType.BLUE, rows = 8, columns = 8)
        assertFalse(MatchDetector.isPotentialValidSwap(board, BoardPosition(0, 0), BoardPosition(1, 1)))
        assertFalse(MatchDetector.isPotentialValidSwap(board, BoardPosition(2, 3), BoardPosition(3, 4)))
    }

    // ==========================================
    // 5. SCORING TESTS
    // ==========================================

    @Test
    fun testNormalMatchesPreserveExistingScoring() {
        assertEquals(30, ScoreCalculator.calculateMatchScore(3))
        assertEquals(60, ScoreCalculator.calculateMatchScore(4))
        assertEquals(100, ScoreCalculator.calculateMatchScore(5))
        assertEquals(100, ScoreCalculator.calculateMatchScore(6))
    }

    @Test
    fun testSpecialActivationAwardsDeterministicBonusPoints() {
        assertEquals(100, ScoreCalculator.calculateSpecialActivationScore(SpecialCandyType.STRIPED_HORIZONTAL))
        assertEquals(100, ScoreCalculator.calculateSpecialActivationScore(SpecialCandyType.STRIPED_VERTICAL))
        assertEquals(200, ScoreCalculator.calculateSpecialActivationScore(SpecialCandyType.COLOR_BOMB))
    }
}
