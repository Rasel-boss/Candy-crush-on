package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
 * Level selection screen for Match-3 campaign mode.
 * Displays level difficulty, multi-objective requirements, progression unlock states,
 * and recorded player high scores.
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Campaign Levels",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("levels_screen_title")
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Main Menu"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier.testTag("levels_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .widthIn(max = 540.dp)
            ) {
                // Header description
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "Match-3 Campaign",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("levels_subtitle")
                    )
                    Text(
                        text = "Complete level objectives to unlock subsequent challenges.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

                Spacer(modifier = Modifier.height(20.dp))
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
    val levelNumber = config.levelNumber

    val (icon, subtitle) = when (levelNumber) {
        1 -> Pair(Icons.Default.Spa, "Starter")
        2 -> Pair(Icons.Default.AutoAwesome, "Sweet Journey")
        3 -> Pair(Icons.Default.Bolt, "Sugar Cascade")
        4 -> Pair(Icons.Default.Stars, "Candy Rush")
        5 -> Pair(Icons.Default.WorkspacePremium, "Master Challenge")
        else -> Pair(Icons.Default.AutoAwesome, "Challenge $levelNumber")
    }

    val (difficultyColor, difficultyLabel) = when (config.difficulty) {
        LevelDifficulty.EASY -> Pair(Color(0xFF10B981), "EASY")
        LevelDifficulty.NORMAL -> Pair(Color(0xFF3B82F6), "NORMAL")
        LevelDifficulty.HARD -> Pair(Color(0xFFF59E0B), "HARD")
        LevelDifficulty.EXPERT -> Pair(Color(0xFFEC4899), "EXPERT")
    }

    val accentColor = if (isUnlocked) {
        when (levelNumber) {
            1 -> MaterialTheme.colorScheme.primary
            2 -> MaterialTheme.colorScheme.secondary
            3 -> MaterialTheme.colorScheme.tertiary
            4 -> Color(0xFFF59E0B)
            5 -> Color(0xFFEC4899)
            else -> MaterialTheme.colorScheme.primary
        }
    } else {
        Color(0xFF64748B)
    }

    val containerColor = if (isUnlocked) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }

    Card(
        onClick = if (isUnlocked) onSelect else { {} },
        enabled = isUnlocked,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnlocked) 2.dp else 0.dp,
            pressedElevation = if (isUnlocked) 6.dp else 0.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("level_card_$levelNumber")
            .then(
                if (status == LevelStatus.CURRENT) {
                    Modifier.border(2.dp, accentColor, RoundedCornerShape(20.dp))
                } else Modifier
            )
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
                    // Level Icon or Lock Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isUnlocked) accentColor.copy(alpha = 0.15f)
                                else Color.White.copy(alpha = 0.05f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(24.dp)
                            )
                        } else if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(26.dp)
                            )
                        } else {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
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
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color(0xFF94A3B8)
                            )

                            // Difficulty Pill
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = difficultyColor.copy(alpha = if (isUnlocked) 0.15f else 0.08f)
                            ) {
                                Text(
                                    text = difficultyLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = if (isUnlocked) difficultyColor else Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF64748B)
                        )
                    }
                }

                // Moves & Grid badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shadowElevation = if (isUnlocked) 1.dp else 0.dp
                ) {
                    Text(
                        text = "${config.startingMoves} Moves",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color(0xFF64748B),
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
                        color = MaterialTheme.colorScheme.background.copy(alpha = if (isUnlocked) 0.6f else 0.3f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = if (isUnlocked) 0.1f else 0.05f)
                        )
                    ) {
                        Text(
                            text = obj.displayTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (bestScore > 0) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Best: ${ScoreCalculator.formatScore(bestScore)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color(0xFFF59E0B)
                    )
                }
            }

            // Action Button
            if (isUnlocked) {
                Button(
                    onClick = onSelect,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) MaterialTheme.colorScheme.secondaryContainer else accentColor,
                        contentColor = if (isCompleted) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
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
                                letterSpacing = 0.8.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
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
