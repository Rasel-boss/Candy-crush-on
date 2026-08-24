package com.example.game.ui.effects

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Visual shape style for game particles.
 */
enum class ParticleShape {
    CIRCLE,
    STAR,
    SPARK_LINE,
    DIAMOND
}

/**
 * Lightweight particle instance managed on the GPU/Canvas.
 */
data class Particle(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val glowColor: Color,
    val size: Float,
    val maxAge: Float,
    var age: Float = 0f,
    val shape: ParticleShape = ParticleShape.CIRCLE,
    val rotation: Float = 0f
) {
    val isDead: Boolean
        get() = age >= maxAge

    val alpha: Float
        get() = (1f - (age / maxAge)).coerceIn(0f, 1f)

    val currentSize: Float
        get() = size * (1f - (age / maxAge) * 0.4f)
}

/**
 * Factory for emitting lightweight electric sparks, glows, and star bursts.
 */
object ParticleFactory {

    private var nextId = 1L

    /**
     * Generates a burst of electric sparkle particles originating at ([centerX], [centerY]).
     *
     * @param count Number of particles (4-6 for match-3, 8-10 for match-4, 12-14 for match-5+).
     * @param baseColor Primary color corresponding to the matched candy.
     * @param glowColor Accent electric spark color.
     */
    fun createMatchBurst(
        centerX: Float,
        centerY: Float,
        count: Int,
        baseColor: Color,
        glowColor: Color = Color.White,
        random: Random = Random.Default
    ): List<Particle> {
        val particles = ArrayList<Particle>(count)
        val speedBase = 120f

        for (i in 0 until count) {
            val angle = (random.nextFloat() * 2.0 * Math.PI).toFloat()
            val speed = speedBase * (0.6f + random.nextFloat() * 0.9f)
            val vx = cos(angle) * speed
            val vy = sin(angle) * speed
            val lifetime = 0.35f + random.nextFloat() * 0.25f
            val size = 4f + random.nextFloat() * 6f
            val shape = when (i % 3) {
                0 -> ParticleShape.STAR
                1 -> ParticleShape.SPARK_LINE
                else -> ParticleShape.CIRCLE
            }

            particles.add(
                Particle(
                    id = nextId++,
                    x = centerX + (random.nextFloat() - 0.5f) * 16f,
                    y = centerY + (random.nextFloat() - 0.5f) * 16f,
                    vx = vx,
                    vy = vy,
                    color = if (random.nextBoolean()) baseColor else glowColor,
                    glowColor = glowColor,
                    size = size,
                    maxAge = lifetime,
                    shape = shape,
                    rotation = random.nextFloat() * 360f
                )
            )
        }
        return particles
    }
}
