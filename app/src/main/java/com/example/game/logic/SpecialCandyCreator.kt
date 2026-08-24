package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.SpecialCandyType

/**
 * Pure, deterministic engine responsible for detecting and creating special candies
 * from matched groups (4-match, 5-match, L/T formations).
 *
 * Deterministic Priority:
 * 1. COLOR_BOMB (5+ in a straight line)
 * 2. WRAPPED (L or T formation >= 5 distinct tiles)
 * 3. STRIPED_HORIZONTAL (4 horizontal match)
 * 4. STRIPED_VERTICAL (4 vertical match)
 */
object SpecialCandyCreator {

    private var nextSpecialId = 700000L

    /**
     * Resets the internal ID counter for reproducible unit testing.
     */
    fun resetIdCounter(start: Long = 700000L) {
        nextSpecialId = start
    }

    /**
     * Evaluates linear matches and determines what special candies should be generated.
     * Prefers placing the special candy on the player's swap coordinate ([swapPosA] or [swapPosB]).
     */
    fun createSpecialCandiesFromMatches(
        matches: List<SingleMatch>,
        swapPosA: BoardPosition? = null,
        swapPosB: BoardPosition? = null
    ): List<SpecialCandyCreation> {
        if (matches.isEmpty()) return emptyList()

        val creations = mutableListOf<SpecialCandyCreation>()
        val processedMatchIndices = mutableSetOf<Int>()

        // 1. Check for L or T shape intersections (Wrapped Candies)
        val horizontalMatches = matches.mapIndexedNotNull { index, m -> if (m.isHorizontal) index to m else null }
        val verticalMatches = matches.mapIndexedNotNull { index, m -> if (!m.isHorizontal) index to m else null }

        for ((hIdx, hMatch) in horizontalMatches) {
            for ((vIdx, vMatch) in verticalMatches) {
                if (hMatch.type == vMatch.type && hMatch.type.isPlayable) {
                    val intersection = hMatch.positions.intersect(vMatch.positions.toSet())
                    if (intersection.isNotEmpty()) {
                        val combinedPositions = (hMatch.positions + vMatch.positions).toSet()
                        if (combinedPositions.size >= 5) {
                            val intersectionPos = intersection.first()
                            val chosenPos = when {
                                swapPosA != null && combinedPositions.contains(swapPosA) -> swapPosA
                                swapPosB != null && combinedPositions.contains(swapPosB) -> swapPosB
                                else -> intersectionPos
                            }
                            creations.add(
                                SpecialCandyCreation(
                                    position = chosenPos,
                                    specialType = SpecialCandyType.WRAPPED,
                                    baseType = hMatch.type
                                )
                            )
                            processedMatchIndices.add(hIdx)
                            processedMatchIndices.add(vIdx)
                        }
                    }
                }
            }
        }

        // 2. Check remaining linear matches for 5-matches (Color Bomb) and 4-matches (Striped)
        for (i in matches.indices) {
            if (processedMatchIndices.contains(i)) continue

            val match = matches[i]
            val positions = match.positions

            if (positions.size >= 5) {
                // 5-match: Color Bomb (Priority 1 for linear matches)
                val chosenPos = when {
                    swapPosA != null && positions.contains(swapPosA) -> swapPosA
                    swapPosB != null && positions.contains(swapPosB) -> swapPosB
                    else -> positions[positions.size / 2]
                }
                creations.add(
                    SpecialCandyCreation(
                        position = chosenPos,
                        specialType = SpecialCandyType.COLOR_BOMB,
                        baseType = match.type
                    )
                )
                processedMatchIndices.add(i)
            } else if (positions.size == 4) {
                // 4-match: Striped Candy (Horizontal or Vertical)
                val specialType = if (match.isHorizontal) {
                    SpecialCandyType.HORIZONTAL_STRIPED
                } else {
                    SpecialCandyType.VERTICAL_STRIPED
                }

                val chosenPos = when {
                    swapPosA != null && positions.contains(swapPosA) -> swapPosA
                    swapPosB != null && positions.contains(swapPosB) -> swapPosB
                    else -> positions[1]
                }

                creations.add(
                    SpecialCandyCreation(
                        position = chosenPos,
                        specialType = specialType,
                        baseType = match.type
                    )
                )
                processedMatchIndices.add(i)
            }
        }

        return creations
    }

    /**
     * Instantiates a new [CandyTile] with the specified special attributes.
     */
    fun createTile(
        position: BoardPosition,
        specialType: SpecialCandyType,
        baseType: CandyType
    ): CandyTile {
        return CandyTile(
            id = nextSpecialId++,
            type = if (specialType == SpecialCandyType.COLOR_BOMB) CandyType.EMPTY else baseType,
            row = position.row,
            column = position.column,
            specialCandyType = specialType
        )
    }
}
