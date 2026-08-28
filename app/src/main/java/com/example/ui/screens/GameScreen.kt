package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.audio.IHapticFeedbackManager
import com.example.game.audio.ISoundManager
import com.example.game.audio.NoOpHapticFeedbackManager
import com.example.game.audio.NoOpSoundManager
import com.example.game.model.BoardPosition
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.ui.components.GameOverDialog
import com.example.game.ui.components.Match3BoardView
import com.example.game.ui.components.ObjectivePanel
import com.example.game.ui.components.PauseOverlay
import com.example.game.ui.components.VictoryDialog
import com.example.game.viewmodel.Match3ViewModel
import com.example.ui.theme.PuzzleMasterTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Primary Game Screen for the Match-3 Puzzle Master game.
 *
 * Full-height responsive layout featuring:
 * - Edge-to-edge ambient cosmic game backdrop with subtle ambient starfield
 * - Translucent elevated top header with level indicator and game controls
 * - Dynamic HUD stat cards (Score, Moves, Target Progress)
 * - Scaled square 8x8 Match-3 board container with zero distortion
 * - Live particle system, floating scores, cascade indicators, and impact feedback
 * - Pause, Victory, and Game Over overlays
 * - Safe insets handling for status and navigation bars
 */
@Composable
fun GameScreen(
    viewModel: Match3ViewModel,
    modifier: Modifier = Modifier,
    soundManager: ISoundManager = NoOpSoundManager(),
    hapticFeedbackManager: IHapticFeedbackManager = NoOpHapticFeedbackManager(),
    onBackClick: () -> Unit = {}
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()

    // Connect sound and haptic callbacks to ViewModel lifecycle events
    DisposableEffect(viewModel, soundManager, hapticFeedbackManager) {
        viewModel.onTileSelectedListener = {
            soundManager.playTileSelect()
            hapticFeedbackManager.performTileSelectFeedback()
        }
        viewModel.onValidSwapListener = {
            soundManager.playTileSwap()
            hapticFeedbackManager.performTileSwapFeedback()
        }
        viewModel.onInvalidSwapListener = {
            soundManager.playInvalidSwap()
            hapticFeedbackManager.performInvalidSwapFeedback()
        }
        viewModel.onMatchResolvedListener = { intensity ->
            soundManager.playMatch(intensity)
            hapticFeedbackManager.performMatchFeedback(intensity)
        }
        viewModel.onCascadeListener = { chain ->
            soundManager.playCascade(chain)
            hapticFeedbackManager.performCascadeFeedback(chain)
        }
        viewModel.onGameOverListener = {
            soundManager.playGameOver()
            hapticFeedbackManager.performGameOverFeedback()
        }

        viewModel.onLevelCompleteListener = {
            soundManager.playLevelComplete()
            hapticFeedbackManager.performLevelCompleteFeedback()
        }

        onDispose {
            viewModel.onTileSelectedListener = null
            viewModel.onValidSwapListener = null
            viewModel.onInvalidSwapListener = null
            viewModel.onMatchResolvedListener = null
            viewModel.onCascadeListener = null
            viewModel.onGameOverListener = null
            viewModel.onLevelCompleteListener = null
        }
    }

    GameScreenContent(
        gameState = gameState,
        onTileClick = { pos ->
            viewModel.selectTile(pos)
        },
        onPauseClick = {
            soundManager.playButtonClick()
            hapticFeedbackManager.performButtonClickFeedback()
            viewModel.pauseGame()
        },
        onResumeClick = {
            soundManager.playButtonClick()
            hapticFeedbackManager.performButtonClickFeedback()
            viewModel.resumeGame()
        },
        onRestartClick = {
            soundManager.playButtonClick()
            hapticFeedbackManager.performButtonClickFeedback()
            viewModel.replayLevel()
        },
        onNextLevelClick = {
            soundManager.playButtonClick()
            hapticFeedbackManager.performButtonClickFeedback()
            viewModel.nextLevel()
        },
        onBackClick = {
            soundManager.playButtonClick()
            hapticFeedbackManager.performButtonClickFeedback()
            viewModel.resetGame()
            onBackClick()
        },
        modifier = modifier
    )
}

@Composable
fun GameScreenContent(
    gameState: GameState,
    onTileClick: (BoardPosition) -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onNextLevelClick: () -> Unit = onRestartClick,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_screen")
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isCompactHeight = screenHeight < 640.dp
        val isTabletWidth = screenWidth > 600.dp

        // 1. Full-bleed cosmic game background with subtle ambient star particles
        GameCosmicBackground(modifier = Modifier.fillMaxSize())

        // 2. Responsive Content Container with Safe Insets
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(
                    horizontal = if (isTabletWidth) 24.dp else 12.dp,
                    vertical = if (isCompactHeight) 4.dp else 8.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar Header
            GameTopHeader(
                level = gameState.level,
                onBackClick = onBackClick,
                onRestartClick = onRestartClick,
                onPauseClick = onPauseClick
            )

            // Top HUD (Score, Moves, Star Progress)
            GameHudSection(
                score = gameState.score,
                movesRemaining = gameState.movesRemaining,
                targetScore = gameState.levelConfig.targetScore ?: 1000,
                isCompact = isCompactHeight
            )

            // Objectives Panel displaying active goals and live progress
            ObjectivePanel(
                objectives = gameState.objectives,
                isCompact = isCompactHeight,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
            )

            // Flexible Game Board Area with dynamically balanced square sizing
            val boardEntranceAlpha = remember { Animatable(0f) }
            val boardEntranceScale = remember { Animatable(0.96f) }
            LaunchedEffect(Unit) {
                boardEntranceAlpha.animateTo(1f, tween(180, easing = FastOutSlowInEasing))
                boardEntranceScale.animateTo(1f, tween(180, easing = FastOutSlowInEasing))
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = if (isCompactHeight) 2.dp else 4.dp)
                    .graphicsLayer {
                        alpha = boardEntranceAlpha.value
                        scaleX = boardEntranceScale.value
                        scaleY = boardEntranceScale.value
                    },
                contentAlignment = Alignment.Center
            ) {
                val maxAllowedWidth = if (isTabletWidth) maxWidth - 48.dp else maxWidth - 8.dp
                val boardSize = minOf(maxAllowedWidth, maxHeight).coerceIn(200.dp, 500.dp)

                Match3BoardView(
                    board = gameState.board,
                    selectedPosition = gameState.selectedPosition,
                    onTileClick = onTileClick,
                    modifier = Modifier.size(boardSize),
                    activeComboType = gameState.activeComboType,
                    comboPositions = gameState.comboPositions,
                    matchingPositions = gameState.matchingPositions,
                    matchIntensity = gameState.matchIntensity,
                    invalidSwapPair = gameState.invalidSwapPair,
                    swappingPair = gameState.swappingPair,
                    cascadeChainCount = gameState.cascadeChainCount,
                    floatingScores = gameState.floatingScoreEvents,
                    isBoardImpact = gameState.isBoardImpact
                )
            }

            // Bottom Intentional Area: Decorative Booster Placeholders
            GameBottomSection(
                isCompact = isCompactHeight
            )
        }

        // 3. Pause Overlay
        if (gameState.status == GameStatus.PAUSED) {
            PauseOverlay(
                onResume = onResumeClick,
                onRestart = onRestartClick,
                onHome = onBackClick
            )
        }

        // 4. Victory Dialog Overlay
        if (gameState.status == GameStatus.COMPLETED || gameState.isLevelCompleted) {
            VictoryDialog(
                moves = gameState.movesRemaining,
                score = gameState.score,
                level = gameState.level,
                objectives = gameState.objectives,
                onNextLevel = onNextLevelClick,
                onPlayAgain = onRestartClick,
                onHome = onBackClick
            )
        }

        // 5. Game Over Dialog Overlay
        if (gameState.status == GameStatus.GAME_OVER || gameState.isGameOver) {
            GameOverDialog(
                score = gameState.score,
                level = gameState.level,
                objectives = gameState.objectives,
                onRestart = onRestartClick,
                onHome = onBackClick
            )
        }
    }
}

/**
 * Edge-to-edge cosmic background with rich indigo-violet tones and subtle ambient glowing stars.
 */
@Composable
private fun GameCosmicBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_ambient")
    val starGlowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star_glow_phase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Deep cosmic vertical gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0F0C20), // Deep space top
                    Color(0xFF181336), // Deep indigo mid
                    Color(0xFF1E1B4B), // Rich indigo center
                    Color(0xFF110E27)  // Obsidian violet base
                ),
                startY = 0f,
                endY = h
            )
        )

        // Soft ambient radial colored glows
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF6366F1).copy(alpha = 0.12f), Color.Transparent),
                center = Offset(w * 0.8f, h * 0.18f),
                radius = w * 0.65f
            ),
            radius = w * 0.65f,
            center = Offset(w * 0.8f, h * 0.18f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFEC4899).copy(alpha = 0.08f), Color.Transparent),
                center = Offset(w * 0.2f, h * 0.82f),
                radius = w * 0.70f
            ),
            radius = w * 0.70f,
            center = Offset(w * 0.2f, h * 0.82f)
        )

        // Deterministic ambient starfield
        val starSeeds = listOf(
            Triple(0.15f, 0.08f, 2.0f),
            Triple(0.85f, 0.12f, 2.4f),
            Triple(0.32f, 0.22f, 1.8f),
            Triple(0.72f, 0.28f, 2.2f),
            Triple(0.10f, 0.40f, 1.6f),
            Triple(0.92f, 0.48f, 2.6f),
            Triple(0.24f, 0.62f, 2.0f),
            Triple(0.80f, 0.70f, 1.8f),
            Triple(0.18f, 0.88f, 2.4f),
            Triple(0.65f, 0.92f, 2.0f)
        )

        starSeeds.forEachIndexed { index, (fx, fy, starSize) ->
            val localPhase = (starGlowPhase + index * 0.1f) % 1f
            val alpha = (sin(localPhase * PI.toFloat()) * 0.45f + 0.25f).coerceIn(0f, 1f)
            val sx = w * fx
            val sy = h * fy

            // Soft star glow
            drawCircle(
                color = Color(0xFFC7D2FE).copy(alpha = alpha * 0.4f),
                radius = starSize * 2.2f,
                center = Offset(sx, sy)
            )
            // Star core
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = starSize,
                center = Offset(sx, sy)
            )
        }
    }
}

/**
 * Top bar header with elevated translucent styling, level badge, and quick control buttons.
 */
@Composable
private fun GameTopHeader(
    level: Int,
    onBackClick: () -> Unit,
    onRestartClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1E1B4B).copy(alpha = 0.75f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4338CA).copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("game_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Menu",
                    tint = Color(0xFFE2E8F0)
                )
            }

            // Level Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF4F46E5),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.6f)),
                modifier = Modifier.testTag("level_badge")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFDE047),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Level $level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = Color.White,
                        modifier = Modifier.testTag("level_indicator")
                    )
                }
            }

            // Quick Actions: Restart & Pause
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onRestartClick,
                    modifier = Modifier.testTag("restart_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart Level",
                        tint = Color(0xFFE2E8F0)
                    )
                }

                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier.testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause Game",
                        tint = Color(0xFFE2E8F0)
                    )
                }
            }
        }
    }
}

/**
 * Top HUD section displaying Score, Moves, and Target Progress bar.
 */
@Composable
private fun GameHudSection(
    score: Int,
    movesRemaining: Int,
    targetScore: Int,
    isCompact: Boolean = false
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "score_counter"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 8.dp)
    ) {
        // Score & Moves Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Score Card
            StatBadgeCard(
                title = "SCORE",
                value = "$animatedScore",
                icon = Icons.Default.Stars,
                accentColor = Color(0xFFF59E0B),
                modifier = Modifier
                    .weight(1f)
                    .testTag("score_card"),
                valueTag = "score_text",
                isCompact = isCompact
            )

            // Moves Remaining Card
            val isLowMoves = movesRemaining <= 5
            StatBadgeCard(
                title = "MOVES",
                value = "$movesRemaining",
                icon = Icons.Default.TouchApp,
                accentColor = if (isLowMoves) Color(0xFFEF4444) else Color(0xFF06B6D4),
                modifier = Modifier
                    .weight(1f)
                    .testTag("moves_card"),
                valueTag = "moves_text",
                isCompact = isCompact,
                isWarning = isLowMoves
            )
        }

        // Score Target Progress Indicator
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E1B4B).copy(alpha = 0.6f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3730A3).copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = if (isCompact) 4.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFDE047),
                    modifier = Modifier.size(if (isCompact) 15.dp else 18.dp)
                )

                val progress = (score.toFloat() / targetScore.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isCompact) 6.dp else 8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFF59E0B),
                    trackColor = Color(0xFF312E81),
                    strokeCap = StrokeCap.Round
                )

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = if (isCompact) MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp) else MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC7D2FE)
                )
            }
        }
    }
}

/**
 * Reusable stat badge card for Top Stats Bar with polished game styling.
 */
@Composable
private fun StatBadgeCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    valueTag: String = "",
    isCompact: Boolean = false,
    isWarning: Boolean = false
) {
    val borderColor = if (isWarning) Color(0xFFEF4444).copy(alpha = 0.7f) else Color(0xFF4338CA).copy(alpha = 0.45f)
    val cardBg = if (isWarning) Color(0xFF2D1225).copy(alpha = 0.85f) else Color(0xFF1E1B4B).copy(alpha = 0.85f)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = if (isCompact) 6.dp else 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 28.dp else 34.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            val valuePulse = remember(value) { Animatable(1.12f) }
            LaunchedEffect(value) {
                valuePulse.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                )
            }

            Text(
                text = value,
                style = if (isCompact) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black) else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = Color.White,
                modifier = (if (valueTag.isNotEmpty()) Modifier.testTag(valueTag) else Modifier)
                    .scale(valuePulse.value)
            )
        }
    }
}

/**
 * Bottom game area featuring decorative booster placeholders.
 */
@Composable
private fun GameBottomSection(
    isCompact: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3 Decorative Booster Placeholders (Visual UI only, no fake action triggers)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BoosterPlaceholderCard(
                name = "Hammer",
                icon = Icons.Default.FlashOn,
                accentColor = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
            BoosterPlaceholderCard(
                name = "Shuffle",
                icon = Icons.Default.Shuffle,
                accentColor = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
            BoosterPlaceholderCard(
                name = "Magic",
                icon = Icons.Default.AutoAwesome,
                accentColor = Color(0xFFA855F7),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Purely decorative booster placeholder slot displaying locked/unlocked visual state.
 */
@Composable
private fun BoosterPlaceholderCard(
    name: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF131131).copy(alpha = 0.65f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF312E81).copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Preview(name = "Game Screen Preview", showBackground = true)
@Composable
fun GameScreenPreview() {
    PuzzleMasterTheme {
        GameScreenContent(
            gameState = GameState.createInitial(),
            onTileClick = {},
            onPauseClick = {},
            onResumeClick = {},
            onRestartClick = {},
            onBackClick = {}
        )
    }
}
