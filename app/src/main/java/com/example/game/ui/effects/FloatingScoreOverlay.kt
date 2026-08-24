package com.example.game.ui.effects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun FloatingScoreOverlay(
    scoreText: String,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = scoreText,
                color = Color(0xFFFBBF24),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
