package com.example.game.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.game.model.CandyType
import com.example.game.model.SpecialCandyType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * High-performance, custom Canvas-rendered original candy artwork system with
 * dynamic electric sparkles, living shimmers, and special candy energy flows.
 *
 * Each candy type features an original, tactile silhouette, rich 3D shading,
 * specular gloss highlights, ambient occlusion drop-shadows, refined rims,
 * and deterministic electric sparkle effects.
 */
@Composable
fun CandyCanvasArtwork(
    candyType: CandyType,
    specialType: SpecialCandyType,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    tileId: Long = 0L,
    isMatching: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "candy_effects")

    // Continuous 0f..1f animation phase for deterministic shimmers
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_phase"
    )

    // Selection pulsation
    val selectionPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "selection_pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = min(w, h) * 0.40f
        if (r <= 0.5f) return@Canvas

        // Draw Selection Halo Aura, Luminous Contour, & Electric Rim Shimmer if selected
        if (isSelected) {
            val candyPath = getCandyPath(candyType, cx, cy, r)
            drawSelectionHalo(cx, cy, r * selectionPulse)
            if (candyType != CandyType.EMPTY) {
                // Golden-white double glow outline matching exact candy silhouette
                drawPath(
                    path = candyPath,
                    color = Color(0xFFFDE047).copy(alpha = 0.95f),
                    style = Stroke(width = r * 0.14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = candyPath,
                    color = Color.White,
                    style = Stroke(width = r * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            drawSelectedElectricShimmer(cx, cy, r, shimmerPhase)
        }

        // Draw Match Glow Energy if candy is resolving
        if (isMatching) {
            drawMatchEnergyGlow(cx, cy, r, shimmerPhase)
        }

        // Draw the specific candy piece
        when (specialType) {
            SpecialCandyType.COLOR_BOMB -> {
                drawColorBomb(cx, cy, r, isSelected, shimmerPhase)
            }
            SpecialCandyType.WRAPPED -> {
                drawWrappedCandy(candyType, cx, cy, r, isSelected, shimmerPhase)
            }
            SpecialCandyType.HORIZONTAL_STRIPED -> {
                drawStripedCandy(candyType, isHorizontal = true, cx, cy, r, isSelected, shimmerPhase)
            }
            SpecialCandyType.VERTICAL_STRIPED -> {
                drawStripedCandy(candyType, isHorizontal = false, cx, cy, r, isSelected, shimmerPhase)
            }
            SpecialCandyType.NONE -> {
                drawNormalCandy(candyType, cx, cy, r, isSelected)
                // Draw periodic subtle electric sparkle on normal candy
                drawNormalCandySparkle(candyType, cx, cy, r, tileId, shimmerPhase)
            }
        }
    }
}

/**
 * Draws the base normal candy with its distinct silhouette, depth, and glossy finish.
 */
private fun DrawScope.drawNormalCandy(
    candyType: CandyType,
    cx: Float,
    cy: Float,
    r: Float,
    isSelected: Boolean
) {
    when (candyType) {
        CandyType.RED -> drawRedStrawberryDrop(cx, cy, r)
        CandyType.BLUE -> drawBlueOvalJelly(cx, cy, r)
        CandyType.GREEN -> drawGreenLeafJelly(cx, cy, r)
        CandyType.YELLOW -> drawYellowLemonDrop(cx, cy, r)
        CandyType.PURPLE -> drawPurpleGrapeJelly(cx, cy, r)
        CandyType.ORANGE -> drawOrangeCitrusDrop(cx, cy, r)
        CandyType.EMPTY -> { /* Empty sentinel */ }
    }
}

/**
 * Draws a Striped Candy using the exact base silhouette of its candy type,
 * with embedded glowing energy stripes and moving electric energy pulses.
 */
private fun DrawScope.drawStripedCandy(
    candyType: CandyType,
    isHorizontal: Boolean,
    cx: Float,
    cy: Float,
    r: Float,
    isSelected: Boolean,
    shimmerPhase: Float = 0f
) {
    val candyPath = getCandyPath(candyType, cx, cy, r)

    // Clip stripes strictly within the candy's 3D silhouette
    clipPath(candyPath) {
        // Draw the base 3D candy first
        drawNormalCandy(candyType, cx, cy, r, isSelected)

        // Draw crisp luminous embedded stripes
        val stripeColor = Color.White.copy(alpha = 0.85f)
        val stripeGlow = Color.White.copy(alpha = 0.35f)
        val barSize = r * 0.28f

        if (isHorizontal) {
            val offsetsY = listOf(cy - r * 0.52f, cy, cy + r * 0.52f)
            for (y in offsetsY) {
                // Outer glow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, stripeGlow, Color.Transparent),
                        startY = y - barSize * 0.8f,
                        endY = y + barSize * 0.8f
                    ),
                    topLeft = Offset(cx - r * 1.5f, y - barSize * 0.8f),
                    size = Size(r * 3f, barSize * 1.6f)
                )
                // Core beam
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(stripeColor.copy(alpha = 0.3f), stripeColor, stripeColor.copy(alpha = 0.3f)),
                        startY = y - barSize * 0.4f,
                        endY = y + barSize * 0.4f
                    ),
                    topLeft = Offset(cx - r * 1.5f, y - barSize * 0.4f),
                    size = Size(r * 3f, barSize * 0.8f)
                )
            }

            // Moving electric pulse traversing horizontally along stripes
            val pulseX = cx - r * 1.2f + (shimmerPhase * r * 2.4f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.9f), Color(0xFF38BDF8).copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(pulseX, cy),
                    radius = r * 0.45f
                ),
                radius = r * 0.45f,
                center = Offset(pulseX, cy)
            )
        } else {
            val offsetsX = listOf(cx - r * 0.52f, cx, cx + r * 0.52f)
            for (x in offsetsX) {
                // Outer glow
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, stripeGlow, Color.Transparent),
                        startX = x - barSize * 0.8f,
                        endX = x + barSize * 0.8f
                    ),
                    topLeft = Offset(x - barSize * 0.8f, cy - r * 1.5f),
                    size = Size(barSize * 1.6f, r * 3f)
                )
                // Core beam
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(stripeColor.copy(alpha = 0.3f), stripeColor, stripeColor.copy(alpha = 0.3f)),
                        startX = x - barSize * 0.4f,
                        endX = x + barSize * 0.4f
                    ),
                    topLeft = Offset(x - barSize * 0.4f, cy - r * 1.5f),
                    size = Size(barSize * 0.8f, r * 3f)
                )
            }

            // Moving electric pulse traversing vertically along stripes
            val pulseY = cy - r * 1.2f + (shimmerPhase * r * 2.4f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.9f), Color(0xFF38BDF8).copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(cx, pulseY),
                    radius = r * 0.45f
                ),
                radius = r * 0.45f,
                center = Offset(cx, pulseY)
            )
        }
    }

    // Re-draw outer rim over stripes to maintain depth integration
    drawPath(
        path = candyPath,
        color = Color.White.copy(alpha = 0.7f),
        style = Stroke(width = r * 0.08f)
    )

    // Subtle edge electric sparks along stripe endpoints
    val sparkIntensity = (sin(shimmerPhase * PI.toFloat() * 4f) * 0.5f + 0.5f)
    if (isHorizontal) {
        drawElectricSpark(cx - r * 0.7f, cy, r * 0.18f, Color.White, Color(0xFF38BDF8), sparkIntensity)
        drawElectricSpark(cx + r * 0.7f, cy, r * 0.18f, Color.White, Color(0xFF38BDF8), sparkIntensity)
    } else {
        drawElectricSpark(cx, cy - r * 0.7f, r * 0.18f, Color.White, Color(0xFF38BDF8), sparkIntensity)
        drawElectricSpark(cx, cy + r * 0.7f, r * 0.18f, Color.White, Color(0xFF38BDF8), sparkIntensity)
    }
}

/**
 * Draws a Wrapped Candy with glowing translucent cellophane wrapper casing,
 * twisted candy-wrapper ribbon tails, radiant core energy, and corner sparkle flares.
 */
private fun DrawScope.drawWrappedCandy(
    candyType: CandyType,
    cx: Float,
    cy: Float,
    r: Float,
    isSelected: Boolean,
    shimmerPhase: Float = 0f
) {
    // 1. Twisted Wrapper Ribbon Ends (Left & Right flairs)
    val ribbonColor = Color.White.copy(alpha = 0.75f)
    val ribbonShade = Color.White.copy(alpha = 0.35f)

    // Left wrapper ribbon flair
    val leftRibbon = Path().apply {
        moveTo(cx - r * 0.8f, cy - r * 0.3f)
        lineTo(cx - r * 1.35f, cy - r * 0.7f)
        cubicTo(cx - r * 1.25f, cy, cx - r * 1.45f, cy + r * 0.2f, cx - r * 1.35f, cy + r * 0.7f)
        lineTo(cx - r * 0.8f, cy + r * 0.3f)
        close()
    }
    drawPath(
        path = leftRibbon,
        brush = Brush.horizontalGradient(
            colors = listOf(ribbonShade, ribbonColor),
            startX = cx - r * 1.4f,
            endX = cx - r * 0.8f
        )
    )
    drawPath(path = leftRibbon, color = Color.White.copy(alpha = 0.9f), style = Stroke(width = r * 0.06f))

    // Right wrapper ribbon flair
    val rightRibbon = Path().apply {
        moveTo(cx + r * 0.8f, cy - r * 0.3f)
        lineTo(cx + r * 1.35f, cy - r * 0.7f)
        cubicTo(cx + r * 1.25f, cy, cx + r * 1.45f, cy + r * 0.2f, cx + r * 1.35f, cy + r * 0.7f)
        lineTo(cx + r * 0.8f, cy + r * 0.3f)
        close()
    }
    drawPath(
        path = rightRibbon,
        brush = Brush.horizontalGradient(
            colors = listOf(ribbonColor, ribbonShade),
            startX = cx + r * 0.8f,
            endX = cx + r * 1.4f
        )
    )
    drawPath(path = rightRibbon, color = Color.White.copy(alpha = 0.9f), style = Stroke(width = r * 0.06f))

    // 2. Base Candy inside wrapper
    drawNormalCandy(candyType, cx, cy, r * 0.9f, isSelected)

    // 3. Shimmering Square Cellophane Wrapper Overlayer
    val wrapperRect = Rect(cx - r * 0.88f, cy - r * 0.88f, cx + r * 0.88f, cy + r * 0.88f)
    val wrapperPath = Path().apply {
        addRoundRect(RoundRect(wrapperRect, CornerRadius(r * 0.35f, r * 0.35f)))
    }

    // Translucent wrapper sheen with pulsating glow
    val wrapperPulse = sin(shimmerPhase * PI.toFloat() * 2f) * 0.15f + 0.35f
    drawPath(
        path = wrapperPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = wrapperPulse + 0.10f),
                Color.White.copy(alpha = 0.08f),
                Color.White.copy(alpha = wrapperPulse)
            ),
            start = Offset(cx - r, cy - r),
            end = Offset(cx + r, cy + r)
        )
    )

    // Crisp double outer wrapper border
    drawPath(
        path = wrapperPath,
        color = Color.White.copy(alpha = 0.95f),
        style = Stroke(width = r * 0.10f)
    )

    // Central explosive starburst emblem with gentle rotational energy
    rotate(degrees = shimmerPhase * 360f * 0.25f, pivot = Offset(cx, cy)) {
        drawCentralStarGlow(cx, cy, r * (0.38f + sin(shimmerPhase * PI.toFloat() * 2f) * 0.05f))
    }

    // 4 Corner Electric Shimmer Sparks
    val cornerOffsets = listOf(
        Offset(cx - r * 0.72f, cy - r * 0.72f),
        Offset(cx + r * 0.72f, cy - r * 0.72f),
        Offset(cx + r * 0.72f, cy + r * 0.72f),
        Offset(cx - r * 0.72f, cy + r * 0.72f)
    )
    cornerOffsets.forEachIndexed { index, corner ->
        val cornerPhase = (shimmerPhase + index * 0.25f) % 1f
        val sparkAlpha = sin(cornerPhase * PI.toFloat()).coerceAtLeast(0f)
        drawElectricSpark(corner.x, corner.y, r * 0.16f, Color.White, Color(0xFFFB923C), sparkAlpha)
    }
}

/**
 * Draws the Color Bomb: a spherical obsidian candy orb with multi-colored candy
 * jewel sprinkles, specular depth shine, rotating cosmic sparkles, and a golden starburst.
 */
private fun DrawScope.drawColorBomb(
    cx: Float,
    cy: Float,
    r: Float,
    isSelected: Boolean,
    shimmerPhase: Float = 0f
) {
    // 1. Ambient Drop Shadow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent),
            center = Offset(cx, cy + r * 0.25f),
            radius = r * 1.15f
        ),
        radius = r * 1.15f,
        center = Offset(cx, cy + r * 0.25f)
    )

    // Cosmic Aura behind the bomb
    val auraRadius = r * (1.18f + sin(shimmerPhase * PI.toFloat() * 2f) * 0.08f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFBBF24).copy(alpha = 0.25f),
                Color(0xFFA855F7).copy(alpha = 0.15f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = auraRadius
        ),
        radius = auraRadius,
        center = Offset(cx, cy)
    )

    // 2. Spherical Obsidian Body
    val bombGradient = Brush.radialGradient(
        colors = listOf(
            Color(0xFF475569), // Slate highlight
            Color(0xFF1E293B), // Slate mid
            Color(0xFF090D16)  // Deep obsidian shadow
        ),
        center = Offset(cx - r * 0.28f, cy - r * 0.32f),
        radius = r * 1.1f
    )
    drawCircle(brush = bombGradient, radius = r, center = Offset(cx, cy))

    // 3. Golden Rim Highlight
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFDE047),
                Color(0xFFF59E0B),
                Color(0xFF78350F)
            ),
            start = Offset(cx - r, cy - r),
            end = Offset(cx + r, cy + r)
        ),
        radius = r,
        center = Offset(cx, cy),
        style = Stroke(width = r * 0.08f)
    )

    // 4. Multi-colored Candy Sprinkle Jewels with orbital rotation
    val sprinkleColors = listOf(
        Color(0xFFFF3366), // Red
        Color(0xFF38BDF8), // Blue
        Color(0xFF4ADE80), // Green
        Color(0xFFFACC15), // Yellow
        Color(0xFFA855F7), // Purple
        Color(0xFFFB923C), // Orange
        Color(0xFFEC4899), // Pink
        Color(0xFF2DD4BF)  // Teal
    )

    val sprinkleRadius = r * 0.68f
    val baseAngleOffset = shimmerPhase * 360.0 * 0.5 // gentle 180 deg per cycle rotation
    for (i in sprinkleColors.indices) {
        val angle = (baseAngleOffset + i * (360.0 / sprinkleColors.size)) * Math.PI / 180.0
        val sx = (cx + sprinkleRadius * cos(angle)).toFloat()
        val sy = (cy + sprinkleRadius * sin(angle)).toFloat()
        val sSize = r * 0.16f

        // Jewel base
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.9f), sprinkleColors[i]),
                center = Offset(sx - sSize * 0.25f, sy - sSize * 0.25f),
                radius = sSize
            ),
            radius = sSize,
            center = Offset(sx, sy)
        )
        // Jewel rim
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = sSize,
            center = Offset(sx, sy),
            style = Stroke(width = 1.2f)
        )

        // Occasional micro-gleam on 2 rotating sprinkles
        if (i % 4 == 0) {
            val gleamAlpha = (sin((shimmerPhase * 4f + i) * PI.toFloat()) * 0.5f + 0.5f)
            drawElectricSpark(sx, sy, r * 0.12f, Color.White, sprinkleColors[i], gleamAlpha)
        }
    }

    // 5. Central Golden Starburst Core with continuous pulse & slow rotation
    rotate(degrees = -shimmerPhase * 360f * 0.35f, pivot = Offset(cx, cy)) {
        drawCentralStarGlow(cx, cy, r * (0.40f + sin(shimmerPhase * PI.toFloat() * 2f) * 0.06f), isGold = true)
    }

    // 6. Top-Left Specular Gleam
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.75f), Color.Transparent),
            center = Offset(cx - r * 0.35f, cy - r * 0.38f),
            radius = r * 0.35f
        ),
        radius = r * 0.35f,
        center = Offset(cx - r * 0.35f, cy - r * 0.38f)
    )
}

/**
 * RED: Rounded strawberry-drop / heart jelly silhouette with rich crimson shading,
 * glossy specular crescent, and delicate candy seed facets.
 */
private fun DrawScope.drawRedStrawberryDrop(cx: Float, cy: Float, r: Float) {
    val path = getRedStrawberryPath(cx, cy, r)

    // Drop shadow
    drawPath(
        path = getRedStrawberryPath(cx, cy + r * 0.15f, r * 1.02f),
        brush = Brush.radialGradient(
            colors = listOf(Color(0x802A040D), Color.Transparent),
            center = Offset(cx, cy + r * 0.3f),
            radius = r * 1.1f
        )
    )

    // Body Fill: 3D Radial Gradient
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFF5376), // Bright coral highlight
                Color(0xFFE11D48), // Rich ruby red
                Color(0xFF880828)  // Deep berry shadow
            ),
            center = Offset(cx - r * 0.22f, cy - r * 0.25f),
            radius = r * 1.25f
        )
    )

    // Outer Rim Stroke
    drawPath(
        path = path,
        color = Color(0xFFFF8FA3).copy(alpha = 0.85f),
        style = Stroke(width = r * 0.08f)
    )

    // Top-Left Glossy Specular Highlight (Curved Pill)
    val highlightPath = Path().apply {
        moveTo(cx - r * 0.55f, cy - r * 0.25f)
        cubicTo(
            cx - r * 0.45f, cy - r * 0.65f,
            cx - r * 0.10f, cy - r * 0.65f,
            cx - r * 0.05f, cy - r * 0.35f
        )
        cubicTo(
            cx - r * 0.15f, cy - r * 0.50f,
            cx - r * 0.40f, cy - r * 0.50f,
            cx - r * 0.55f, cy - r * 0.25f
        )
        close()
    }
    drawPath(
        path = highlightPath,
        brush = Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.15f)),
            start = Offset(cx - r * 0.5f, cy - r * 0.6f),
            end = Offset(cx, cy - r * 0.2f)
        )
    )

    // Subtle sweet candy sparkle dots
    val seedOffsets = listOf(
        Offset(cx - r * 0.3f, cy + r * 0.15f),
        Offset(cx + r * 0.25f, cy + r * 0.10f),
        Offset(cx - r * 0.05f, cy + r * 0.45f)
    )
    for (dot in seedOffsets) {
        drawCircle(
            color = Color(0xFFFFB3C1).copy(alpha = 0.75f),
            radius = r * 0.065f,
            center = dot
        )
    }
}

/**
 * BLUE: Smooth rounded oval jelly lozenge with deep sapphire-cyan translucency,
 * curved specular glass reflection, and polished edge rim.
 */
private fun DrawScope.drawBlueOvalJelly(cx: Float, cy: Float, r: Float) {
    val ovalRect = Rect(cx - r * 0.96f, cy - r * 0.80f, cx + r * 0.96f, cy + r * 0.80f)
    val roundRadius = CornerRadius(r * 0.80f, r * 0.72f)
    val ovalPath = Path().apply {
        addRoundRect(RoundRect(ovalRect, roundRadius))
    }

    // Ambient shadow
    drawRoundRect(
        color = Color(0x700B1D47),
        topLeft = Offset(cx - r * 0.96f, cy - r * 0.68f),
        size = Size(r * 1.92f, r * 1.60f),
        cornerRadius = roundRadius
    )

    // 3D Sapphire Gradient Fill
    drawPath(
        path = ovalPath,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF60A5FA), // Cyan-blue light
                Color(0xFF2563EB), // Royal sapphire
                Color(0xFF172554)  // Deep abyss shadow
            ),
            center = Offset(cx - r * 0.25f, cy - r * 0.30f),
            radius = r * 1.25f
        )
    )

    // Polished Azure Rim
    drawPath(
        path = ovalPath,
        color = Color(0xFF93C5FD).copy(alpha = 0.85f),
        style = Stroke(width = r * 0.08f)
    )

    // Top-Left Glossy Pill Arc
    val glossRect = Rect(cx - r * 0.68f, cy - r * 0.58f, cx + r * 0.35f, cy - r * 0.20f)
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.10f)),
            start = Offset(cx - r * 0.6f, cy - r * 0.5f),
            end = Offset(cx + r * 0.3f, cy - r * 0.2f)
        ),
        topLeft = glossRect.topLeft,
        size = glossRect.size,
        cornerRadius = CornerRadius(r * 0.20f, r * 0.20f)
    )

    // Secondary specular bead
    drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = r * 0.08f,
        center = Offset(cx - r * 0.48f, cy - r * 0.38f)
    )
}

/**
 * GREEN: Smooth rounded leaf-like jelly candy with emerald gradient,
 * subtle central vein gloss spine, and vibrant mint rim.
 */
private fun DrawScope.drawGreenLeafJelly(cx: Float, cy: Float, r: Float) {
    val path = getGreenLeafPath(cx, cy, r)

    // Ambient drop shadow
    drawPath(
        path = getGreenLeafPath(cx, cy + r * 0.14f, r * 1.02f),
        brush = Brush.radialGradient(
            colors = listOf(Color(0x70032617), Color.Transparent),
            center = Offset(cx, cy + r * 0.3f),
            radius = r * 1.15f
        )
    )

    // Emerald 3D Body Fill
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF4ADE80), // Lime-emerald highlight
                Color(0xFF16A34A), // Emerald green
                Color(0xFF052E16)  // Deep forest shadow
            ),
            center = Offset(cx - r * 0.25f, cy - r * 0.30f),
            radius = r * 1.25f
        )
    )

    // Mint Rim
    drawPath(
        path = path,
        color = Color(0xFF86EFAC).copy(alpha = 0.85f),
        style = Stroke(width = r * 0.08f)
    )

    // Center Curved Vein Gloss
    val veinPath = Path().apply {
        moveTo(cx, cy - r * 0.72f)
        cubicTo(cx + r * 0.15f, cy - r * 0.1f, cx - r * 0.1f, cy + r * 0.3f, cx, cy + r * 0.72f)
    }
    drawPath(
        path = veinPath,
        color = Color(0xFFBBF7D0).copy(alpha = 0.65f),
        style = Stroke(width = r * 0.07f, cap = StrokeCap.Round)
    )

    // Top-Left Glossy Crescent
    val leafGloss = Path().apply {
        moveTo(cx - r * 0.55f, cy - r * 0.1f)
        cubicTo(cx - r * 0.5f, cy - r * 0.6f, cx - r * 0.1f, cy - r * 0.7f, cx - r * 0.05f, cy - r * 0.65f)
        cubicTo(cx - r * 0.25f, cy - r * 0.55f, cx - r * 0.45f, cy - r * 0.35f, cx - r * 0.55f, cy - r * 0.1f)
        close()
    }
    drawPath(
        path = leafGloss,
        brush = Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.15f)),
            start = Offset(cx - r * 0.5f, cy - r * 0.6f),
            end = Offset(cx, cy - r * 0.2f)
        )
    )
}

/**
 * YELLOW: Faceted lemon-drop lozenge candy with radiant golden facets,
 * sharp diamond bevels, and dazzling specular highlights.
 */
private fun DrawScope.drawYellowLemonDrop(cx: Float, cy: Float, r: Float) {
    val path = getYellowLemonPath(cx, cy, r)

    // Ambient drop shadow
    drawPath(
        path = getYellowLemonPath(cx, cy + r * 0.14f, r * 1.02f),
        brush = Brush.radialGradient(
            colors = listOf(Color(0x703F2305), Color.Transparent),
            center = Offset(cx, cy + r * 0.3f),
            radius = r * 1.15f
        )
    )

    // Radiant Golden 3D Body Fill
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFEF08A), // Pale lemon shine
                Color(0xFFEAB308), // Bright golden amber
                Color(0xFF713F12)  // Warm caramel shadow
            ),
            center = Offset(cx - r * 0.22f, cy - r * 0.25f),
            radius = r * 1.25f
        )
    )

    // Faceted Candy Lines
    val facetPath1 = Path().apply {
        moveTo(cx, cy - r * 0.88f)
        lineTo(cx, cy + r * 0.88f)
    }
    val facetPath2 = Path().apply {
        moveTo(cx - r * 0.88f, cy)
        lineTo(cx + r * 0.88f, cy)
    }
    drawPath(
        path = facetPath1,
        color = Color(0xFFFEF9C3).copy(alpha = 0.55f),
        style = Stroke(width = r * 0.05f)
    )
    drawPath(
        path = facetPath2,
        color = Color(0xFFFEF9C3).copy(alpha = 0.55f),
        style = Stroke(width = r * 0.05f)
    )

    // Polished Golden Rim
    drawPath(
        path = path,
        color = Color(0xFFFEF9C3).copy(alpha = 0.90f),
        style = Stroke(width = r * 0.08f)
    )

    // Top-Left Glossy Facet Gleam
    val facetGlint = Path().apply {
        moveTo(cx - r * 0.08f, cy - r * 0.72f)
        lineTo(cx - r * 0.65f, cy - r * 0.12f)
        lineTo(cx - r * 0.25f, cy - r * 0.12f)
        lineTo(cx - r * 0.08f, cy - r * 0.45f)
        close()
    }
    drawPath(
        path = facetGlint,
        brush = Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.15f)),
            start = Offset(cx - r * 0.5f, cy - r * 0.6f),
            end = Offset(cx, cy)
        )
    )
}

/**
 * PURPLE: Plump grape/triple-berry jelly with rich amethyst violet tones,
 * multi-lobe spherical gloss beads, and lavender rim.
 */
private fun DrawScope.drawPurpleGrapeJelly(cx: Float, cy: Float, r: Float) {
    val path = getPurpleGrapePath(cx, cy, r)

    // Ambient drop shadow
    drawPath(
        path = getPurpleGrapePath(cx, cy + r * 0.14f, r * 1.02f),
        brush = Brush.radialGradient(
            colors = listOf(Color(0x702E0854), Color.Transparent),
            center = Offset(cx, cy + r * 0.3f),
            radius = r * 1.15f
        )
    )

    // Royal Amethyst 3D Body Fill
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFD8B4FE), // Lavender highlight
                Color(0xFF9333EA), // Royal purple
                Color(0xFF3B0764)  // Deep plum shadow
            ),
            center = Offset(cx - r * 0.22f, cy - r * 0.28f),
            radius = r * 1.25f
        )
    )

    // Lavender Rim
    drawPath(
        path = path,
        color = Color(0xFFF3E8FF).copy(alpha = 0.85f),
        style = Stroke(width = r * 0.08f)
    )

    // Triple Glossy Spheres (one on each plump lobe)
    val glossPoints = listOf(
        Offset(cx - r * 0.38f, cy - r * 0.35f),
        Offset(cx + r * 0.32f, cy - r * 0.35f),
        Offset(cx, cy + r * 0.10f)
    )
    for (p in glossPoints) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.85f), Color.Transparent),
                center = Offset(p.x - r * 0.06f, p.y - r * 0.06f),
                radius = r * 0.24f
            ),
            radius = r * 0.24f,
            center = p
        )
    }
}

/**
 * ORANGE: Rounded citrus-drop candy with vibrant tangerine peach tones,
 * radial wedge segment grooves, and glossy arch highlight.
 */
private fun DrawScope.drawOrangeCitrusDrop(cx: Float, cy: Float, r: Float) {
    val path = getOrangeCitrusPath(cx, cy, r)

    // Ambient drop shadow
    drawPath(
        path = getOrangeCitrusPath(cx, cy + r * 0.14f, r * 1.02f),
        brush = Brush.radialGradient(
            colors = listOf(Color(0x70431407), Color.Transparent),
            center = Offset(cx, cy + r * 0.3f),
            radius = r * 1.15f
        )
    )

    // Tangerine 3D Body Fill
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFDBA74), // Peach-orange shine
                Color(0xFFF97316), // Vivid tangerine
                Color(0xFF7C2D12)  // Deep burnt-orange shadow
            ),
            center = Offset(cx - r * 0.25f, cy - r * 0.28f),
            radius = r * 1.25f
        )
    )

    // Inner Juicy Citrus Segment Rays
    for (i in 0 until 6) {
        val angle = (i * 60.0 * Math.PI / 180.0)
        val ex = (cx + r * 0.65f * cos(angle)).toFloat()
        val ey = (cy + r * 0.65f * sin(angle)).toFloat()
        drawLine(
            color = Color(0xFFFFEDD5).copy(alpha = 0.45f),
            start = Offset(cx, cy),
            end = Offset(ex, ey),
            strokeWidth = r * 0.05f,
            cap = StrokeCap.Round
        )
    }

    // Tangerine Rim
    drawPath(
        path = path,
        color = Color(0xFFFFEDD5).copy(alpha = 0.85f),
        style = Stroke(width = r * 0.08f)
    )

    // Top-Left Glossy Crescent Arc
    val glossArc = Path().apply {
        moveTo(cx - r * 0.62f, cy - r * 0.15f)
        cubicTo(cx - r * 0.55f, cy - r * 0.60f, cx - r * 0.15f, cy - r * 0.68f, cx, cy - r * 0.65f)
        cubicTo(cx - r * 0.25f, cy - r * 0.52f, cx - r * 0.48f, cy - r * 0.35f, cx - r * 0.62f, cy - r * 0.15f)
        close()
    }
    drawPath(
        path = glossArc,
        brush = Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.15f)),
            start = Offset(cx - r * 0.5f, cy - r * 0.6f),
            end = Offset(cx, cy - r * 0.2f)
        )
    )
}

/**
 * Returns the exact Compose Path for each candy type's unique silhouette.
 */
fun getCandyPath(candyType: CandyType, cx: Float, cy: Float, r: Float): Path {
    return when (candyType) {
        CandyType.RED -> getRedStrawberryPath(cx, cy, r)
        CandyType.BLUE -> {
            val ovalRect = Rect(cx - r * 0.96f, cy - r * 0.80f, cx + r * 0.96f, cy + r * 0.80f)
            Path().apply { addRoundRect(RoundRect(ovalRect, CornerRadius(r * 0.80f, r * 0.72f))) }
        }
        CandyType.GREEN -> getGreenLeafPath(cx, cy, r)
        CandyType.YELLOW -> getYellowLemonPath(cx, cy, r)
        CandyType.PURPLE -> getPurpleGrapePath(cx, cy, r)
        CandyType.ORANGE -> getOrangeCitrusPath(cx, cy, r)
        CandyType.EMPTY -> Path()
    }
}

// Geometric Silhouette Paths for each candy:

private fun getRedStrawberryPath(cx: Float, cy: Float, r: Float): Path {
    return Path().apply {
        moveTo(cx, cy - r * 0.52f)
        // Top right lobe
        cubicTo(cx + r * 0.55f, cy - r * 0.95f, cx + r * 0.98f, cy - r * 0.35f, cx + r * 0.80f, cy + r * 0.15f)
        // Down to bottom rounded tip
        cubicTo(cx + r * 0.65f, cy + r * 0.62f, cx + r * 0.32f, cy + r * 0.92f, cx, cy + r * 0.96f)
        // Up left side
        cubicTo(cx - r * 0.32f, cy + r * 0.92f, cx - r * 0.65f, cy + r * 0.62f, cx - r * 0.80f, cy + r * 0.15f)
        // Top left lobe back to dip
        cubicTo(cx - r * 0.98f, cy - r * 0.35f, cx - r * 0.55f, cy - r * 0.95f, cx, cy - r * 0.52f)
        close()
    }
}

private fun getGreenLeafPath(cx: Float, cy: Float, r: Float): Path {
    return Path().apply {
        moveTo(cx, cy - r * 0.94f)
        // Right curved belly
        cubicTo(cx + r * 0.98f, cy - r * 0.45f, cx + r * 0.88f, cy + r * 0.55f, cx, cy + r * 0.94f)
        // Left curved belly
        cubicTo(cx - r * 0.88f, cy + r * 0.55f, cx - r * 0.98f, cy - r * 0.45f, cx, cy - r * 0.94f)
        close()
    }
}

private fun getYellowLemonPath(cx: Float, cy: Float, r: Float): Path {
    return Path().apply {
        moveTo(cx, cy - r * 0.92f)
        // Top right arc
        cubicTo(cx + r * 0.55f, cy - r * 0.55f, cx + r * 0.92f, cy - r * 0.20f, cx + r * 0.92f, cy)
        // Bottom right arc
        cubicTo(cx + r * 0.92f, cy + r * 0.20f, cx + r * 0.55f, cy + r * 0.55f, cx, cy + r * 0.92f)
        // Bottom left arc
        cubicTo(cx - r * 0.55f, cy + r * 0.55f, cx - r * 0.92f, cy + r * 0.20f, cx - r * 0.92f, cy)
        // Top left arc
        cubicTo(cx - r * 0.92f, cy - r * 0.20f, cx - r * 0.55f, cy - r * 0.55f, cx, cy - r * 0.92f)
        close()
    }
}

private fun getPurpleGrapePath(cx: Float, cy: Float, r: Float): Path {
    return Path().apply {
        // Plump 3-lobe cluster jelly
        moveTo(cx, cy - r * 0.45f)
        // Top Right lobe
        cubicTo(cx + r * 0.48f, cy - r * 0.92f, cx + r * 0.96f, cy - r * 0.45f, cx + r * 0.78f, cy - r * 0.05f)
        // Bottom Right / Bottom Lobe
        cubicTo(cx + r * 0.85f, cy + r * 0.45f, cx + r * 0.45f, cy + r * 0.94f, cx, cy + r * 0.92f)
        // Bottom Left Lobe
        cubicTo(cx - r * 0.45f, cy + r * 0.94f, cx - r * 0.85f, cy + r * 0.45f, cx - r * 0.78f, cy - r * 0.05f)
        // Top Left Lobe back to top dip
        cubicTo(cx - r * 0.96f, cy - r * 0.45f, cx - r * 0.48f, cy - r * 0.92f, cx, cy - r * 0.45f)
        close()
    }
}

private fun getOrangeCitrusPath(cx: Float, cy: Float, r: Float): Path {
    val roundRect = Rect(cx - r * 0.90f, cy - r * 0.90f, cx + r * 0.90f, cy + r * 0.90f)
    return Path().apply {
        addRoundRect(RoundRect(roundRect, CornerRadius(r * 0.65f, r * 0.65f)))
    }
}

/**
 * Draws an animated glowing selection halo behind the selected candy piece.
 */
private fun DrawScope.drawSelectionHalo(cx: Float, cy: Float, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.55f),
                Color(0xFFFDE047).copy(alpha = 0.45f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = radius * 1.35f
        ),
        radius = radius * 1.35f,
        center = Offset(cx, cy)
    )
}

/**
 * Draws a 4-point golden/white cosmic star gleam in the center.
 */
private fun DrawScope.drawCentralStarGlow(cx: Float, cy: Float, starR: Float, isGold: Boolean = false) {
    val coreColor = if (isGold) Color(0xFFFDE047) else Color.White
    val starPath = Path().apply {
        moveTo(cx, cy - starR)
        cubicTo(cx + starR * 0.15f, cy - starR * 0.15f, cx + starR * 0.15f, cy - starR * 0.15f, cx + starR, cy)
        cubicTo(cx + starR * 0.15f, cy + starR * 0.15f, cx + starR * 0.15f, cy + starR * 0.15f, cx, cy + starR)
        cubicTo(cx - starR * 0.15f, cy + starR * 0.15f, cx - starR * 0.15f, cy + starR * 0.15f, cx - starR, cy)
        cubicTo(cx - starR * 0.15f, cy - starR * 0.15f, cx - starR * 0.15f, cy - starR * 0.15f, cx, cy - starR)
        close()
    }

    drawPath(
        path = starPath,
        brush = Brush.radialGradient(
            colors = listOf(coreColor, coreColor.copy(alpha = 0.4f)),
            center = Offset(cx, cy),
            radius = starR
        )
    )
}

/**
 * Draws a lightweight, crisp 4-pointed electric spark with glowing core and halo.
 */
private fun DrawScope.drawElectricSpark(
    x: Float,
    y: Float,
    size: Float,
    color: Color = Color.White,
    glowColor: Color = Color(0xFFFDE047),
    intensity: Float = 1f
) {
    if (intensity <= 0.01f) return
    val alpha = intensity.coerceIn(0f, 1f)

    // Outer soft glow
    drawCircle(
        color = glowColor.copy(alpha = alpha * 0.45f),
        radius = size * 1.6f,
        center = Offset(x, y)
    )

    // Inner bright center core
    drawCircle(
        color = Color.White.copy(alpha = alpha * 0.95f),
        radius = size * 0.45f,
        center = Offset(x, y)
    )

    // 4-point electric sparkle diamond star
    val starLength = size * 1.5f
    val starWidth = size * 0.32f
    val path = Path().apply {
        moveTo(x, y - starLength)
        lineTo(x + starWidth, y)
        lineTo(x, y + starLength)
        lineTo(x - starWidth, y)
        close()
        moveTo(x - starLength, y)
        lineTo(x, y + starWidth)
        lineTo(x + starLength, y)
        lineTo(x, y - starWidth)
        close()
    }
    drawPath(
        path = path,
        color = color.copy(alpha = alpha * 0.9f)
    )
}

/**
 * Draws occasional, subtle deterministic electric sparkles and diagonal glints on normal candies.
 */
private fun DrawScope.drawNormalCandySparkle(
    candyType: CandyType,
    cx: Float,
    cy: Float,
    r: Float,
    tileId: Long,
    shimmerPhase: Float
) {
    if (candyType == CandyType.EMPTY) return

    // Deterministic seed per candy based on tile id / hash
    val seed = ((tileId * 2654435761L) xor (tileId shr 16)).toFloat().mod(1000f) / 1000f
    val localPhase = (shimmerPhase + seed) % 1f

    // Sparkle occurs during a small 22% window of the total animation loop
    if (localPhase in 0.0f..0.22f) {
        val progress = localPhase / 0.22f // 0f..1f
        val sparkleAlpha = sin(progress * PI.toFloat()).coerceIn(0f, 1f)

        // Sparkle offset position on candy body
        val angle = seed * 2.0 * Math.PI
        val dist = r * (0.30f + (seed * 0.35f))
        val sx = (cx + dist * cos(angle)).toFloat()
        val sy = (cy + dist * sin(angle)).toFloat()

        // Draw micro sparkle
        drawElectricSpark(
            x = sx,
            y = sy,
            size = r * 0.20f,
            color = Color.White,
            glowColor = candyType.color.copy(alpha = 0.8f),
            intensity = sparkleAlpha
        )

        // Quick diagonal micro-glint streak
        val glintLen = r * 0.35f * sparkleAlpha
        drawLine(
            color = Color.White.copy(alpha = sparkleAlpha * 0.75f),
            start = Offset(sx - glintLen, sy + glintLen),
            end = Offset(sx + glintLen, sy - glintLen),
            strokeWidth = 1.6f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Draws dynamic electric rim sparks and energy tendrils around the selected candy.
 */
private fun DrawScope.drawSelectedElectricShimmer(
    cx: Float,
    cy: Float,
    r: Float,
    shimmerPhase: Float
) {
    // 3 orbiting electric spark points along the outer rim
    val rimRadius = r * 1.15f
    val sparkColors = listOf(Color(0xFFFDE047), Color(0xFF67E8F9), Color.White)

    for (i in 0 until 3) {
        val angle = (shimmerPhase * 360.0 * 1.5 + (i * 120.0)) * Math.PI / 180.0
        val sx = (cx + rimRadius * cos(angle)).toFloat()
        val sy = (cy + rimRadius * sin(angle)).toFloat()

        // Subtle electric spark
        drawElectricSpark(
            x = sx,
            y = sy,
            size = r * 0.18f,
            color = Color.White,
            glowColor = sparkColors[i],
            intensity = 0.85f + (sin(shimmerPhase * PI.toFloat() * 6f + i) * 0.15f)
        )

        // Tiny electric lightning tendril connecting to inner rim
        val innerX = (cx + r * 0.88f * cos(angle + 0.15)).toFloat()
        val innerY = (cy + r * 0.88f * sin(angle + 0.15)).toFloat()
        drawLine(
            color = sparkColors[i].copy(alpha = 0.45f),
            start = Offset(sx, sy),
            end = Offset(innerX, innerY),
            strokeWidth = 1.4f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Draws radiant expanding energy bloom when candies match or clear.
 */
private fun DrawScope.drawMatchEnergyGlow(
    cx: Float,
    cy: Float,
    r: Float,
    shimmerPhase: Float
) {
    val bloomPhase = (shimmerPhase * 3f) % 1f
    val bloomRadius = r * (0.8f + bloomPhase * 0.7f)
    val alpha = (1f - bloomPhase).coerceIn(0f, 1f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha * 0.85f),
                Color(0xFFFDE047).copy(alpha = alpha * 0.5f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = bloomRadius
        ),
        radius = bloomRadius,
        center = Offset(cx, cy)
    )

    // Radiating spark rays
    for (i in 0 until 4) {
        val angle = (i * 90.0 + bloomPhase * 45.0) * Math.PI / 180.0
        val sparkX = (cx + bloomRadius * cos(angle)).toFloat()
        val sparkY = (cy + bloomRadius * sin(angle)).toFloat()
        drawElectricSpark(sparkX, sparkY, r * 0.15f, Color.White, Color(0xFFFDE047), alpha)
    }
}
