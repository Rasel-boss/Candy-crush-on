package com.example.game.logic

import com.example.game.model.CandyType
import com.example.game.model.DEFAULT_COLUMNS
import com.example.game.model.DEFAULT_ROWS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BoardGeneratorTest {

    @Test
    fun `6 - generated board has correct dimensions`() {
        val board = BoardGenerator.generateBoard(rows = 8, columns = 8, random = Random(123))
        assertEquals(8, board.rows)
        assertEquals(8, board.columns)
        assertEquals(DEFAULT_ROWS, board.rows)
        assertEquals(DEFAULT_COLUMNS, board.columns)
        assertEquals(8, board.tiles.size)
        assertTrue(board.tiles.all { it.size == 8 })
    }

    @Test
    fun `7 - generated board contains no initial horizontal matches`() {
        for (seed in 1..25) {
            val board = BoardGenerator.generateBoard(8, 8, Random(seed))
            for (r in 0 until board.rows) {
                for (c in 0 until board.columns - 2) {
                    val t1 = board.getTile(r, c)!!.type
                    val t2 = board.getTile(r, c + 1)!!.type
                    val t3 = board.getTile(r, c + 2)!!.type

                    val isHorizontalMatch = (t1 == t2 && t2 == t3 && t1.isPlayable)
                    assertFalse("Found horizontal match at row $r starting col $c with seed $seed", isHorizontalMatch)
                }
            }
        }
    }

    @Test
    fun `8 - generated board contains no initial vertical matches`() {
        for (seed in 1..25) {
            val board = BoardGenerator.generateBoard(8, 8, Random(seed))
            for (c in 0 until board.columns) {
                for (r in 0 until board.rows - 2) {
                    val t1 = board.getTile(r, c)!!.type
                    val t2 = board.getTile(r + 1, c)!!.type
                    val t3 = board.getTile(r + 2, c)!!.type

                    val isVerticalMatch = (t1 == t2 && t2 == t3 && t1.isPlayable)
                    assertFalse("Found vertical match at col $c starting row $r with seed $seed", isVerticalMatch)
                }
            }
        }
    }

    @Test
    fun `9 - generated board contains only valid playable candy types`() {
        val board = BoardGenerator.generateBoard(8, 8, Random(999))
        for (tile in board.allTiles) {
            assertTrue(tile.isPlayable)
            assertNotEquals(CandyType.EMPTY, tile.type)
            assertTrue(CandyType.PLAYABLE_TYPES.contains(tile.type))
        }
    }

    @Test
    fun `10 - multiple generated boards can differ`() {
        val boardA = BoardGenerator.generateBoard(8, 8, Random(101))
        val boardB = BoardGenerator.generateBoard(8, 8, Random(202))

        val typesA = boardA.allTiles.map { it.type }
        val typesB = boardB.allTiles.map { it.type }

        assertNotEquals(typesA, typesB)
    }
}
