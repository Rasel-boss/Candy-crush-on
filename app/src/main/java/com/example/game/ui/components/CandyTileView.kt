package com.example.game.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.game.model.BoardPosition
import com.example.game.model.CandyTile

@Composable
fun CandyTileView(
    tile: CandyTile?,
    position: BoardPosition,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "tileScale"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x22FFFFFF))
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, Color(0xFFFFD700), RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("candy_tile_${position.row}_${position.col}"),
        contentAlignment = Alignment.Center
    ) {
        if (tile != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                CandyArt(tile = tile)
            }
        }
    }
}
