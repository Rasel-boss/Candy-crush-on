package com.example.game.logic

import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.LevelConfig
import com.example.game.model.LevelObjective
import com.example.game.model.ObjectiveType
import com.example.game.model.SpecialCandyType

/**
 * Dedicated manager responsible for tracking, calculating, updating, and evaluating
 * level objectives and completion conditions in a pure, deterministic manner.
 */
object ObjectiveManager {

    /**
     * Initializes objectives from a [LevelConfig] with zeroed starting progress.
     */
    fun initializeObjectives(config: LevelConfig): List<LevelObjective> {
        return config.objectives.map { objective ->
            objective.copy(currentProgress = 0)
        }
    }

    /**
     * Updates [COLLECT_CANDY] objectives when a collection of [removedTiles] are cleared from the board.
     *
     * Rules:
     * - Only counts playable normal/colored candies matching [objective.candyType].
     * - Does NOT count neutral special candies like [SpecialCandyType.COLOR_BOMB] as normal candy collections.
     * - Increments current progress by the count of removed matching tiles.
     */
    fun onCandiesRemoved(
        currentObjectives: List<LevelObjective>,
        removedTiles: List<CandyTile>
    ): List<LevelObjective> {
        if (removedTiles.isEmpty() || currentObjectives.isEmpty()) return currentObjectives

        // Filter and tally removed colored candies
        val removedCountsByColor = mutableMapOf<CandyType, Int>()
        for (tile in removedTiles) {
            // Only count valid colored playable candies (exclude EMPTY and neutral COLOR_BOMB)
            if (tile.type.isPlayable && tile.specialCandyType != SpecialCandyType.COLOR_BOMB) {
                removedCountsByColor[tile.type] = (removedCountsByColor[tile.type] ?: 0) + 1
            }
        }

        return currentObjectives.map { objective ->
            if (objective.type == ObjectiveType.COLLECT_CANDY && objective.candyType != null) {
                val gainedCount = removedCountsByColor[objective.candyType] ?: 0
                if (gainedCount > 0) {
                    objective.copy(currentProgress = objective.currentProgress + gainedCount)
                } else {
                    objective
                }
            } else {
                objective
            }
        }
    }

    /**
     * Updates [TARGET_SCORE] objectives with the latest total [currentScore].
     * Target score progress tracks current session score directly.
     */
    fun onScoreChanged(
        currentObjectives: List<LevelObjective>,
        currentScore: Int
    ): List<LevelObjective> {
        if (currentObjectives.isEmpty()) return currentObjectives

        return currentObjectives.map { objective ->
            if (objective.type == ObjectiveType.TARGET_SCORE) {
                objective.copy(currentProgress = currentScore)
            } else {
                objective
            }
        }
    }

    /**
     * Updates [MAKE_MATCHES] objectives when [matchesCount] valid linear or cascade matches occur.
     */
    fun onMatchesMade(
        currentObjectives: List<LevelObjective>,
        matchesCount: Int
    ): List<LevelObjective> {
        if (matchesCount <= 0 || currentObjectives.isEmpty()) return currentObjectives

        return currentObjectives.map { objective ->
            if (objective.type == ObjectiveType.MAKE_MATCHES) {
                objective.copy(currentProgress = objective.currentProgress + matchesCount)
            } else {
                objective
            }
        }
    }

    /**
     * Determines whether ALL objectives in the given list have been completed.
     * Returns true ONLY if objectives is non-empty and every objective is satisfied.
     */
    fun areAllObjectivesCompleted(objectives: List<LevelObjective>): Boolean {
        if (objectives.isEmpty()) return false
        return objectives.all { it.isCompleted }
    }

    /**
     * Returns the subset of objectives that are currently completed.
     */
    fun getCompletedObjectives(objectives: List<LevelObjective>): List<LevelObjective> {
        return objectives.filter { it.isCompleted }
    }
}
