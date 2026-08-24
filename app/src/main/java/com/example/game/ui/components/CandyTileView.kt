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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.game.model.CandyTile
import com.example.game.model.SpecialCandyType

/**
 * Visual representation of an individual candy tile on the Match-3 grid,
 * powered by the tactile, dimensional custom [CandyCanvasArtwork] system.
 *
 * Supports smooth selection scaling, match dissolve animations (highlight, pulse, fade),
 * invalid swap rejection shake animations, and full accessibility descriptions.
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
    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "candy_tile_scale"
    )

    // Match Dissolve Animation: Scale Pulse and Fade
    val matchAlpha = remember { Animatable(1f) }
    val matchScale = remember { Animatable(1f) }

    LaunchedEffect(isMatching) {
        if (isMatching) {
            matchScale.animateTo(
                targetValue = 1.18f,
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
            )
            matchScale.animateTo(
                targetValue = 0.2f,
                animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing)
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
                animationSpec = tween(durationMillis = 200, delayMillis = 40, easing = LinearEasing)
            )
        }
    }

    // Invalid Swap Rejection Shake Animation
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isInvalidSwap) {
        if (isInvalidSwap) {
            // Quick 3-cycle shake
            repeat(2) {
                shakeOffset.animateTo(6f, tween(35, easing = LinearEasing))
                shakeOffset.animateTo(-6f, tween(35, easing = LinearEasing))
            }
            shakeOffset.animateTo(0f, tween(35, easing = FastOutSlowInEasing))
        }
    }

    val candyType = tile.type
    val specialType = tile.specialCandyType

    val finalScale = if (isMatching) matchScale.value else selectionScale
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
            .offset { IntOffset(shakeOffset.value.toInt(), 0) }
            .scale(finalScale)
            .alpha(finalAlpha)
            .semantics {
                contentDescription = tileAccessibilityLabel
            }
            .testTag("candy_tile_${tile.row}_${tile.column}")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
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

            // Render the rich custom Canvas artwork with sparkles and shimmers
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
