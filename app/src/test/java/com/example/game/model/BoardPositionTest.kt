package com.example.game.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardPositionTest {

    @Test
    fun `11 - position equality and hashCode work correctly`() {
        val posA = BoardPosition(2, 3)
        val posB = BoardPosition(2, 3)
        val posC = BoardPosition(3, 2)

        assertEquals(posA, posB)
        assertEquals(posA.hashCode(), posB.hashCode())
        assertNotEquals(posA, posC)
    }

    @Test
    fun `12 - left and right adjacency works`() {
        val center = BoardPosition(3, 3)
        val left = BoardPosition(3, 2)
        val right = BoardPosition(3, 4)

        assertTrue(center.isAdjacent(left))
        assertTrue(center.isAdjacent(right))
        assertTrue(left.isAdjacent(center))
        assertTrue(right.isAdjacent(center))
    }

    @Test
    fun `13 - up and down adjacency works`() {
        val center = BoardPosition(3, 3)
        val up = BoardPosition(2, 3)
        val down = BoardPosition(4, 3)

        assertTrue(center.isAdjacent(up))
        assertTrue(center.isAdjacent(down))
        assertTrue(up.isAdjacent(center))
        assertTrue(down.isAdjacent(center))
    }

    @Test
    fun `14 - diagonal positions are not adjacent`() {
        val center = BoardPosition(3, 3)
        val topLeft = BoardPosition(2, 2)
        val topRight = BoardPosition(2, 4)
        val bottomLeft = BoardPosition(4, 2)
        val bottomRight = BoardPosition(4, 4)

        assertFalse(center.isAdjacent(topLeft))
        assertFalse(center.isAdjacent(topRight))
        assertFalse(center.isAdjacent(bottomLeft))
        assertFalse(center.isAdjacent(bottomRight))
    }

    @Test
    fun `15 - same position is not considered adjacent`() {
        val pos = BoardPosition(3, 3)
        assertFalse(pos.isAdjacent(pos))
    }

    @Test
    fun `far away positions are not adjacent`() {
        val posA = BoardPosition(0, 0)
        val posB = BoardPosition(0, 5)
        val posC = BoardPosition(5, 0)

        assertFalse(posA.isAdjacent(posB))
        assertFalse(posA.isAdjacent(posC))
    }
}
