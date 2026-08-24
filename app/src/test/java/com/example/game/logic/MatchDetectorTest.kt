package com.example.game.logic

import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchDetectorTest {

    private val detector = MatchDetector()

    @Test
    fun testHorizontalMatch() {
        var board = Match3Board()
        board = board.set(0, 0, CandyTile(1, CandyType.RED))
        board = board.set(0, 1, CandyTile(2, CandyType.RED))
        board = board.set(0, 2, CandyTile(3, CandyType.RED))

        val matches = detector.findMatches(board)
        assertEquals(1, matches.size)
        assertEquals(3, matches[0].size)
        assertEquals(CandyType.RED, matches[0].type)
        assertTrue(matches[0].isHorizontal)
    }

    @Test
    fun testVerticalMatch() {
        var board = Match3Board()
        board = board.set(0, 0, CandyTile(1, CandyType.BLUE))
        board = board.set(1, 0, CandyTile(2, CandyType.BLUE))
        board = board.set(2, 0, CandyTile(3, CandyType.BLUE))

        val matches = detector.findMatches(board)
        assertEquals(1, matches.size)
        assertEquals(3, matches[0].size)
        assertEquals(CandyType.BLUE, matches[0].type)
        assertTrue(matches[0].isVertical)
    }
}
