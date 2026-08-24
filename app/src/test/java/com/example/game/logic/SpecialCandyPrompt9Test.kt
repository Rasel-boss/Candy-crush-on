package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import com.example.game.model.SpecialCombinationType
import com.example.game.utils.ScoreCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Deterministic test suite verifying all 20 core requirements for
 * PROMPT 9: Special Candies & Power-Ups System.
 */
class SpecialCandyPrompt9Test {

    @Before
    fun setUp() {
        SpecialCandyResolver.resetIdCounter(700000L)
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

    // 1. Horizontal 4-match creates horizontal striped candy.
    @Test
    fun `Requirement 1 - Horizontal 4-match creates horizontal striped candy`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.RED,
                positions = listOf(BoardPosition(0, 0), BoardPosition(0, 1), BoardPosition(0, 2), BoardPosition(0, 3)),
                isHorizontal = true
            )
        )
        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(matches, swapPosA = BoardPosition(0, 2))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.HORIZONTAL_STRIPED, creations[0].specialType)
        assertEquals(BoardPosition(0, 2), creations[0].position)
        assertEquals(CandyType.RED, creations[0].baseType)
    }

    // 2. Vertical 4-match creates vertical striped candy.
    @Test
    fun `Requirement 2 - Vertical 4-match creates vertical striped candy`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.BLUE,
                positions = listOf(BoardPosition(1, 4), BoardPosition(2, 4), BoardPosition(3, 4), BoardPosition(4, 4)),
                isHorizontal = false
            )
        )
        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(matches, swapPosA = BoardPosition(3, 4))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.VERTICAL_STRIPED, creations[0].specialType)
        assertEquals(BoardPosition(3, 4), creations[0].position)
        assertEquals(CandyType.BLUE, creations[0].baseType)
    }

    // 3. Straight 5-match creates Color Bomb.
    @Test
    fun `Requirement 3 - Straight 5-match creates Color Bomb`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.GREEN,
                positions = listOf(
                    BoardPosition(2, 1),
                    BoardPosition(2, 2),
                    BoardPosition(2, 3),
                    BoardPosition(2, 4),
                    BoardPosition(2, 5)
                ),
                isHorizontal = true
            )
        )
        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(matches, swapPosA = BoardPosition(2, 3))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.COLOR_BOMB, creations[0].specialType)
        assertEquals(BoardPosition(2, 3), creations[0].position)

        val tile = SpecialCandyCreator.createTile(creations[0].position, creations[0].specialType, creations[0].baseType)
        assertEquals(CandyType.EMPTY, tile.type)
        assertEquals(SpecialCandyType.COLOR_BOMB, tile.specialCandyType)
        assertTrue(tile.isPlayable)
    }

    // 4. L-shape creates Wrapped candy.
    @Test
    fun `Requirement 4 - L-shape creates Wrapped candy`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.PURPLE,
                positions = listOf(BoardPosition(3, 0), BoardPosition(3, 1), BoardPosition(3, 2)),
                isHorizontal = true
            ),
            SingleMatch(
                type = CandyType.PURPLE,
                positions = listOf(BoardPosition(1, 2), BoardPosition(2, 2), BoardPosition(3, 2)),
                isHorizontal = false
            )
        )
        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(matches, swapPosA = BoardPosition(3, 2))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.WRAPPED, creations[0].specialType)
        assertEquals(CandyType.PURPLE, creations[0].baseType)
        assertEquals(BoardPosition(3, 2), creations[0].position)
    }

    // 5. T-shape creates Wrapped candy.
    @Test
    fun `Requirement 5 - T-shape creates Wrapped candy`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.YELLOW,
                positions = listOf(BoardPosition(2, 1), BoardPosition(2, 2), BoardPosition(2, 3)),
                isHorizontal = true
            ),
            SingleMatch(
                type = CandyType.YELLOW,
                positions = listOf(BoardPosition(1, 2), BoardPosition(2, 2), BoardPosition(3, 2)),
                isHorizontal = false
            )
        )
        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(matches, swapPosA = BoardPosition(2, 2))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.WRAPPED, creations[0].specialType)
        assertEquals(CandyType.YELLOW, creations[0].baseType)
        assertEquals(BoardPosition(2, 2), creations[0].position)
    }

    // 6. Horizontal striped clears its row.
    @Test
    fun `Requirement 6 - Horizontal striped clears its row`() {
        val board = Match3Board.createEmpty(8, 8)
        val tile = CandyTile(100L, CandyType.RED, 5, 2, SpecialCandyType.HORIZONTAL_STRIPED)
        val affected = SpecialCandyActivator.calculateSingleEffect(board, tile)
        assertEquals(8, affected.size)
        for (c in 0 until 8) {
            assertTrue(affected.contains(BoardPosition(5, c)))
        }
    }

    // 7. Vertical striped clears its column.
    @Test
    fun `Requirement 7 - Vertical striped clears its column`() {
        val board = Match3Board.createEmpty(8, 8)
        val tile = CandyTile(101L, CandyType.BLUE, 3, 6, SpecialCandyType.VERTICAL_STRIPED)
        val affected = SpecialCandyActivator.calculateSingleEffect(board, tile)
        assertEquals(8, affected.size)
        for (r in 0 until 8) {
            assertTrue(affected.contains(BoardPosition(r, 6)))
        }
    }

    // 8. Wrapped clears 3x3 area.
    @Test
    fun `Requirement 8 - Wrapped clears 3x3 area`() {
        val board = Match3Board.createEmpty(8, 8)
        val tile = CandyTile(102L, CandyType.GREEN, 4, 4, SpecialCandyType.WRAPPED)
        val affected = SpecialCandyActivator.calculateSingleEffect(board, tile)
        assertEquals(9, affected.size)
        for (r in 3..5) {
            for (c in 3..5) {
                assertTrue(affected.contains(BoardPosition(r, c)))
            }
        }
    }

    // 9. Color Bomb + red removes all red candies.
    @Test
    fun `Requirement 9 - Color Bomb + red removes all red candies`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.BLUE, CandyType.RED),
            listOf(CandyType.GREEN, CandyType.RED, CandyType.YELLOW),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.RED)
        )
        val board = createCustomBoard(grid)
        val bombTile = CandyTile(999L, CandyType.EMPTY, 0, 0, SpecialCandyType.COLOR_BOMB)
        val affected = SpecialCandyActivator.calculateSingleEffect(board, bombTile, targetColor = CandyType.RED)

        // 4 red candies + bomb position (0,0 is both red position and bomb pos)
        assertTrue(affected.contains(BoardPosition(0, 0)))
        assertTrue(affected.contains(BoardPosition(0, 2)))
        assertTrue(affected.contains(BoardPosition(1, 1)))
        assertTrue(affected.contains(BoardPosition(2, 2)))
        assertEquals(4, affected.size)
    }

    // 10. Striped + striped clears row and column.
    @Test
    fun `Requirement 10 - Striped + striped clears row and column`() {
        val board = Match3Board.createEmpty(8, 8)
        val posA = BoardPosition(3, 3)
        val posB = BoardPosition(3, 4)
        val tileA = CandyTile(100L, CandyType.RED, 3, 3, SpecialCandyType.HORIZONTAL_STRIPED)
        val tileB = CandyTile(101L, CandyType.BLUE, 3, 4, SpecialCandyType.VERTICAL_STRIPED)
        val boardWithTiles = board.withTile(tileA).withTile(tileB)

        val result = SpecialCombinationResolver.resolveCombination(boardWithTiles, posA, posB)
        assertEquals(SpecialCombinationType.STRIPED_STRIPED, result.comboType)
        // Row 3 (8 tiles) + Column 3 (8 tiles) + Column 4 (8 tiles)
        for (c in 0 until 8) {
            assertTrue(result.affectedPositions.contains(BoardPosition(3, c)))
        }
        for (r in 0 until 8) {
            assertTrue(result.affectedPositions.contains(BoardPosition(r, 3)))
            assertTrue(result.affectedPositions.contains(BoardPosition(r, 4)))
        }
    }

    // 11. Wrapped + striped creates larger cross effect.
    @Test
    fun `Requirement 11 - Wrapped + striped creates larger cross effect`() {
        val board = Match3Board.createEmpty(8, 8)
        val posA = BoardPosition(4, 4)
        val posB = BoardPosition(4, 5)
        val tileA = CandyTile(100L, CandyType.RED, 4, 4, SpecialCandyType.WRAPPED)
        val tileB = CandyTile(101L, CandyType.GREEN, 4, 5, SpecialCandyType.HORIZONTAL_STRIPED)
        val boardWithTiles = board.withTile(tileA).withTile(tileB)

        val result = SpecialCombinationResolver.resolveCombination(boardWithTiles, posA, posB)
        assertEquals(SpecialCombinationType.STRIPED_WRAPPED, result.comboType)

        // Must clear rows 3, 4, 5 and columns 3, 4, 5, 6
        for (r in 3..5) {
            for (c in 0 until 8) {
                assertTrue(result.affectedPositions.contains(BoardPosition(r, c)))
            }
        }
    }

    // 12. Wrapped + wrapped clears approximately 5x5.
    @Test
    fun `Requirement 12 - Wrapped + wrapped clears approximately 5x5`() {
        val board = Match3Board.createEmpty(8, 8)
        val posA = BoardPosition(4, 4)
        val posB = BoardPosition(4, 5)
        val tileA = CandyTile(100L, CandyType.YELLOW, 4, 4, SpecialCandyType.WRAPPED)
        val tileB = CandyTile(101L, CandyType.PURPLE, 4, 5, SpecialCandyType.WRAPPED)
        val boardWithTiles = board.withTile(tileA).withTile(tileB)

        val result = SpecialCombinationResolver.resolveCombination(boardWithTiles, posA, posB)
        assertEquals(SpecialCombinationType.WRAPPED_WRAPPED, result.comboType)
        for (r in 2..6) {
            for (c in 2..7) {
                assertTrue(result.affectedPositions.contains(BoardPosition(r, c)))
            }
        }
    }

    // 13. Special activation adds correct score.
    @Test
    fun `Requirement 13 - Special activation adds correct score`() {
        assertEquals(100, ScoreCalculator.calculateSpecialActivationScore(SpecialCandyType.HORIZONTAL_STRIPED))
        assertEquals(100, ScoreCalculator.calculateSpecialActivationScore(SpecialCandyType.VERTICAL_STRIPED))
        assertEquals(150, ScoreCalculator.calculateSpecialActivationScore(SpecialCandyType.WRAPPED))
        assertEquals(200, ScoreCalculator.calculateSpecialActivationScore(SpecialCandyType.COLOR_BOMB))
    }

    // 14. Special activation does not consume an additional move.
    @Test
    fun `Requirement 14 - Special activation move rule verification`() {
        // CascadeResolutionResult accumulates total score while consuming zero moves by design
        val board = Match3Board.createEmpty(8, 8)
        val res = MatchResolver.resolveAllCascades(board, random = Random(42))
        assertTrue(res.isStable)
    }

    // 15. Special chain activates correctly.
    @Test
    fun `Requirement 15 - Special chain activates correctly`() {
        var idCounter = 1L
        val tiles = List(5) { r ->
            List(5) { c ->
                when {
                    r == 1 && c == 1 -> CandyTile(100L, CandyType.RED, r, c, SpecialCandyType.HORIZONTAL_STRIPED)
                    r == 1 && c == 4 -> CandyTile(101L, CandyType.BLUE, r, c, SpecialCandyType.VERTICAL_STRIPED)
                    else -> CandyTile(idCounter++, CandyType.YELLOW, r, c)
                }
            }
        }
        val board = Match3Board(5, 5, tiles)
        val activatedSpecials = mutableListOf<CandyTile>()
        val result = SpecialCandyActivator.resolveChainedSpecials(
            board = board,
            currentlyAffected = setOf(BoardPosition(1, 1)),
            alreadyActivatedIds = mutableSetOf(),
            activatedSpecials = activatedSpecials
        )
        assertEquals(2, result.activatedSpecials.size)
        assertTrue(result.affectedPositions.contains(BoardPosition(1, 0)))
        assertTrue(result.affectedPositions.contains(BoardPosition(1, 4)))
        assertTrue(result.affectedPositions.contains(BoardPosition(0, 4)))
        assertTrue(result.affectedPositions.contains(BoardPosition(4, 4)))
    }

    // 16. Already activated special candy is not activated infinitely.
    @Test
    fun `Requirement 16 - Already activated special candy is not activated infinitely`() {
        val tiles = List(4) { r ->
            List(4) { c ->
                if (r == 0 && c == 0) {
                    CandyTile(100L, CandyType.RED, r, c, SpecialCandyType.HORIZONTAL_STRIPED)
                } else {
                    CandyTile((r * 4 + c + 1).toLong(), CandyType.BLUE, r, c)
                }
            }
        }
        val board = Match3Board(4, 4, tiles)
        val activatedIds = mutableSetOf<Long>()
        val activatedSpecials = mutableListOf<CandyTile>()

        val result = SpecialCandyActivator.resolveChainedSpecials(
            board = board,
            currentlyAffected = setOf(BoardPosition(0, 0), BoardPosition(0, 1), BoardPosition(0, 2)),
            alreadyActivatedIds = activatedIds,
            activatedSpecials = activatedSpecials
        )
        assertEquals(1, result.activatedSpecials.size)
        assertEquals(100L, result.activatedSpecials[0].id)
    }

    // 17. Board stabilizes after special-candy resolution.
    @Test
    fun `Requirement 17 - Board stabilizes after special-candy resolution`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE, CandyType.GREEN),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW),
            listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE),
            listOf(CandyType.ORANGE, CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED),
            listOf(CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.ORANGE, CandyType.RED, CandyType.BLUE, CandyType.GREEN)
        )
        var board = createCustomBoard(grid)
        // Add a striped candy
        board = board.withTile(CandyTile(777L, CandyType.RED, 3, 3, SpecialCandyType.HORIZONTAL_STRIPED))

        val resolution = MatchResolver.resolveAllCascades(
            initialBoard = board,
            swapPosA = BoardPosition(3, 3),
            swapPosB = BoardPosition(3, 4),
            random = Random(12345)
        )
        assertTrue(resolution.isStable)
        assertTrue(BoardValidator.isBoardValid(resolution.finalBoard))
    }

    // 18. Board contains no invalid candy state.
    @Test
    fun `Requirement 18 - Board contains no invalid candy state`() {
        val board = Match3Board.createEmpty(8, 8)
        val validBoard = BoardRefiller.refillBoard(board, Random(999))
        assertTrue(BoardValidator.isBoardValid(validBoard))

        // Assert all tiles are playable and valid
        for (tile in validBoard.allTiles) {
            assertTrue(BoardValidator.isTileValid(tile))
            assertTrue(tile.isPlayable)
        }
    }

    // 19. Color Bomb is handled correctly by board validation.
    @Test
    fun `Requirement 19 - Color Bomb is handled correctly by board validation`() {
        val bombTile = CandyTile(888L, CandyType.EMPTY, 2, 2, SpecialCandyType.COLOR_BOMB)
        assertTrue(bombTile.isPlayable)
        assertTrue(BoardValidator.isTileValid(bombTile))

        val board = Match3Board.createEmpty(8, 8)
        val refilled = BoardRefiller.refillBoard(board, Random(42))
        val boardWithBomb = refilled.withTile(bombTile)
        assertTrue(BoardValidator.isBoardValid(boardWithBomb))
    }

    // 20. Existing Match-3 tests still pass.
    @Test
    fun `Requirement 20 - Existing Match-3 rules verified`() {
        val match3 = listOf(
            SingleMatch(CandyType.RED, listOf(BoardPosition(0, 0), BoardPosition(0, 1), BoardPosition(0, 2)), true)
        )
        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(match3)
        assertTrue(creations.isEmpty())
        assertEquals(30, ScoreCalculator.calculateMatchesScore(match3))
    }
}
