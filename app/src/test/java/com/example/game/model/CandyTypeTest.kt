package com.example.game.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CandyTypeTest {

    @Test
    fun `1 - all six normal candy types exist`() {
        val playableTypes = CandyType.PLAYABLE_TYPES
        assertEquals(6, playableTypes.size)
        assertTrue(playableTypes.contains(CandyType.RED))
        assertTrue(playableTypes.contains(CandyType.BLUE))
        assertTrue(playableTypes.contains(CandyType.GREEN))
        assertTrue(playableTypes.contains(CandyType.YELLOW))
        assertTrue(playableTypes.contains(CandyType.PURPLE))
        assertTrue(playableTypes.contains(CandyType.ORANGE))
    }

    @Test
    fun `2 - EMPTY is not treated as a playable candy`() {
        assertFalse(CandyType.EMPTY.isPlayable)
        assertFalse(CandyType.PLAYABLE_TYPES.contains(CandyType.EMPTY))
    }

    @Test
    fun `all playable candy types have unique symbols and non-empty display names`() {
        val playable = CandyType.PLAYABLE_TYPES
        val symbols = playable.map { it.symbol }.toSet()
        val names = playable.map { it.displayName }.toSet()

        assertEquals(playable.size, symbols.size)
        assertEquals(playable.size, names.size)
    }
}
