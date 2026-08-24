package com.example.game.model

data class LevelConfig(
    val levelNumber: Int,
    val maxMoves: Int,
    val targetScore: Int,
    val objectives: List<LevelObjective> = emptyList(),
    val title: String = "Level $levelNumber"
)
