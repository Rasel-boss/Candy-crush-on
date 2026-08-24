package com.example.game.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class Match3BoardTest {

    @Test
    fun testBoardSetAndGet() {
        val board = Match3Board(rows = 8, cols = 8)
        assertNull(board[0, 0])

        val tile = CandyTile(1, CandyType.RED)
        val updated = board.set(0, 0, tile)
        assertEquals(tile, updated[0, 0])
        assertNull(board[0, 0]) // Immutability
    }

    @Test
    fun testBoardSwap() {
        val tile1 = CandyTile(1, CandyType.RED)
        val tile2 = CandyTile(2, CandyType.BLUE)
        val pos1 = BoardPosition(0, 0)
        val pos2 = BoardPosition(0, 1)

        val board = Match3Board().set(pos1, tile1).set(pos2, tile2)
        val swapped = board.swap(pos1, pos2)

        assertEquals(tile2, swapped[pos1])
        assertEquals(tile1, swapped[pos2])
    }
}
