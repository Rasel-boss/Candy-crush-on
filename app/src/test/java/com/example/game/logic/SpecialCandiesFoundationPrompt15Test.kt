package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.LevelObjective
import com.example.game.model.Match3Board
import com.example.game.model.ObjectiveType
import com.example.game.model.SpecialCandyType
import com.example.game.utils.ScoreCalculator
import com.example.game.viewmodel.Match3ViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Deterministic test suite for Prompt 15:
 * Special Candies Foundation & Advanced Match Reactions.
 *
 * Covers:
 * 1. 4 horizontal match creates horizontal striped candy.
 * 2. 4 vertical match creates vertical striped candy.
 * 3. Valid T shape creates wrapped candy.
 * 4. Valid L shape creates wrapped candy.
 * 5. Straight 5 match creates Color Bomb.
 * 6. Normal 3-match does not create special candy.
 * 7. Special candy identity is preserved after board updates.
 * 8. Horizontal striped candy clears its row.
 * 9. Vertical striped candy clears its column.
 * 10. Wrapped candy clears surrounding 3x3 area.
 * 11. Color Bomb removes the selected candy type.
 * 12. Effects stay inside board boundaries (Corners, Edges, Center).
 * 13. Special candy activation can trigger a cascade.
 * 14. Cascade finishes with a stable board.
 * 15. No duplicate special activation occurs.
 * 16. Special candy activation awards the expected bonus.
 * 17. Score is not duplicated.
 * 18. Removed candies update objectives correctly.
 * 19. Special-candy removals do not double-count.
 */
class SpecialCandiesFoundationPrompt15Test {

    private lateinit var viewModel: Match3ViewModel

    @Before
    fun setUp() {
        viewModel = Match3ViewModel().apply { stepDelayMs = 0L }
        SpecialCandyResolver.resetIdCounter(800000L)
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

    // 1. 4 horizontal match creates horizontal striped candy
    @Test
    fun `1 - 4 horizontal match creates horizontal striped candy`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.RED,
                positions = listOf(BoardPosition(2, 1), BoardPosition(2, 2), BoardPosition(2, 3), BoardPosition(2, 4)),
                isHorizontal = true
            )
        )
        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(matches, swapPosA = BoardPosition(2, 2))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.HORIZONTAL_STRIPED, creations[0].specialType)
        assertEquals(CandyType.RED, creations[0].baseType)
        assertEquals(BoardPosition(2, 2), creations[0].position)
    }

    // 2. 4 vertical match creates vertical striped candy
    @Test
    fun `2 - 4 vertical match creates vertical striped candy`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.BLUE,
                positions = listOf(BoardPosition(1, 4), BoardPosition(2, 4), BoardPosition(3, 4), BoardPosition(4, 4)),
                isHorizontal = false
            )
        )
        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(matches, swapPosA = BoardPosition(3, 4))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.VERTICAL_STRIPED, creations[0].specialType)
        assertEquals(CandyType.BLUE, creations[0].baseType)
        assertEquals(BoardPosition(3, 4), creations[0].position)
    }

    // 3. Valid T shape creates wrapped candy
    @Test
    fun `3 - Valid T shape creates wrapped candy`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.GREEN,
                positions = listOf(BoardPosition(1, 1), BoardPosition(1, 2), BoardPosition(1, 3)),
                isHorizontal = true
            ),
            SingleMatch(
                type = CandyType.GREEN,
                positions = listOf(BoardPosition(1, 2), BoardPosition(2, 2), BoardPosition(3, 2)),
                isHorizontal = false
            )
        )
        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(matches, swapPosA = BoardPosition(1, 2))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.WRAPPED, creations[0].specialType)
        assertEquals(CandyType.GREEN, creations[0].baseType)
        assertEquals(BoardPosition(1, 2), creations[0].position)
    }

    // 4. Valid L shape creates wrapped candy
    @Test
    fun `4 - Valid L shape creates wrapped candy`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.YELLOW,
                positions = listOf(BoardPosition(0, 0), BoardPosition(0, 1), BoardPosition(0, 2)),
                isHorizontal = true
            ),
            SingleMatch(
                type = CandyType.YELLOW,
                positions = listOf(BoardPosition(0, 0), BoardPosition(1, 0), BoardPosition(2, 0)),
                isHorizontal = false
            )
        )
        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(matches, swapPosA = BoardPosition(0, 0))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.WRAPPED, creations[0].specialType)
        assertEquals(CandyType.YELLOW, creations[0].baseType)
        assertEquals(BoardPosition(0, 0), creations[0].position)
    }

    // 5. Straight 5 match creates Color Bomb
    @Test
    fun `5 - Straight 5 match creates Color Bomb`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.PURPLE,
                positions = listOf(
                    BoardPosition(4, 1),
                    BoardPosition(4, 2),
                    BoardPosition(4, 3),
                    BoardPosition(4, 4),
                    BoardPosition(4, 5)
                ),
                isHorizontal = true
            )
        )
        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(matches, swapPosA = BoardPosition(4, 3))
        assertEquals(1, creations.size)
        assertEquals(SpecialCandyType.COLOR_BOMB, creations[0].specialType)
        assertEquals(BoardPosition(4, 3), creations[0].position)
    }

    // 6. Normal 3-match does not create special candy
    @Test
    fun `6 - Normal 3-match does not create special candy`() {
        val matches = listOf(
            SingleMatch(
                type = CandyType.ORANGE,
                positions = listOf(BoardPosition(0, 0), BoardPosition(0, 1), BoardPosition(0, 2)),
                isHorizontal = true
            )
        )
        val creations = SpecialCandyResolver.determineCreatedSpecialCandies(matches)
        assertTrue(creations.isEmpty())
    }

    // 7. Special candy identity is preserved after board updates
    @Test
    fun `7 - Special candy identity is preserved after board updates`() {
        val board = Match3Board.createEmpty(8, 8)
        val specialTile = SpecialCandyResolver.createSpecialTile(
            position = BoardPosition(3, 3),
            specialType = SpecialCandyType.HORIZONTAL_STRIPED,
            baseType = CandyType.RED
        )
        val updatedBoard = board.withTile(specialTile)
        val retrieved = updatedBoard.getTile(3, 3)

        assertNotNull(retrieved)
        assertEquals(SpecialCandyType.HORIZONTAL_STRIPED, retrieved?.specialCandyType)
        assertEquals(CandyType.RED, retrieved?.type)
        assertTrue(retrieved?.isSpecial == true)
        assertTrue(retrieved?.isStriped == true)
    }

    // 8. Horizontal striped candy clears its row
    @Test
    fun `8 - Horizontal striped candy clears its row`() {
        val board = Match3Board.createEmpty(8, 8)
        val striped = CandyTile(
            id = 50L,
            type = CandyType.RED,
            row = 4,
            column = 2,
            specialCandyType = SpecialCandyType.HORIZONTAL_STRIPED
        )
        val affected = SpecialCandyResolver.calculateSingleSpecialEffect(board, striped)
        assertEquals(8, affected.size)
        for (col in 0 until 8) {
            assertTrue(affected.contains(BoardPosition(4, col)))
        }
    }

    // 9. Vertical striped candy clears its column
    @Test
    fun `9 - Vertical striped candy clears its column`() {
        val board = Match3Board.createEmpty(8, 8)
        val striped = CandyTile(
            id = 51L,
            type = CandyType.BLUE,
            row = 2,
            column = 6,
            specialCandyType = SpecialCandyType.VERTICAL_STRIPED
        )
        val affected = SpecialCandyResolver.calculateSingleSpecialEffect(board, striped)
        assertEquals(8, affected.size)
        for (row in 0 until 8) {
            assertTrue(affected.contains(BoardPosition(row, 6)))
        }
    }

    // 10. Wrapped candy clears surrounding 3x3 area
    @Test
    fun `10 - Wrapped candy clears surrounding 3x3 area`() {
        val board = Match3Board.createEmpty(8, 8)
        val wrapped = CandyTile(
            id = 52L,
            type = CandyType.GREEN,
            row = 4,
            column = 4,
            specialCandyType = SpecialCandyType.WRAPPED
        )
        val affected = SpecialCandyResolver.calculateSingleSpecialEffect(board, wrapped)
        assertEquals(9, affected.size)
        for (r in 3..5) {
            for (c in 3..5) {
                assertTrue(affected.contains(BoardPosition(r, c)))
            }
        }
    }

    // 11. Color Bomb removes the selected candy type
    @Test
    fun `11 - Color Bomb removes the selected candy type`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.BLUE, CandyType.RED, CandyType.GREEN),
            listOf(CandyType.YELLOW, CandyType.RED, CandyType.PURPLE, CandyType.RED),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.RED, CandyType.YELLOW),
            listOf(CandyType.RED, CandyType.PURPLE, CandyType.BLUE, CandyType.RED)
        )
        val board = createCustomBoard(grid)
        val bombTile = CandyTile(
            id = 99L,
            type = CandyType.EMPTY,
            row = 0,
            column = 0,
            specialCandyType = SpecialCandyType.COLOR_BOMB
        )
        val affected = SpecialCandyResolver.calculateSingleSpecialEffect(board, bombTile, targetColor = CandyType.RED)

        // All 7 RED candies + the bomb position itself
        assertTrue(affected.contains(BoardPosition(0, 0)))
        assertTrue(affected.contains(BoardPosition(0, 2)))
        assertTrue(affected.contains(BoardPosition(1, 1)))
        assertTrue(affected.contains(BoardPosition(1, 3)))
        assertTrue(affected.contains(BoardPosition(2, 2)))
        assertTrue(affected.contains(BoardPosition(3, 0)))
        assertTrue(affected.contains(BoardPosition(3, 3)))
    }

    // 12. Effects stay inside board boundaries
    @Test
    fun `12 - Effects stay inside board boundaries`() {
        val board = Match3Board.createEmpty(8, 8)

        // Corners
        val corners = listOf(
            BoardPosition(0, 0), // Top-Left
            BoardPosition(0, 7), // Top-Right
            BoardPosition(7, 0), // Bottom-Left
            BoardPosition(7, 7)  // Bottom-Right
        )
        for (corner in corners) {
            val wrapped = CandyTile(id = 10L, type = CandyType.RED, row = corner.row, column = corner.column, specialCandyType = SpecialCandyType.WRAPPED)
            val affected = SpecialCandyResolver.calculateSingleSpecialEffect(board, wrapped)
            assertEquals(4, affected.size)
            assertTrue(affected.all { it.row in 0..7 && it.column in 0..7 })
        }

        // Edges
        val edges = listOf(
            BoardPosition(0, 3), // Top edge
            BoardPosition(7, 3), // Bottom edge
            BoardPosition(3, 0), // Left edge
            BoardPosition(3, 7)  // Right edge
        )
        for (edge in edges) {
            val wrapped = CandyTile(id = 11L, type = CandyType.BLUE, row = edge.row, column = edge.column, specialCandyType = SpecialCandyType.WRAPPED)
            val affected = SpecialCandyResolver.calculateSingleSpecialEffect(board, wrapped)
            assertEquals(6, affected.size)
            assertTrue(affected.all { it.row in 0..7 && it.column in 0..7 })
        }
    }

    // 13. Special candy activation can trigger a cascade
    @Test
    fun `13 - Special candy activation can trigger a cascade`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW),
            listOf(CandyType.BLUE, CandyType.BLUE, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.RED),
            listOf(CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW)
        )
        val initialBoard = createCustomBoard(grid)
        // Place horizontal striped candy at (0, 0)
        val boardWithSpecial = initialBoard.withTile(
            CandyTile(id = 999L, type = CandyType.RED, row = 0, column = 0, specialCandyType = SpecialCandyType.HORIZONTAL_STRIPED)
        )

        val result = MatchResolver.resolveAllCascades(
            initialBoard = boardWithSpecial,
            swapPosA = BoardPosition(0, 0),
            swapPosB = BoardPosition(0, 1),
            random = Random(42)
        )
        assertTrue(result.steps.isNotEmpty())
        assertTrue(result.totalScoreGained > 0)
    }

    // 14. Cascade finishes with a stable board
    @Test
    fun `14 - Cascade finishes with a stable board`() {
        val grid = listOf(
            listOf(CandyType.RED, CandyType.RED, CandyType.RED, CandyType.BLUE),
            listOf(CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE),
            listOf(CandyType.GREEN, CandyType.YELLOW, CandyType.PURPLE, CandyType.RED),
            listOf(CandyType.RED, CandyType.BLUE, CandyType.GREEN, CandyType.YELLOW)
        )
        val board = createCustomBoard(grid)
        val result = MatchResolver.resolveAllCascades(board, random = Random(99))

        assertTrue(result.isStable)
        assertFalse(MatchDetector.hasAnyMatches(result.finalBoard))
    }

    // 15. No duplicate special activation occurs
    @Test
    fun `15 - No duplicate special activation occurs`() {
        val board = Match3Board.createEmpty(8, 8)
        val specialA = CandyTile(1L, CandyType.RED, 0, 0, SpecialCandyType.HORIZONTAL_STRIPED)
        val specialB = CandyTile(2L, CandyType.BLUE, 0, 5, SpecialCandyType.VERTICAL_STRIPED)
        val boardWithBoth = board.withTile(specialA).withTile(specialB)

        val activatedIds = mutableSetOf<Long>()
        val activatedSpecials = mutableListOf<CandyTile>()

        val result = SpecialCandyResolver.resolveChainedSpecials(
            board = boardWithBoth,
            currentlyAffected = setOf(BoardPosition(0, 0)),
            alreadyActivatedIds = activatedIds,
            activatedSpecials = activatedSpecials
        )

        // Both specials are triggered once and visited IDs prevent duplicates
        assertEquals(2, activatedIds.size)
        assertTrue(activatedIds.contains(1L))
        assertTrue(activatedIds.contains(2L))
        assertEquals(2, activatedSpecials.size)
    }

    // 16. Special candy activation awards the expected bonus
    @Test
    fun `16 - Special candy activation awards the expected bonus`() {
        val stripedScore = ScoreCalculator.calculateSpecialActivationScore(SpecialCandyType.HORIZONTAL_STRIPED)
        val wrappedScore = ScoreCalculator.calculateSpecialActivationScore(SpecialCandyType.WRAPPED)
        val bombScore = ScoreCalculator.calculateSpecialActivationScore(SpecialCandyType.COLOR_BOMB)

        assertEquals(ScoreCalculator.STRIPED_ACTIVATION_POINTS, stripedScore)
        assertEquals(ScoreCalculator.WRAPPED_ACTIVATION_POINTS, wrappedScore)
        assertEquals(ScoreCalculator.COLOR_BOMB_ACTIVATION_POINTS, bombScore)
    }

    // 17. Score is not duplicated
    @Test
    fun `17 - Score is not duplicated`() {
        val matches = listOf(
            SingleMatch(type = CandyType.RED, positions = listOf(BoardPosition(0, 0), BoardPosition(0, 1), BoardPosition(0, 2)), isHorizontal = true)
        )
        val score1 = ScoreCalculator.calculateMatchesScore(matches)
        val score2 = ScoreCalculator.calculateMatchesScore(matches)

        assertEquals(score1, score2)
        assertEquals(ScoreCalculator.MATCH_3_POINTS, score1)
    }

    // 18. Removed candies update objectives correctly
    @Test
    fun `18 - Removed candies update objectives correctly`() {
        val objective = LevelObjective(
            id = "obj_blue",
            type = ObjectiveType.COLLECT_CANDY,
            target = 5,
            currentProgress = 0,
            candyType = CandyType.BLUE
        )
        val removed = listOf(
            CandyTile(1L, CandyType.BLUE, 0, 0),
            CandyTile(2L, CandyType.BLUE, 0, 1),
            CandyTile(3L, CandyType.BLUE, 0, 2),
            CandyTile(4L, CandyType.RED, 1, 0)
        )
        val updated = ObjectiveManager.onCandiesRemoved(listOf(objective), removed)

        assertEquals(3, updated.first().currentProgress)
        assertFalse(updated.first().isCompleted)
    }

    // 19. Special-candy removals do not double-count
    @Test
    fun `19 - Special-candy removals do not double-count`() {
        val objective = LevelObjective(
            id = "obj_red",
            type = ObjectiveType.COLLECT_CANDY,
            target = 5,
            currentProgress = 2,
            candyType = CandyType.RED
        )
        val activated = listOf(
            CandyTile(1L, CandyType.RED, 0, 0, specialCandyType = SpecialCandyType.HORIZONTAL_STRIPED),
            CandyTile(2L, CandyType.EMPTY, 0, 1, specialCandyType = SpecialCandyType.COLOR_BOMB)
        )
        val updated = ObjectiveManager.onCandiesRemoved(listOf(objective), activated)

        // Only the RED striped candy adds +1, Color Bomb does not add red candy progress
        assertEquals(3, updated.first().currentProgress)

        // Passing empty list does not increment
        val secondPass = ObjectiveManager.onCandiesRemoved(updated, emptyList())
        assertEquals(3, secondPass.first().currentProgress)
    }
}
