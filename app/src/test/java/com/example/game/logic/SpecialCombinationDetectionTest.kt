package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import com.example.game.model.SpecialCombinationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpecialCombinationDetectionTest {

    @Test
    fun testDetectStripedStriped() {
        val tileA = CandyTile(1L, CandyType.RED, 0, 0, SpecialCandyType.HORIZONTAL_STRIPED)
        val tileB = CandyTile(2L, CandyType.BLUE, 0, 1, SpecialCandyType.VERTICAL_STRIPED)

        assertEquals(SpecialCombinationType.STRIPED_STRIPED, SpecialCombinationResolver.detectCombination(tileA, tileB))
    }

    @Test
    fun testDetectWrappedWrapped() {
        val tileA = CandyTile(1L, CandyType.GREEN, 1, 1, SpecialCandyType.WRAPPED)
        val tileB = CandyTile(2L, CandyType.YELLOW, 1, 2, SpecialCandyType.WRAPPED)

        assertEquals(SpecialCombinationType.WRAPPED_WRAPPED, SpecialCombinationResolver.detectCombination(tileA, tileB))
    }

    @Test
    fun testDetectStripedWrappedBothOrders() {
        val striped = CandyTile(1L, CandyType.PURPLE, 2, 2, SpecialCandyType.HORIZONTAL_STRIPED)
        val wrapped = CandyTile(2L, CandyType.ORANGE, 2, 3, SpecialCandyType.WRAPPED)

        assertEquals(SpecialCombinationType.STRIPED_WRAPPED, SpecialCombinationResolver.detectCombination(striped, wrapped))
        assertEquals(SpecialCombinationType.STRIPED_WRAPPED, SpecialCombinationResolver.detectCombination(wrapped, striped))
    }

    @Test
    fun testDetectColorBombNormal() {
        val bomb = CandyTile(1L, CandyType.EMPTY, 3, 3, SpecialCandyType.COLOR_BOMB)
        val normal = CandyTile(2L, CandyType.RED, 3, 4, SpecialCandyType.NONE)

        assertEquals(SpecialCombinationType.COLOR_BOMB_NORMAL, SpecialCombinationResolver.detectCombination(bomb, normal))
        assertEquals(SpecialCombinationType.COLOR_BOMB_NORMAL, SpecialCombinationResolver.detectCombination(normal, bomb))
    }

    @Test
    fun testDetectColorBombStriped() {
        val bomb = CandyTile(1L, CandyType.EMPTY, 4, 4, SpecialCandyType.COLOR_BOMB)
        val striped = CandyTile(2L, CandyType.BLUE, 4, 5, SpecialCandyType.VERTICAL_STRIPED)

        assertEquals(SpecialCombinationType.COLOR_BOMB_STRIPED, SpecialCombinationResolver.detectCombination(bomb, striped))
        assertEquals(SpecialCombinationType.COLOR_BOMB_STRIPED, SpecialCombinationResolver.detectCombination(striped, bomb))
    }

    @Test
    fun testDetectColorBombWrapped() {
        val bomb = CandyTile(1L, CandyType.EMPTY, 5, 5, SpecialCandyType.COLOR_BOMB)
        val wrapped = CandyTile(2L, CandyType.GREEN, 5, 6, SpecialCandyType.WRAPPED)

        assertEquals(SpecialCombinationType.COLOR_BOMB_WRAPPED, SpecialCombinationResolver.detectCombination(bomb, wrapped))
        assertEquals(SpecialCombinationType.COLOR_BOMB_WRAPPED, SpecialCombinationResolver.detectCombination(wrapped, bomb))
    }

    @Test
    fun testDetectColorBombColorBomb() {
        val bomb1 = CandyTile(1L, CandyType.EMPTY, 6, 6, SpecialCandyType.COLOR_BOMB)
        val bomb2 = CandyTile(2L, CandyType.EMPTY, 6, 7, SpecialCandyType.COLOR_BOMB)

        assertEquals(SpecialCombinationType.COLOR_BOMB_COLOR_BOMB, SpecialCombinationResolver.detectCombination(bomb1, bomb2))
    }

    @Test
    fun testRejectNormalNormal() {
        val tileA = CandyTile(1L, CandyType.RED, 0, 0)
        val tileB = CandyTile(2L, CandyType.BLUE, 0, 1)

        assertEquals(SpecialCombinationType.NONE, SpecialCombinationResolver.detectCombination(tileA, tileB))
    }

    @Test
    fun testCanCombineOnBoard() {
        var board = Match3Board.createEmpty(8, 8)
        val bomb = CandyTile(1L, CandyType.EMPTY, 3, 3, SpecialCandyType.COLOR_BOMB)
        val striped = CandyTile(2L, CandyType.RED, 3, 4, SpecialCandyType.HORIZONTAL_STRIPED)
        val farTile = CandyTile(3L, CandyType.GREEN, 0, 0, SpecialCandyType.WRAPPED)

        board = board.withTile(bomb)
        board = board.withTile(striped)
        board = board.withTile(farTile)

        // Adjacent combination
        assertTrue(SpecialCombinationResolver.canCombine(board, BoardPosition(3, 3), BoardPosition(3, 4)))
        // Non-adjacent combination
        assertFalse(SpecialCombinationResolver.canCombine(board, BoardPosition(3, 3), BoardPosition(0, 0)))
    }
}
