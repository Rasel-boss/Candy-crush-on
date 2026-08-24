package com.example.game.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.CandyTile
import com.example.game.model.LevelObjective

@Composable
fun ObjectivePanel(
    objectives: List<LevelObjective>,
    modifier: Modifier = Modifier
) {
    if (objectives.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x33FFFFFF))
            .padding(10.dp)
    ) {
        Text(
            text = "LEVEL OBJECTIVES",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (obj in objectives) {
                ObjectiveItem(
                    objective = obj,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ObjectiveItem(
    objective: LevelObjective,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x22000000))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (objective.targetCandyType != null) {
            Box(modifier = Modifier.size(24.dp)) {
                CandyArt(tile = CandyTile(id = 0, type = objective.targetCandyType))
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${objective.currentAmount}/${objective.targetAmount}",
                    color = if (objective.isCompleted) Color(0xFF10B981) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                if (objective.isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
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
            Spacer(modifier = Modifier.height(2.dp))
            LinearProgressIndicator(
                progress = { objective.progressRatio },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = if (objective.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}
