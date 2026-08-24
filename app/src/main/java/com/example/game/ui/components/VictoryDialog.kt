package com.example.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VictoryDialog(
    score: Int,
    level: Int,
    onNextLevel: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            Button(
                onClick = onNextLevel,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.fillMaxWidth().testTag("victory_next_button")
            ) {
                Text("Next Level", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth().testTag("victory_replay_button")
                ) {
                    Text("Replay Level", color = Color.White)
                }
                OutlinedButton(
                    onClick = onMainMenu,
                    modifier = Modifier.fillMaxWidth().testTag("victory_menu_button")
                ) {
                    Text("Main Menu", color = Color.White)
                }
            }
        },
        title = {
            Text(
                text = "Level $level Completed!",
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = Color(0xFF10B981)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Score: $score",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFBBF24)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fantastic job! All objectives cleared.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFF1E293B)
    )
}
