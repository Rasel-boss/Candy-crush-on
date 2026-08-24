package com.example.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.CandyType
import com.example.game.model.LevelObjective
import com.example.game.model.ObjectiveType

/**
 * Top-level objective dashboard presenting the player's active level objectives,
 * visual progress indicators, and completion state.
 */
@Composable
fun ObjectivePanel(
    objectives: List<LevelObjective>,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E1B4B).copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color(0xFF818CF8).copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("objective_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = if (isCompact) 6.dp else 8.dp),
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TARGET OBJECTIVES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color(0xFFA5B4FC)
                )

                val completedCount = objectives.count { it.isCompleted }
                Text(
                    text = "$completedCount / ${objectives.size} Done",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (completedCount == objectives.size && objectives.isNotEmpty()) {
                        Color(0xFF4ADE80)
                    } else {
                        Color(0xFFCBD5E1)
                    },
                    modifier = Modifier.testTag("objectives_summary_count")
                )
            }

            // Objective items displayed horizontally or in a responsive wrap
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                objectives.forEachIndexed { index, objective ->
                    ObjectiveItemView(
                        objective = objective,
                        index = index,
                        modifier = Modifier.weight(1f),
                        isCompact = isCompact
                    )
                }
            }
        }
    }
}

/**
 * Individual objective card rendering the icon, progress, target, and completion check.
 */
@Composable
fun ObjectiveItemView(
    objective: LevelObjective,
    index: Int,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val animatedProgress by animateFloatAsState(
        targetValue = objective.progressRatio,
        animationSpec = tween(durationMillis = 400),
        label = "obj_progress_${objective.id}"
    )

    val containerBgColor by animateColorAsState(
        targetValue = if (objective.isCompleted) Color(0xFF064E3B).copy(alpha = 0.7f) else Color(0xFF2D2A5E).copy(alpha = 0.6f),
        label = "obj_bg_${objective.id}"
    )

    val borderColor by animateColorAsState(
        targetValue = if (objective.isCompleted) Color(0xFF34D399) else Color(0xFF6366F1).copy(alpha = 0.3f),
        label = "obj_border_${objective.id}"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerBgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .semantics { contentDescription = objective.accessibilityDescription }
            .padding(horizontal = 8.dp, vertical = if (isCompact) 4.dp else 6.dp)
            .testTag("objective_item_${objective.id}")
            .testTag("objective_item_$index")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Objective Icon / Badge
            ObjectiveBadgeIcon(
                objective = objective,
                isCompact = isCompact
            )

            // Progress Text & Bar
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (objective.type) {
                            ObjectiveType.COLLECT_CANDY -> objective.candyType?.displayName ?: "Candy"
                            ObjectiveType.TARGET_SCORE -> "Score"
                            ObjectiveType.MAKE_MATCHES -> "Matches"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFE2E8F0),
                        maxLines = 1
                    )

                    Text(
                        text = "${objective.currentProgress}/${objective.target}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (objective.isCompleted) Color(0xFF34D399) else Color(0xFFF8FAFC),
                        modifier = Modifier.testTag("objective_progress_${objective.id}")
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (objective.isCompleted) Color(0xFF34D399) else Color(0xFF818CF8),
                    trackColor = Color(0xFF1E1B4B).copy(alpha = 0.8f),
                    strokeCap = StrokeCap.Round
                )
            }

            // Completion checkmark badge
            AnimatedVisibility(
                visible = objective.isCompleted,
                enter = fadeIn() + scaleIn()
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Renders the thematic graphic or colored candy badge corresponding to the objective.
 */
@Composable
private fun ObjectiveBadgeIcon(
    objective: LevelObjective,
    isCompact: Boolean = false
) {
    val iconSize = if (isCompact) 20.dp else 24.dp

    when (objective.type) {
        ObjectiveType.COLLECT_CANDY -> {
            val candyColor = when (objective.candyType) {
                CandyType.RED -> Color(0xFFEF4444)
                CandyType.BLUE -> Color(0xFF3B82F6)
                CandyType.GREEN -> Color(0xFF10B981)
                CandyType.YELLOW -> Color(0xFFFACC15)
                CandyType.ORANGE -> Color(0xFFF97316)
                CandyType.PURPLE -> Color(0xFFA855F7)
                else -> Color(0xFF94A3B8)
            }

            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(candyColor)
                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (objective.candyType) {
                        CandyType.RED -> "🍬"
                        CandyType.BLUE -> "🔷"
                        CandyType.GREEN -> "🟢"
                        CandyType.YELLOW -> "⭐"
                        CandyType.ORANGE -> "🟠"
                        CandyType.PURPLE -> "🟣"
                        else -> "🍬"
                    },
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        ObjectiveType.TARGET_SCORE -> {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(Color(0xFFF59E0B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        ObjectiveType.MAKE_MATCHES -> {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(Color(0xFF8B5CF6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
