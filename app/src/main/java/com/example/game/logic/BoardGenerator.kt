package com.example.game.logic

import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import kotlin.random.Random

class BoardGenerator(
    private val matchDetector: MatchDetector = MatchDetector(),
    private val boardValidator: BoardValidator = BoardValidator(),
    private val random: Random = Random.Default
) {
    private var tileIdCounter = 1L

    fun generateBoard(
        rows: Int = 8,
        cols: Int = 8,
        allowedTypes: List<CandyType> = CandyType.entries
    ): Match3Board {
        var board: Match3Board
        var attempts = 0
        do {
            board = createRandomBoardNoImmediateMatches(rows, cols, allowedTypes)
            attempts++
        } while (!boardValidator.hasPossibleMoves(board) && attempts < 100)

        return board
    }

    private fun createRandomBoardNoImmediateMatches(
        rows: Int,
        cols: Int,
        allowedTypes: List<CandyType>
    ): Match3Board {
        var board = Match3Board(rows, cols)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val forbidden = mutableSetOf<CandyType>()
                if (c >= 2) {
                    val left1 = board[r, c - 1]?.type
                    val left2 = board[r, c - 2]?.type
                    if (left1 != null && left1 == left2) {
                        forbidden.add(left1)
                    }
                }
                if (r >= 2) {
                    val up1 = board[r - 1, c]?.type
                    val up2 = board[r - 2, c]?.type
                    if (up1 != null && up1 == up2) {
                        forbidden.add(up1)
                    }
                }

                val available = allowedTypes.filter { it !in forbidden }
                val chosenType = if (available.isNotEmpty()) {
                    available[random.nextInt(available.size)]
                } else {
                    allowedTypes[random.nextInt(allowedTypes.size)]
                }

                board = board.set(r, c, CandyTile(id = ++tileIdCounter, type = chosenType))
            }
        }

        return board
    }
}
