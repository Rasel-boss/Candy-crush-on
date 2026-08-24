package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchRemovalTest {

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

    @Test
    fun `1 - A horizontal 3-match is removed`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)
        assertEquals(1, matches.size)

        val matchedPositions = matches.flatMap { it.positions }.toSet()
        val boardAfterRemoval = MatchResolver.removeMatches(board, matchedPositions)

        assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(0, 0)?.type)
        assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(0, 1)?.type)
        assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(0, 2)?.type)
        assertEquals(CandyType.BLUE, boardAfterRemoval.getTile(0, 3)?.type)
    }

    @Test
    fun `2 - A vertical 3-match is removed`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW),
            listOf(CandyType.RED, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.RED, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.BLUE, CandyType.ORANGE, CandyType.BLUE, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)
        assertEquals(1, matches.size)

        val matchedPositions = matches.flatMap { it.positions }.toSet()
        val boardAfterRemoval = MatchResolver.removeMatches(board, matchedPositions)

        assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(0, 0)?.type)
        assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(1, 0)?.type)
        assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(2, 0)?.type)
        assertEquals(CandyType.BLUE, boardAfterRemoval.getTile(3, 0)?.type)
    }

    @Test
    fun `3 - A horizontal 4-match is removed`() {
        val grid = listOf(
            listOf(CandyType.GREEN, CandyType.GREEN, CandyType.GREEN, CandyType.GREEN),
            listOf(CandyType.RED, CandyType.BLUE, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)
        assertEquals(1, matches.size)
        assertEquals(4, matches[0].length)

        val matchedPositions = matches.flatMap { it.positions }.toSet()
        val boardAfterRemoval = MatchResolver.removeMatches(board, matchedPositions)

        for (c in 0 until 4) {
            assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(0, c)?.type)
        }
    }

    @Test
    fun `4 - A vertical 4-match is removed`() {
        val grid = listOf(
            listOf(CandyType.YELLOW, CandyType.BLUE, CandyType.GREEN, CandyType.RED),
            listOf(CandyType.YELLOW, CandyType.RED, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.YELLOW, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.YELLOW, CandyType.ORANGE, CandyType.BLUE, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)
        assertEquals(1, matches.size)
        assertEquals(4, matches[0].length)

        val matchedPositions = matches.flatMap { it.positions }.toSet()
        val boardAfterRemoval = MatchResolver.removeMatches(board, matchedPositions)

        for (r in 0 until 4) {
            assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(r, 0)?.type)
        }
    }

    @Test
    fun `5 - Multiple matches are removed simultaneously`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.BLUE, CandyType.BLUE, CandyType.BLUE, CandyType.GREEN),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.YELLOW, CandyType.YELLOW)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)
        assertEquals(2, matches.size)

        val matchedPositions = matches.flatMap { it.positions }.toSet()
        val boardAfterRemoval = MatchResolver.removeMatches(board, matchedPositions)

        // Row 0 first 3 are EMPTY
        assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(0, 0)?.type)
        assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(0, 1)?.type)
        assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(0, 2)?.type)

        // Row 2 first 3 are EMPTY
        assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(2, 0)?.type)
        assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(2, 1)?.type)
        assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(2, 2)?.type)
    }

    @Test
    fun `6 - Overlapping horizontal and vertical match removes each position only once`() {
        val grid = listOf(
            listOf(CandyType.GREEN, CandyType.RED, CandyType.BLUE, CandyType.YELLOW),
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.PURPLE),
            listOf(CandyType.ORANGE, CandyType.RED, CandyType.GREEN, CandyType.BLUE),
            listOf(CandyType.YELLOW, CandyType.BLUE, CandyType.ORANGE, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)
        assertEquals(2, matches.size) // One horizontal (1,0)..(1,2), one vertical (0,1)..(2,1)

        val matchedPositions = matches.flatMap { it.positions }.toSet()
        assertEquals(5, matchedPositions.size) // Center (1,1) deduplicated in the Set

        val boardAfterRemoval = MatchResolver.removeMatches(board, matchedPositions)
        for (pos in matchedPositions) {
            assertEquals(CandyType.EMPTY, boardAfterRemoval.getTile(pos)?.type)
        }
    }

    @Test
    fun `7 - Non-matching candies remain unchanged`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)
        val matchedPositions = matches.flatMap { it.positions }.toSet()
        val boardAfterRemoval = MatchResolver.removeMatches(board, matchedPositions)

        assertEquals(CandyType.GREEN, boardAfterRemoval.getTile(1, 0)?.type)
        assertEquals(CandyType.YELLOW, boardAfterRemoval.getTile(1, 1)?.type)
        assertEquals(CandyType.PURPLE, boardAfterRemoval.getTile(1, 2)?.type)
        assertEquals(CandyType.ORANGE, boardAfterRemoval.getTile(1, 3)?.type)
    }

    @Test
    fun `8 - Removed positions become EMPTY`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)
        val matchedPositions = matches.flatMap { it.positions }.toSet()
        val boardAfterRemoval = MatchResolver.removeMatches(board, matchedPositions)

        assertTrue(boardAfterRemoval.getTile(0, 0)!!.type == CandyType.EMPTY)
        assertFalse(boardAfterRemoval.getTile(0, 0)!!.isPlayable)
    }
}
