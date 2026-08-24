package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.Match3Board

class MatchDetector {

    fun findMatches(board: Match3Board): List<MatchGroup> {
        val horizontalMatches = findLineMatches(board, isHorizontal = true)
        val verticalMatches = findLineMatches(board, isHorizontal = false)

        val merged = mutableListOf<MatchGroup>()
        val usedHorizontal = mutableSetOf<MatchGroup>()
        val usedVertical = mutableSetOf<MatchGroup>()

        // Check for T or L shapes (intersecting horizontal and vertical matches)
        for (hMatch in horizontalMatches) {
            for (vMatch in verticalMatches) {
                if (hMatch.type == vMatch.type) {
                    val intersection = hMatch.positions.intersect(vMatch.positions)
                    if (intersection.isNotEmpty()) {
                        val combined = hMatch.positions + vMatch.positions
                        merged.add(
                            MatchGroup(
                                positions = combined,
                                type = hMatch.type,
                                isHorizontal = true,
                                isVertical = true,
                                isTOrLShape = true
                            )
                        )
                        usedHorizontal.add(hMatch)
                        usedVertical.add(vMatch)
                    }
                }
            }
        }

        for (hMatch in horizontalMatches) {
            if (hMatch !in usedHorizontal) {
                merged.add(hMatch)
            }
        }

        for (vMatch in verticalMatches) {
            if (vMatch !in usedVertical) {
                merged.add(vMatch)
            }
        }

        return merged
    }

    private fun findLineMatches(board: Match3Board, isHorizontal: Boolean): List<MatchGroup> {
        val matches = mutableListOf<MatchGroup>()
        val primaryLimit = if (isHorizontal) board.rows else board.cols
        val secondaryLimit = if (isHorizontal) board.cols else board.rows

        for (primary in 0 until primaryLimit) {
            var currentRun = mutableListOf<BoardPosition>()
            var currentType: com.example.game.model.CandyType? = null

            for (secondary in 0 until secondaryLimit) {
                val row = if (isHorizontal) primary else secondary
                val col = if (isHorizontal) secondary else primary
                val pos = BoardPosition(row, col)
                val tile = board[pos]

                if (tile != null && !tile.isColorBomb) {
                    if (tile.type == currentType) {
                        currentRun.add(pos)
                    } else {
                        if (currentRun.size >= 3 && currentType != null) {
                            matches.add(
                                MatchGroup(
                                    positions = currentRun.toSet(),
                                    type = currentType,
                                    isHorizontal = isHorizontal,
                                    isVertical = !isHorizontal
                                )
                            )
                        }
                        currentRun = mutableListOf(pos)
                        currentType = tile.type
                    }
                } else {
                    if (currentRun.size >= 3 && currentType != null) {
                        matches.add(
                            MatchGroup(
                                positions = currentRun.toSet(),
                                type = currentType,
                                isHorizontal = isHorizontal,
                                isVertical = !isHorizontal
                            )
                        )
                    }
                    currentRun = mutableListOf()
                    currentType = null
                }
            }

            if (currentRun.size >= 3 && currentType != null) {
                matches.add(
                    MatchGroup(
                        positions = currentRun.toSet(),
                        type = currentType,
                        isHorizontal = isHorizontal,
                        isVertical = !isHorizontal
                    )
                )
            }
        }

        return matches
    }
}
