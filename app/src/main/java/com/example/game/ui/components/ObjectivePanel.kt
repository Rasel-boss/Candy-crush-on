package com.example.game.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.CandyType
import com.example.game.model.LevelObjective
import com.example.game.model.ObjectiveType
import com.example.game.model.SpecialCandyType

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

            // Objective items displayed horizontally with consistent height across all cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(if (objectives.size >= 3) 6.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                objectives.forEachIndexed { index, objective ->
                    ObjectiveItemView(
                        objective = objective,
                        index = index,
                        totalObjectives = objectives.size,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
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
    totalObjectives: Int = 2,
    isCompact: Boolean = false
) {
    val animatedProgress by animateFloatAsState(
        targetValue = objective.progressRatio,
        animationSpec = tween(durationMillis = 400),
        label = "obj_progress_${objective.resolvedId}"
    )

    val containerBgColor by animateColorAsState(
        targetValue = if (objective.isCompleted) Color(0xFF064E3B).copy(alpha = 0.7f) else Color(0xFF2D2A5E).copy(alpha = 0.6f),
        label = "obj_bg_${objective.resolvedId}"
    )

    val borderColor by animateColorAsState(
        targetValue = if (objective.isCompleted) Color(0xFF34D399) else Color(0xFF6366F1).copy(alpha = 0.3f),
        label = "obj_border_${objective.resolvedId}"
    )

    val isDense = totalObjectives >= 3 || isCompact
    val labelTextSize = if (isDense) 9.sp else 10.sp
    val progressTextSize = if (isDense) 9.sp else 10.sp
    val horizontalPadding = if (isDense) 6.dp else 8.dp
    val verticalPadding = if (isCompact) 4.dp else 6.dp

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerBgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .semantics { contentDescription = objective.accessibilityDescription }
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .testTag("objective_item_${objective.resolvedId}")
            .testTag("objective_item_$index"),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isDense) 4.dp else 6.dp)
        ) {
            // Objective Icon / Badge with integrated Completion Checkmark
            ObjectiveBadgeIcon(
                objective = objective,
                isCompact = isDense
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
                        text = objective.shortDisplayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = labelTextSize,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (objective.isCompleted) Color(0xFF34D399) else Color(0xFFE2E8F0),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    val progressTextPulse = remember(objective.displayCurrent) { Animatable(1.1f) }
                    LaunchedEffect(objective.displayCurrent) {
                        progressTextPulse.animateTo(1.0f, tween(180, easing = FastOutSlowInEasing))
                    }

                    Text(
                        text = "${objective.displayCurrent}/${objective.target}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = progressTextSize,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (objective.isCompleted) Color(0xFF34D399) else Color(0xFFF8FAFC),
                        maxLines = 1,
                        modifier = Modifier
                            .testTag("objective_progress_${objective.resolvedId}")
                            .scale(progressTextPulse.value)
                    )
                }

                Spacer(modifier = Modifier.height(if (isCompact) 2.dp else 3.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isCompact) 3.dp else 4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (objective.isCompleted) Color(0xFF34D399) else Color(0xFF818CF8),
                    trackColor = Color(0xFF1E1B4B).copy(alpha = 0.8f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Renders the thematic graphic or colored candy badge corresponding to the objective.
 * When completed, displays a clean green checkmark circle keeping layout stable.
 */
@Composable
private fun ObjectiveBadgeIcon(
    objective: LevelObjective,
    isCompact: Boolean = false
) {
    val iconSize = if (isCompact) 20.dp else 24.dp

    Box(
        modifier = Modifier.size(iconSize),
        contentAlignment = Alignment.Center
    ) {
        if (objective.isCompleted) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
                    .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(if (isCompact) 12.dp else 14.dp)
                )
            }
        } else {
            when (objective.type) {
                ObjectiveType.COLLECT_CANDY -> {
                    val targetCandyType = objective.candyType ?: CandyType.RED
                    Box(
                        modifier = Modifier
                            .size(iconSize)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CandyCanvasArtwork(
                            candyType = targetCandyType,
                            specialType = SpecialCandyType.NONE,
                            isSelected = false,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
                ObjectiveType.TARGET_SCORE,
                ObjectiveType.SCORE_TARGET -> {
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
                            modifier = Modifier.size(if (isCompact) 12.dp else 14.dp)
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
                            modifier = Modifier.size(if (isCompact) 12.dp else 14.dp)
                        )
                    }
                }
                ObjectiveType.CLEAR_BLOCKERS -> {
                    Box(
                        modifier = Modifier
                            .size(iconSize)
                            .clip(CircleShape)
                            .background(Color(0xFF64748B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(if (isCompact) 12.dp else 14.dp)
                        )
                    }
                }
            }
        }
    }
}
