package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BoardValidatorTest {

    @Test
    fun `isInsideBoard validates board boundaries correctly`() {
        assertTrue(BoardValidator.isInsideBoard(0, 0, 8, 8))
        assertTrue(BoardValidator.isInsideBoard(7, 7, 8, 8))
        assertTrue(BoardValidator.isInsideBoard(3, 4, 8, 8))

        assertFalse(BoardValidator.isInsideBoard(-1, 0, 8, 8))
        assertFalse(BoardValidator.isInsideBoard(0, -1, 8, 8))
        assertFalse(BoardValidator.isInsideBoard(8, 0, 8, 8))
        assertFalse(BoardValidator.isInsideBoard(0, 8, 8, 8))
    }

    @Test
    fun `isValidPosition validates BoardPosition boundaries correctly`() {
        assertTrue(BoardValidator.isValidPosition(BoardPosition(0, 0)))
        assertTrue(BoardValidator.isValidPosition(BoardPosition(7, 7)))

        assertFalse(BoardValidator.isValidPosition(BoardPosition(-1, 4)))
        assertFalse(BoardValidator.isValidPosition(BoardPosition(4, 8)))
    }

    @Test
    fun `isBoardValid passes on generated valid board`() {
        val board = BoardGenerator.generateBoard(8, 8, Random(42))
        assertTrue(BoardValidator.isBoardValid(board, 8, 8))
    }

    @Test
    fun `isBoardValid fails if board contains EMPTY candy`() {
        val board = BoardGenerator.generateBoard(8, 8, Random(42))
        val modifiedTiles = board.tiles.map { row ->
            row.map { tile ->
                if (tile.row == 0 && tile.column == 0) {
                    tile.copy(type = CandyType.EMPTY)
                } else tile
            }
        }
        val corruptedBoard = board.copy(tiles = modifiedTiles)
        assertFalse(BoardValidator.isBoardValid(corruptedBoard))
    }
}
