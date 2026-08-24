package com.example.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.audio.HapticFeedbackManager
import com.example.game.audio.ISoundManager
import com.example.game.audio.SoundManager
import com.example.game.logic.BoardGenerator
import com.example.game.logic.BoardRefiller
import com.example.game.logic.BoardValidator
import com.example.game.logic.GravityProcessor
import com.example.game.logic.LevelProvider
import com.example.game.logic.MatchDetector
import com.example.game.logic.MatchResolver
import com.example.game.logic.ObjectiveManager
import com.example.game.logic.SpecialCandyActivator
import com.example.game.logic.SpecialCandyCreator
import com.example.game.logic.SpecialCandyResolver
import com.example.game.logic.SpecialCombinationResolver
import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.Match3Board
import com.example.game.utils.ScoreCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class Match3ViewModel(
    private val boardGenerator: BoardGenerator = BoardGenerator(),
    private val matchDetector: MatchDetector = MatchDetector(),
    private val specialCreator: SpecialCandyCreator = SpecialCandyCreator(),
    private val specialResolver: SpecialCandyResolver = SpecialCandyResolver(),
    private val combinationResolver: SpecialCombinationResolver = SpecialCombinationResolver(),
    private val specialActivator: SpecialCandyActivator = SpecialCandyActivator(),
    private val gravityProcessor: GravityProcessor = GravityProcessor(),
    private val boardRefiller: BoardRefiller = BoardRefiller(),
    private val boardValidator: BoardValidator = BoardValidator(),
    private val scoreCalculator: ScoreCalculator = ScoreCalculator(),
    private val objectiveManager: ObjectiveManager = ObjectiveManager(),
    private val soundManager: ISoundManager = SoundManager(),
    private val hapticManager: HapticFeedbackManager? = null
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    init {
        startLevel(1)
    }

    fun startLevel(level: Int) {
        val config = LevelProvider.getLevelConfig(level)
        val initialBoard = boardGenerator.generateBoard()

        _gameState.value = GameState(
            board = initialBoard,
            score = 0,
            targetScore = config.targetScore,
            movesRemaining = config.maxMoves,
            currentLevel = level,
            status = GameStatus.IDLE,
            selectedPosition = null,
            levelConfig = config,
            objectives = config.objectives
        )
    }

    fun restartCurrentLevel() {
        startLevel(_gameState.value.currentLevel)
    }

    fun nextLevel() {
        startLevel(_gameState.value.currentLevel + 1)
    }

    fun pauseGame() {
        if (_gameState.value.status == GameStatus.IDLE) {
            _gameState.update { it.copy(status = GameStatus.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_gameState.value.status == GameStatus.PAUSED) {
            _gameState.update { it.copy(status = GameStatus.IDLE) }
        }
    }

    fun onTileClicked(position: BoardPosition) {
        val state = _gameState.value
        if (state.status != GameStatus.IDLE) return

        val selected = state.selectedPosition
        if (selected == null) {
            _gameState.update { it.copy(selectedPosition = position) }
        } else {
            if (selected == position) {
                _gameState.update { it.copy(selectedPosition = null) }
            } else if (selected.isAdjacentTo(position)) {
                _gameState.update { it.copy(selectedPosition = null) }
                performSwap(selected, position)
            } else {
                _gameState.update { it.copy(selectedPosition = position) }
            }
        }
    }

    private fun performSwap(pos1: BoardPosition, pos2: BoardPosition) {
        viewModelScope.launch {
            val state = _gameState.value
            val tile1 = state.board[pos1] ?: return@launch
            val tile2 = state.board[pos2] ?: return@launch

            _gameState.update { it.copy(status = GameStatus.SWAPPING) }

            // Check if special combination exists
            val comboType = combinationResolver.checkCombination(tile1, tile2)
            if (comboType != null) {
                val comboResult = combinationResolver.resolveCombination(
                    state.board, pos1, pos2, tile1, tile2
                )
                if (comboResult != null) {
                    executeSpecialCombination(comboResult, pos2)
                    return@launch
                }
            }

            // Check if single color bomb activated with normal candy
            if (tile1.isColorBomb || tile2.isColorBomb) {
                val bombPos = if (tile1.isColorBomb) pos1 else pos2
                val targetCandy = if (tile1.isColorBomb) tile2 else tile1
                executeColorBombDetonation(bombPos, targetCandy.type)
                return@launch
            }

            // Standard match swap
            val swappedBoard = state.board.swap(pos1, pos2)
            val matches = matchDetector.findMatches(swappedBoard)

            if (matches.isNotEmpty()) {
                val newMoves = state.movesRemaining - 1
                _gameState.update {
                    it.copy(
                        board = swappedBoard,
                        movesRemaining = newMoves,
                        status = GameStatus.MATCHING
                    )
                }
                soundManager.playMatchSound()
                hapticManager?.performMatchHaptic()
                resolveCascades(swappedBoard, pos2)
            } else {
                // Invalid swap: brief visual feedback then revert
                val tempBoard = state.board.swap(pos1, pos2)
                _gameState.update { it.copy(board = tempBoard) }
                delay(200)
                _gameState.update { it.copy(board = state.board, status = GameStatus.IDLE) }
            }
        }
    }

    private suspend fun executeSpecialCombination(
        comboResult: com.example.game.logic.CombinationResult,
        center: BoardPosition
    ) {
        val state = _gameState.value
        val newMoves = state.movesRemaining - 1
        _gameState.update { it.copy(movesRemaining = newMoves, status = GameStatus.MATCHING) }
        soundManager.playDetonationSound()
        hapticManager?.performSpecialExplosionHaptic()

        val removedTiles = mutableListOf<CandyTile>()
        for (pos in comboResult.affectedPositions) {
            val tile = state.board[pos]
            if (tile != null) removedTiles.add(tile)
        }

        val stepScore = scoreCalculator.calculateMatchScore(
            matchCount = 1,
            totalCandiesCleared = comboResult.affectedPositions.size,
            cascadeIndex = 0,
            specialsActivated = 2
        ) * comboResult.scoreMultiplier

        var boardAfterRemoval = state.board
        for (pos in comboResult.affectedPositions) {
            boardAfterRemoval = boardAfterRemoval.set(pos, null)
        }

        val updatedObjectives = objectiveManager.updateObjectives(
            objectives = state.objectives,
            removedTiles = removedTiles,
            matchesFormed = 1,
            currentScore = state.score + stepScore
        )

        _gameState.update {
            it.copy(
                board = boardAfterRemoval,
                score = it.score + stepScore,
                objectives = updatedObjectives
            )
        }
        delay(250)

        // Gravity & Refill
        val gravity = gravityProcessor.applyGravity(boardAfterRemoval)
        val refill = boardRefiller.refillBoard(gravity.updatedBoard)
        _gameState.update { it.copy(board = refill.updatedBoard) }
        delay(200)

        resolveCascades(refill.updatedBoard, center)
    }

    private suspend fun executeColorBombDetonation(bombPos: BoardPosition, targetColor: CandyType) {
        val state = _gameState.value
        val newMoves = state.movesRemaining - 1
        _gameState.update { it.copy(movesRemaining = newMoves, status = GameStatus.MATCHING) }
        soundManager.playDetonationSound()
        hapticManager?.performSpecialExplosionHaptic()

        val affected = specialActivator.getAffectedPositions(
            state.board, bombPos, com.example.game.model.SpecialCandyType.COLOR_BOMB, targetColor
        )

        val removedTiles = mutableListOf<CandyTile>()
        for (pos in affected) {
            val t = state.board[pos]
            if (t != null) removedTiles.add(t)
        }

        val stepScore = scoreCalculator.calculateMatchScore(
            matchCount = 1,
            totalCandiesCleared = affected.size,
            cascadeIndex = 0,
            specialsActivated = 1
        )

        var boardAfterRemoval = state.board
        for (pos in affected) {
            boardAfterRemoval = boardAfterRemoval.set(pos, null)
        }

        val updatedObjectives = objectiveManager.updateObjectives(
            objectives = state.objectives,
            removedTiles = removedTiles,
            matchesFormed = 1,
            currentScore = state.score + stepScore
        )

        _gameState.update {
            it.copy(
                board = boardAfterRemoval,
                score = it.score + stepScore,
                objectives = updatedObjectives
            )
        }
        delay(250)

        val gravity = gravityProcessor.applyGravity(boardAfterRemoval)
        val refill = boardRefiller.refillBoard(gravity.updatedBoard)
        _gameState.update { it.copy(board = refill.updatedBoard) }
        delay(200)

        resolveCascades(refill.updatedBoard, bombPos)
    }

    private suspend fun resolveCascades(initialBoard: Match3Board, triggerPos: BoardPosition?) {
        var currentBoard = initialBoard
        var cascadeIndex = 0

        while (true) {
            val matches = matchDetector.findMatches(currentBoard)
            if (matches.isEmpty()) break

            _gameState.update {
                it.copy(
                    status = GameStatus.MATCHING,
                    cascadeCount = cascadeIndex,
                    comboMultiplier = cascadeIndex + 1
                )
            }

            val matchedPositions = mutableSetOf<BoardPosition>()
            val createdSpecials = mutableListOf<com.example.game.logic.CreatedSpecialCandy>()

            for (match in matches) {
                matchedPositions.addAll(match.positions)
                val special = specialCreator.checkAndCreateSpecial(
                    match,
                    if (cascadeIndex == 0) triggerPos else null
                )
                if (special != null) {
                    createdSpecials.add(special)
                    soundManager.playSpecialCreatedSound()
                }
            }

            val detonationResult = specialResolver.resolveDetonations(currentBoard, matchedPositions)
            val allRemoved = matchedPositions + detonationResult.affectedPositions

            val removedTiles = mutableListOf<CandyTile>()
            for (pos in allRemoved) {
                val tile = currentBoard[pos]
                if (tile != null) removedTiles.add(tile)
            }

            val stepScore = scoreCalculator.calculateMatchScore(
                matchCount = matches.size,
                totalCandiesCleared = allRemoved.size,
                cascadeIndex = cascadeIndex,
                specialsActivated = detonationResult.secondaryActivations.size
            )

            var boardAfterRemoval = currentBoard
            for (pos in allRemoved) {
                boardAfterRemoval = boardAfterRemoval.set(pos, null)
            }
            for (spec in createdSpecials) {
                boardAfterRemoval = boardAfterRemoval.set(spec.position, spec.tile)
            }

            val curState = _gameState.value
            val updatedObjectives = objectiveManager.updateObjectives(
                objectives = curState.objectives,
                removedTiles = removedTiles,
                matchesFormed = matches.size,
                currentScore = curState.score + stepScore
            )

            _gameState.update {
                it.copy(
                    board = boardAfterRemoval,
                    score = it.score + stepScore,
                    objectives = updatedObjectives
                )
            }
            delay(220)

            _gameState.update { it.copy(status = GameStatus.FALLING) }
            val gravity = gravityProcessor.applyGravity(boardAfterRemoval)
            _gameState.update { it.copy(board = gravity.updatedBoard) }
            delay(150)

            val refill = boardRefiller.refillBoard(gravity.updatedBoard)
            _gameState.update { it.copy(board = refill.updatedBoard) }
            delay(150)

            currentBoard = refill.updatedBoard
            cascadeIndex++
        }

        checkGameEndConditions()
    }

    private fun checkGameEndConditions() {
        val state = _gameState.value
        if (state.isLevelCompleted) {
            soundManager.playVictorySound()
            hapticManager?.performVictoryHaptic()
            _gameState.update { it.copy(status = GameStatus.VICTORY) }
        } else if (state.movesRemaining <= 0) {
            soundManager.playGameOverSound()
            _gameState.update { it.copy(status = GameStatus.GAME_OVER) }
        } else {
            _gameState.update {
                it.copy(
                    status = GameStatus.IDLE,
                    cascadeCount = 0,
                    comboMultiplier = 1
                )
            }
        }
    }
}
