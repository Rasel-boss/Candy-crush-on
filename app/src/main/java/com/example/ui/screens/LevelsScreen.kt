package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.logic.LevelProgressionManager
import com.example.game.logic.LevelProvider
import com.example.game.logic.LevelStatus
import com.example.game.model.LevelConfig
import com.example.game.model.LevelDifficulty
import com.example.game.utils.ScoreCalculator
import com.example.ui.theme.PuzzleMasterTheme

/**
 * Level selection screen for Candy Crush Lite campaign mode.
 * Displays level difficulty, multi-objective requirements, progression unlock states,
 * and recorded player high scores with modern jewel & crystal styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelsScreen(
    onSelectLevel: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progression by LevelProgressionManager.progression.collectAsStateWithLifecycle()
    val allConfigs = LevelProvider.getAllLevelConfigs()

    val completedCount = allConfigs.count { LevelProgressionManager.getLevelStatus(it.levelNumber) == LevelStatus.COMPLETED }
    val totalScore = allConfigs.sumOf { LevelProgressionManager.getBestScore(it.levelNumber) }

    // Ambient background gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0B24), // Twilight purple-black top
            Color(0xFF1B123C), // Deep royal violet middle
            Color(0xFF130E2B), // Midnight indigo base
            Color(0xFF0A0718)  // Dark foundation
        )
    )

    // Subtle ambient glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "levels_ambient_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Candy Crush Lite",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.testTag("levels_screen_title")
                        )
                        Text(
                            text = "Campaign Levels",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFFCBD5E1)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .testTag("back_button")
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Main Menu",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0B24),
                    titleContentColor = Color.White
                ),
                modifier = Modifier.border(
                    BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f))
                )
            )
        },
        modifier = modifier.testTag("levels_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundBrush)
                .drawBehind {
                    if (size.width > 0.5f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF8B5CF6).copy(alpha = 0.15f * glowAlpha),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.5f, size.height * 0.15f),
                                radius = size.width * 0.6f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF06B6D4).copy(alpha = 0.10f * glowAlpha),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.9f, size.height * 0.7f),
                                radius = size.width * 0.5f
                            )
                        )
                    }
                },
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .widthIn(max = 520.dp)
            ) {
                // Header Progress Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E163B).copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.35f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Campaign Progress",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.testTag("levels_subtitle")
                                )
                                Text(
                                    text = "Complete objectives to unlock new stages",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFCBD5E1)
                                )
                            }

                            // Completion Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF8B5CF6).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color(0xFFC084FC).copy(alpha = 0.4f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "$completedCount / ${allConfigs.size} Done",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        if (totalScore > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Total Campaign Score: ${ScoreCalculator.formatScore(totalScore)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color(0xFFFDE68A)
                                )
                            }
                        }
                    }
                }

                // Render each campaign level dynamically
                allConfigs.forEach { config ->
                    val status = LevelProgressionManager.getLevelStatus(config.levelNumber)
                    val bestScore = LevelProgressionManager.getBestScore(config.levelNumber)

                    LevelItemCard(
                        config = config,
                        status = status,
                        bestScore = bestScore,
                        onSelect = { onSelectLevel(config.levelNumber) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Interactive card representing a single Match-3 level with progression feedback.
 */
@Composable
private fun LevelItemCard(
    config: LevelConfig,
    status: LevelStatus,
    bestScore: Int,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnlocked = status != LevelStatus.LOCKED
    val isCompleted = status == LevelStatus.COMPLETED
    val isCurrent = status == LevelStatus.CURRENT
    val levelNumber = config.levelNumber

    val (icon, subtitle) = when (levelNumber) {
        1 -> Pair(Icons.Default.Spa, "Starter Confection")
        2 -> Pair(Icons.Default.AutoAwesome, "Sweet Journey")
        3 -> Pair(Icons.Default.Bolt, "Sugar Cascade")
        4 -> Pair(Icons.Default.Stars, "Candy Rush")
        5 -> Pair(Icons.Default.WorkspacePremium, "Master Challenge")
        else -> Pair(Icons.Default.AutoAwesome, "Challenge $levelNumber")
    }

    val (difficultyColor, difficultyLabel) = when (config.difficulty) {
        LevelDifficulty.EASY -> Pair(Color(0xFF10B981), "EASY")
        LevelDifficulty.NORMAL -> Pair(Color(0xFF38BDF8), "NORMAL")
        LevelDifficulty.HARD -> Pair(Color(0xFFF59E0B), "HARD")
        LevelDifficulty.EXPERT -> Pair(Color(0xFFEC4899), "EXPERT")
    }

    val jewelGradient = when (levelNumber) {
        1 -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
        2 -> Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7)))
        3 -> Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))
        4 -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
        5 -> Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFBE185D)))
        else -> Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))
    }

    val currentGlowBorder = Brush.horizontalGradient(
        listOf(
            Color(0xFF8B5CF6),
            Color(0xFFD946EF),
            Color(0xFFF43F5E)
        )
    )

    val playButtonGradient = Brush.horizontalGradient(
        listOf(
            Color(0xFF8B5CF6),
            Color(0xFFD946EF),
            Color(0xFFF43F5E)
        )
    )

    val replayButtonGradient = Brush.horizontalGradient(
        listOf(
            Color(0xFF059669),
            Color(0xFF10B981)
        )
    )

    val cardBg = if (isUnlocked) {
        if (isCurrent) Color(0xFF261D47) else Color(0xFF1E163B)
    } else {
        Color(0xFF140F27).copy(alpha = 0.7f)
    }

    val cardBorder = when {
        isCurrent -> BorderStroke(2.dp, currentGlowBorder)
        isCompleted -> BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
        isUnlocked -> BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
        else -> BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    }

    Card(
        onClick = if (isUnlocked) onSelect else { {} },
        enabled = isUnlocked,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg,
            disabledContainerColor = cardBg
        ),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnlocked) 6.dp else 0.dp,
            pressedElevation = if (isUnlocked) 2.dp else 0.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("level_card_$levelNumber")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Level Icon or Lock Icon Box
                    val iconBgModifier = if (!isUnlocked) {
                        Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                    } else if (isCompleted) {
                        Modifier.background(
                            Brush.linearGradient(listOf(Color(0xFF065F46), Color(0xFF10B981))),
                            CircleShape
                        )
                    } else {
                        Modifier.background(jewelGradient, CircleShape)
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(
                                elevation = if (isUnlocked) 8.dp else 0.dp,
                                shape = CircleShape,
                                spotColor = if (isCompleted) Color(0xFF10B981) else Color(0xFF8B5CF6)
                            )
                            .clip(CircleShape)
                            .then(iconBgModifier)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isUnlocked) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(22.dp)
                            )
                        } else if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        } else {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Level $levelNumber",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isUnlocked) Color.White else Color(0xFF64748B)
                            )

                            // Difficulty Pill
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = difficultyColor.copy(alpha = if (isUnlocked) 0.2f else 0.08f),
                                border = BorderStroke(
                                    0.5.dp,
                                    if (isUnlocked) difficultyColor.copy(alpha = 0.5f) else Color.Transparent
                                )
                            ) {
                                Text(
                                    text = difficultyLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = if (isUnlocked) difficultyColor else Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (isUnlocked) Color(0xFFCBD5E1) else Color(0xFF64748B)
                        )
                    }
                }

                // Moves badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isUnlocked) Color(0xFF2E2254) else Color.White.copy(alpha = 0.04f),
                    border = BorderStroke(
                        0.5.dp,
                        if (isUnlocked) Color(0xFF818CF8).copy(alpha = 0.35f) else Color.Transparent
                    )
                ) {
                    Text(
                        text = "${config.startingMoves} Moves",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isUnlocked) Color(0xFFF1F5F9) else Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Objectives description chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                config.objectives.forEach { obj ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F0B24).copy(alpha = if (isUnlocked) 0.7f else 0.3f),
                        border = BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = if (isUnlocked) 0.12f else 0.05f)
                        )
                    ) {
                        Text(
                            text = obj.displayTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isUnlocked) Color(0xFFCBD5E1) else Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (bestScore > 0) {
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Best: ${ScoreCalculator.formatScore(bestScore)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color(0xFFFDE68A)
                        )
                    }
                }
            }

            // Action Button
            if (isUnlocked) {
                Button(
                    onClick = onSelect,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 1.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(14.dp),
                            spotColor = if (isCompleted) Color(0xFF10B981) else Color(0xFFF43F5E)
                        )
                        .background(
                            if (isCompleted) replayButtonGradient else playButtonGradient,
                            RoundedCornerShape(14.dp)
                        )
                        .border(
                            BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            RoundedCornerShape(14.dp)
                        )
                        .testTag("select_level_${levelNumber}_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isCompleted) "REPLAY LEVEL $levelNumber" else "PLAY LEVEL $levelNumber",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.0.sp
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.04f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Complete Level ${levelNumber - 1} to Unlock",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Levels Screen Preview", showBackground = true)
@Composable
fun LevelsScreenPreview() {
    PuzzleMasterTheme {
        LevelsScreen(
            onSelectLevel = {},
            onBackClick = {}
        )
    }
}
