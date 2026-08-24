package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyType
import com.example.game.model.SpecialCandyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SpecialCandyCreationTest {

    private val creator = SpecialCandyCreator()

    @Test
    fun testStripedCandyCreationFor4Match() {
        val group = MatchGroup(
            positions = setOf(
                BoardPosition(0, 0),
                BoardPosition(0, 1),
                BoardPosition(0, 2),
                BoardPosition(0, 3)
            ),
            type = CandyType.RED,
            isHorizontal = true
        )

        val special = creator.checkAndCreateSpecial(group)
        assertNotNull(special)
        assertEquals(SpecialCandyType.STRIPED_VERTICAL, special?.tile?.specialType)
    }

    @Test
    fun testColorBombCreationFor5Match() {
        val group = MatchGroup(
            positions = setOf(
                BoardPosition(0, 0),
                BoardPosition(0, 1),
                BoardPosition(0, 2),
                BoardPosition(0, 3),
                BoardPosition(0, 4)
            ),
            type = CandyType.YELLOW,
            isHorizontal = true
        )

        val special = creator.checkAndCreateSpecial(group)
        assertNotNull(special)
        assertEquals(SpecialCandyType.COLOR_BOMB, special?.tile?.specialType)
    }

    @Test
    fun testWrappedCandyCreationForTShape() {
        val group = MatchGroup(
            positions = setOf(
                BoardPosition(0, 0),
                BoardPosition(0, 1),
                BoardPosition(0, 2),
                BoardPosition(1, 1),
                BoardPosition(2, 1)
            ),
            type = CandyType.GREEN,
            isHorizontal = true,
            isVertical = true,
            isTOrLShape = true
        )

        val special = creator.checkAndCreateSpecial(group)
        assertNotNull(special)
        assertEquals(SpecialCandyType.WRAPPED, special?.tile?.specialType)
    }
}
