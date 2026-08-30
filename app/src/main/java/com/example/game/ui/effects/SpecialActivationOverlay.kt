package com.example.game.ui.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.model.BoardPosition
import com.example.game.model.SpecialCombinationType

/**
 * Lightweight, high-performance Canvas overlay providing special candy activation
 * visual feedback (Horizontal Laser Sweeps, Vertical Beam Blasts, Expanding Shockwaves,
 * and Radial Color Bomb Lightning).
 */
@Composable
fun SpecialActivationOverlay(
    matchingPositions: Set<BoardPosition>,
    boardRows: Int,
    boardCols: Int,
    modifier: Modifier = Modifier
) {
    if (matchingPositions.isEmpty()) return

    // Detect if this match corresponds to full rows, full columns, or area shockwaves
    val rowsRepresented = matchingPositions.groupBy { it.row }
    val colsRepresented = matchingPositions.groupBy { it.column }

    val fullRows = rowsRepresented.filter { it.value.size >= boardCols }.keys
    val fullCols = colsRepresented.filter { it.value.size >= boardRows }.keys
    val isAreaBlast = matchingPositions.size >= 4 && (fullRows.size < boardRows && fullCols.size < boardCols)

    if (fullRows.isEmpty() && fullCols.isEmpty() && !isAreaBlast) return

    val animProgress = remember(matchingPositions) { Animatable(0f) }

    LaunchedEffect(matchingPositions) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val tileW = size.width / boardCols
        val tileH = size.height / boardRows
        val progress = animProgress.value
        val alpha = (1f - progress).coerceIn(0f, 1f)

        // 1. Horizontal Striped Laser Sweeps
        for (r in fullRows) {
            val centerY = (r + 0.5f) * tileH
            val beamHalfHeight = tileH * 0.45f * (1f - progress * 0.5f)

            // Outer cyan glow sweep
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF38BDF8).copy(alpha = 0.8f * alpha),
                        Color.White.copy(alpha = 0.95f * alpha),
                        Color(0xFF38BDF8).copy(alpha = 0.8f * alpha),
                        Color.Transparent
                    ),
                    startY = centerY - beamHalfHeight,
                    endY = centerY + beamHalfHeight
                ),
                topLeft = Offset(0f, centerY - beamHalfHeight),
                size = Size(size.width, beamHalfHeight * 2f)
            )

            // Sharp core laser line
            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = 4f * (1f - progress * 0.3f),
                cap = StrokeCap.Round
            )
        }

        // 2. Vertical Striped Laser Sweeps
        for (c in fullCols) {
            val centerX = (c + 0.5f) * tileW
            val beamHalfWidth = tileW * 0.45f * (1f - progress * 0.5f)

            // Outer amber/cyan glow sweep
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF38BDF8).copy(alpha = 0.8f * alpha),
                        Color.White.copy(alpha = 0.95f * alpha),
                        Color(0xFF38BDF8).copy(alpha = 0.8f * alpha),
                        Color.Transparent
                    ),
                    startX = centerX - beamHalfWidth,
                    endX = centerX + beamHalfWidth
                ),
                topLeft = Offset(centerX - beamHalfWidth, 0f),
                size = Size(beamHalfWidth * 2f, size.height)
            )

            // Sharp core laser line
            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = Offset(centerX, 0f),
                end = Offset(centerX, size.height),
                strokeWidth = 4f * (1f - progress * 0.3f),
                cap = StrokeCap.Round
            )
        }

        // 3. Wrapped Candy Expanding Shockwave Burst
        if (isAreaBlast) {
            val centerR = matchingPositions.map { it.row }.average().toFloat()
            val centerC = matchingPositions.map { it.column }.average().toFloat()
            val center = Offset((centerC + 0.5f) * tileW, (centerR + 0.5f) * tileH)
            val maxRadius = if (matchingPositions.size >= 16) tileW * 3.5f else tileW * 2.2f
            val currentRadius = maxRadius * progress

            // Radial energy aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFB923C).copy(alpha = 0.6f * alpha),
                        Color(0xFFF43F5E).copy(alpha = 0.3f * alpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius
                ),
                center = center,
                radius = currentRadius
            )

            // Expanding shockwave ring
            drawCircle(
                color = Color(0xFFFFE082).copy(alpha = alpha),
                center = center,
                radius = currentRadius,
                style = Stroke(width = 6f * (1f - progress), cap = StrokeCap.Round)
            )
        }
    }
}
