package com.example.game.logic

import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.SpecialCandyType
import com.example.game.model.SpecialCombinationType
import org.junit.Assert.assertEquals
import org.junit.Test

class SpecialCombinationDetectionTest {

    private val resolver = SpecialCombinationResolver()

    @Test
    fun testStripedPlusStriped() {
        val tile1 = CandyTile(1, CandyType.RED, SpecialCandyType.STRIPED_HORIZONTAL)
        val tile2 = CandyTile(2, CandyType.BLUE, SpecialCandyType.STRIPED_VERTICAL)

        val combo = resolver.checkCombination(tile1, tile2)
        assertEquals(SpecialCombinationType.STRIPED_PLUS_STRIPED, combo)
    }

    @Test
    fun testColorBombPlusColorBomb() {
        val tile1 = CandyTile(1, CandyType.RED, SpecialCandyType.COLOR_BOMB)
        val tile2 = CandyTile(2, CandyType.BLUE, SpecialCandyType.COLOR_BOMB)

        val combo = resolver.checkCombination(tile1, tile2)
        assertEquals(SpecialCombinationType.COLOR_BOMB_PLUS_COLOR_BOMB, combo)
    }

    @Test
    fun testColorBombPlusStriped() {
        val tile1 = CandyTile(1, CandyType.RED, SpecialCandyType.COLOR_BOMB)
        val tile2 = CandyTile(2, CandyType.GREEN, SpecialCandyType.STRIPED_HORIZONTAL)

        val combo = resolver.checkCombination(tile1, tile2)
        assertEquals(SpecialCombinationType.COLOR_BOMB_PLUS_STRIPED, combo)
    }
}
