package com.example.game.ui.effects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CascadeIndicator(
    cascadeCount: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = cascadeCount > 1,
        enter = scaleIn(),
        exit = scaleOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF8B5CF6))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "${cascadeCount}x COMBO!",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
    }
}
