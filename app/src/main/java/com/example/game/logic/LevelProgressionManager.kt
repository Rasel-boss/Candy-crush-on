package com.example.game.logic

import android.content.Context
import android.content.SharedPreferences
import com.example.game.model.LevelConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Visual/gameplay status for a level in the campaign.
 */
enum class LevelStatus {
    LOCKED,
    UNLOCKED,
    CURRENT,
    COMPLETED
}

/**
 * Snapshot representation of a campaign level's configuration and progress status.
 */
data class LevelSummary(
    val levelNumber: Int,
    val config: LevelConfig,
    val status: LevelStatus,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val bestScore: Int
)

/**
 * Progression data model tracking player unlocked levels, completed levels, and best scores.
 */
data class ProgressionData(
    val unlockedLevels: Set<Int> = setOf(1),
    val completedLevels: Set<Int> = emptySet(),
    val bestScores: Map<Int, Int> = emptyMap()
)

/**
 * Dedicated manager responsible for tracking level unlock progression,
 * completion status, best scores per level, and persistent storage.
 *
 * Rules:
 * 1. Level 1 is unlocked initially by default.
 * 2. Completing Level N unlocks Level N + 1.
 * 3. Locked levels cannot be started.
 * 4. Completed levels remain completed when replaying.
 * 5. Replaying a level and getting a higher score replaces the best score.
 * 6. Replaying a level and getting a lower score does NOT replace the best score.
 */
object LevelProgressionManager {

    private const val PREFS_NAME = "puzzle_master_progression_prefs"
    private const val KEY_UNLOCKED_LEVELS = "unlocked_levels"
    private const val KEY_COMPLETED_LEVELS = "completed_levels"
    private const val KEY_PREFIX_BEST_SCORE = "best_score_lvl_"

    private val _progression = MutableStateFlow(ProgressionData())
    val progression: StateFlow<ProgressionData> = _progression.asStateFlow()

    private var sharedPreferences: SharedPreferences? = null

    /**
     * Optional initialization with Android Context to enable persistence across app launches.
     */
    fun init(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences = prefs
        loadFromPreferences(prefs)
    }

    private fun loadFromPreferences(prefs: SharedPreferences) {
        val unlockedSet = prefs.getStringSet(KEY_UNLOCKED_LEVELS, null)
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet() ?: setOf(1)

        val completedSet = prefs.getStringSet(KEY_COMPLETED_LEVELS, null)
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet() ?: emptySet()

        val scores = mutableMapOf<Int, Int>()
        for (i in 1..20) {
            val score = prefs.getInt("$KEY_PREFIX_BEST_SCORE$i", 0)
            if (score > 0) {
                scores[i] = score
            }
        }

        _progression.value = ProgressionData(
            unlockedLevels = (unlockedSet + 1),
            completedLevels = completedSet,
            bestScores = scores
        )
    }

    /**
     * Checks if the given [levelNumber] is unlocked and playable.
     * Level 1 is always unlocked.
     */
    fun isLevelUnlocked(levelNumber: Int): Boolean {
        if (levelNumber <= 1) return true
        val currentUnlocked = _progression.value.unlockedLevels
        return currentUnlocked.contains(levelNumber) || isLevelCompleted(levelNumber - 1)
    }

    /**
     * Checks if the given [levelNumber] has been completed at least once.
     */
    fun isLevelCompleted(levelNumber: Int): Boolean {
        return _progression.value.completedLevels.contains(levelNumber)
    }

    /**
     * Gets the recorded best score for the given [levelNumber], or 0 if none recorded.
     */
    fun getBestScore(levelNumber: Int): Int {
        return _progression.value.bestScores[levelNumber] ?: 0
    }

    /**
     * Records completion of a level, unlocking the subsequent level and updating the best score.
     *
     * @param levelNumber The level that was cleared.
     * @param finalScore The score achieved in this session.
     * @return True if a new best score was established.
     */
    fun recordLevelCompletion(levelNumber: Int, finalScore: Int): Boolean {
        if (levelNumber <= 0) return false

        var isNewBest = false

        _progression.update { current ->
            val newCompleted = current.completedLevels + levelNumber
            val newUnlocked = current.unlockedLevels + levelNumber + (levelNumber + 1)

            val currentBest = current.bestScores[levelNumber] ?: 0
            val newBestScores = current.bestScores.toMutableMap()
            if (finalScore > currentBest) {
                newBestScores[levelNumber] = finalScore
                isNewBest = true
            }

            current.copy(
                unlockedLevels = newUnlocked,
                completedLevels = newCompleted,
                bestScores = newBestScores
            )
        }

        persistState()
        return isNewBest
    }

    /**
     * Explicitly unlocks a level (e.g. for progression bypass or debug).
     */
    fun unlockLevel(levelNumber: Int) {
        if (levelNumber <= 0) return
        _progression.update { current ->
            current.copy(unlockedLevels = current.unlockedLevels + levelNumber)
        }
        persistState()
    }

    /**
     * Returns the set of all currently unlocked level numbers.
     */
    fun getUnlockedLevels(): Set<Int> {
        return _progression.value.unlockedLevels
    }

    /**
     * Returns the set of all currently completed level numbers.
     */
    fun getCompletedLevels(): Set<Int> {
        return _progression.value.completedLevels
    }

    /**
     * Returns the map of level number to best score.
     */
    fun getAllBestScores(): Map<Int, Int> {
        return _progression.value.bestScores
    }

    /**
     * Determines the [LevelStatus] for a specific level relative to the current active session.
     */
    fun getLevelStatus(levelNumber: Int, activePlayingLevel: Int? = null): LevelStatus {
        val completed = isLevelCompleted(levelNumber)
        val unlocked = isLevelUnlocked(levelNumber)

        return when {
            activePlayingLevel == levelNumber -> LevelStatus.CURRENT
            completed -> LevelStatus.COMPLETED
            unlocked -> LevelStatus.UNLOCKED
            else -> LevelStatus.LOCKED
        }
    }

    /**
     * Generates a comprehensive summary for a given level.
     */
    fun getLevelSummary(levelNumber: Int, activePlayingLevel: Int? = null): LevelSummary {
        val config = LevelProvider.getLevelConfig(levelNumber)
        val status = getLevelStatus(levelNumber, activePlayingLevel)
        return LevelSummary(
            levelNumber = levelNumber,
            config = config,
            status = status,
            isUnlocked = status != LevelStatus.LOCKED,
            isCompleted = status == LevelStatus.COMPLETED,
            bestScore = getBestScore(levelNumber)
        )
    }

    /**
     * Resets all progress back to initial default state (Level 1 unlocked, 0 completions, no scores).
     */
    fun resetProgression() {
        _progression.value = ProgressionData(
            unlockedLevels = setOf(1),
            completedLevels = emptySet(),
            bestScores = emptyMap()
        )
        sharedPreferences?.edit()?.clear()?.apply()
    }

    private fun persistState() {
        val prefs = sharedPreferences ?: return
        val current = _progression.value
        val editor = prefs.edit()

        editor.putStringSet(KEY_UNLOCKED_LEVELS, current.unlockedLevels.map { it.toString() }.toSet())
        editor.putStringSet(KEY_COMPLETED_LEVELS, current.completedLevels.map { it.toString() }.toSet())

        current.bestScores.forEach { (lvl, score) ->
            editor.putInt("$KEY_PREFIX_BEST_SCORE$lvl", score)
        }

        editor.apply()
    }
}
