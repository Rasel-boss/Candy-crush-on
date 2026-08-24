package com.example.game.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardPositionTest {

    @Test
    fun testAdjacency() {
        val pos = BoardPosition(2, 2)
        assertTrue(pos.isAdjacentTo(BoardPosition(2, 3)))
        assertTrue(pos.isAdjacentTo(BoardPosition(2, 1)))
        assertTrue(pos.isAdjacentTo(BoardPosition(1, 2)))
        assertTrue(pos.isAdjacentTo(BoardPosition(3, 2)))

        assertFalse(pos.isAdjacentTo(BoardPosition(2, 2)))
        assertFalse(pos.isAdjacentTo(BoardPosition(1, 1)))
        assertFalse(pos.isAdjacentTo(BoardPosition(3, 3)))
        assertFalse(pos.isAdjacentTo(BoardPosition(2, 4)))
    }
}
