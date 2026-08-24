package com.example.game.ui.effects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class Particle(
    val id: Long,
    val position: Offset,
    val velocity: Offset,
    val color: Color,
    val size: Float,
    val alpha: Float = 1f,
    val lifetime: Float = 1f,
    val age: Float = 0f
) {
    val isDead: Boolean get() = age >= lifetime
}
