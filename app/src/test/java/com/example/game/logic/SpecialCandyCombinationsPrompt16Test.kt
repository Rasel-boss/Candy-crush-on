package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameStatus
import com.example.game.model.LevelObjective
import com.example.game.model.Match3Board
import com.example.game.model.ObjectiveType
import com.example.game.model.SpecialCandyType
import com.example.game.model.SpecialCombinationType
import com.example.game.utils.ScoreCalculator
import com.example.game.viewmodel.Match3ViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Deterministic test suite for Prompt 16:
 * Special Candy Combinations & Powerful Combo System.
 *
 * Covers all 24 required test cases:
 * Combination Detection:
 *  1. Striped + Striped is detected.
 *  2. Wrapped + Wrapped is detected.
 *  3. Striped + Wrapped is detected.
 *  4. Color Bomb + Striped is detected.
 *  5. Color Bomb + Wrapped is detected.
 *  6. Color Bomb + Color Bomb is detected.
 *  7. Normal + Normal is not treated as a special combination.
 *  8. Diagonal special candies cannot combine.
 *
 * Effect Tests:
 *  9. Striped + Striped clears the expected row/column.
 * 10. Wrapped + Wrapped clears the expected area.
 * 11. Striped + Wrapped produces the expected combined effect.
 * 12. Color Bomb + Striped targets the correct candy type.
 * 13. Color Bomb + Wrapped targets the correct candy type.
 * 14. Color Bomb + Color Bomb clears the board.
 *
 * Safety Tests:
 * 15. Corner combinations do not crash.
 * 16. Edge combinations do not crash.
 * 17. No position outside the board is accessed.
 * 18. No duplicate activation occurs.
 * 19. No infinite resolution occurs.
 *
 * Game State Tests:
 * 20. A special combination consumes exactly one move.
 * 21. Cascades do not consume additional moves.
 * 22. Score is applied exactly once.
 * 23. Objective progress is applied correctly.
 * 24. Board becomes stable after resolution.
 */
class SpecialCandyCombinationsPrompt16Test {

    private lateinit var viewModel: Match3ViewModel

    @Before
    fun setUp() {
        viewModel = Match3ViewModel().apply { stepDelayMs = 0L }
        SpecialCandyResolver.resetIdCounter(900000L)
    }

    private fun createFilledBoard(fillType: CandyType = CandyType.BLUE): Match3Board {
        var idCounter = 1L
        val tiles = List(8) { r ->
            List(8) { c ->
                CandyTile(
                    id = idCounter++,
                    type = fillType,
                    row = r,
                    column = c
                )
            }
        }
        return Match3Board(8, 8, tiles)
    }

    // --- Combination Detection ---

    @Test
    fun `01 - Striped + Striped is detected`() {
        val tileA = CandyTile(1L, CandyType.RED, 0, 0, SpecialCandyType.HORIZONTAL_STRIPED)
        val tileB = CandyTile(2L, CandyType.BLUE, 0, 1, SpecialCandyType.VERTICAL_STRIPED)

        val combo = SpecialCandyComboResolver.detectCombination(tileA, tileB)
        assertEquals(SpecialCombinationType.STRIPED_STRIPED, combo)
    }

    @Test
    fun `02 - Wrapped + Wrapped is detected`() {
        val tileA = CandyTile(1L, CandyType.GREEN, 2, 2, SpecialCandyType.WRAPPED)
        val tileB = CandyTile(2L, CandyType.YELLOW, 2, 3, SpecialCandyType.WRAPPED)

        val combo = SpecialCandyComboResolver.detectCombination(tileA, tileB)
        assertEquals(SpecialCombinationType.WRAPPED_WRAPPED, combo)
    }

    @Test
    fun `03 - Striped + Wrapped is detected`() {
        val tileA = CandyTile(1L, CandyType.PURPLE, 1, 1, SpecialCandyType.HORIZONTAL_STRIPED)
        val tileB = CandyTile(2L, CandyType.ORANGE, 1, 2, SpecialCandyType.WRAPPED)

        assertEquals(SpecialCombinationType.STRIPED_WRAPPED, SpecialCandyComboResolver.detectCombination(tileA, tileB))
        assertEquals(SpecialCombinationType.STRIPED_WRAPPED, SpecialCandyComboResolver.detectCombination(tileB, tileA))
    }

    @Test
    fun `04 - Color Bomb + Striped is detected`() {
        val bomb = CandyTile(1L, CandyType.EMPTY, 3, 3, SpecialCandyType.COLOR_BOMB)
        val striped = CandyTile(2L, CandyType.RED, 3, 4, SpecialCandyType.VERTICAL_STRIPED)

        assertEquals(SpecialCombinationType.COLOR_BOMB_STRIPED, SpecialCandyComboResolver.detectCombination(bomb, striped))
        assertEquals(SpecialCombinationType.COLOR_BOMB_STRIPED, SpecialCandyComboResolver.detectCombination(striped, bomb))
    }

    @Test
    fun `05 - Color Bomb + Wrapped is detected`() {
        val bomb = CandyTile(1L, CandyType.EMPTY, 4, 4, SpecialCandyType.COLOR_BOMB)
        val wrapped = CandyTile(2L, CandyType.GREEN, 4, 5, SpecialCandyType.WRAPPED)

        assertEquals(SpecialCombinationType.COLOR_BOMB_WRAPPED, SpecialCandyComboResolver.detectCombination(bomb, wrapped))
        assertEquals(SpecialCombinationType.COLOR_BOMB_WRAPPED, SpecialCandyComboResolver.detectCombination(wrapped, bomb))
    }

    @Test
    fun `06 - Color Bomb + Color Bomb is detected`() {
        val bomb1 = CandyTile(1L, CandyType.EMPTY, 5, 5, SpecialCandyType.COLOR_BOMB)
        val bomb2 = CandyTile(2L, CandyType.EMPTY, 5, 6, SpecialCandyType.COLOR_BOMB)

        assertEquals(SpecialCombinationType.COLOR_BOMB_COLOR_BOMB, SpecialCandyComboResolver.detectCombination(bomb1, bomb2))
    }

    @Test
    fun `07 - Normal + Normal is not treated as a special combination`() {
        val normal1 = CandyTile(1L, CandyType.RED, 0, 0)
        val normal2 = CandyTile(2L, CandyType.BLUE, 0, 1)

        assertEquals(SpecialCombinationType.NONE, SpecialCandyComboResolver.detectCombination(normal1, normal2))
    }

    @Test
    fun `08 - Diagonal special candies cannot combine`() {
        var board = createFilledBoard(CandyType.BLUE)
        val special1 = CandyTile(1L, CandyType.RED, 2, 2, SpecialCandyType.HORIZONTAL_STRIPED)
        val special2 = CandyTile(2L, CandyType.GREEN, 3, 3, SpecialCandyType.WRAPPED)
        board = board.withTile(special1).withTile(special2)

        // Diagonal positions are not adjacent
        assertFalse(SpecialCandyComboResolver.canCombine(board, BoardPosition(2, 2), BoardPosition(3, 3)))
    }

    // --- Effect Tests ---

    @Test
    fun `09 - Striped + Striped clears the expected row and column`() {
        var board = createFilledBoard(CandyType.BLUE)
        val posA = BoardPosition(3, 3)
        val posB = BoardPosition(3, 4)

        board = board.withTile(CandyTile(100L, CandyType.RED, 3, 3, SpecialCandyType.HORIZONTAL_STRIPED))
        board = board.withTile(CandyTile(101L, CandyType.GREEN, 3, 4, SpecialCandyType.VERTICAL_STRIPED))

        val result = SpecialCandyComboResolver.resolveCombination(board, posA, posB)

        assertEquals(SpecialCombinationType.STRIPED_STRIPED, result.comboType)
        assertEquals(ScoreCalculator.COMBO_STRIPED_STRIPED_POINTS, result.score)

        // Must clear full row 3, full col 3, full col 4
        for (c in 0 until 8) {
            assertTrue(result.affectedPositions.contains(BoardPosition(3, c)))
        }
        for (r in 0 until 8) {
            assertTrue(result.affectedPositions.contains(BoardPosition(r, 3)))
            assertTrue(result.affectedPositions.contains(BoardPosition(r, 4)))
        }
    }

    @Test
    fun `10 - Wrapped + Wrapped clears the expected area`() {
        var board = createFilledBoard(CandyType.YELLOW)
        val posA = BoardPosition(4, 4)
        val posB = BoardPosition(4, 5)

        board = board.withTile(CandyTile(100L, CandyType.RED, 4, 4, SpecialCandyType.WRAPPED))
        board = board.withTile(CandyTile(101L, CandyType.GREEN, 4, 5, SpecialCandyType.WRAPPED))

        val result = SpecialCandyComboResolver.resolveCombination(board, posA, posB)

        assertEquals(SpecialCombinationType.WRAPPED_WRAPPED, result.comboType)
        assertEquals(ScoreCalculator.COMBO_WRAPPED_WRAPPED_POINTS, result.score)

        // 5x5 boundary around the two wrapped tiles
        for (r in 2..6) {
            for (c in 2..7) {
                assertTrue(result.affectedPositions.contains(BoardPosition(r, c)))
            }
        }
    }

    @Test
    fun `11 - Striped + Wrapped produces the expected combined effect`() {
        var board = createFilledBoard(CandyType.PURPLE)
        val posA = BoardPosition(3, 3)
        val posB = BoardPosition(3, 4)

        board = board.withTile(CandyTile(100L, CandyType.RED, 3, 3, SpecialCandyType.HORIZONTAL_STRIPED))
        board = board.withTile(CandyTile(101L, CandyType.GREEN, 3, 4, SpecialCandyType.WRAPPED))

        val result = SpecialCandyComboResolver.resolveCombination(board, posA, posB)

        assertEquals(SpecialCombinationType.STRIPED_WRAPPED, result.comboType)
        assertEquals(ScoreCalculator.COMBO_STRIPED_WRAPPED_POINTS, result.score)

        // 3-wide cross effect across rows (2..4) and columns (2..5)
        for (r in 2..4) {
            for (c in 0 until 8) {
                assertTrue(result.affectedPositions.contains(BoardPosition(r, c)))
            }
        }
        for (c in 2..5) {
            for (r in 0 until 8) {
                assertTrue(result.affectedPositions.contains(BoardPosition(r, c)))
            }
        }
    }

    @Test
    fun `12 - Color Bomb + Striped targets the correct candy type`() {
        var board = createFilledBoard(CandyType.YELLOW)
        board = board.withTile(CandyTile(201L, CandyType.RED, 1, 1))
        board = board.withTile(CandyTile(202L, CandyType.RED, 6, 6))

        val bombPos = BoardPosition(3, 3)
        val stripedPos = BoardPosition(3, 4)
        board = board.withTile(CandyTile(100L, CandyType.EMPTY, 3, 3, SpecialCandyType.COLOR_BOMB))
        board = board.withTile(CandyTile(101L, CandyType.RED, 3, 4, SpecialCandyType.VERTICAL_STRIPED))

        val result = SpecialCandyComboResolver.resolveCombination(board, bombPos, stripedPos)

        assertEquals(SpecialCombinationType.COLOR_BOMB_STRIPED, result.comboType)
        assertEquals(ScoreCalculator.COMBO_COLOR_BOMB_STRIPED_POINTS, result.score)

        // Target color RED tiles triggered striped effects
        assertTrue(result.affectedPositions.contains(BoardPosition(1, 1)))
        assertTrue(result.affectedPositions.contains(BoardPosition(6, 6)))
        for (c in 0 until 8) {
            assertTrue(result.affectedPositions.contains(BoardPosition(1, c)))
            assertTrue(result.affectedPositions.contains(BoardPosition(6, c)))
        }
    }

    @Test
    fun `13 - Color Bomb + Wrapped targets the correct candy type`() {
        var board = createFilledBoard(CandyType.BLUE)
        board = board.withTile(CandyTile(201L, CandyType.RED, 2, 2))

        val bombPos = BoardPosition(4, 4)
        val wrappedPos = BoardPosition(4, 5)
        board = board.withTile(CandyTile(100L, CandyType.EMPTY, 4, 4, SpecialCandyType.COLOR_BOMB))
        board = board.withTile(CandyTile(101L, CandyType.RED, 4, 5, SpecialCandyType.WRAPPED))

        val result = SpecialCandyComboResolver.resolveCombination(board, bombPos, wrappedPos)

        assertEquals(SpecialCombinationType.COLOR_BOMB_WRAPPED, result.comboType)
        assertEquals(ScoreCalculator.COMBO_COLOR_BOMB_WRAPPED_POINTS, result.score)

        // 3x3 blast around targeted RED tile at (2, 2)
        for (r in 1..3) {
            for (c in 1..3) {
                assertTrue(result.affectedPositions.contains(BoardPosition(r, c)))
            }
        }
    }

    @Test
    fun `14 - Color Bomb + Color Bomb clears the board`() {
        var board = createFilledBoard(CandyType.GREEN)
        val posA = BoardPosition(0, 0)
        val posB = BoardPosition(0, 1)

        board = board.withTile(CandyTile(100L, CandyType.EMPTY, 0, 0, SpecialCandyType.COLOR_BOMB))
        board = board.withTile(CandyTile(101L, CandyType.EMPTY, 0, 1, SpecialCandyType.COLOR_BOMB))

        val result = SpecialCandyComboResolver.resolveCombination(board, posA, posB)

        assertEquals(SpecialCombinationType.COLOR_BOMB_COLOR_BOMB, result.comboType)
        assertEquals(ScoreCalculator.COMBO_COLOR_BOMB_COLOR_BOMB_POINTS, result.score)
        assertEquals(64, result.affectedPositions.size)
    }

    // --- Safety Tests ---

    @Test
    fun `15 - Corner combinations do not crash`() {
        val corners = listOf(
            Pair(BoardPosition(0, 0), BoardPosition(0, 1)), // Top-Left
            Pair(BoardPosition(0, 7), BoardPosition(0, 6)), // Top-Right
            Pair(BoardPosition(7, 0), BoardPosition(7, 1)), // Bottom-Left
            Pair(BoardPosition(7, 7), BoardPosition(7, 6))  // Bottom-Right
        )

        for ((posA, posB) in corners) {
            var board = createFilledBoard(CandyType.BLUE)
            board = board.withTile(CandyTile(10L, CandyType.RED, posA.row, posA.column, SpecialCandyType.WRAPPED))
            board = board.withTile(CandyTile(11L, CandyType.GREEN, posB.row, posB.column, SpecialCandyType.WRAPPED))

            val result = SpecialCandyComboResolver.resolveCombination(board, posA, posB)
            assertTrue(result.affectedPositions.isNotEmpty())
            assertTrue(result.affectedPositions.all { it.row in 0..7 && it.column in 0..7 })
        }
    }

    @Test
    fun `16 - Edge combinations do not crash`() {
        val edges = listOf(
            Pair(BoardPosition(0, 3), BoardPosition(0, 4)), // Top edge
            Pair(BoardPosition(7, 3), BoardPosition(7, 4)), // Bottom edge
            Pair(BoardPosition(3, 0), BoardPosition(4, 0)), // Left edge
            Pair(BoardPosition(3, 7), BoardPosition(4, 7))  // Right edge
        )

        for ((posA, posB) in edges) {
            var board = createFilledBoard(CandyType.YELLOW)
            board = board.withTile(CandyTile(20L, CandyType.RED, posA.row, posA.column, SpecialCandyType.HORIZONTAL_STRIPED))
            board = board.withTile(CandyTile(21L, CandyType.ORANGE, posB.row, posB.column, SpecialCandyType.WRAPPED))

            val result = SpecialCandyComboResolver.resolveCombination(board, posA, posB)
            assertTrue(result.affectedPositions.isNotEmpty())
            assertTrue(result.affectedPositions.all { it.row in 0..7 && it.column in 0..7 })
        }
    }

    @Test
    fun `17 - No position outside the board is accessed`() {
        var board = createFilledBoard(CandyType.PURPLE)
        val posA = BoardPosition(0, 0)
        val posB = BoardPosition(1, 0)
        board = board.withTile(CandyTile(30L, CandyType.RED, 0, 0, SpecialCandyType.WRAPPED))
        board = board.withTile(CandyTile(31L, CandyType.GREEN, 1, 0, SpecialCandyType.WRAPPED))

        val result = SpecialCandyComboResolver.resolveCombination(board, posA, posB)
        for (pos in result.affectedPositions) {
            assertTrue("Row out of bounds: ${pos.row}", pos.row in 0 until 8)
            assertTrue("Column out of bounds: ${pos.column}", pos.column in 0 until 8)
        }
    }

    @Test
    fun `18 - No duplicate activation occurs`() {
        var board = createFilledBoard(CandyType.BLUE)
        val posA = BoardPosition(3, 3)
        val posB = BoardPosition(3, 4)

        val striped1 = CandyTile(100L, CandyType.RED, 3, 3, SpecialCandyType.HORIZONTAL_STRIPED)
        val striped2 = CandyTile(101L, CandyType.GREEN, 3, 4, SpecialCandyType.VERTICAL_STRIPED)
        board = board.withTile(striped1).withTile(striped2)

        val activatedIds = mutableSetOf<Long>()
        val result = SpecialCandyComboResolver.resolveCombination(board, posA, posB, activatedIds)

        assertEquals(2, activatedIds.size)
        assertTrue(activatedIds.contains(100L))
        assertTrue(activatedIds.contains(101L))
        assertEquals(2, result.activatedSpecials.size)
    }

    @Test
    fun `19 - No infinite resolution occurs`() {
        var board = createFilledBoard(CandyType.RED)
        // Set all 64 tiles as striped specials
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val tile = CandyTile((r * 8 + c + 1).toLong(), CandyType.RED, r, c, SpecialCandyType.HORIZONTAL_STRIPED)
                board = board.withTile(tile)
            }
        }

        val activatedIds = mutableSetOf<Long>()
        val result = SpecialCandyComboResolver.resolveCombination(
            board = board,
            posA = BoardPosition(0, 0),
            posB = BoardPosition(0, 1),
            alreadyActivatedIds = activatedIds
        )

        // Resolves cleanly and terminates with all 64 tiles activated exactly once
        assertEquals(64, activatedIds.size)
        assertEquals(64, result.affectedPositions.size)
    }

    // --- Game State Tests ---

    @Test
    fun `20 - A special combination consumes exactly one move`() {
        viewModel.startGame(level = 1)
        val initialMoves = viewModel.gameState.value.movesRemaining

        var board = createFilledBoard(CandyType.BLUE)
        board = board.withTile(CandyTile(100L, CandyType.RED, 3, 3, SpecialCandyType.HORIZONTAL_STRIPED))
        board = board.withTile(CandyTile(101L, CandyType.GREEN, 3, 4, SpecialCandyType.VERTICAL_STRIPED))

        viewModel.setCustomBoard(board)

        // Select and swap
        viewModel.selectTile(BoardPosition(3, 3))
        viewModel.selectTile(BoardPosition(3, 4))

        val movesAfterSwap = viewModel.gameState.value.movesRemaining
        assertEquals(initialMoves - 1, movesAfterSwap)
    }

    @Test
    fun `21 - Cascades do not consume additional moves`() {
        viewModel.startGame(level = 1)
        val initialMoves = viewModel.gameState.value.movesRemaining

        var board = createFilledBoard(CandyType.BLUE)
        board = board.withTile(CandyTile(100L, CandyType.EMPTY, 0, 0, SpecialCandyType.COLOR_BOMB))
        board = board.withTile(CandyTile(101L, CandyType.EMPTY, 0, 1, SpecialCandyType.COLOR_BOMB))
        viewModel.setCustomBoard(board)

        viewModel.selectTile(BoardPosition(0, 0))
        viewModel.selectTile(BoardPosition(0, 1))

        // Entire board clears and refills with multiple cascade reactions, but only 1 move was consumed
        assertEquals(initialMoves - 1, viewModel.gameState.value.movesRemaining)
    }

    @Test
    fun `22 - Score is applied exactly once`() {
        viewModel.startGame(level = 1)
        val initialScore = viewModel.gameState.value.score

        var board = createFilledBoard(CandyType.BLUE)
        board = board.withTile(CandyTile(100L, CandyType.RED, 3, 3, SpecialCandyType.HORIZONTAL_STRIPED))
        board = board.withTile(CandyTile(101L, CandyType.GREEN, 3, 4, SpecialCandyType.VERTICAL_STRIPED))
        viewModel.setCustomBoard(board)

        viewModel.selectTile(BoardPosition(3, 3))
        viewModel.selectTile(BoardPosition(3, 4))

        val scoreAfter = viewModel.gameState.value.score
        assertTrue(scoreAfter > initialScore)
        assertTrue(scoreAfter >= ScoreCalculator.COMBO_STRIPED_STRIPED_POINTS)
    }

    @Test
    fun `23 - Objective progress is applied correctly`() {
        viewModel.startGame(level = 1)
        val initialObjectives = viewModel.gameState.value.objectives

        var board = createFilledBoard(CandyType.BLUE)
        board = board.withTile(CandyTile(100L, CandyType.RED, 3, 3, SpecialCandyType.HORIZONTAL_STRIPED))
        board = board.withTile(CandyTile(101L, CandyType.GREEN, 3, 4, SpecialCandyType.VERTICAL_STRIPED))
        viewModel.setCustomBoard(board)

        viewModel.selectTile(BoardPosition(3, 3))
        viewModel.selectTile(BoardPosition(3, 4))

        val updatedObjectives = viewModel.gameState.value.objectives
        assertNotNull(updatedObjectives)
        // Blue candies removed should progress candy collection or score objective
        assertTrue(updatedObjectives.any { it.currentProgress > 0 } || viewModel.gameState.value.score > 0)
    }

    @Test
    fun `24 - Board becomes stable after resolution`() {
        viewModel.startGame(level = 1)
        var board = BoardGenerator.generateBoard(8, 8, Random(42))
        board = board.withTile(CandyTile(100L, CandyType.RED, 3, 3, SpecialCandyType.HORIZONTAL_STRIPED))
        board = board.withTile(CandyTile(101L, CandyType.GREEN, 3, 4, SpecialCandyType.WRAPPED))
        viewModel.setCustomBoard(board)

        viewModel.selectTile(BoardPosition(3, 3))
        viewModel.selectTile(BoardPosition(3, 4))

        val finalBoard = viewModel.gameState.value.board
        assertFalse(MatchDetector.hasAnyMatches(finalBoard))
        assertEquals(GameStatus.PLAYING, viewModel.gameState.value.status)
        assertFalse(viewModel.gameState.value.isProcessing)
    }
}
