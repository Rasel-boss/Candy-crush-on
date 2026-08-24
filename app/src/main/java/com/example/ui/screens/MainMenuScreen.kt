package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.game.model.CandyTile
import com.example.game.model.CandyType
import com.example.game.ui.components.CandyArt
import com.example.ui.theme.BackgroundDark

@Composable
fun MainMenuScreen(
    onPlayClick: () -> Unit,
    onLevelsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Animated/Artistic Logo Candy Cluster
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CandyArt(tile = CandyTile(id = 1, type = CandyType.RED))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PUZZLE MASTER",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Text(
                text = "Match-3 Match & Blast Adventure",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Button(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(54.dp).testTag("play_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("PLAY NOW", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onLevelsClick,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("levels_button")
                    ) {
                        Icon(Icons.Default.GridView, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("SELECT LEVEL", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onSettingsClick,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("SETTINGS", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }
}
