package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SpecialCandyEffectsTest {

    @Before
    fun setUp() {
        SpecialCandyResolver.resetIdCounter()
    }

    private fun createCustomBoard(grid: List<List<CandyType>>): Match3Board {
        val rows = grid.size
        val cols = grid[0].size
        var nextId = 1L
        val tiles = grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, type ->
                CandyTile(id = nextId++, type = type, row = r, column = c)
            }
        }
        return Match3Board(rows = rows, columns = cols, tiles = tiles)
    }

    // 11. Horizontal striped removes all positions in its row.
    @Test
    fun `11 - Horizontal striped removes all positions in its row`() {
        val board = Match3Board.createEmpty(8, 8)
        val stripedTile = CandyTile(
            id = 100L,
            type = CandyType.RED,
            row = 3,
            column = 4,
            specialCandyType = SpecialCandyType.HORIZONTAL_STRIPED
        )
        val affected = SpecialCandyResolver.calculateSingleSpecialEffect(board, stripedTile)
        assertEquals(8, affected.size)
        for (c in 0 until 8) {
            assertTrue(affected.contains(BoardPosition(3, c)))
        }
    }

    // 12. Vertical striped removes all positions in its column.
    @Test
    fun `12 - Vertical striped removes all positions in its column`() {
        val board = Match3Board.createEmpty(8, 8)
        val stripedTile = CandyTile(
            id = 101L,
            type = CandyType.BLUE,
            row = 2,
            column = 5,
            specialCandyType = SpecialCandyType.VERTICAL_STRIPED
        )
        val affected = SpecialCandyResolver.calculateSingleSpecialEffect(board, stripedTile)
        assertEquals(8, affected.size)
        for (r in 0 until 8) {
            assertTrue(affected.contains(BoardPosition(r, 5)))
        }
    }

    // 13. Wrapped removes a 3x3 area.
    @Test
    fun `13 - Wrapped removes a 3x3 area`() {
        val board = Match3Board.createEmpty(8, 8)
        val wrappedTile = CandyTile(
            id = 102L,
            type = CandyType.GREEN,
            row = 4,
            column = 4,
            specialCandyType = SpecialCandyType.WRAPPED
        )
        val affected = SpecialCandyResolver.calculateSingleSpecialEffect(board, wrappedTile)
        assertEquals(9, affected.size)
        for (r in 3..5) {
            for (c in 3..5) {
                assertTrue(affected.contains(BoardPosition(r, c)))
            }
        }
    }

    // 14. Wrapped candy near a corner does not access invalid positions.
    @Test
    fun `14 - Wrapped candy near a corner does not access invalid positions`() {
        val board = Match3Board.createEmpty(8, 8)
        val cornerWrappedTile = CandyTile(
            id = 103L,
            type = CandyType.YELLOW,
            row = 0,
            column = 0,
            specialCandyType = SpecialCandyType.WRAPPED
        )
        val affected = SpecialCandyResolver.calculateSingleSpecialEffect(board, cornerWrappedTile)
        assertEquals(4, affected.size) // (0,0), (0,1), (1,0), (1,1)
        assertTrue(affected.contains(BoardPosition(0, 0)))
        assertTrue(affected.contains(BoardPosition(0, 1)))
        assertTrue(affected.contains(BoardPosition(1, 0)))
        assertTrue(affected.contains(BoardPosition(1, 1)))
        assertFalse(affected.contains(BoardPosition(-1, -1)))
    }

    // 15. Color Bomb + RED removes every RED candy.
    @Test
    fun `15 - Color Bomb + RED removes every RED candy`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.BLUE, CandyType.RED, CandyType.GREEN),
            listOf(CandyType.YELLOW, CandyType.RED, CandyType.PURPLE, CandyType.RED),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.RED, CandyType.YELLOW),
            listOf(CandyType.RED, CandyType.PURPLE, CandyType.BLUE, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val bombTile = CandyTile(id = 999L, type = CandyType.EMPTY, row = 0, column = 0, specialCandyType = SpecialCandyType.COLOR_BOMB)

        val affected = SpecialCandyResolver.calculateSingleSpecialEffect(board, bombTile, targetColor = CandyType.RED)
        // Red candies are at (0,0), (0,2), (1,1), (1,3), (2,2), (3,0) => 6 tiles
        assertTrue(affected.contains(BoardPosition(0, 0)))
        assertTrue(affected.contains(BoardPosition(0, 2)))
        assertTrue(affected.contains(BoardPosition(1, 1)))
        assertTrue(affected.contains(BoardPosition(1, 3)))
        assertTrue(affected.contains(BoardPosition(2, 2)))
        assertTrue(affected.contains(BoardPosition(3, 0)))
        assertEquals(6, affected.size)
    }

    // 16. Color Bomb does not remove unrelated colors.
    @Test
    fun `16 - Color Bomb does not remove unrelated colors`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.BLUE),
            listOf(CandyType.GREEN, CandyType.YELLOW)
        )
        val board = createCustomBoard(grid)
        val bombTile = CandyTile(id = 999L, type = CandyType.EMPTY, row = 0, column = 0, specialCandyType = SpecialCandyType.COLOR_BOMB)

        val affected = SpecialCandyResolver.calculateSingleSpecialEffect(board, bombTile, targetColor = CandyType.RED)
        assertFalse(affected.contains(BoardPosition(0, 1))) // BLUE
        assertFalse(affected.contains(BoardPosition(1, 0))) // GREEN
        assertFalse(affected.contains(BoardPosition(1, 1))) // YELLOW
    }

    // 17. Color Bomb itself is consumed.
    @Test
    fun `17 - Color Bomb itself is consumed`() {
        val grid = listOf(
            listOf(CandyType.EMPTY, CandyType.RED),
            listOf(CandyType.GREEN, CandyType.YELLOW)
        )
        val board = createCustomBoard(grid)
        val bombTile = CandyTile(id = 999L, type = CandyType.EMPTY, row = 0, column = 0, specialCandyType = SpecialCandyType.COLOR_BOMB)

        val affected = SpecialCandyResolver.calculateSingleSpecialEffect(board, bombTile, targetColor = CandyType.RED)
        assertTrue(affected.contains(BoardPosition(0, 0)))
    }

    // 18. Special candy activation can trigger another special candy.
    @Test
    fun `18 - Special candy activation can trigger another special candy`() {
        // Row 0 has a Horizontal Striped at (0, 0) and at (0, 3) there is a Vertical Striped
        var nextId = 1L
        val tiles = List(4) { r ->
            List(4) { c ->
                when {
                    r == 0 && c == 0 -> CandyTile(id = 100L, type = CandyType.RED, row = r, column = c, specialCandyType = SpecialCandyType.HORIZONTAL_STRIPED)
                    r == 0 && c == 3 -> CandyTile(id = 101L, type = CandyType.BLUE, row = r, column = c, specialCandyType = SpecialCandyType.VERTICAL_STRIPED)
                    else -> CandyTile(id = nextId++, type = CandyType.GREEN, row = r, column = c)
                }
            }
        }
        val board = Match3Board(rows = 4, columns = 4, tiles = tiles)

        val activatedIds = mutableSetOf<Long>()
        val activatedSpecials = mutableListOf<CandyTile>()
        val result = SpecialCandyResolver.resolveChainedSpecials(
            board = board,
            currentlyAffected = setOf(BoardPosition(0, 0)),
            alreadyActivatedIds = activatedIds,
            activatedSpecials = activatedSpecials
        )

        // The horizontal stripe triggers all of row 0: (0,0), (0,1), (0,2), (0,3).
        // (0,3) is a vertical stripe, which activates and adds column 3: (1,3), (2,3), (3,3).
        assertEquals(2, result.activatedSpecials.size)
        assertTrue(result.affectedPositions.contains(BoardPosition(0, 0)))
        assertTrue(result.affectedPositions.contains(BoardPosition(0, 1)))
        assertTrue(result.affectedPositions.contains(BoardPosition(0, 2)))
        assertTrue(result.affectedPositions.contains(BoardPosition(0, 3)))
        assertTrue(result.affectedPositions.contains(BoardPosition(1, 3)))
        assertTrue(result.affectedPositions.contains(BoardPosition(2, 3)))
        assertTrue(result.affectedPositions.contains(BoardPosition(3, 3)))
    }

    // 19. Same special candy cannot activate twice in one resolution chain.
    @Test
    fun `19 - Same special candy cannot activate twice in one resolution chain`() {
        val tiles = List(4) { r ->
            List(4) { c ->
                if (r == 0 && c == 0) {
                    CandyTile(id = 100L, type = CandyType.RED, row = r, column = c, specialCandyType = SpecialCandyType.HORIZONTAL_STRIPED)
                } else {
                    CandyTile(id = (r * 4 + c + 1).toLong(), type = CandyType.BLUE, row = r, column = c)
                }
            }
        }
        val board = Match3Board(rows = 4, columns = 4, tiles = tiles)

        val activatedIds = mutableSetOf<Long>()
        val activatedSpecials = mutableListOf<CandyTile>()
        val result = SpecialCandyResolver.resolveChainedSpecials(
            board = board,
            currentlyAffected = setOf(BoardPosition(0, 0), BoardPosition(0, 1)),
            alreadyActivatedIds = activatedIds,
            activatedSpecials = activatedSpecials
        )

        assertEquals(1, result.activatedSpecials.size)
        assertEquals(100L, result.activatedSpecials[0].id)
    }

    // 20. No infinite activation loop occurs.
    @Test
    fun `20 - No infinite activation loop occurs`() {
        // Two adjacent special candies pointing to each other
        val tiles = List(4) { r ->
            List(4) { c ->
                when {
                    r == 0 && c == 0 -> CandyTile(id = 100L, type = CandyType.RED, row = r, column = c, specialCandyType = SpecialCandyType.HORIZONTAL_STRIPED)
                    r == 0 && c == 1 -> CandyTile(id = 101L, type = CandyType.BLUE, row = r, column = c, specialCandyType = SpecialCandyType.HORIZONTAL_STRIPED)
                    else -> CandyTile(id = (r * 4 + c + 1).toLong(), type = CandyType.GREEN, row = r, column = c)
                }
            }
        }
        val board = Match3Board(rows = 4, columns = 4, tiles = tiles)

        val activatedIds = mutableSetOf<Long>()
        val activatedSpecials = mutableListOf<CandyTile>()
        val result = SpecialCandyResolver.resolveChainedSpecials(
            board = board,
            currentlyAffected = setOf(BoardPosition(0, 0)),
            alreadyActivatedIds = activatedIds,
            activatedSpecials = activatedSpecials
        )

        // Loop terminates normally with exactly 2 special activations
        assertEquals(2, result.activatedSpecials.size)
        assertEquals(4, result.affectedPositions.size) // Row 0
    }
}
