package com.example.game.utils

import com.example.game.logic.SingleMatch
import com.example.game.model.BoardPosition
import com.example.game.model.CandyType
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreCalculatorTest {

    @Test
    fun `calculateMatchScore for 3 candies is 30`() {
        val score = ScoreCalculator.calculateMatchScore(3)
        assertEquals(30, score)
    }

    @Test
    fun `calculateMatchScore for 4 candies is 60`() {
        val score = ScoreCalculator.calculateMatchScore(4)
        assertEquals(60, score)
    }

    @Test
    fun `calculateMatchScore for 5 candies is 100`() {
        val score = ScoreCalculator.calculateMatchScore(5)
        assertEquals(100, score)
    }

    @Test
    fun `calculateSpecialActivationScore returns correct bonus points`() {
        assertEquals(100, ScoreCalculator.calculateSpecialActivationScore(com.example.game.model.SpecialCandyType.HORIZONTAL_STRIPED))
        assertEquals(100, ScoreCalculator.calculateSpecialActivationScore(com.example.game.model.SpecialCandyType.VERTICAL_STRIPED))
        assertEquals(150, ScoreCalculator.calculateSpecialActivationScore(com.example.game.model.SpecialCandyType.WRAPPED))
        assertEquals(200, ScoreCalculator.calculateSpecialActivationScore(com.example.game.model.SpecialCandyType.COLOR_BOMB))
        assertEquals(0, ScoreCalculator.calculateSpecialActivationScore(com.example.game.model.SpecialCandyType.NONE))
    }

    @Test
    fun `calculateMatchScore for invalid length less than 3 is 0`() {
        assertEquals(0, ScoreCalculator.calculateMatchScore(2))
        assertEquals(0, ScoreCalculator.calculateMatchScore(1))
        assertEquals(0, ScoreCalculator.calculateMatchScore(0))
    }

    @Test
    fun `calculateMatchesScore calculates total across multiple matches`() {
        val matches = listOf(
            SingleMatch(CandyType.RED, listOf(BoardPosition(0, 0), BoardPosition(0, 1), BoardPosition(0, 2)), true),
            SingleMatch(CandyType.BLUE, listOf(BoardPosition(2, 0), BoardPosition(2, 1), BoardPosition(2, 2), BoardPosition(2, 3)), true)
        )
        // 30 + 60 = 90
        val total = ScoreCalculator.calculateMatchesScore(matches)
        assertEquals(90, total)
    }

    @Test
    fun `calculateRemainingMovesBonus calculates 50 points per remaining move`() {
        assertEquals(500, ScoreCalculator.calculateRemainingMovesBonus(10))
        assertEquals(0, ScoreCalculator.calculateRemainingMovesBonus(0))
        assertEquals(0, ScoreCalculator.calculateRemainingMovesBonus(-5))
    }

    @Test
    fun `formatScore produces readable integers with thousands separators`() {
        assertEquals("0", ScoreCalculator.formatScore(0))
        assertEquals("850", ScoreCalculator.formatScore(850))
        assertEquals("1,000", ScoreCalculator.formatScore(1000))
        assertEquals("1,250", ScoreCalculator.formatScore(1250))
        assertEquals("2,750", ScoreCalculator.formatScore(2750))
        assertEquals("1,552", ScoreCalculator.formatScore(1552))
    }
}
