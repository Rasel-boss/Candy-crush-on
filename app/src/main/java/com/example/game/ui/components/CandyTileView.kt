package com.example.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.game.model.CandyTile
import com.example.game.model.SpecialCandyType

/**
 * Visual representation of an individual candy tile on the Match-3 grid,
 * powered by the tactile, dimensional custom [CandyCanvasArtwork] system.
 *
 * Supports smooth selection scaling, match dissolve animations (highlight, pulse, fade),
 * physical fall / drop animations, invalid swap rejection shake, and full accessibility descriptions.
 *
 * @param tile The candy tile data model.
 * @param isSelected Whether this tile is currently selected by the player.
 * @param isMatching Whether this tile is currently in a match resolution cycle.
 * @param isInvalidSwap Whether this tile was just involved in an invalid swap attempt.
 * @param onClick Callback invoked when this tile is clicked.
 */
@Composable
fun CandyTileView(
    tile: CandyTile,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isMatching: Boolean = false,
    isInvalidSwap: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetScale = when {
        isPressed -> 0.92f
        isSelected -> 1.14f
        else -> 1.0f
    }

    val selectionScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = if (isPressed) Spring.StiffnessMedium else Spring.StiffnessLow
        ),
        label = "candy_tile_scale"
    )

    // Match Dissolve Animation: Brighten Highlight, Scale Pulse, and Fade
    val matchAlpha = remember { Animatable(1f) }
    val matchScale = remember { Animatable(1f) }

    LaunchedEffect(isMatching) {
        if (isMatching) {
            matchScale.animateTo(
                targetValue = 1.16f,
                animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
            )
            matchScale.animateTo(
                targetValue = 0.15f,
                animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing)
            )
        } else {
            matchScale.snapTo(1f)
            matchAlpha.snapTo(1f)
        }
    }

    LaunchedEffect(isMatching) {
        if (isMatching) {
            matchAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 180, delayMillis = 30, easing = LinearEasing)
            )
        }
    }

    // Invalid Swap Rejection Shake Animation
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isInvalidSwap) {
        if (isInvalidSwap) {
            repeat(2) {
                shakeOffset.animateTo(5f, tween(30, easing = LinearEasing))
                shakeOffset.animateTo(-5f, tween(30, easing = LinearEasing))
            }
            shakeOffset.animateTo(0f, tween(30, easing = FastOutSlowInEasing))
        }
    }

    // Fall & Spawn Animation: Smooth downward translation when row changes or new tile spawns
    var previousRow by remember(tile.id) { mutableIntStateOf(tile.row) }
    val fallRowOffset = remember(tile.id) { Animatable(0f) }
    val spawnScale = remember(tile.id) { Animatable(1f) }

    LaunchedEffect(tile.row) {
        if (previousRow != tile.row) {
            val rowDifference = (previousRow - tile.row).toFloat()
            fallRowOffset.snapTo(rowDifference)
            fallRowOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = (140 + minOf(3, tile.row - previousRow) * 25).coerceIn(120, 240),
                    easing = FastOutSlowInEasing
                )
            )
            previousRow = tile.row
        }
    }

    val candyType = tile.type
    val specialType = tile.specialCandyType

    val finalScale = if (isMatching) {
        matchScale.value
    } else {
        selectionScale * spawnScale.value
    }
    val finalAlpha = if (isMatching) matchAlpha.value else 1f

    val tileAccessibilityLabel = remember(tile, isSelected) {
        buildString {
            if (specialType == SpecialCandyType.COLOR_BOMB) {
                append("Color Bomb")
            } else {
                append(tile.type.displayName)
                append(" candy")
                when (specialType) {
                    SpecialCandyType.HORIZONTAL_STRIPED -> append(", horizontal striped")
                    SpecialCandyType.VERTICAL_STRIPED -> append(", vertical striped")
                    SpecialCandyType.WRAPPED -> append(", wrapped")
                    else -> {}
                }
            }
            append(", row ${tile.row + 1}, column ${tile.column + 1}")
            if (isSelected) append(", selected")
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .padding(1.5.dp)
            .graphicsLayer {
                translationX = shakeOffset.value
                translationY = fallRowOffset.value * size.height
            }
            .scale(finalScale)
            .alpha(finalAlpha)
            .semantics {
                contentDescription = tileAccessibilityLabel
            }
            .testTag("candy_tile_${tile.row}_${tile.column}")
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 24.dp, color = Color.White.copy(alpha = 0.5f)),
                enabled = tile.isPlayable,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (tile.isPlayable) {
            // Rejection aura if invalid swap
            if (isInvalidSwap) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFE11D48).copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                )
            }

            // Render the rich custom Canvas artwork with sparkles, shimmers, and selection aura
            CandyCanvasArtwork(
                candyType = candyType,
                specialType = specialType,
                isSelected = isSelected,
                tileId = tile.id,
                isMatching = isMatching,
                modifier = Modifier.fillMaxSize()
            )

            // Semantic test tag anchors for UI verification suite
            when (specialType) {
                SpecialCandyType.HORIZONTAL_STRIPED -> {
                    Box(modifier = Modifier.sizeIn(minWidth = 1.dp, minHeight = 1.dp).testTag("striped_horizontal"))
                }
                SpecialCandyType.VERTICAL_STRIPED -> {
                    Box(modifier = Modifier.sizeIn(minWidth = 1.dp, minHeight = 1.dp).testTag("striped_vertical"))
                }
                SpecialCandyType.WRAPPED -> {
                    Box(modifier = Modifier.sizeIn(minWidth = 1.dp, minHeight = 1.dp).testTag("wrapped_candy_badge"))
                }
                SpecialCandyType.COLOR_BOMB -> {
                    Box(modifier = Modifier.sizeIn(minWidth = 1.dp, minHeight = 1.dp).testTag("color_bomb_badge"))
                }
                SpecialCandyType.NONE -> {}
            }
        }
    }
}
