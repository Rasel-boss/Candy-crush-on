package com.example.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.logic.BoardGenerator
import com.example.game.logic.BoardValidator
import com.example.game.logic.GravityProcessor
import com.example.game.logic.BoardRefiller
import com.example.game.logic.LevelProgressionManager
import com.example.game.logic.LevelProvider
import com.example.game.logic.MatchDetector
import com.example.game.logic.MatchResolver
import com.example.game.logic.ObjectiveManager
import com.example.game.logic.SpecialCandyResolver
import com.example.game.logic.SpecialCombinationResolver
import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.DEFAULT_COLUMNS
import com.example.game.model.DEFAULT_MOVES
import com.example.game.model.DEFAULT_ROWS
import com.example.game.model.FloatingScoreEvent
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCombinationType
import com.example.game.utils.ScoreCalculator
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Result of a player tile interaction or swap attempt.
 */
sealed class SwapInteractionResult {
    data class Selected(val position: BoardPosition) : SwapInteractionResult()
    object Deselected : SwapInteractionResult()
    data class SelectionChanged(val newPosition: BoardPosition) : SwapInteractionResult()
    data class ValidSwap(val from: BoardPosition, val to: BoardPosition, val movesLeft: Int) : SwapInteractionResult()
    data class InvalidSwap(val from: BoardPosition, val to: BoardPosition) : SwapInteractionResult()
    object Ignored : SwapInteractionResult()
}

/**
 * ViewModel managing the Match-3 puzzle game lifecycle, tile selection,
 * adjacent tile swap validation, match resolution, gravity, board refill,
 * cascade iterations, score calculation, game feel effects, and move counting.
 */
class Match3ViewModel : ViewModel() {

    private val _gameState = MutableStateFlow(GameState.createInitial())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var cascadeJob: Job? = null
    private var invalidSwapJob: Job? = null
    private var floatingScoreId = 1L

    /** Delay in milliseconds between cascade visual steps (0 for instantaneous tests) */
    var stepDelayMs: Long = 120L

    /** Callbacks for sound and haptics triggered directly from ViewModel events */
    var onTileSelectedListener: (() -> Unit)? = null
    var onValidSwapListener: (() -> Unit)? = null
    var onInvalidSwapListener: (() -> Unit)? = null
    var onMatchResolvedListener: ((matchIntensity: Int) -> Unit)? = null
    var onCascadeListener: ((chainCount: Int) -> Unit)? = null
    var onGameOverListener: (() -> Unit)? = null
    var onLevelCompleteListener: (() -> Unit)? = null

    private var isStartingLevel = false

    /**
     * Starts or initiates a new Match-3 game session for the specified [level].
     * Loads the level configuration, creates a fresh board, resets score to 0,
     * initializes level objectives, and transitions status to [GameStatus.PLAYING].
     */
    fun startGame(
        level: Int = 1,
        rows: Int = DEFAULT_ROWS,
        columns: Int = DEFAULT_COLUMNS,
        random: Random = Random.Default,
        enforceLock: Boolean = false
    ): Boolean {
        cascadeJob?.cancel()
        invalidSwapJob?.cancel()

        val safeLevel = if (level <= 0) 1 else level
        if (enforceLock && !LevelProgressionManager.isLevelUnlocked(safeLevel)) {
            return false
        }

        val config = LevelProvider.getLevelConfig(safeLevel)
        val actualRows = if (config.rows > 0) config.rows else rows
        val actualCols = if (config.columns > 0) config.columns else columns
        val freshBoard = BoardGenerator.generateBoard(actualRows, actualCols, random)
        val initialObjectives = ObjectiveManager.initializeObjectives(config)

        _gameState.update {
            GameState(
                board = freshBoard,
                rows = actualRows,
                columns = actualCols,
                selectedPosition = null,
                score = 0,
                movesRemaining = config.startingMoves,
                level = safeLevel,
                levelConfig = config,
                objectives = initialObjectives,
                isGameStarted = true,
                isProcessing = false,
                isLevelCompleted = false,
                isGameOver = false,
                status = GameStatus.PLAYING,
                cascadeChainCount = 0,
                matchingPositions = emptySet(),
                invalidSwapPair = null,
                swappingPair = null,
                floatingScoreEvents = emptyList(),
                isBoardImpact = false
            )
        }
        return true
    }

    /**
     * Starts a level only if it is unlocked in the progression system.
     */
    fun startLevelIfUnlocked(
        level: Int,
        random: Random = Random.Default
    ): Boolean {
        return startGame(level = level, random = random, enforceLock = true)
    }

    /**
     * Handles user interaction when tapping a tile at [position].
     *
     * Rules:
     * 1. If game status is NOT [GameStatus.PLAYING] or is currently processing, ignore.
     * 2. If [position] is outside board bounds, ignore.
     * 3. If tile at [position] is EMPTY / not playable, ignore.
     * 4. If no tile is selected: select this tile.
     * 5. If tapped tile is the currently selected tile: deselect it (no move consumed).
     * 6. If tapped tile is NOT adjacent: update selection to the new tile (no move consumed).
     * 7. If tapped tile IS adjacent:
     *    - Evaluate whether swapping creates a valid match of 3 or more involving either swapped tile.
     *    - VALID SWAP:
     *        - Apply swapped board state.
     *        - Decrease [movesRemaining] by 1 (cannot become negative).
     *        - Clear [selectedPosition].
     *        - Transition to [GameStatus.PROCESSING] and resolve matches, gravity, refills, and cascades.
     *    - INVALID SWAP:
     *        - Restore / keep original board state.
     *        - [movesRemaining] and [score] unchanged.
     *        - Trigger brief rejection feedback on swapped tiles.
     *        - Clear [selectedPosition].
     *        - Return to [GameStatus.PLAYING].
     *
     * Returns true if selection changed or valid swap executed, false if invalid swap or ignored.
     */
    fun selectTile(position: BoardPosition, random: Random = Random.Default): Boolean {
        val currentState = _gameState.value

        // Guard 1: Interactions only allowed during active Playing state without processing lock
        if (currentState.status != GameStatus.PLAYING || currentState.isProcessing) {
            return false
        }

        // Guard 2: Position must be within board bounds
        if (!BoardValidator.isValidPosition(position, currentState.rows, currentState.columns)) {
            return false
        }

        // Guard 3: Tile must be playable (not EMPTY or missing)
        val tappedTile = currentState.board.getTile(position)
        if (tappedTile == null || !tappedTile.isPlayable) {
            return false
        }

        val currentSelected = currentState.selectedPosition

        if (currentSelected == null) {
            // First tile selection
            _gameState.update { it.copy(selectedPosition = position, invalidSwapPair = null) }
            onTileSelectedListener?.invoke()
            return true
        } else if (currentSelected == position) {
            // Tapped same tile again -> deselect
            _gameState.update { it.copy(selectedPosition = null, invalidSwapPair = null) }
            return true
        } else if (currentSelected.isAdjacent(position)) {
            // Adjacent tile tapped -> perform swap evaluation
            return attemptSwap(currentSelected, position, random)
        } else {
            // Non-adjacent tile tapped -> change selection to the new tile
            _gameState.update { it.copy(selectedPosition = position, invalidSwapPair = null) }
            onTileSelectedListener?.invoke()
            return true
        }
    }

    /**
     * Public alias for selecting/tapping a tile at [position], providing a clean API.
     */
    fun onTileTapped(position: BoardPosition, random: Random = Random.Default): Boolean {
        return selectTile(position, random)
    }

    /**
     * Public alias for tapping by row and column coordinates.
     */
    fun onTileTapped(row: Int, column: Int, random: Random = Random.Default): Boolean {
        return selectTile(BoardPosition(row, column), random)
    }

    /**
     * Convenience overload for selecting by row and column coordinates.
     */
    fun selectTile(row: Int, column: Int): Boolean = selectTile(BoardPosition(row, column))

    /**
     * Evaluates and attempts a swap between adjacent positions [from] and [to].
     * If valid, applies swap, decrements moves by 1, transitions to PROCESSING, and resolves cascade.
     * If invalid, triggers rejection feedback, restores original positions, preserves moves and score.
     */
    private fun attemptSwap(
        from: BoardPosition,
        to: BoardPosition,
        random: Random = Random.Default
    ): Boolean {
        val currentState = _gameState.value
        val selectedTile = currentState.board.getTile(from)
        val targetTile = currentState.board.getTile(to)
        if (selectedTile == null || !selectedTile.isPlayable || targetTile == null || !targetTile.isPlayable) {
            _gameState.update { it.copy(selectedPosition = null, invalidSwapPair = null) }
            return false
        }

        val isSpecialCombination = SpecialCombinationResolver.canCombine(
            board = currentState.board,
            posA = from,
            posB = to
        )
        val isSpecialSwap = isSpecialCombination || SpecialCandyResolver.isDirectSpecialSwap(
            board = currentState.board,
            posA = from,
            posB = to
        )
        val isMatchSwap = if (!isSpecialSwap) {
            MatchDetector.doesSwapCreateMatch(
                board = currentState.board,
                posA = from,
                posB = to
            )
        } else {
            false
        }

        val isValidSwap = isSpecialSwap || isMatchSwap

        if (isValidSwap) {
            val newMovesRemaining = (currentState.movesRemaining - 1).coerceAtLeast(0)
            onValidSwapListener?.invoke()

            if (stepDelayMs == 0L) {
                // Synchronous immediate execution (tests)
                val swappedBoard = currentState.board.swapTiles(from, to)
                _gameState.update {
                    it.copy(
                        board = swappedBoard,
                        selectedPosition = null,
                        movesRemaining = newMovesRemaining,
                        isProcessing = true,
                        invalidSwapPair = null,
                        swappingPair = null,
                        status = GameStatus.PROCESSING
                    )
                }
                resolveCascadesSynchronously(from, to, random)
            } else {
                // Smooth visual sliding animation phase
                _gameState.update {
                    it.copy(
                        selectedPosition = null,
                        movesRemaining = newMovesRemaining,
                        isProcessing = true,
                        invalidSwapPair = null,
                        swappingPair = Pair(from, to),
                        status = GameStatus.PROCESSING
                    )
                }

                cascadeJob?.cancel()
                cascadeJob = viewModelScope.launch {
                    delay(140L)
                    val swappedBoard = _gameState.value.board.swapTiles(from, to)
                    _gameState.update {
                        it.copy(
                            board = swappedBoard,
                            swappingPair = null
                        )
                    }
                    resolveMatchesAndCascades(from, to, random)
                }
            }
            return true
        } else {
            // INVALID SWAP: Trigger rejection feedback, restore original board, do not decrement moves
            _gameState.update {
                it.copy(
                    selectedPosition = null,
                    invalidSwapPair = Pair(from, to),
                    swappingPair = null,
                    status = GameStatus.PLAYING
                )
            }
            onInvalidSwapListener?.invoke()

            if (stepDelayMs > 0) {
                invalidSwapJob?.cancel()
                invalidSwapJob = viewModelScope.launch {
                    delay(220L)
                    _gameState.update { it.copy(invalidSwapPair = null) }
                }
            } else {
                _gameState.update { it.copy(invalidSwapPair = null) }
            }
            return false
        }
    }

    /**
     * Calculates deterministic match score for match length [matchLength].
     */
    fun calculateMatchScore(matchLength: Int): Int = ScoreCalculator.calculateMatchScore(matchLength)

    /**
     * Removes matched candy tiles at [positions], returning the updated board with EMPTY slots.
     */
    fun removeMatches(board: Match3Board, positions: Set<BoardPosition>): Match3Board {
        return MatchResolver.removeMatches(board, positions)
    }

    /**
     * Applies downward column gravity, shifting floating candies down.
     */
    fun collapseBoard(board: Match3Board): Match3Board {
        return GravityProcessor.applyGravity(board)
    }

    /**
     * Refills all EMPTY slots from the top with fresh random playable candies.
     */
    fun refillBoard(board: Match3Board, random: Random = Random.Default): Match3Board {
        return BoardRefiller.refillBoard(board, random)
    }

    /**
     * Executes asynchronous cascade resolution pipeline:
     * 1. Detect all matches and special activations on current board.
     * 2. For each step:
     *    - Score points for matches and special activations.
     *    - Trigger match dissolve animation (highlight, pulse, fade).
     *    - Create floating score indicator.
     *    - Remove matches/specials (and place new special candies).
     *    - Apply downward gravity (candies fall, EMPTY at top).
     *    - Refill empty positions with new random candies.
     *    - Repeat until no matches remain.
     * 3. Transition to [GameStatus.GAME_OVER] if moves reached 0, or [GameStatus.PLAYING] otherwise.
     */
    private fun resolveMatchesAndCascades(
        swapPosA: BoardPosition? = null,
        swapPosB: BoardPosition? = null,
        random: Random = Random.Default
    ) {
        cascadeJob?.cancel()
        cascadeJob = viewModelScope.launch {
            var currentBoard = _gameState.value.board
            var iterations = 0
            val maxIterations = MatchResolver.MAX_CASCADE_ITERATIONS
            val alreadyActivatedIds = mutableSetOf<Long>()

            // 1. If direct special swap was initiated
            if (swapPosA != null && swapPosB != null && SpecialCandyResolver.isDirectSpecialSwap(currentBoard, swapPosA, swapPosB)) {
                val tileA = currentBoard.getTile(swapPosA)
                val tileB = currentBoard.getTile(swapPosB)
                val comboType = if (tileA != null && tileB != null) {
                    SpecialCombinationResolver.detectCombination(tileA, tileB)
                } else {
                    SpecialCombinationType.NONE
                }

                val specialStep = MatchResolver.resolveDirectSpecialSwapStep(
                    currentBoard = currentBoard,
                    posA = swapPosA,
                    posB = swapPosB,
                    random = random,
                    alreadyActivatedIds = alreadyActivatedIds
                )

                val centerRow = if (specialStep.matchedPositions.isNotEmpty()) {
                    specialStep.matchedPositions.map { it.row }.average().toFloat()
                } else 3.5f
                val centerCol = if (specialStep.matchedPositions.isNotEmpty()) {
                    specialStep.matchedPositions.map { it.column }.average().toFloat()
                } else 3.5f

                val floatingEvent = FloatingScoreEvent(
                    id = floatingScoreId++,
                    score = specialStep.stepScore,
                    centerRow = centerRow,
                    centerColumn = centerCol,
                    cascadeCount = 1
                )

                _gameState.update {
                    it.copy(
                        matchingPositions = specialStep.matchedPositions,
                        matchIntensity = 5,
                        cascadeChainCount = 1,
                        activeComboType = comboType,
                        comboPositions = specialStep.matchedPositions,
                        floatingScoreEvents = it.floatingScoreEvents + floatingEvent,
                        isBoardImpact = true
                    )
                }
                onMatchResolvedListener?.invoke(5)
                if (stepDelayMs > 0) delay(stepDelayMs)

                val newScore = _gameState.value.score + specialStep.stepScore
                var updatedObjectives = ObjectiveManager.onCandiesRemoved(_gameState.value.objectives, specialStep.removedTiles)
                updatedObjectives = ObjectiveManager.onScoreChanged(updatedObjectives, newScore)

                _gameState.update {
                    it.copy(
                        board = specialStep.boardAfterRemoval,
                        score = newScore,
                        objectives = updatedObjectives,
                        matchingPositions = emptySet(),
                        isBoardImpact = false
                    )
                }
                if (stepDelayMs > 0) delay(stepDelayMs)

                _gameState.update { it.copy(board = specialStep.boardAfterGravity) }
                if (stepDelayMs > 0) delay(stepDelayMs)

                _gameState.update {
                    it.copy(
                        board = specialStep.boardAfterRefill,
                        activeComboType = SpecialCombinationType.NONE,
                        comboPositions = emptySet()
                    )
                }
                if (stepDelayMs > 0) delay(stepDelayMs)

                currentBoard = specialStep.boardAfterRefill
                iterations++
            }

            while (iterations < maxIterations) {
                val isFirstStep = iterations == 0
                val stepSwapA = if (isFirstStep) swapPosA else null
                val stepSwapB = if (isFirstStep) swapPosB else null

                val step = MatchResolver.resolveSingleStep(
                    currentBoard = currentBoard,
                    swapPosA = stepSwapA,
                    swapPosB = stepSwapB,
                    random = random,
                    alreadyActivatedIds = alreadyActivatedIds
                ) ?: break

                val chainCount = iterations + 1
                val maxMatchLength = step.matches.maxOfOrNull { it.length } ?: 3

                val centerRow = if (step.matchedPositions.isNotEmpty()) {
                    step.matchedPositions.map { it.row }.average().toFloat()
                } else 3.5f
                val centerCol = if (step.matchedPositions.isNotEmpty()) {
                    step.matchedPositions.map { it.column }.average().toFloat()
                } else 3.5f

                val floatingEvent = FloatingScoreEvent(
                    id = floatingScoreId++,
                    score = step.stepScore,
                    centerRow = centerRow,
                    centerColumn = centerCol,
                    cascadeCount = chainCount
                )

                // Match step: highlight, glow, pulse
                _gameState.update {
                    it.copy(
                        matchingPositions = step.matchedPositions,
                        matchIntensity = maxMatchLength,
                        cascadeChainCount = chainCount,
                        floatingScoreEvents = it.floatingScoreEvents + floatingEvent,
                        isBoardImpact = (step.matchedPositions.size >= 6 || chainCount >= 2)
                    )
                }

                if (chainCount >= 2) {
                    onCascadeListener?.invoke(chainCount)
                } else {
                    onMatchResolvedListener?.invoke(maxMatchLength)
                }
                if (stepDelayMs > 0) delay(stepDelayMs)

                val newScore = _gameState.value.score + step.stepScore
                var updatedObjectives = ObjectiveManager.onCandiesRemoved(_gameState.value.objectives, step.removedTiles)
                updatedObjectives = ObjectiveManager.onMatchesMade(updatedObjectives, step.matches.size)
                updatedObjectives = ObjectiveManager.onScoreChanged(updatedObjectives, newScore)

                // Update board after removal & placing specials
                _gameState.update {
                    it.copy(
                        board = step.boardAfterRemoval,
                        score = newScore,
                        objectives = updatedObjectives,
                        matchingPositions = emptySet(),
                        isBoardImpact = false
                    )
                }
                if (stepDelayMs > 0) delay(stepDelayMs)

                // Update board after gravity
                _gameState.update { it.copy(board = step.boardAfterGravity) }
                if (stepDelayMs > 0) delay(stepDelayMs)

                // Update board after refill
                _gameState.update { it.copy(board = step.boardAfterRefill) }
                if (stepDelayMs > 0) delay(stepDelayMs)

                currentBoard = step.boardAfterRefill
                iterations++
            }

            // Board is now stable; determine next game state
            val finalState = _gameState.value
            val wasAlreadyCompleted = finalState.isLevelCompleted || finalState.status == GameStatus.COMPLETED
            val wasAlreadyGameOver = finalState.isGameOver || finalState.status == GameStatus.GAME_OVER

            val allCompleted = ObjectiveManager.areAllObjectivesCompleted(finalState.objectives)
            val finalMoves = finalState.movesRemaining
            val isGameOver = !allCompleted && finalMoves == 0
            val isCompleted = allCompleted

            val nextStatus = when {
                isCompleted -> GameStatus.COMPLETED
                isGameOver -> GameStatus.GAME_OVER
                else -> GameStatus.PLAYING
            }

            _gameState.update {
                it.copy(
                    isProcessing = false,
                    isLevelCompleted = isCompleted,
                    isGameOver = isGameOver,
                    status = nextStatus,
                    cascadeChainCount = 0,
                    matchingPositions = emptySet(),
                    isBoardImpact = false
                )
            }

            // Automatic Dead Board Recovery: If playing and no moves remain, regenerate board safely
            if (nextStatus == GameStatus.PLAYING && !MatchDetector.hasPossibleMoves(currentBoard)) {
                val recoveredBoard = BoardGenerator.generateBoard(
                    rows = finalState.rows,
                    columns = finalState.columns,
                    random = random,
                    allowedTypes = CandyType.PLAYABLE_TYPES,
                    ensurePossibleMoves = true
                )
                _gameState.update {
                    it.copy(
                        board = recoveredBoard,
                        selectedPosition = null
                    )
                }
            }

            if (isCompleted && !wasAlreadyCompleted) {
                LevelProgressionManager.recordLevelCompletion(finalState.level, finalState.score)
                onLevelCompleteListener?.invoke()
            } else if (isGameOver && !wasAlreadyGameOver) {
                onGameOverListener?.invoke()
            }
        }
    }

    /**
     * Synchronously resolves matches and cascades on the current state.
     * Useful for deterministic testing and immediate state verification.
     */
    fun resolveCascadesSynchronously(
        swapPosA: BoardPosition? = null,
        swapPosB: BoardPosition? = null,
        random: Random = Random.Default
    ) {
        val wasAlreadyCompleted = _gameState.value.isLevelCompleted || _gameState.value.status == GameStatus.COMPLETED
        val wasAlreadyGameOver = _gameState.value.isGameOver || _gameState.value.status == GameStatus.GAME_OVER

        val currentBoard = _gameState.value.board
        val result = MatchResolver.resolveAllCascades(
            initialBoard = currentBoard,
            swapPosA = swapPosA,
            swapPosB = swapPosB,
            random = random
        )
        val newScore = _gameState.value.score + result.totalScoreGained
        var updatedObjectives = _gameState.value.objectives
        for (step in result.steps) {
            updatedObjectives = ObjectiveManager.onCandiesRemoved(updatedObjectives, step.removedTiles)
            updatedObjectives = ObjectiveManager.onMatchesMade(updatedObjectives, step.matches.size)
        }
        updatedObjectives = ObjectiveManager.onScoreChanged(updatedObjectives, newScore)

        val allCompleted = ObjectiveManager.areAllObjectivesCompleted(updatedObjectives)
        val finalMoves = _gameState.value.movesRemaining
        val isGameOver = !allCompleted && finalMoves == 0
        val isCompleted = allCompleted

        val nextStatus = when {
            isCompleted -> GameStatus.COMPLETED
            isGameOver -> GameStatus.GAME_OVER
            else -> GameStatus.PLAYING
        }

        var finalBoard = result.finalBoard
        // Automatic Dead Board Recovery: If playing and no moves remain, regenerate board safely
        if (nextStatus == GameStatus.PLAYING && !MatchDetector.hasPossibleMoves(finalBoard)) {
            finalBoard = BoardGenerator.generateBoard(
                rows = _gameState.value.rows,
                columns = _gameState.value.columns,
                random = random,
                allowedTypes = CandyType.PLAYABLE_TYPES,
                ensurePossibleMoves = true
            )
        }

        _gameState.update {
            it.copy(
                board = finalBoard,
                score = newScore,
                objectives = updatedObjectives,
                selectedPosition = null,
                isProcessing = false,
                isLevelCompleted = isCompleted,
                isGameOver = isGameOver,
                status = nextStatus,
                cascadeChainCount = 0,
                matchingPositions = emptySet(),
                invalidSwapPair = null,
                isBoardImpact = false
            )
        }

        if (isCompleted && !wasAlreadyCompleted) {
            LevelProgressionManager.recordLevelCompletion(_gameState.value.level, newScore)
            onLevelCompleteListener?.invoke()
        } else if (isGameOver && !wasAlreadyGameOver) {
            onGameOverListener?.invoke()
        }
    }

    /**
     * Checks whether the active game board has any valid possible moves remaining.
     * If no valid moves exist and the game is active, automatically recovers/reshuffles the board
     * while preserving score, moves remaining, objectives, and level.
     */
    fun checkAndRecoverDeadBoard(random: Random = Random.Default): Boolean {
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING || state.isProcessing || state.isGameOver || state.isLevelCompleted) {
            return false
        }
        if (!MatchDetector.hasPossibleMoves(state.board)) {
            val recoveredBoard = BoardGenerator.generateBoard(
                rows = state.rows,
                columns = state.columns,
                random = random,
                allowedTypes = CandyType.PLAYABLE_TYPES,
                ensurePossibleMoves = true
            )
            _gameState.update {
                it.copy(
                    board = recoveredBoard,
                    selectedPosition = null
                )
            }
            return true
        }
        return false
    }

    /**
     * Synchronous resolution overload without swap positions for cascade testing.
     */
    fun resolveCascadesSynchronously(random: Random = Random.Default) {
        resolveCascadesSynchronously(null, null, random)
    }

    /**
     * Restarts the current Match-3 level with a freshly generated valid board,
     * resetting score to 0, moves to starting moves, and objective progress.
     */
    fun restartGame(random: Random = Random.Default): Boolean {
        return replayLevel(random)
    }

    /**
     * Replays the active level.
     * Generates a fresh board, resets score to 0, moves to initial starting moves,
     * resets objective progress, and transitions status to [GameStatus.PLAYING].
     */
    fun replayLevel(random: Random = Random.Default): Boolean {
        if (isStartingLevel) return false
        isStartingLevel = true
        try {
            val currentLevel = _gameState.value.level
            return startGame(level = currentLevel, random = random)
        } finally {
            isStartingLevel = false
        }
    }

    /**
     * Advances to the next level in sequence.
     * Increments the level number, loads the corresponding [LevelConfig],
     * resets score to 0, starting moves, and initializes fresh objectives.
     */
    fun nextLevel(random: Random = Random.Default): Boolean {
        if (isStartingLevel) return false
        isStartingLevel = true
        try {
            val nextLevelNumber = _gameState.value.level + 1
            return startGame(level = nextLevelNumber, random = random)
        } finally {
            isStartingLevel = false
        }
    }

    /**
     * Pauses the active game session.
     */
    fun pauseGame() {
        _gameState.update { currentState ->
            if (currentState.status == GameStatus.PLAYING && !currentState.isProcessing) {
                currentState.copy(status = GameStatus.PAUSED)
            } else {
                currentState
            }
        }
    }

    /**
     * Resumes the paused game session.
     */
    fun resumeGame() {
        _gameState.update { currentState ->
            if (currentState.status == GameStatus.PAUSED) {
                currentState.copy(status = GameStatus.PLAYING)
            } else {
                currentState
            }
        }
    }

    /**
     * Resets the game back to the initial Ready state for Level 1.
     */
    fun resetGame() {
        cascadeJob?.cancel()
        invalidSwapJob?.cancel()
        _gameState.update { currentState ->
            GameState.createInitial(currentState.rows, currentState.columns, level = 1)
        }
    }

    /**
     * Direct board injection helper for deterministic unit and UI testing.
     */
    internal fun setCustomBoard(board: Match3Board) {
        cascadeJob?.cancel()
        invalidSwapJob?.cancel()
        _gameState.update { it.copy(board = board) }
    }

    /**
     * Direct state injection helper for deterministic unit and UI testing.
     */
    internal fun setCustomState(state: GameState) {
        cascadeJob?.cancel()
        invalidSwapJob?.cancel()
        _gameState.value = state
    }
}
