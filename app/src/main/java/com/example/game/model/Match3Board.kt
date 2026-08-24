package com.example.game.model

/** Default number of rows on the standard Match-3 board */
const val DEFAULT_ROWS = 8

/** Default number of columns on the standard Match-3 board */
const val DEFAULT_COLUMNS = 8

/** Default number of starting moves for standard level */
const val DEFAULT_MOVES = 30

/**
 * Immutable representation of the 2D Match-3 grid of candy tiles.
 *
 * @property rows The number of rows in the grid (default 8).
 * @property columns The number of columns in the grid (default 8).
 * @property tiles 2D matrix of tiles organized by [row][column].
 */
data class Match3Board(
    val rows: Int = DEFAULT_ROWS,
    val columns: Int = DEFAULT_COLUMNS,
    val tiles: List<List<CandyTile>>
) {
    /**
     * Total number of tile slots on the board.
     */
    val totalPositions: Int
        get() = rows * columns

    /**
     * Flat list of all tiles on the board.
     */
    val allTiles: List<CandyTile>
        get() = tiles.flatten()

    /**
     * Retrieves the tile at the specified [row] and [column], or null if out of bounds.
     */
    fun getTile(row: Int, column: Int): CandyTile? {
        if (row !in 0 until rows || column !in 0 until columns) return null
        return tiles.getOrNull(row)?.getOrNull(column)
    }

    /**
     * Retrieves the tile at [position], or null if out of bounds.
     */
    fun getTile(position: BoardPosition): CandyTile? = getTile(position.row, position.column)

    /**
     * Returns a new [Match3Board] with the tile at [row] and [column] replaced by [tile].
     */
    fun withTile(row: Int, column: Int, tile: CandyTile): Match3Board {
        if (row !in 0 until rows || column !in 0 until columns) return this
        val updatedTiles = tiles.mapIndexed { r, rowList ->
            if (r == row) {
                rowList.mapIndexed { c, currentTile ->
                    if (c == column) tile.copy(row = row, column = column) else currentTile
                }
            } else {
                rowList
            }
        }
        return copy(tiles = updatedTiles)
    }

    /**
     * Returns a new [Match3Board] with the tile at [tile.position] replaced by [tile].
     */
    fun withTile(tile: CandyTile): Match3Board = withTile(tile.row, tile.column, tile)

    /**
     * Returns a new [Match3Board] with the tiles at [posA] and [posB] swapped.
     * Tile positions (row, column) are updated accordingly.
     */
    fun swapTiles(posA: BoardPosition, posB: BoardPosition): Match3Board {
        val tileA = getTile(posA) ?: return this
        val tileB = getTile(posB) ?: return this

        val updatedTiles = tiles.map { rowList ->
            rowList.map { tile ->
                when (tile.position) {
                    posA -> tileB.copy(row = posA.row, column = posA.column)
                    posB -> tileA.copy(row = posB.row, column = posB.column)
                    else -> tile
                }
            }
        }
        return copy(tiles = updatedTiles)
    }

    companion object {
        /**
         * Creates an empty board filled with [CandyType.EMPTY] tiles.
         */
        fun createEmpty(rows: Int = DEFAULT_ROWS, columns: Int = DEFAULT_COLUMNS): Match3Board {
            var nextId = 1L
            val grid = List(rows) { r ->
                List(columns) { c ->
                    CandyTile(
                        id = nextId++,
                        type = CandyType.EMPTY,
                        row = r,
                        column = c
                    )
                }
            }
            return Match3Board(rows = rows, columns = columns, tiles = grid)
        }
    }
}
