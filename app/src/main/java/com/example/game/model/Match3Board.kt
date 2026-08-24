package com.example.game.model

data class Match3Board(
    val rows: Int = 8,
    val cols: Int = 8,
    val tiles: List<List<CandyTile?>> = List(rows) { List(cols) { null } }
) {
    operator fun get(row: Int, col: Int): CandyTile? {
        if (row !in 0 until rows || col !in 0 until cols) return null
        return tiles[row][col]
    }

    operator fun get(pos: BoardPosition): CandyTile? = get(pos.row, pos.col)

    fun set(row: Int, col: Int, tile: CandyTile?): Match3Board {
        if (row !in 0 until rows || col !in 0 until cols) return this
        val newTiles = tiles.mapIndexed { r, rowList ->
            if (r == row) {
                rowList.mapIndexed { c, currentTile ->
                    if (c == col) tile else currentTile
                }
            } else {
                rowList
            }
        }
        return copy(tiles = newTiles)
    }

    fun set(pos: BoardPosition, tile: CandyTile?): Match3Board = set(pos.row, pos.col, tile)

    fun swap(pos1: BoardPosition, pos2: BoardPosition): Match3Board {
        val tile1 = get(pos1)
        val tile2 = get(pos2)
        return set(pos1, tile2).set(pos2, tile1)
    }

    fun isValidPosition(pos: BoardPosition): Boolean =
        pos.row in 0 until rows && pos.col in 0 until cols

    fun allPositions(): List<BoardPosition> {
        val list = mutableListOf<BoardPosition>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                list.add(BoardPosition(r, c))
            }
        }
        return list
    }
}
