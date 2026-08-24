package com.example.game.model

import com.example.game.logic.BoardGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class Match3BoardTest {

    @Test
    fun `3 - default board has 8 rows and 8 columns`() {
        val board = BoardGenerator.generateBoard(DEFAULT_ROWS, DEFAULT_COLUMNS, Random(42))
        assertEquals(8, board.rows)
        assertEquals(8, board.columns)
        assertEquals(DEFAULT_ROWS, board.rows)
        assertEquals(DEFAULT_COLUMNS, board.columns)
    }

    @Test
    fun `4 - board contains 64 positions`() {
        val board = BoardGenerator.generateBoard(8, 8, Random(42))
        assertEquals(64, board.totalPositions)
        assertEquals(64, board.allTiles.size)
    }

    @Test
    fun `5 - every playable position contains a valid CandyType`() {
        val board = BoardGenerator.generateBoard(8, 8, Random(42))
        for (r in 0 until board.rows) {
            for (c in 0 until board.columns) {
                val tile = board.getTile(r, c)
                assertNotNull("Tile at ($r, $c) must not be null", tile)
                assertTrue("Tile at ($r, $c) must be playable", tile!!.isPlayable)
                assertTrue("Tile type must be in PLAYABLE_TYPES", CandyType.PLAYABLE_TYPES.contains(tile.type))
                assertEquals(r, tile.row)
                assertEquals(c, tile.column)
            }
        }
    }

    @Test
    fun `swapTiles correctly swaps two tiles and preserves positions`() {
        val board = BoardGenerator.generateBoard(8, 8, Random(42))
        val posA = BoardPosition(0, 0)
        val posB = BoardPosition(0, 1)

        val tileA = board.getTile(posA)!!
        val tileB = board.getTile(posB)!!

        val swapped = board.swapTiles(posA, posB)

        val newTileA = swapped.getTile(posA)!!
        val newTileB = swapped.getTile(posB)!!

        assertEquals(tileB.type, newTileA.type)
        assertEquals(tileA.type, newTileB.type)
        assertEquals(0, newTileA.row)
        assertEquals(0, newTileA.column)
        assertEquals(0, newTileB.row)
        assertEquals(1, newTileB.column)
    }

    @Test
    fun `getTile returns null for out of bounds coordinates`() {
        val board = BoardGenerator.generateBoard(8, 8, Random(42))
        assertNull(board.getTile(-1, 0))
        assertNull(board.getTile(0, -1))
        assertNull(board.getTile(8, 0))
        assertNull(board.getTile(0, 8))
        assertNull(board.getTile(BoardPosition(10, 10)))
    }
}
