package com.example.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.model.SpecialCandyType

@Composable
fun CandyArt(
    tile: CandyTile,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2, h / 2)

        if (tile.isColorBomb) {
            drawColorBomb(center, w * 0.42f)
            return@Canvas
        }

        val baseColor = tile.type.color

        when (tile.type) {
            CandyType.RED -> {
                // Heart / Round Gem Shape
                drawCircle(
                    color = baseColor,
                    radius = w * 0.38f,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = w * 0.12f,
                    center = Offset(center.x - w * 0.12f, center.y - h * 0.12f)
                )
            }
            CandyType.BLUE -> {
                // Diamond Shape
                val path = Path().apply {
                    moveTo(center.x, h * 0.12f)
                    lineTo(w * 0.88f, center.y)
                    lineTo(center.x, h * 0.88f)
                    lineTo(w * 0.12f, center.y)
                    close()
                }
                drawPath(path, color = baseColor)
                drawPath(path, color = Color.White.copy(alpha = 0.4f), style = Stroke(width = 3f))
            }
            CandyType.GREEN -> {
                // Square Pill
                drawRoundRect(
                    color = baseColor,
                    topLeft = Offset(w * 0.15f, h * 0.15f),
                    size = Size(w * 0.7f, h * 0.7f),
                    cornerRadius = CornerRadius(16f, 16f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.3f),
                    topLeft = Offset(w * 0.2f, h * 0.2f),
                    size = Size(w * 0.6f, h * 0.25f),
                    cornerRadius = CornerRadius(10f, 10f)
                )
            }
            CandyType.YELLOW -> {
                // Hexagon / Star Drop
                val path = Path().apply {
                    moveTo(center.x, h * 0.1f)
                    lineTo(w * 0.85f, h * 0.32f)
                    lineTo(w * 0.85f, h * 0.68f)
                    lineTo(center.x, h * 0.9f)
                    lineTo(w * 0.15f, h * 0.68f)
                    lineTo(w * 0.15f, h * 0.32f)
                    close()
                }
                drawPath(path, color = baseColor)
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = w * 0.1f,
                    center = Offset(center.x - w * 0.1f, center.y - h * 0.15f)
                )
            }
            CandyType.PURPLE -> {
                // Oval Drop
                drawOval(
                    color = baseColor,
                    topLeft = Offset(w * 0.2f, h * 0.12f),
                    size = Size(w * 0.6f, h * 0.76f)
                )
                drawOval(
                    color = Color.White.copy(alpha = 0.35f),
                    topLeft = Offset(w * 0.28f, h * 0.18f),
                    size = Size(w * 0.22f, h * 0.25f)
                )
            }
            CandyType.ORANGE -> {
                // Triangle Candy
                val path = Path().apply {
                    moveTo(center.x, h * 0.15f)
                    lineTo(w * 0.85f, h * 0.82f)
                    lineTo(w * 0.15f, h * 0.82f)
                    close()
                }
                drawPath(path, color = baseColor)
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = w * 0.08f,
                    center = Offset(center.x, center.y + h * 0.05f)
                )
            }
        }

        // Draw Special Overlays
        when (tile.specialType) {
            SpecialCandyType.STRIPED_HORIZONTAL -> {
                drawStripes(isHorizontal = true, w = w, h = h)
            }
            SpecialCandyType.STRIPED_VERTICAL -> {
                drawStripes(isHorizontal = false, w = w, h = h)
            }
            SpecialCandyType.WRAPPED -> {
                drawWrappedRibbon(center = center, radius = w * 0.44f)
            }
            else -> Unit
        }
    }
}

private fun DrawScope.drawStripes(isHorizontal: Boolean, w: Float, h: Float) {
    val stripeColor = Color.White.copy(alpha = 0.85f)
    val strokeW = 6f

    if (isHorizontal) {
        drawLine(stripeColor, Offset(w * 0.1f, h * 0.35f), Offset(w * 0.9f, h * 0.35f), strokeWidth = strokeW)
        drawLine(stripeColor, Offset(w * 0.05f, h * 0.50f), Offset(w * 0.95f, h * 0.50f), strokeWidth = strokeW)
        drawLine(stripeColor, Offset(w * 0.1f, h * 0.65f), Offset(w * 0.9f, h * 0.65f), strokeWidth = strokeW)
    } else {
        drawLine(stripeColor, Offset(w * 0.35f, h * 0.1f), Offset(w * 0.35f, h * 0.9f), strokeWidth = strokeW)
        drawLine(stripeColor, Offset(w * 0.50f, h * 0.05f), Offset(w * 0.50f, h * 0.95f), strokeWidth = strokeW)
        drawLine(stripeColor, Offset(w * 0.65f, h * 0.1f), Offset(w * 0.65f, h * 0.9f), strokeWidth = strokeW)
    }
}

private fun DrawScope.drawWrappedRibbon(center: Offset, radius: Float) {
    drawCircle(
        color = Color(0xFFFFD700).copy(alpha = 0.9f),
        radius = radius,
        center = center,
        style = Stroke(width = 7f)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.7f),
        radius = radius * 0.7f,
        center = center,
        style = Stroke(width = 3f)
    )
}

private fun DrawScope.drawColorBomb(center: Offset, radius: Float) {
    // Chocolate / Rainbow sphere
    drawCircle(
        color = Color(0xFF3B1E08),
        radius = radius,
        center = center
    )
    // Multi-color sprinkles
    val sprinkleColors = listOf(
        Color(0xFFEF4444),
        Color(0xFF3B82F6),
        Color(0xFF10B981),
        Color(0xFFF59E0B),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899)
    )
    val offsets = listOf(
        Offset(-radius * 0.5f, -radius * 0.4f),
        Offset(radius * 0.4f, -radius * 0.5f),
        Offset(0f, 0f),
        Offset(-radius * 0.4f, radius * 0.4f),
        Offset(radius * 0.5f, radius * 0.3f),
        Offset(-radius * 0.1f, -radius * 0.5f)
    )
    for (i in sprinkleColors.indices) {
        drawCircle(
            color = sprinkleColors[i],
            radius = radius * 0.22f,
            center = center + offsets[i]
        )
    }
}
