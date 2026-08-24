package com.example.game.logic

import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CascadeResolverTest {

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
    fun `1 - Single step match resolution flow`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val step = MatchResolver.resolveSingleStep(board, Random(42))

        assertTrue(step != null)
        assertEquals(1, step!!.matches.size)
        assertEquals(30, step.stepScore)
        assertEquals(3, step.matchedPositions.size)

        // After removal, row 0 first 3 are EMPTY
        assertEquals(CandyType.EMPTY, step.boardAfterRemoval.getTile(0, 0)?.type)

        // After gravity, bottom tiles shifted
        assertEquals(CandyType.GREEN, step.boardAfterGravity.getTile(1, 0)?.type)

        // After refill, no EMPTY tiles
        assertEquals(0, step.boardAfterRefill.allTiles.count { it.type == CandyType.EMPTY })
    }

    @Test
    fun `2 - Deterministic cascade chain resolves until stable`() {
        // Board where row 0 has RED, RED, RED
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val result = MatchResolver.resolveAllCascades(board, Random(42))

        assertTrue(result.steps.isNotEmpty())
        assertTrue(result.totalScoreGained >= 30)
        assertTrue(result.isStable)
        assertEquals(0, result.finalBoard.allTiles.count { it.type == CandyType.EMPTY })
        assertFalse(MatchDetector.hasAnyMatches(result.finalBoard))
    }

    @Test
    fun `3 - 4-match awards 60 points and 5-match awards 100 points`() {
        val grid4 = listOf(
            listOf(CandyType.BLUE, CandyType.BLUE, CandyType.BLUE, CandyType.BLUE),
            listOf(CandyType.RED, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.YELLOW)
        )
        val board4 = createCustomBoard(grid4)
        val step4 = MatchResolver.resolveSingleStep(board4, random = Random(42))
        assertEquals(60, step4?.stepScore)

        val grid5 = listOf(
            listOf(CandyType.YELLOW, CandyType.YELLOW, CandyType.YELLOW, CandyType.YELLOW, CandyType.YELLOW),
            listOf(CandyType.RED, CandyType.GREEN, CandyType.BLUE, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.BLUE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.YELLOW, CandyType.GREEN),
            listOf(CandyType.BLUE, CandyType.PURPLE, CandyType.GREEN, CandyType.ORANGE, CandyType.RED)
        )
        val board5 = createCustomBoard(grid5)
        val step5 = MatchResolver.resolveSingleStep(board5, random = Random(42))
        assertEquals(100, step5?.stepScore)
    }

    @Test
    fun `4 - Final board contains 64 playable tiles on 8x8`() {
        val emptyBoard = Match3Board.createEmpty(8, 8)
        val initialFilled = BoardRefiller.refillBoard(emptyBoard, Random(99))
        val result = MatchResolver.resolveAllCascades(initialFilled, Random(99))

        assertEquals(8, result.finalBoard.rows)
        assertEquals(8, result.finalBoard.columns)
        assertEquals(64, result.finalBoard.allTiles.count { it.type.isPlayable })
        assertEquals(0, result.finalBoard.allTiles.count { it.type == CandyType.EMPTY })
    }
}
