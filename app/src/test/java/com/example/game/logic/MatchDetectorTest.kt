package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MatchDetectorTest {

    private fun createCustomBoard(grid: List<List<CandyType>>): Match3Board {
        val rows = grid.size
        val cols = grid[0].size
        var nextId = 1L
        val tiles = grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, type ->
                CandyTile(
                    id = nextId++,
                    type = type,
                    row = r,
                    column = c
                )
            }
        }
        return Match3Board(rows = rows, columns = cols, tiles = tiles)
    }

    @Test
    fun `16 - detects horizontal 3-match`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.ORANGE, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)

        assertEquals(1, matches.size)
        val match = matches[0]
        assertEquals(CandyType.RED, match.type)
        assertTrue(match.isHorizontal)
        assertEquals(3, match.length)
        assertEquals(
            listOf(BoardPosition(0, 0), BoardPosition(0, 1), BoardPosition(0, 2)),
            match.positions
        )
        assertTrue(MatchDetector.hasAnyMatches(board))
    }

    @Test
    fun `17 - detects vertical 3-match`() {
        val grid = listOf(
            listOf(CandyType.BLUE, CandyType.RED, CandyType.GREEN, CandyType.YELLOW),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.BLUE, CandyType.ORANGE, CandyType.RED, CandyType.GREEN),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.RED)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)

        assertEquals(1, matches.size)
        val match = matches[0]
        assertEquals(CandyType.BLUE, match.type)
        assertFalse(match.isHorizontal)
        assertEquals(3, match.length)
        assertEquals(
            listOf(BoardPosition(0, 0), BoardPosition(1, 0), BoardPosition(2, 0)),
            match.positions
        )
        assertTrue(MatchDetector.hasAnyMatches(board))
    }

    @Test
    fun `18 - detects horizontal 4-match`() {
        val grid = listOf(
            listOf(CandyType.GREEN, CandyType.GREEN, CandyType.GREEN, CandyType.GREEN),
            listOf(CandyType.BLUE, CandyType.RED, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.ORANGE, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE, CandyType.RED)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)

        assertEquals(1, matches.size)
        val match = matches[0]
        assertEquals(CandyType.GREEN, match.type)
        assertTrue(match.isHorizontal)
        assertEquals(4, match.length)
        assertEquals(4, match.positions.size)
    }

    @Test
    fun `19 - detects vertical 4-match`() {
        val grid = listOf(
            listOf(CandyType.YELLOW, CandyType.RED, CandyType.GREEN, CandyType.BLUE),
            listOf(CandyType.YELLOW, CandyType.GREEN, CandyType.BLUE, CandyType.PURPLE),
            listOf(CandyType.YELLOW, CandyType.ORANGE, CandyType.RED, CandyType.GREEN),
            listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.BLUE, CandyType.RED)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)

        assertEquals(1, matches.size)
        val match = matches[0]
        assertEquals(CandyType.YELLOW, match.type)
        assertFalse(match.isHorizontal)
        assertEquals(4, match.length)
    }

    @Test
    fun `20 - does not report a non-match`() {
        val board = BoardGenerator.generateBoard(8, 8, Random(42))
        val matches = MatchDetector.findMatches(board)
        assertTrue("Generated initial board should have zero matches", matches.isEmpty())
        assertFalse(MatchDetector.hasAnyMatches(board))
        assertTrue(MatchDetector.findAllMatchedPositions(board).isEmpty())
    }

    @Test
    fun `detects 5-in-a-row match`() {
        val grid = listOf(
            listOf(CandyType.PURPLE, CandyType.PURPLE, CandyType.PURPLE, CandyType.PURPLE, CandyType.PURPLE),
            listOf(CandyType.BLUE, CandyType.RED, CandyType.YELLOW, CandyType.GREEN, CandyType.ORANGE),
            listOf(CandyType.RED, CandyType.YELLOW, CandyType.GREEN, CandyType.BLUE, CandyType.PURPLE),
            listOf(CandyType.YELLOW, CandyType.GREEN, CandyType.BLUE, CandyType.ORANGE, CandyType.RED),
            listOf(CandyType.GREEN, CandyType.BLUE, CandyType.ORANGE, CandyType.PURPLE, CandyType.YELLOW)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)

        assertEquals(1, matches.size)
        assertEquals(5, matches[0].length)
        assertEquals(CandyType.PURPLE, matches[0].type)
    }

    @Test
    fun `wouldSwapCreateMatch returns true for swap creating a match`() {
        // Row 0: RED, RED, BLUE, GREEN
        // Row 1: BLUE, GREEN, RED, YELLOW
        // Swapping (0, 2) [BLUE] and (1, 2) [RED] creates RED, RED, RED on row 0
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.BLUE, CandyType.GREEN),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.RED, CandyType.YELLOW),
            listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.RED)
        )
        val board = createCustomBoard(grid)
        val posA = BoardPosition(0, 2)
        val posB = BoardPosition(1, 2)

        assertTrue(MatchDetector.wouldSwapCreateMatch(board, posA, posB))
    }
}
