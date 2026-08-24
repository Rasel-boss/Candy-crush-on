package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.model.GameStatus
import com.example.game.ui.components.GameOverDialog
import com.example.game.ui.components.Match3BoardView
import com.example.game.ui.components.ObjectivePanel
import com.example.game.ui.components.PauseOverlay
import com.example.game.ui.components.VictoryDialog
import com.example.game.ui.effects.CascadeIndicator
import com.example.game.viewmodel.Match3ViewModel
import com.example.ui.theme.BackgroundDark

@Composable
fun GameScreen(
    level: Int,
    viewModel: Match3ViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsStateWithLifecycle()

    LaunchedEffect(level) {
        if (state.currentLevel != level) {
            viewModel.startLevel(level)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Level ${state.currentLevel}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = state.levelConfig?.title ?: "Classic Match",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                IconButton(
                    onClick = { viewModel.pauseGame() },
                    modifier = Modifier.testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White
                    )
                }
            }

            // Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "SCORE",
                    value = "${state.score}",
                    color = Color(0xFFFBBF24),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "MOVES",
                    value = "${state.movesRemaining}",
                    color = if (state.movesRemaining <= 5) Color(0xFFEF4444) else Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }

            // Objectives
            ObjectivePanel(
                objectives = state.objectives,
                modifier = Modifier.fillMaxWidth()
            )

            // Combo & Cascade Banner
            CascadeIndicator(cascadeCount = state.cascadeCount)

            // The Match-3 Board View
            Match3BoardView(
                board = state.board,
                selectedPosition = state.selectedPosition,
                onTileClick = { pos -> viewModel.onTileClicked(pos) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Dialogs & Overlays
        if (state.status == GameStatus.PAUSED) {
            PauseOverlay(
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.restartCurrentLevel() },
                onMainMenu = onNavigateBack
            )
        }

        if (state.status == GameStatus.VICTORY) {
            VictoryDialog(
                score = state.score,
                level = state.currentLevel,
                onNextLevel = { viewModel.nextLevel() },
                onRestart = { viewModel.restartCurrentLevel() },
                onMainMenu = onNavigateBack
            )
        }

        if (state.status == GameStatus.GAME_OVER) {
            GameOverDialog(
                score = state.score,
                onRestart = { viewModel.restartCurrentLevel() },
                onMainMenu = onNavigateBack
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}
