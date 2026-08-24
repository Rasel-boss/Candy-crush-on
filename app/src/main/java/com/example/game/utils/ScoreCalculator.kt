package com.example.game.utils

class ScoreCalculator {

    fun calculateMatchScore(
        matchCount: Int,
        totalCandiesCleared: Int,
        cascadeIndex: Int = 0,
        specialsActivated: Int = 0
    ): Int {
        val basePerCandy = 60
        val baseScore = totalCandiesCleared * basePerCandy
        val cascadeMultiplier = 1 + (cascadeIndex * 0.5f)
        val specialBonus = specialsActivated * 200
        val multiMatchBonus = if (matchCount > 1) (matchCount - 1) * 150 else 0

        return ((baseScore + multiMatchBonus + specialBonus) * cascadeMultiplier).toInt()
    }

    fun calculateComboBonus(comboCount: Int): Int {
        return comboCount * 100
    }
}
