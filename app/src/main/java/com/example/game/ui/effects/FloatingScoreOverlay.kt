package com.example.game.ui.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.FloatingScoreEvent

/**
 * Overlay rendering floating score indicators (+30, +60, +90 CHAIN x2)
 * directly above match locations on the Match-3 board.
 */
@Composable
fun FloatingScoreOverlay(
    events: List<FloatingScoreEvent>,
    tileSizeDp: Dp,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        for (event in events) {
            FloatingScoreItemView(
                event = event,
                tileSizeDp = tileSizeDp
            )
        }
    }
}

@Composable
private fun FloatingScoreItemView(
    event: FloatingScoreEvent,
    tileSizeDp: Dp
) {
    val animY = remember { Animatable(0f) }
    val animAlpha = remember { Animatable(1f) }
    val animScale = remember { Animatable(0.7f) }

    LaunchedEffect(event.id) {
        // Pop in and float upward
        animScale.animateTo(
            targetValue = if (event.cascadeCount > 1) 1.25f else 1.1f,
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
        )
        animY.animateTo(
            targetValue = -35f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(event.id) {
        // Hold briefly then fade out smoothly
        animAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 650, delayMillis = 100, easing = FastOutSlowInEasing)
        )
    }

    val posX = (event.centerColumn * tileSizeDp.value)
    val posY = (event.centerRow * tileSizeDp.value) + animY.value

    val textColor = when {
        event.cascadeCount >= 4 -> Color(0xFFFFD54F) // Radiant Amber Gold
        event.cascadeCount >= 2 -> Color(0xFF00E5FF) // Electric Cyan
        event.score >= 80 -> Color(0xFFFF80AB)       // Neon Pink
        else -> Color(0xFFFFFFFF)                    // Crisp White
    }

    val glowColor = when {
        event.cascadeCount >= 2 -> Color(0xFF00B0FF)
        else -> Color(0xAA000000)
    }

    Text(
        text = event.text,
        style = TextStyle(
            fontSize = if (event.cascadeCount > 1) 18.sp else 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            textAlign = TextAlign.Center,
            shadow = Shadow(
                color = glowColor,
                offset = Offset(2f, 2f),
                blurRadius = 6f
            )
        ),
        modifier = Modifier
            .offset { IntOffset((posX * 2.5f).toInt(), (posY * 2.5f).toInt()) }
            .scale(animScale.value)
            .alpha(animAlpha.value)
            .testTag("floating_score_${event.id}")
    )
}
