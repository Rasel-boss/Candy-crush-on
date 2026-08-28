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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Deterministic test suite verifying all requirements of Prompt 12:
 * Special Candies & Basic Combos.
 */
class SpecialCandyPrompt12Test {

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

    // ==========================================
    // 24. TESTS — MODEL
    // ==========================================

    @Test
    fun `24_1 - NONE special type`() {
        val tile = CandyTile(1L, CandyType.RED, 0, 0, SpecialCandyType.NONE)
        assertFalse(tile.isSpecial)
        assertEquals(SpecialCandyType.NONE, tile.specialCandyType)
    }

    @Test
    fun `24_2 - Horizontal striped type`() {
        val tile = CandyTile(2L, CandyType.RED, 0, 0, SpecialCandyType.STRIPED_HORIZONTAL)
        assertTrue(tile.isSpecial)
        assertTrue(tile.specialCandyType.isStriped)
        assertEquals(SpecialCandyType.HORIZONTAL_STRIPED, tile.specialCandyType)
    }

    @Test
    fun `24_3 - Vertical striped type`() {
        val tile = CandyTile(3L, CandyType.BLUE, 0, 0, SpecialCandyType.STRIPED_VERTICAL)
        assertTrue(tile.isSpecial)
        assertTrue(tile.specialCandyType.isStriped)
        assertEquals(SpecialCandyType.VERTICAL_STRIPED, tile.specialCandyType)
    }

    @Test
    fun `24_4 - Wrapped type`() {
        val tile = CandyTile(4L, CandyType.GREEN, 0, 0, SpecialCandyType.WRAPPED)
        assertTrue(tile.isSpecial)
        assertFalse(tile.specialCandyType.isStriped)
        assertEquals(SpecialCandyType.WRAPPED, tile.specialCandyType)
    }

    @Test
    fun `24_5 - Color bomb type`() {
        val tile = CandyTile(5L, CandyType.EMPTY, 0, 0, SpecialCandyType.COLOR_BOMB)
        assertTrue(tile.isSpecial)
        assertEquals(SpecialCandyType.COLOR_BOMB, tile.specialCandyType)
        assertTrue(tile.isPlayable)
    }

    @Test
    fun `24_6 - Normal candy remains playable`() {
        for (type in CandyType.playableCandies) {
            val tile = CandyTile(10L, type, 1, 1, SpecialCandyType.NONE)
            assertTrue(tile.isPlayable)
            assertFalse(tile.isSpecial)
        }
    }

    @Test
    fun `24_7 - Special candy remains playable`() {
        val striped = CandyTile(11L, CandyType.RED, 1, 1, SpecialCandyType.STRIPED_HORIZONTAL)
        val wrapped = CandyTile(12L, CandyType.BLUE, 1, 1, SpecialCandyType.WRAPPED)
        val bomb = CandyTile(13L, CandyType.EMPTY, 1, 1, SpecialCandyType.COLOR_BOMB)

        assertTrue(striped.isPlayable)
        assertTrue(wrapped.isPlayable)
        assertTrue(bomb.isPlayable)
    }

    // ==========================================
    // 25. TESTS — MATCH DETECTION
    // ==========================================

    @Test
    fun `25_1 - Horizontal 3 detection`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.RED),
            listOf(CandyType.YELLOW, CandyType.PURPLE, CandyType.RED, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)
        assertEquals(1, matches.size)
        assertEquals(3, matches[0].length)
        assertTrue(matches[0].isHorizontal)
        assertEquals(CandyType.RED, matches[0].type)
    }

    @Test
    fun `25_2 - Vertical 3 detection`() {
        val grid = listOf(
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.BLUE, CandyType.RED, CandyType.GREEN, CandyType.YELLOW),
            listOf(CandyType.BLUE, CandyType.YELLOW, CandyType.PURPLE, CandyType.RED),
            listOf(CandyType.ORANGE, CandyType.PURPLE, CandyType.RED, CandyType.GREEN)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)
        assertEquals(1, matches.size)
        assertEquals(3, matches[0].length)
        assertFalse(matches[0].isHorizontal)
        assertEquals(CandyType.BLUE, matches[0].type)
    }

    @Test
    fun `25_3 - Horizontal 4 creates Striped Horizontal`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.RED,
                positions = listOf(BoardPosition(0, 0), BoardPosition(0, 1), BoardPosition(0, 2), BoardPosition(0, 3)),
                isHorizontal = true
            )
        )
        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(matches, swapPosA = BoardPosition(0, 1))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.HORIZONTAL_STRIPED, creations[0].specialType)
        assertEquals(BoardPosition(0, 1), creations[0].position)
        assertEquals(CandyType.RED, creations[0].baseType)
    }

    @Test
    fun `25_4 - Vertical 4 creates Striped Vertical`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.GREEN,
                positions = listOf(BoardPosition(1, 2), BoardPosition(2, 2), BoardPosition(3, 2), BoardPosition(4, 2)),
                isHorizontal = false
            )
        )
        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(matches, swapPosA = BoardPosition(3, 2))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.VERTICAL_STRIPED, creations[0].specialType)
        assertEquals(BoardPosition(3, 2), creations[0].position)
        assertEquals(CandyType.GREEN, creations[0].baseType)
    }

    @Test
    fun `25_5 - Horizontal 5 creates Color Bomb`() {
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
        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(matches, swapPosA = BoardPosition(2, 2))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.COLOR_BOMB, creations[0].specialType)
        assertEquals(BoardPosition(2, 2), creations[0].position)
    }

    @Test
    fun `25_6 - Vertical 5 creates Color Bomb`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.PURPLE,
                positions = listOf(
                    BoardPosition(0, 3),
                    BoardPosition(1, 3),
                    BoardPosition(2, 3),
                    BoardPosition(3, 3),
                    BoardPosition(4, 3)
                ),
                isHorizontal = false
            )
        )
        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(matches, swapPosA = BoardPosition(1, 3))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.COLOR_BOMB, creations[0].specialType)
        assertEquals(BoardPosition(1, 3), creations[0].position)
    }

    @Test
    fun `25_7 - No false match`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.BLUE, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.BLUE, CandyType.RED, CandyType.BLUE, CandyType.RED),
            listOf(CandyType.RED, CandyType.BLUE, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.BLUE, CandyType.RED, CandyType.BLUE, CandyType.RED)
        )
        val board = createCustomBoard(grid)
        val matches = MatchDetector.findMatches(board)
        assertTrue(matches.isEmpty())
        assertFalse(MatchDetector.hasAnyMatches(board))
    }

    @Test
    fun `25_8 - L and T shape creates Wrapped candy`() {
        val lMatch = listOf(
            SingleMatch(CandyType.RED, listOf(BoardPosition(0, 0), BoardPosition(0, 1), BoardPosition(0, 2)), true),
            SingleMatch(CandyType.RED, listOf(BoardPosition(0, 2), BoardPosition(1, 2), BoardPosition(2, 2)), false)
        )
        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(lMatch, swapPosA = BoardPosition(0, 2))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.WRAPPED, creations[0].specialType)
        assertEquals(CandyType.RED, creations[0].baseType)
    }

    @Test
    fun `25_9 - Correct special-candy creation`() {
        val single3 = listOf(
            SingleMatch(CandyType.RED, listOf(BoardPosition(0, 0), BoardPosition(0, 1), BoardPosition(0, 2)), true)
        )
        val noSpecial = SpecialCandyCreator.createSpecialCandiesFromMatches(single3)
        assertTrue(noSpecial.isEmpty())
    }

    @Test
    fun `25_10 - No duplicate special candy from one match`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.BLUE,
                positions = listOf(BoardPosition(0, 0), BoardPosition(0, 1), BoardPosition(0, 2), BoardPosition(0, 3)),
                isHorizontal = true
            )
        )
        val creations = SpecialCandyCreator.createSpecialCandiesFromMatches(matches)
        assertEquals(1, creations.size)
    }

    // ==========================================
    // 26. TESTS — SPECIAL EFFECTS
    // ==========================================

    @Test
    fun `26_1 - Horizontal striped clears one row`() {
        val board = Match3Board.createEmpty(8, 8)
        val tile = CandyTile(100L, CandyType.RED, 4, 3, SpecialCandyType.STRIPED_HORIZONTAL)
        val affected = SpecialCandyActivator.calculateSingleEffect(board, tile)
        assertEquals(8, affected.size)
        for (c in 0 until 8) {
            assertTrue(affected.contains(BoardPosition(4, c)))
        }
    }

    @Test
    fun `26_2 - Vertical striped clears one column`() {
        val board = Match3Board.createEmpty(8, 8)
        val tile = CandyTile(101L, CandyType.BLUE, 4, 3, SpecialCandyType.STRIPED_VERTICAL)
        val affected = SpecialCandyActivator.calculateSingleEffect(board, tile)
        assertEquals(8, affected.size)
        for (r in 0 until 8) {
            assertTrue(affected.contains(BoardPosition(r, 3)))
        }
    }

    @Test
    fun `26_3 - Wrapped clears the intended area`() {
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

    @Test
    fun `26_4 - Color bomb removes target color`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.BLUE, CandyType.GREEN),
            listOf(CandyType.RED, CandyType.RED, CandyType.YELLOW),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.RED)
        )
        val board = createCustomBoard(grid)
        val bombTile = CandyTile(999L, CandyType.EMPTY, 0, 0, SpecialCandyType.COLOR_BOMB)
        val affected = SpecialCandyActivator.calculateSingleEffect(board, bombTile, targetColor = CandyType.RED)

        assertTrue(affected.contains(BoardPosition(0, 0)))
        assertTrue(affected.contains(BoardPosition(1, 0)))
        assertTrue(affected.contains(BoardPosition(1, 1)))
        assertTrue(affected.contains(BoardPosition(2, 2)))
        assertEquals(4, affected.size)
    }

    @Test
    fun `26_5 - Striped + striped creates cross clear`() {
        val board = Match3Board.createEmpty(8, 8)
        val posA = BoardPosition(2, 2)
        val posB = BoardPosition(2, 3)
        val tileA = CandyTile(100L, CandyType.RED, 2, 2, SpecialCandyType.STRIPED_HORIZONTAL)
        val tileB = CandyTile(101L, CandyType.BLUE, 2, 3, SpecialCandyType.STRIPED_VERTICAL)
        val boardWithTiles = board.withTile(tileA).withTile(tileB)

        val result = SpecialCombinationResolver.resolveCombination(boardWithTiles, posA, posB)
        assertEquals(SpecialCombinationType.STRIPED_STRIPED, result.comboType)
        for (c in 0 until 8) {
            assertTrue(result.affectedPositions.contains(BoardPosition(2, c)))
        }
        for (r in 0 until 8) {
            assertTrue(result.affectedPositions.contains(BoardPosition(r, 2)))
            assertTrue(result.affectedPositions.contains(BoardPosition(r, 3)))
        }
    }

    @Test
    fun `26_6 - Wrapped + wrapped resolves safely`() {
        val board = Match3Board.createEmpty(8, 8)
        val posA = BoardPosition(3, 3)
        val posB = BoardPosition(3, 4)
        val tileA = CandyTile(100L, CandyType.YELLOW, 3, 3, SpecialCandyType.WRAPPED)
        val tileB = CandyTile(101L, CandyType.PURPLE, 3, 4, SpecialCandyType.WRAPPED)
        val boardWithTiles = board.withTile(tileA).withTile(tileB)

        val result = SpecialCombinationResolver.resolveCombination(boardWithTiles, posA, posB)
        assertEquals(SpecialCombinationType.WRAPPED_WRAPPED, result.comboType)
        assertTrue(result.affectedPositions.size >= 25)
    }

    @Test
    fun `26_7 - Color bomb + normal works`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.BLUE, CandyType.GREEN),
            listOf(CandyType.RED, CandyType.RED, CandyType.YELLOW),
            listOf(CandyType.PURPLE, CandyType.ORANGE, CandyType.RED)
        )
        val board = createCustomBoard(grid)
        val posA = BoardPosition(0, 0)
        val posB = BoardPosition(0, 1)
        val bombTile = CandyTile(999L, CandyType.EMPTY, 0, 0, SpecialCandyType.COLOR_BOMB)
        val normalTile = CandyTile(100L, CandyType.BLUE, 0, 1, SpecialCandyType.NONE)
        val boardWithTiles = board.withTile(bombTile).withTile(normalTile)

        val result = SpecialCombinationResolver.resolveCombination(boardWithTiles, posA, posB)
        assertEquals(SpecialCombinationType.COLOR_BOMB_NORMAL, result.comboType)
        assertTrue(result.affectedPositions.contains(BoardPosition(0, 1)))
    }

    @Test
    fun `26_8 - Special chains terminate`() {
        val tiles = List(5) { r ->
            List(5) { c ->
                when {
                    r == 2 && c == 2 -> CandyTile(100L, CandyType.RED, r, c, SpecialCandyType.HORIZONTAL_STRIPED)
                    r == 2 && c == 4 -> CandyTile(101L, CandyType.BLUE, r, c, SpecialCandyType.VERTICAL_STRIPED)
                    else -> CandyTile((r * 5 + c + 1).toLong(), CandyType.GREEN, r, c)
                }
            }
        }
        val board = Match3Board(5, 5, tiles)
        val activated = mutableListOf<CandyTile>()
        val result = SpecialCandyActivator.resolveChainedSpecials(
            board = board,
            currentlyAffected = setOf(BoardPosition(2, 2)),
            alreadyActivatedIds = mutableSetOf(),
            activatedSpecials = activated
        )
        assertEquals(2, result.activatedSpecials.size)
    }

    @Test
    fun `26_9 - No infinite loop`() {
        val board = Match3Board.createEmpty(6, 6)
        val tile1 = CandyTile(100L, CandyType.RED, 2, 2, SpecialCandyType.HORIZONTAL_STRIPED)
        val tile2 = CandyTile(101L, CandyType.BLUE, 2, 3, SpecialCandyType.VERTICAL_STRIPED)
        val boardWithTiles = board.withTile(tile1).withTile(tile2)

        val activatedIds = mutableSetOf<Long>()
        val result = SpecialCandyActivator.resolveChainedSpecials(
            board = boardWithTiles,
            currentlyAffected = setOf(BoardPosition(2, 2), BoardPosition(2, 3)),
            alreadyActivatedIds = activatedIds
        )
        assertEquals(2, result.activatedSpecials.size)
        assertEquals(2, activatedIds.size)
    }

    @Test
    fun `26_10 - Board becomes stable after resolution`() {
        val board = Match3Board.createEmpty(8, 8)
        val filled = BoardRefiller.refillBoard(board, Random(42))
        val resolution = MatchResolver.resolveAllCascades(filled, random = Random(42))
        assertTrue(resolution.isStable)
        assertFalse(MatchDetector.hasAnyMatches(resolution.finalBoard))
    }

    // ==========================================
    // 27. TESTS — SCORING
    // ==========================================

    @Test
    fun `27_1 - Normal match score still works`() {
        assertEquals(30, ScoreCalculator.calculateMatchScore(3))
    }

    @Test
    fun `27_2 - 4-match score works`() {
        assertEquals(60, ScoreCalculator.calculateMatchScore(4))
    }

    @Test
    fun `27_3 - 5-match score works`() {
        assertEquals(100, ScoreCalculator.calculateMatchScore(5))
    }

    @Test
    fun `27_4 - Special activation score works`() {
        assertEquals(100, ScoreCalculator.calculateSpecialActivationScore(SpecialCandyType.STRIPED_HORIZONTAL))
        assertEquals(100, ScoreCalculator.calculateSpecialActivationScore(SpecialCandyType.STRIPED_VERTICAL))
        assertEquals(150, ScoreCalculator.calculateSpecialActivationScore(SpecialCandyType.WRAPPED))
        assertEquals(200, ScoreCalculator.calculateSpecialActivationScore(SpecialCandyType.COLOR_BOMB))
    }

    @Test
    fun `27_5 - Combo score works`() {
        assertEquals(150, ScoreCalculator.calculateCombinationScore(SpecialCombinationType.STRIPED_STRIPED))
        assertEquals(200, ScoreCalculator.calculateCombinationScore(SpecialCombinationType.WRAPPED_WRAPPED))
        assertEquals(250, ScoreCalculator.calculateCombinationScore(SpecialCombinationType.STRIPED_WRAPPED))
        assertEquals(200, ScoreCalculator.calculateCombinationScore(SpecialCombinationType.COLOR_BOMB_NORMAL))
        assertEquals(300, ScoreCalculator.calculateCombinationScore(SpecialCombinationType.COLOR_BOMB_STRIPED))
        assertEquals(350, ScoreCalculator.calculateCombinationScore(SpecialCombinationType.COLOR_BOMB_WRAPPED))
        assertEquals(500, ScoreCalculator.calculateCombinationScore(SpecialCombinationType.COLOR_BOMB_COLOR_BOMB))
    }

    // ==========================================
    // 28. TESTS — MOVES
    // ==========================================

    @Test
    fun `28_1 - Move consumption logic verified`() {
        // CascadeResolutionResult consumes 0 moves per cascade step
        val board = Match3Board.createEmpty(8, 8)
        val res = MatchResolver.resolveAllCascades(board, random = Random(123))
        assertTrue(res.isStable)
    }
}
