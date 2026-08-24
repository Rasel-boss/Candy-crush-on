package com.example.game.logic

import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SpecialCandyGravityRefillTest {

    // 21. Special candies fall correctly.
    @Test
    fun `21 - Special candies fall correctly`() {
        val tiles = listOf(
            listOf(
                CandyTile(1, CandyType.RED, 0, 0, SpecialCandyType.HORIZONTAL_STRIPED),
                CandyTile(2, CandyType.BLUE, 0, 1)
            ),
            listOf(
                CandyTile(3, CandyType.EMPTY, 1, 0),
                CandyTile(4, CandyType.GREEN, 1, 1)
            ),
            listOf(
                CandyTile(5, CandyType.EMPTY, 2, 0),
                CandyTile(6, CandyType.YELLOW, 2, 1)
            )
        )
        val board = Match3Board(rows = 3, columns = 2, tiles = tiles)
        val boardAfterGravity = GravityProcessor.applyGravity(board)

        // The striped candy from (0,0) must fall to the bottom (2,0)
        val bottomTile = boardAfterGravity.getTile(2, 0)
        assertTrue(bottomTile != null)
        assertEquals(1L, bottomTile!!.id)
        assertEquals(CandyType.RED, bottomTile.type)
        assertEquals(SpecialCandyType.HORIZONTAL_STRIPED, bottomTile.specialCandyType)
    }

    // 22. Special candy type is preserved during gravity.
    @Test
    fun `22 - Special candy type is preserved during gravity`() {
        val tiles = listOf(
            listOf(CandyTile(10, CandyType.EMPTY, 0, 0, SpecialCandyType.COLOR_BOMB)),
            listOf(CandyTile(11, CandyType.EMPTY, 1, 0, SpecialCandyType.NONE))
        )
        val board = Match3Board(rows = 2, columns = 1, tiles = tiles)
        val boardAfterGravity = GravityProcessor.applyGravity(board)

        val fallenBomb = boardAfterGravity.getTile(1, 0)
        assertTrue(fallenBomb != null)
        assertEquals(SpecialCandyType.COLOR_BOMB, fallenBomb!!.specialCandyType)
    }

    // 23. Newly spawned candies have SpecialCandyType NONE.
    @Test
    fun `23 - Newly spawned candies have SpecialCandyType NONE`() {
        val emptyBoard = Match3Board.createEmpty(4, 4)
        val refilled = BoardRefiller.refillBoard(emptyBoard, Random(123))

        for (tile in refilled.allTiles) {
            assertEquals(SpecialCandyType.NONE, tile.specialCandyType)
            assertTrue(tile.type.isPlayable)
        }
    }

    // 24. Special candy unique IDs remain unique.
    @Test
    fun `24 - Special candy unique IDs remain unique`() {
        val special1 = SpecialCandyResolver.createSpecialTile(com.example.game.model.BoardPosition(0, 0), SpecialCandyType.WRAPPED, CandyType.PURPLE)
        val special2 = SpecialCandyResolver.createSpecialTile(com.example.game.model.BoardPosition(1, 1), SpecialCandyType.HORIZONTAL_STRIPED, CandyType.GREEN)
        val special3 = SpecialCandyResolver.createSpecialTile(com.example.game.model.BoardPosition(2, 2), SpecialCandyType.COLOR_BOMB, CandyType.EMPTY)

        val ids = setOf(special1.id, special2.id, special3.id)
        assertEquals(3, ids.size)
    }

    // 25. Final stable board contains no EMPTY positions.
    @Test
    fun `25 - Final stable board contains no EMPTY positions`() {
        val board = Match3Board.createEmpty(8, 8)
        val filled = BoardRefiller.refillBoard(board, Random(42))
        val result = MatchResolver.resolveAllCascades(filled, Random(42))

        assertTrue(result.isStable)
        assertEquals(64, result.finalBoard.allTiles.count { it.isPlayable })
        assertEquals(0, result.finalBoard.allTiles.count { it.isEmpty })
    }
}
