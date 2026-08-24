package com.example.game.ui.effects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Animated cascade chain indicator banner displayed during multi-step cascades.
 * Shows "CASCADE!" or "CHAIN x2", "CHAIN x3", "CHAIN x4".
 */
@Composable
fun CascadeIndicator(
    chainCount: Int,
    modifier: Modifier = Modifier
) {
    val isVisible = chainCount >= 2

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(120)) + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)),
        exit = fadeOut(tween(220)) + scaleOut(tween(180)),
        modifier = modifier
    ) {
        val titleText = when (chainCount) {
            2 -> "CASCADE! CHAIN x2"
            3 -> "SUPER CASCADE! CHAIN x3"
            4 -> "MEGA CASCADE! CHAIN x4"
            else -> "ULTRA CASCADE! CHAIN x$chainCount"
        }

        val gradient = when (chainCount) {
            2 -> listOf(Color(0xFF00E5FF), Color(0xFF2979FF))
            3 -> listOf(Color(0xFFFF9100), Color(0xFFFF3D00))
            else -> listOf(Color(0xFFFFD700), Color(0xFFFF1744))
        }

        val borderColor = when (chainCount) {
            2 -> Color(0xFF80D8FF)
            3 -> Color(0xFFFFD180)
            else -> Color(0xFFFFFF8D)
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xDD0D1117),
            shadowElevation = 8.dp,
            modifier = Modifier
                .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(24.dp))
                .testTag("cascade_indicator")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = if (chainCount >= 3) Icons.Default.ElectricBolt else Icons.Default.Bolt,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = titleText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp,
                    color = Color.White,
                    modifier = Modifier.testTag("cascade_text")
                )
            }
        }
    }
}
