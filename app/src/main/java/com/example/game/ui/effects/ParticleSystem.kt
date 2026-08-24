package com.example.game.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

class ParticleSystem {
    private var nextParticleId = 0L

    fun spawnBurst(center: Offset, color: Color, count: Int = 12): List<Particle> {
        val particles = mutableListOf<Particle>()
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val speed = Random.nextFloat() * 150f + 50f
            val vx = Math.cos(angle.toDouble()).toFloat() * speed
            val vy = Math.sin(angle.toDouble()).toFloat() * speed

            particles.add(
                Particle(
                    id = ++nextParticleId,
                    position = center,
                    velocity = Offset(vx, vy),
                    color = color,
                    size = Random.nextFloat() * 6f + 4f,
                    lifetime = Random.nextFloat() * 0.4f + 0.4f
                )
            )
        }
        return particles
    }
}

@Composable
fun ParticleOverlay(
    particles: List<Particle>,
    modifier: Modifier = Modifier
) {
    if (particles.isEmpty()) return

    Canvas(modifier = modifier.fillMaxSize()) {
        for (p in particles) {
            val remainingAlpha = (1f - (p.age / p.lifetime)).coerceIn(0f, 1f)
            drawCircle(
                color = p.color.copy(alpha = remainingAlpha),
                radius = p.size,
                center = p.position
            )
        }
    }
}
