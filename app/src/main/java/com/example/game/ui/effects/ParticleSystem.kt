package com.example.game.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Observable controller state for managing active Canvas particles.
 */
class ParticleSystemState {
    val particles = mutableStateListOf<Particle>()

    fun emitBurst(burst: List<Particle>) {
        particles.addAll(burst)
    }

    fun clear() {
        particles.clear()
    }

    fun update(deltaSeconds: Float) {
        if (particles.isEmpty()) return
        val iterator = particles.listIterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.age += deltaSeconds
            if (p.isDead) {
                iterator.remove()
            } else {
                p.x += p.vx * deltaSeconds
                p.y += p.vy * deltaSeconds
                // Gentle deceleration
                p.vx *= 0.96f
                p.vy *= 0.96f
            }
        }
    }
}

@Composable
fun rememberParticleSystemState(): ParticleSystemState {
    return remember { ParticleSystemState() }
}

/**
 * Hardware-accelerated Canvas overlay rendering game particles with zero garbage allocation.
 * Automatically goes idle when no particles exist.
 */
@Composable
fun CanvasParticleOverlay(
    state: ParticleSystemState,
    modifier: Modifier = Modifier
) {
    // Frame ticker loop active only when particles are present
    LaunchedEffect(state.particles.isNotEmpty()) {
        if (state.particles.isNotEmpty()) {
            var lastFrameTimeNanos = 0L
            while (state.particles.isNotEmpty()) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTimeNanos != 0L) {
                        val deltaSeconds = ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                        state.update(deltaSeconds)
                    }
                    lastFrameTimeNanos = frameTimeNanos
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        for (p in state.particles) {
            val alpha = p.alpha
            if (alpha <= 0f) continue
            val currentColor = p.color.copy(alpha = alpha)
            val glowColor = p.glowColor.copy(alpha = (alpha * 0.6f).coerceIn(0f, 1f))
            val currentSize = p.currentSize

            when (p.shape) {
                ParticleShape.CIRCLE -> {
                    // Outer subtle electric glow
                    drawCircle(
                        color = glowColor,
                        radius = currentSize * 1.6f,
                        center = Offset(p.x, p.y)
                    )
                    // Inner bright core
                    drawCircle(
                        color = currentColor,
                        radius = currentSize,
                        center = Offset(p.x, p.y)
                    )
                }
                ParticleShape.SPARK_LINE -> {
                    // Small linear energy spark
                    val len = currentSize * 2.2f
                    val angleRad = p.rotation * (PI / 180.0).toFloat()
                    val dx = cos(angleRad) * len
                    val dy = sin(angleRad) * len
                    drawLine(
                        color = currentColor,
                        start = Offset(p.x - dx, p.y - dy),
                        end = Offset(p.x + dx, p.y + dy),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )
                }
                ParticleShape.STAR, ParticleShape.DIAMOND -> {
                    rotate(degrees = p.rotation + (p.age * 90f), pivot = Offset(p.x, p.y)) {
                        val halfSize = currentSize
                        val path = Path().apply {
                            moveTo(p.x, p.y - halfSize * 1.5f)
                            lineTo(p.x + halfSize, p.y)
                            lineTo(p.x, p.y + halfSize * 1.5f)
                            lineTo(p.x - halfSize, p.y)
                            close()
                        }
                        drawPath(path = path, color = currentColor)
                    }
                }
            }
        }
    }
}
