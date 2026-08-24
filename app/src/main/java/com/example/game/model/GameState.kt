package com.example.game.model

data class GameState(
    val board: Match3Board = Match3Board(),
    val score: Int = 0,
    val targetScore: Int = 1000,
    val movesRemaining: Int = 20,
    val currentLevel: Int = 1,
    val status: GameStatus = GameStatus.IDLE,
    val selectedPosition: BoardPosition? = null,
    val comboMultiplier: Int = 1,
    val cascadeCount: Int = 0,
    val levelConfig: LevelConfig? = null,
    val objectives: List<LevelObjective> = emptyList()
) {
    val isLevelCompleted: Boolean
        get() = (objectives.isNotEmpty() && objectives.all { it.isCompleted }) ||
                (objectives.isEmpty() && score >= targetScore)

    val allObjectivesCompleted: Boolean
        get() = isLevelCompleted
}
