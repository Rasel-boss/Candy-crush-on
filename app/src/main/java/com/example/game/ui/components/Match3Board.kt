package com.example.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.BoardPosition
import com.example.game.model.FloatingScoreEvent
import com.example.game.model.Match3Board
import com.example.game.model.SpecialCombinationType
import com.example.game.ui.effects.CanvasParticleOverlay
import com.example.game.ui.effects.CascadeIndicator
import com.example.game.ui.effects.FloatingScoreOverlay
import com.example.game.ui.effects.ParticleFactory
import com.example.game.ui.effects.rememberParticleSystemState

/**
 * Modern Match-3 game grid view with a rounded dark container, soft shadow,
 * responsive tile layout, particle overlay, floating scores, cascade indicator,
 * and animated special combination overlays.
 *
 * @param board The active [Match3Board] grid.
 * @param selectedPosition The currently selected [BoardPosition], or null.
 * @param onTileClick Callback invoked when a tile is tapped at [BoardPosition].
 * @param activeComboType The active special combo being animated, if any.
 * @param comboPositions Positions affected by the active combo.
 * @param matchingPositions Positions of candies currently dissolving in a match.
 * @param matchIntensity Intensity of the match for particle burst scaling.
 * @param invalidSwapPair Positions of candies currently undergoing rejection animation.
 * @param swappingPair Positions of candies currently sliding in an active swap.
 * @param cascadeChainCount Current cascade chain step (2 = CHAIN x2, etc.).
 * @param floatingScores Active floating score text events.
 * @param isBoardImpact Whether a subtle board-wide glow pulse is active.
 */
@Composable
fun Match3BoardView(
    board: Match3Board,
    selectedPosition: BoardPosition?,
    onTileClick: (BoardPosition) -> Unit,
    modifier: Modifier = Modifier,
    activeComboType: SpecialCombinationType = SpecialCombinationType.NONE,
    comboPositions: Set<BoardPosition> = emptySet(),
    matchingPositions: Set<BoardPosition> = emptySet(),
    matchIntensity: Int = 3,
    invalidSwapPair: Pair<BoardPosition, BoardPosition>? = null,
    swappingPair: Pair<BoardPosition, BoardPosition>? = null,
    cascadeChainCount: Int = 0,
    floatingScores: List<FloatingScoreEvent> = emptyList(),
    isBoardImpact: Boolean = false
) {
    val boardShape = RoundedCornerShape(24.dp)
    val boardBackground = Color(0xFF1E1B4B)

    // Dynamic border color with subtle glow if board impact is active
    val boardBorderColor = if (isBoardImpact || cascadeChainCount >= 2) {
        Color(0xFF818CF8)
    } else {
        Color(0xFF4338CA).copy(alpha = 0.5f)
    }

    val particleState = rememberParticleSystemState()

    val swapProgress by animateFloatAsState(
        targetValue = if (swappingPair != null) 1f else 0f,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "swap_progress"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp)
            .aspectRatio(1f)
            .padding(8.dp)
            .shadow(elevation = 12.dp, shape = boardShape, spotColor = Color(0xFF312E81))
            .clip(boardShape)
            .background(boardBackground)
            .border(width = 2.dp, color = boardBorderColor, shape = boardShape)
            .padding(6.dp)
            .testTag("match3_board"),
        contentAlignment = Alignment.Center
    ) {
        val boardWidthPx = constraints.maxWidth.toFloat()
        val tileSizePx = boardWidthPx / board.columns

        // Emit particles when new matches occur
        LaunchedEffect(matchingPositions) {
            if (matchingPositions.isNotEmpty()) {
                val burstParticles = mutableListOf<com.example.game.ui.effects.Particle>()
                for (pos in matchingPositions) {
                    val tile = board.getTile(pos)
                    val baseColor = tile?.type?.color ?: Color(0xFFFFD54F)
                    val cx = (pos.column + 0.5f) * tileSizePx
                    val cy = (pos.row + 0.5f) * tileSizePx
                    val particleCount = when {
                        matchIntensity >= 5 -> 12
                        matchIntensity == 4 -> 8
                        else -> 5
                    }
                    burstParticles.addAll(
                        ParticleFactory.createMatchBurst(
                            centerX = cx,
                            centerY = cy,
                            count = particleCount,
                            baseColor = baseColor,
                            glowColor = Color.White
                        )
                    )
                }
                particleState.emitBurst(burstParticles)
            }
        }

        // Board Impact Subtle Flash
        if (isBoardImpact || cascadeChainCount >= 2) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF6366F1).copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.minDimension * 0.7f
                    )
                )
            }
        }

        // 8x8 Tile Grid
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            for (r in 0 until board.rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (c in 0 until board.columns) {
                        val tile = board.getTile(r, c)
                        val pos = BoardPosition(r, c)
                        val isSelected = selectedPosition == pos
                        val isComboTile = comboPositions.contains(pos)
                        val isMatching = matchingPositions.contains(pos)
                        val isInvalidSwap = invalidSwapPair?.first == pos || invalidSwapPair?.second == pos

                        // Calculate visual offset during active swap
                        var swapOffsetX = 0f
                        var swapOffsetY = 0f
                        if (swappingPair != null) {
                            if (swappingPair.first == pos) {
                                val target = swappingPair.second
                                swapOffsetX = (target.column - pos.column) * tileSizePx * swapProgress
                                swapOffsetY = (target.row - pos.row) * tileSizePx * swapProgress
                            } else if (swappingPair.second == pos) {
                                val target = swappingPair.first
                                swapOffsetX = (target.column - pos.column) * tileSizePx * swapProgress
                                swapOffsetY = (target.row - pos.row) * tileSizePx * swapProgress
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(1.dp)
                                .background(
                                    color = Color(0xFF131131).copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (tile != null) {
                                CandyTileView(
                                    tile = tile,
                                    isSelected = isSelected,
                                    isMatching = isMatching || isComboTile,
                                    isInvalidSwap = isInvalidSwap,
                                    onClick = { onTileClick(pos) },
                                    modifier = Modifier.offset {
                                        IntOffset(swapOffsetX.toInt(), swapOffsetY.toInt())
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Particle System Overlay (GPU Canvas)
        CanvasParticleOverlay(
            state = particleState,
            modifier = Modifier.fillMaxSize()
        )

        // Floating Scores (+30, +60, CHAIN x2)
        FloatingScoreOverlay(
            events = floatingScores,
            tileSizeDp = 44.dp,
            modifier = Modifier.fillMaxSize()
        )

        // Cascade Chain Banner Indicator ("CASCADE!", "CHAIN x2", "CHAIN x3")
        CascadeIndicator(
            chainCount = cascadeChainCount,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )

        // Animated Special Combination Overlay
        if (activeComboType != SpecialCombinationType.NONE) {
            ComboEffectOverlay(
                comboType = activeComboType,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Visual effects overlay displaying dynamic pulses, sweep beams, and combo title badges.
 */
@Composable
fun ComboEffectOverlay(
    comboType: SpecialCombinationType,
    modifier: Modifier = Modifier
) {
    val comboTitle = when (comboType) {
        SpecialCombinationType.STRIPED_STRIPED -> "CROSS BEAM!"
        SpecialCombinationType.WRAPPED_WRAPPED -> "MEGA BLAST!"
        SpecialCombinationType.STRIPED_WRAPPED -> "SUPER CROSS!"
        SpecialCombinationType.COLOR_BOMB_NORMAL -> "COLOR CLEARED!"
        SpecialCombinationType.COLOR_BOMB_STRIPED -> "STRIPE STORM!"
        SpecialCombinationType.COLOR_BOMB_WRAPPED -> "CLUSTER BOMB!"
        SpecialCombinationType.COLOR_BOMB_COLOR_BOMB -> "COSMIC CLEAR!"
        SpecialCombinationType.NONE -> ""
    }

    val comboColor = when (comboType) {
        SpecialCombinationType.STRIPED_STRIPED -> Color(0xFF38BDF8)
        SpecialCombinationType.WRAPPED_WRAPPED -> Color(0xFFFB923C)
        SpecialCombinationType.STRIPED_WRAPPED -> Color(0xFFA855F7)
        SpecialCombinationType.COLOR_BOMB_NORMAL -> Color(0xFFFBBF24)
        SpecialCombinationType.COLOR_BOMB_STRIPED -> Color(0xFF34D399)
        SpecialCombinationType.COLOR_BOMB_WRAPPED -> Color(0xFFF43F5E)
        SpecialCombinationType.COLOR_BOMB_COLOR_BOMB -> Color(0xFFEC4899)
        SpecialCombinationType.NONE -> Color.White
    }

    Box(
        modifier = modifier
            .testTag("combo_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Decorative energy radial glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) * 1.1f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(comboColor.copy(alpha = 0.4f), Color.Transparent),
                    center = center,
                    radius = radius
                ),
                center = center,
                radius = radius
            )
        }

        // Combo Announcement Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = comboColor.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            modifier = Modifier
                .padding(16.dp)
                .testTag("combo_${comboType.name.lowercase()}")
        ) {
            Text(
                text = comboTitle,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                ),
                color = Color.White,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .testTag("combo_title_text")
            )
        }
    }
}
