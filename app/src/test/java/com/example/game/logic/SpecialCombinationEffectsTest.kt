package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCandyType
import com.example.game.model.SpecialCombinationType
import com.example.game.utils.ScoreCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpecialCombinationEffectsTest {

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

    @Test
    fun testStripedStripedCrossClearing() {
        var board = createFilledBoard(CandyType.BLUE)
        val posA = BoardPosition(3, 3)
        val posB = BoardPosition(3, 4)

        board = board.withTile(CandyTile(100L, CandyType.RED, 3, 3, SpecialCandyType.HORIZONTAL_STRIPED))
        board = board.withTile(CandyTile(101L, CandyType.GREEN, 3, 4, SpecialCandyType.VERTICAL_STRIPED))

        val result = SpecialCombinationResolver.resolveCombination(board, posA, posB)

        assertEquals(SpecialCombinationType.STRIPED_STRIPED, result.comboType)
        assertEquals(ScoreCalculator.COMBO_STRIPED_STRIPED_POINTS, result.score)

        // Must include entire row 3, entire column 3, and entire column 4
        for (c in 0 until 8) {
            assertTrue("Row 3 should be cleared at col $c", result.affectedPositions.contains(BoardPosition(3, c)))
        }
        for (r in 0 until 8) {
            assertTrue("Col 3 should be cleared at row $r", result.affectedPositions.contains(BoardPosition(r, 3)))
            assertTrue("Col 4 should be cleared at row $r", result.affectedPositions.contains(BoardPosition(r, 4)))
        }
    }

    @Test
    fun testWrappedWrappedDualExplosion() {
        var board = createFilledBoard(CandyType.YELLOW)
        val posA = BoardPosition(4, 4)
        val posB = BoardPosition(4, 5)

        board = board.withTile(CandyTile(100L, CandyType.RED, 4, 4, SpecialCandyType.WRAPPED))
        board = board.withTile(CandyTile(101L, CandyType.GREEN, 4, 5, SpecialCandyType.WRAPPED))

        val result = SpecialCombinationResolver.resolveCombination(board, posA, posB)

        assertEquals(SpecialCombinationType.WRAPPED_WRAPPED, result.comboType)
        assertEquals(ScoreCalculator.COMBO_WRAPPED_WRAPPED_POINTS, result.score)

        // Check 5x5 blast area around center
        for (r in 2..6) {
            for (c in 2..7) {
                assertTrue("Position ($r, $c) should be affected by Wrapped+Wrapped", result.affectedPositions.contains(BoardPosition(r, c)))
            }
        }
    }

    @Test
    fun testStripedWrappedGiantCross() {
        var board = createFilledBoard(CandyType.PURPLE)
        val posA = BoardPosition(3, 3)
        val posB = BoardPosition(3, 4)

        board = board.withTile(CandyTile(100L, CandyType.RED, 3, 3, SpecialCandyType.HORIZONTAL_STRIPED))
        board = board.withTile(CandyTile(101L, CandyType.GREEN, 3, 4, SpecialCandyType.WRAPPED))

        val result = SpecialCombinationResolver.resolveCombination(board, posA, posB)

        assertEquals(SpecialCombinationType.STRIPED_WRAPPED, result.comboType)
        assertEquals(ScoreCalculator.COMBO_STRIPED_WRAPPED_POINTS, result.score)

        // Must clear 3 full rows (2, 3, 4) and full columns (2, 3, 4, 5)
        for (r in 2..4) {
            for (c in 0 until 8) {
                assertTrue("Row $r col $c should be cleared", result.affectedPositions.contains(BoardPosition(r, c)))
            }
        }
        for (c in 2..5) {
            for (r in 0 until 8) {
                assertTrue("Row $r col $c should be cleared", result.affectedPositions.contains(BoardPosition(r, c)))
            }
        }
    }

    @Test
    fun testColorBombNormal() {
        var board = createFilledBoard(CandyType.BLUE)
        // Put some RED candies
        board = board.withTile(CandyTile(201L, CandyType.RED, 0, 0))
        board = board.withTile(CandyTile(202L, CandyType.RED, 2, 2))
        board = board.withTile(CandyTile(203L, CandyType.RED, 5, 5))

        val bombPos = BoardPosition(3, 3)
        val normalPos = BoardPosition(3, 4)
        board = board.withTile(CandyTile(100L, CandyType.EMPTY, 3, 3, SpecialCandyType.COLOR_BOMB))
        board = board.withTile(CandyTile(101L, CandyType.RED, 3, 4))

        val result = SpecialCombinationResolver.resolveCombination(board, bombPos, normalPos)

        assertEquals(SpecialCombinationType.COLOR_BOMB_NORMAL, result.comboType)
        assertEquals(ScoreCalculator.COMBO_COLOR_BOMB_NORMAL_POINTS, result.score)

        // All RED tiles plus bomb must be in affectedPositions
        assertTrue(result.affectedPositions.contains(bombPos))
        assertTrue(result.affectedPositions.contains(normalPos))
        assertTrue(result.affectedPositions.contains(BoardPosition(0, 0)))
        assertTrue(result.affectedPositions.contains(BoardPosition(2, 2)))
        assertTrue(result.affectedPositions.contains(BoardPosition(5, 5)))
    }

    @Test
    fun testColorBombStriped() {
        var board = createFilledBoard(CandyType.YELLOW)
        // Add specific RED candies across the board
        board = board.withTile(CandyTile(201L, CandyType.RED, 1, 1))
        board = board.withTile(CandyTile(202L, CandyType.RED, 6, 6))

        val bombPos = BoardPosition(3, 3)
        val stripedPos = BoardPosition(3, 4)
        board = board.withTile(CandyTile(100L, CandyType.EMPTY, 3, 3, SpecialCandyType.COLOR_BOMB))
        board = board.withTile(CandyTile(101L, CandyType.RED, 3, 4, SpecialCandyType.VERTICAL_STRIPED))

        val result = SpecialCombinationResolver.resolveCombination(board, bombPos, stripedPos)

        assertEquals(SpecialCombinationType.COLOR_BOMB_STRIPED, result.comboType)
        assertEquals(ScoreCalculator.COMBO_COLOR_BOMB_STRIPED_POINTS, result.score)

        // For tile at (1,1), (1+1)%2 == 0 -> row 1 cleared
        for (c in 0 until 8) {
            assertTrue(result.affectedPositions.contains(BoardPosition(1, c)))
        }
        // For tile at (6,6), (6+6)%2 == 0 -> row 6 cleared
        for (c in 0 until 8) {
            assertTrue(result.affectedPositions.contains(BoardPosition(6, c)))
        }
    }

    @Test
    fun testColorBombColorBombClearsFullBoard() {
        var board = createFilledBoard(CandyType.GREEN)
        val posA = BoardPosition(0, 0)
        val posB = BoardPosition(0, 1)

        board = board.withTile(CandyTile(100L, CandyType.EMPTY, 0, 0, SpecialCandyType.COLOR_BOMB))
        board = board.withTile(CandyTile(101L, CandyType.EMPTY, 0, 1, SpecialCandyType.COLOR_BOMB))

        val result = SpecialCombinationResolver.resolveCombination(board, posA, posB)

        assertEquals(SpecialCombinationType.COLOR_BOMB_COLOR_BOMB, result.comboType)
        assertEquals(ScoreCalculator.COMBO_COLOR_BOMB_COLOR_BOMB_POINTS, result.score)
        assertEquals(64, result.affectedPositions.size)
    }

    @Test
    fun testCombinationChainedSecondarySpecials() {
        var board = createFilledBoard(CandyType.BLUE)
        val posA = BoardPosition(3, 3)
        val posB = BoardPosition(3, 4)

        board = board.withTile(CandyTile(100L, CandyType.RED, 3, 3, SpecialCandyType.HORIZONTAL_STRIPED))
        board = board.withTile(CandyTile(101L, CandyType.GREEN, 3, 4, SpecialCandyType.VERTICAL_STRIPED))

        // Place another WRAPPED candy on row 3 at (3, 0)
        val chainedWrapped = CandyTile(102L, CandyType.YELLOW, 3, 0, SpecialCandyType.WRAPPED)
        board = board.withTile(chainedWrapped)

        val result = SpecialCombinationResolver.resolveCombination(board, posA, posB)

        // The wrapped candy at (3,0) should explode (3x3 area around 3,0)
        assertTrue(result.affectedPositions.contains(BoardPosition(2, 0)))
        assertTrue(result.affectedPositions.contains(BoardPosition(4, 0)))
        assertTrue(result.affectedPositions.contains(BoardPosition(2, 1)))
        assertTrue(result.affectedPositions.contains(BoardPosition(4, 1)))

        // Score includes combo score + wrapped activation bonus
        assertEquals(
            ScoreCalculator.COMBO_STRIPED_STRIPED_POINTS + ScoreCalculator.WRAPPED_ACTIVATION_POINTS,
            result.score
        )
    }
}
