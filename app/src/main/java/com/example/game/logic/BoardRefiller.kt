package com.example.game.logic

import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.Match3Board
import kotlin.random.Random

data class SpawnedTile(
    val position: BoardPosition,
    val tile: CandyTile
)

data class RefillResult(
    val updatedBoard: Match3Board,
    val spawnedTiles: List<SpawnedTile>
)

class BoardRefiller(private val random: Random = Random.Default) {

    private var nextId = 500000L

    fun refillBoard(
        board: Match3Board,
        allowedCandyTypes: List<CandyType> = CandyType.entries
    ): RefillResult {
        var currentBoard = board
        val spawned = mutableListOf<SpawnedTile>()

        for (col in 0 until board.cols) {
            for (row in 0 until board.rows) {
                if (currentBoard[row, col] == null) {
                    val randomType = allowedCandyTypes[random.nextInt(allowedCandyTypes.size)]
                    val newTile = CandyTile(id = ++nextId, type = randomType)
                    val pos = BoardPosition(row, col)
                    spawned.add(SpawnedTile(pos, newTile))
                    currentBoard = currentBoard.set(pos, newTile)
                }
            }
        }

        return RefillResult(currentBoard, spawned)
    }
}
