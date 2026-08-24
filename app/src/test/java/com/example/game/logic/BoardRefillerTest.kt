package com.example.game.logic

import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BoardRefillerTest {

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
    fun `1 - EMPTY positions are filled`() {
        val grid = listOf(
            listOf(CandyType.EMPTY, CandyType.EMPTY),
            listOf(CandyType.RED, CandyType.BLUE)
        )
        val board = createCustomBoard(grid)
        val refilled = BoardRefiller.refillBoard(board, Random(42))

        for (r in 0 until 2) {
            for (c in 0 until 2) {
                assertTrue(refilled.getTile(r, c)!!.type.isPlayable)
                assertFalse(refilled.getTile(r, c)!!.type == CandyType.EMPTY)
            }
        }
    }

    @Test
    fun `2 - Existing candies are not replaced`() {
        val grid = listOf(
            listOf(CandyType.EMPTY, CandyType.EMPTY),
            listOf(CandyType.RED, CandyType.BLUE)
        )
        val board = createCustomBoard(grid)
        val refilled = BoardRefiller.refillBoard(board, Random(42))

        assertEquals(CandyType.RED, refilled.getTile(1, 0)?.type)
        assertEquals(CandyType.BLUE, refilled.getTile(1, 1)?.type)
    }

    @Test
    fun `3 - Only valid playable candy types are generated`() {
        val emptyBoard = Match3Board.createEmpty(8, 8)
        val refilled = BoardRefiller.refillBoard(emptyBoard, Random(123))

        for (tile in refilled.allTiles) {
            assertTrue(tile.type.isPlayable)
            assertTrue(CandyType.playableCandies.contains(tile.type))
        }
    }

    @Test
    fun `4 - Final board has no EMPTY positions`() {
        val emptyBoard = Match3Board.createEmpty(8, 8)
        val refilled = BoardRefiller.refillBoard(emptyBoard, Random(100))

        assertEquals(64, refilled.allTiles.count { it.type.isPlayable })
        assertEquals(0, refilled.allTiles.count { it.type == CandyType.EMPTY })
    }

    @Test
    fun `5 - Board dimensions remain 8x8`() {
        val emptyBoard = Match3Board.createEmpty(8, 8)
        val refilled = BoardRefiller.refillBoard(emptyBoard, Random(42))

        assertEquals(8, refilled.rows)
        assertEquals(8, refilled.columns)
        assertEquals(8, refilled.tiles.size)
        assertEquals(8, refilled.tiles[0].size)
    }
}
