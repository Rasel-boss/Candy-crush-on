package com.example.game.logic

import com.example.game.model.LevelConfig
import com.example.game.model.LevelObjective
import com.example.game.model.ObjectiveType

/**
 * Result of level configuration validation.
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult() {
        val errorMessage: String
            get() = errors.joinToString("; ")
    }
}

/**
 * Validates [LevelConfig] objects to ensure they are internally consistent,
 * adhere to gameplay constraints, and contain no malformed or missing objective data.
 */
object LevelConfigValidator {

    /**
     * Validates the provided [config]. Returns [ValidationResult.Valid] if all rules pass,
     * or [ValidationResult.Invalid] with specific error descriptions.
     */
    fun validate(config: LevelConfig?): ValidationResult {
        if (config == null) {
            return ValidationResult.Invalid(listOf("LevelConfig is null"))
        }

        val errors = mutableListOf<String>()

        if (config.levelNumber <= 0) {
            errors.add("levelNumber must be > 0 (found ${config.levelNumber})")
        }

        if (config.rows <= 0) {
            errors.add("rows must be > 0 (found ${config.rows})")
        }

        if (config.columns <= 0) {
            errors.add("columns must be > 0 (found ${config.columns})")
        }

        if (config.startingMoves <= 0) {
            errors.add("startingMoves must be > 0 (found ${config.startingMoves})")
        }

        if (config.objectives.isEmpty()) {
            errors.add("objectives list cannot be empty")
        } else {
            config.objectives.forEachIndexed { index, obj ->
                validateObjective(obj, index, errors)
            }
        }

        config.targetScore?.let { score ->
            if (score <= 0) {
                errors.add("targetScore must be > 0 if specified (found $score)")
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    private fun validateObjective(objective: LevelObjective, index: Int, errors: MutableList<String>) {
        if (objective.resolvedId.isBlank()) {
            errors.add("Objective at index $index has blank id")
        }
        if (objective.target <= 0) {
            errors.add("Objective '${objective.resolvedId}' target must be > 0 (found ${objective.target})")
        }
        when (objective.type) {
            ObjectiveType.COLLECT_CANDY -> {
                if (objective.candyType == null) {
                    errors.add("Objective '${objective.resolvedId}' of type COLLECT_CANDY requires a non-null candyType")
                } else if (!objective.candyType.isPlayable) {
                    errors.add("Objective '${objective.resolvedId}' specifies unplayable candyType '${objective.candyType.name}'")
                }
            }
            ObjectiveType.TARGET_SCORE,
            ObjectiveType.SCORE_TARGET -> {
                if (objective.target <= 0) {
                    errors.add("Objective '${objective.resolvedId}' of type TARGET_SCORE requires target > 0")
                }
            }
            ObjectiveType.MAKE_MATCHES -> {
                if (objective.target <= 0) {
                    errors.add("Objective '${objective.resolvedId}' of type MAKE_MATCHES requires target > 0")
                }
            }
            ObjectiveType.CLEAR_BLOCKERS -> {
                if (objective.target <= 0) {
                    errors.add("Objective '${objective.resolvedId}' of type CLEAR_BLOCKERS requires target > 0")
                }
            }
        }
    }

    /**
     * Fast boolean validation helper.
     */
    fun isValid(config: LevelConfig?): Boolean = validate(config) is ValidationResult.Valid
}
