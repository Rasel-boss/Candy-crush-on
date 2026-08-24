package com.example.game.logic

import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GravityProcessorTest {

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
    fun `1 - Empty spaces below candies are filled correctly`() {
        val grid = listOf(
            listOf(CandyType.RED),
            listOf(CandyType.EMPTY),
            listOf(CandyType.BLUE)
        )
        val board = createCustomBoard(grid)
        val afterGravity = GravityProcessor.applyGravity(board)

        assertEquals(CandyType.EMPTY, afterGravity.getTile(0, 0)?.type)
        assertEquals(CandyType.RED, afterGravity.getTile(1, 0)?.type)
        assertEquals(CandyType.BLUE, afterGravity.getTile(2, 0)?.type)
    }

    @Test
    fun `2 - Candies fall downward`() {
        val grid = listOf(
            listOf(CandyType.GREEN),
            listOf(CandyType.EMPTY),
            listOf(CandyType.EMPTY)
        )
        val board = createCustomBoard(grid)
        val afterGravity = GravityProcessor.applyGravity(board)

        assertEquals(CandyType.EMPTY, afterGravity.getTile(0, 0)?.type)
        assertEquals(CandyType.EMPTY, afterGravity.getTile(1, 0)?.type)
        assertEquals(CandyType.GREEN, afterGravity.getTile(2, 0)?.type)
    }

    @Test
    fun `3 - Relative candy order is preserved`() {
        // Example from prompt 3:
        // Row 0: RED, Row 1: EMPTY, Row 2: BLUE, Row 3: EMPTY, Row 4: GREEN, Row 5: EMPTY, Row 6: YELLOW, Row 7: PURPLE
        // Result: Row 0..2 EMPTY, Row 3: RED, Row 4: BLUE, Row 5: GREEN, Row 6: YELLOW, Row 7: PURPLE
        val column = listOf(
            listOf(CandyType.RED),
            listOf(CandyType.EMPTY),
            listOf(CandyType.BLUE),
            listOf(CandyType.EMPTY),
            listOf(CandyType.GREEN),
            listOf(CandyType.EMPTY),
            listOf(CandyType.YELLOW),
            listOf(CandyType.PURPLE)
        )
        val board = createCustomBoard(column)
        val afterGravity = GravityProcessor.applyGravity(board)

        assertEquals(CandyType.EMPTY, afterGravity.getTile(0, 0)?.type)
        assertEquals(CandyType.EMPTY, afterGravity.getTile(1, 0)?.type)
        assertEquals(CandyType.EMPTY, afterGravity.getTile(2, 0)?.type)
        assertEquals(CandyType.RED, afterGravity.getTile(3, 0)?.type)
        assertEquals(CandyType.BLUE, afterGravity.getTile(4, 0)?.type)
        assertEquals(CandyType.GREEN, afterGravity.getTile(5, 0)?.type)
        assertEquals(CandyType.YELLOW, afterGravity.getTile(6, 0)?.type)
        assertEquals(CandyType.PURPLE, afterGravity.getTile(7, 0)?.type)
    }

    @Test
    fun `4 - Multiple empty spaces are handled`() {
        val grid = listOf(
            listOf(CandyType.EMPTY, CandyType.RED),
            listOf(CandyType.BLUE, CandyType.EMPTY),
            listOf(CandyType.EMPTY, CandyType.EMPTY),
            listOf(CandyType.GREEN, CandyType.YELLOW)
        )
        val board = createCustomBoard(grid)
        val afterGravity = GravityProcessor.applyGravity(board)

        // Col 0: BLUE and GREEN fall to bottom
        assertEquals(CandyType.EMPTY, afterGravity.getTile(0, 0)?.type)
        assertEquals(CandyType.EMPTY, afterGravity.getTile(1, 0)?.type)
        assertEquals(CandyType.BLUE, afterGravity.getTile(2, 0)?.type)
        assertEquals(CandyType.GREEN, afterGravity.getTile(3, 0)?.type)

        // Col 1: RED and YELLOW fall to bottom
        assertEquals(CandyType.EMPTY, afterGravity.getTile(0, 1)?.type)
        assertEquals(CandyType.EMPTY, afterGravity.getTile(1, 1)?.type)
        assertEquals(CandyType.RED, afterGravity.getTile(2, 1)?.type)
        assertEquals(CandyType.YELLOW, afterGravity.getTile(3, 1)?.type)
    }

    @Test
    fun `5 - A completely full column remains unchanged`() {
        val grid = listOf(
            listOf(CandyType.RED),
            listOf(CandyType.BLUE),
            listOf(CandyType.GREEN),
            listOf(CandyType.YELLOW)
        )
        val board = createCustomBoard(grid)
        val afterGravity = GravityProcessor.applyGravity(board)

        assertEquals(CandyType.RED, afterGravity.getTile(0, 0)?.type)
        assertEquals(CandyType.BLUE, afterGravity.getTile(1, 0)?.type)
        assertEquals(CandyType.GREEN, afterGravity.getTile(2, 0)?.type)
        assertEquals(CandyType.YELLOW, afterGravity.getTile(3, 0)?.type)
    }

    @Test
    fun `6 - An empty column remains empty`() {
        val grid = listOf(
            listOf(CandyType.EMPTY),
            listOf(CandyType.EMPTY),
            listOf(CandyType.EMPTY)
        )
        val board = createCustomBoard(grid)
        val afterGravity = GravityProcessor.applyGravity(board)

        assertEquals(CandyType.EMPTY, afterGravity.getTile(0, 0)?.type)
        assertEquals(CandyType.EMPTY, afterGravity.getTile(1, 0)?.type)
        assertEquals(CandyType.EMPTY, afterGravity.getTile(2, 0)?.type)
    }

    @Test
    fun `7 - Gravity does not move candies horizontally`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.EMPTY),
            listOf(CandyType.EMPTY, CandyType.BLUE)
        )
        val board = createCustomBoard(grid)
        val afterGravity = GravityProcessor.applyGravity(board)

        // Column 0 stays in column 0 (falls to row 1)
        assertEquals(CandyType.EMPTY, afterGravity.getTile(0, 0)?.type)
        assertEquals(CandyType.RED, afterGravity.getTile(1, 0)?.type)

        // Column 1 stays in column 1 (was at row 1, stays at row 1)
        assertEquals(CandyType.EMPTY, afterGravity.getTile(0, 1)?.type)
        assertEquals(CandyType.BLUE, afterGravity.getTile(1, 1)?.type)
    }
}
