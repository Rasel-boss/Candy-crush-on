package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SpecialCandyCreationTest {

    @Before
    fun setUp() {
        SpecialCandyResolver.resetIdCounter()
    }

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

    // 1. Horizontal 4-match creates HORIZONTAL_STRIPED.
    @Test
    fun `1 - Horizontal 4-match creates HORIZONTAL_STRIPED`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.RED,
                positions = listOf(
                    BoardPosition(0, 0),
                    BoardPosition(0, 1),
                    BoardPosition(0, 2),
                    BoardPosition(0, 3)
                ),
                isHorizontal = true
            )
        )
        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(matches, swapPosA = BoardPosition(0, 1))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.HORIZONTAL_STRIPED, creations[0].specialType)
        assertEquals(BoardPosition(0, 1), creations[0].position)
    }

    // 2. Vertical 4-match creates VERTICAL_STRIPED.
    @Test
    fun `2 - Vertical 4-match creates VERTICAL_STRIPED`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.BLUE,
                positions = listOf(
                    BoardPosition(0, 0),
                    BoardPosition(1, 0),
                    BoardPosition(2, 0),
                    BoardPosition(3, 0)
                ),
                isHorizontal = false
            )
        )
        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(matches, swapPosA = BoardPosition(2, 0))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.VERTICAL_STRIPED, creations[0].specialType)
        assertEquals(BoardPosition(2, 0), creations[0].position)
    }

    // 3. Special candy keeps the correct base color.
    @Test
    fun `3 - Special candy keeps the correct base color`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.GREEN,
                positions = listOf(
                    BoardPosition(1, 0),
                    BoardPosition(1, 1),
                    BoardPosition(1, 2),
                    BoardPosition(1, 3)
                ),
                isHorizontal = true
            )
        )
        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(matches)
        assertEquals(1, creations.size)
        assertEquals(CandyType.GREEN, creations[0].baseType)

        val tile = SpecialCandyResolver.createSpecialTile(creations[0].position, creations[0].specialType, creations[0].baseType)
        assertEquals(CandyType.GREEN, tile.type)
        assertEquals(SpecialCandyType.HORIZONTAL_STRIPED, tile.specialCandyType)
    }

    // 4. Special candy has a unique ID.
    @Test
    fun `4 - Special candy has a unique ID`() {
        val tile1 = SpecialCandyResolver.createSpecialTile(BoardPosition(0, 0), SpecialCandyType.HORIZONTAL_STRIPED, CandyType.RED)
        val tile2 = SpecialCandyResolver.createSpecialTile(BoardPosition(1, 1), SpecialCandyType.VERTICAL_STRIPED, CandyType.BLUE)
        assertNotEquals(tile1.id, tile2.id)
    }

    // 5. Straight 5-match creates COLOR_BOMB.
    @Test
    fun `5 - Straight 5-match creates COLOR_BOMB`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.YELLOW,
                positions = listOf(
                    BoardPosition(2, 0),
                    BoardPosition(2, 1),
                    BoardPosition(2, 2),
                    BoardPosition(2, 3),
                    BoardPosition(2, 4)
                ),
                isHorizontal = true
            )
        )
        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(matches, swapPosA = BoardPosition(2, 3))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.COLOR_BOMB, creations[0].specialType)
        assertEquals(BoardPosition(2, 3), creations[0].position)
    }

    // 6. Color Bomb does not depend on a normal candy color.
    @Test
    fun `6 - Color Bomb does not depend on a normal candy color`() {
        val bombTile = SpecialCandyResolver.createSpecialTile(BoardPosition(0, 0), SpecialCandyType.COLOR_BOMB, CandyType.PURPLE)
        assertEquals(SpecialCandyType.COLOR_BOMB, bombTile.specialCandyType)
        assertTrue(bombTile.isSpecial)
        assertTrue(bombTile.isPlayable)
    }

    // 7. L-shaped match creates WRAPPED.
    @Test
    fun `7 - L-shaped match creates WRAPPED`() {
        // Horizontal: (2, 0), (2, 1), (2, 2)
        // Vertical: (0, 2), (1, 2), (2, 2)
        // Intersection at (2, 2), total 5 distinct tiles
        val matches = listOf(
            SingleMatch(
                type = CandyType.PURPLE,
                positions = listOf(BoardPosition(2, 0), BoardPosition(2, 1), BoardPosition(2, 2)),
                isHorizontal = true
            ),
            SingleMatch(
                type = CandyType.PURPLE,
                positions = listOf(BoardPosition(0, 2), BoardPosition(1, 2), BoardPosition(2, 2)),
                isHorizontal = false
            )
        )
        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(matches, swapPosA = BoardPosition(2, 2))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.WRAPPED, creations[0].specialType)
        assertEquals(CandyType.PURPLE, creations[0].baseType)
        assertEquals(BoardPosition(2, 2), creations[0].position)
    }

    // 8. T-shaped match creates WRAPPED.
    @Test
    fun `8 - T-shaped match creates WRAPPED`() {
        // Horizontal: (1, 0), (1, 1), (1, 2)
        // Vertical: (0, 1), (1, 1), (2, 1)
        // Intersection at (1, 1), total 5 distinct tiles
        val matches = listOf(
            SingleMatch(
                type = CandyType.ORANGE,
                positions = listOf(BoardPosition(1, 0), BoardPosition(1, 1), BoardPosition(1, 2)),
                isHorizontal = true
            ),
            SingleMatch(
                type = CandyType.ORANGE,
                positions = listOf(BoardPosition(0, 1), BoardPosition(1, 1), BoardPosition(2, 1)),
                isHorizontal = false
            )
        )
        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(matches, swapPosA = BoardPosition(1, 1))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.WRAPPED, creations[0].specialType)
        assertEquals(CandyType.ORANGE, creations[0].baseType)
        assertEquals(BoardPosition(1, 1), creations[0].position)
    }

    // 9. Normal 3-match does not create a special candy.
    @Test
    fun `9 - Normal 3-match does not create a special candy`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.RED,
                positions = listOf(BoardPosition(0, 0), BoardPosition(0, 1), BoardPosition(0, 2)),
                isHorizontal = true
            )
        )
        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(matches, swapPosA = BoardPosition(0, 0))
        assertTrue(creations.isEmpty())
    }

    // 10. Special creation does not happen randomly.
    @Test
    fun `10 - Special creation does not happen randomly`() {
        val emptyMatches = emptyList<SingleMatch>()
        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(emptyMatches)
        assertTrue(creations.isEmpty())
    }
}
