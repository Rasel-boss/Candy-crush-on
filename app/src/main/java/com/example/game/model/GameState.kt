package com.example.game.model

import com.example.game.logic.LevelProvider

/**
 * Transient floating score indicator created when matches/cascades resolve.
 */
data class FloatingScoreEvent(
    val id: Long,
    val score: Int,
    val centerRow: Float,
    val centerColumn: Float,
    val cascadeCount: Int = 1,
    val text: String = if (cascadeCount > 1) "+$score\nCHAIN x$cascadeCount" else "+$score",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Immutable data state representing the entire Match-3 puzzle gameplay session.
 *
 * @property board The current 2D grid containing all candy tiles.
 * @property rows Number of rows in the board (default 8).
 * @property columns Number of columns in the board (default 8).
 * @property selectedPosition The position of the currently selected tile, if any.
 * @property score Accumulated score in the current game session (default 0).
 * @property movesRemaining Moves left for the player in this level (default 30).
 * @property level Active level number (default 1).
 * @property levelConfig Configuration details and targets for the active level.
 * @property objectives List of level objectives and their current progress.
 * @property isGameStarted Whether a game session is actively started.
 * @property isProcessing Whether board animations, matches, or collapses are resolving.
 * @property isLevelCompleted Whether the level was cleared.
 * @property isGameOver Whether the game concluded without victory.
 * @property status High-level lifecycle status.
 * @property cascadeChainCount Number of consecutive cascade match chains (0 when idle, 1 on 1st match, 2+ on cascades).
 * @property matchingPositions Positions of candy tiles currently matching / exploding.
 * @property matchIntensity Intensity level of the match (3 = normal 3-match, 4 = 4-match, 5 = 5+ match).
 * @property invalidSwapPair Positions of adjacent candies that were rejected during an invalid swap attempt.
 * @property floatingScoreEvents Active floating score indicators.
 * @property isBoardImpact Whether a subtle board-wide impact glow is currently active.
 */
data class GameState(
    val board: Match3Board,
    val rows: Int = DEFAULT_ROWS,
    val columns: Int = DEFAULT_COLUMNS,
    val selectedPosition: BoardPosition? = null,
    val score: Int = 0,
    val movesRemaining: Int = DEFAULT_MOVES,
    val level: Int = 1,
    val levelConfig: LevelConfig = LevelProvider.getLevelConfig(level),
    val objectives: List<LevelObjective> = emptyList(),
    val isGameStarted: Boolean = false,
    val isProcessing: Boolean = false,
    val isLevelCompleted: Boolean = false,
    val isGameOver: Boolean = false,
    val status: GameStatus = GameStatus.READY,
    val activeComboType: SpecialCombinationType = SpecialCombinationType.NONE,
    val comboPositions: Set<BoardPosition> = emptySet(),
    val cascadeChainCount: Int = 0,
    val matchingPositions: Set<BoardPosition> = emptySet(),
    val matchIntensity: Int = 3,
    val invalidSwapPair: Pair<BoardPosition, BoardPosition>? = null,
    val swappingPair: Pair<BoardPosition, BoardPosition>? = null,
    val floatingScoreEvents: List<FloatingScoreEvent> = emptyList(),
    val isBoardImpact: Boolean = false
) {
    /**
     * Currently selected tile instance, if a valid position is selected.
     */
    val selectedTile: CandyTile?
        get() = selectedPosition?.let { board.getTile(it) }

    /**
     * List of objectives that have satisfied their required targets.
     */
    val completedObjectives: List<LevelObjective>
        get() = objectives.filter { it.isCompleted }

    /**
     * Whether all objectives defined for this level have been fully completed.
     */
    val allObjectivesCompleted: Boolean
        get() = objectives.isNotEmpty() && objectives.all { it.isCompleted }

    companion object {
        /**
         * Creates the initial default state before a game session begins.
         */
        fun createInitial(
            rows: Int = DEFAULT_ROWS,
            columns: Int = DEFAULT_COLUMNS,
            level: Int = 1
        ): GameState {
            val emptyBoard = Match3Board.createEmpty(rows, columns)
            val config = LevelProvider.getLevelConfig(level)
            return GameState(
                board = emptyBoard,
                rows = rows,
                columns = columns,
                selectedPosition = null,
                score = 0,
                movesRemaining = config.startingMoves,
                level = level,
                levelConfig = config,
                objectives = config.objectives,
                isGameStarted = false,
                isProcessing = false,
                isLevelCompleted = false,
                isGameOver = false,
                status = GameStatus.READY,
                activeComboType = SpecialCombinationType.NONE,
                comboPositions = emptySet(),
                cascadeChainCount = 0,
                matchingPositions = emptySet(),
                matchIntensity = 3,
                invalidSwapPair = null,
                swappingPair = null,
                floatingScoreEvents = emptyList(),
                isBoardImpact = false
            )
        }
    }
}
