package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PuzzleMasterTheme

@Composable
fun MainMenuScreen(
    onPlayClick: () -> Unit,
    onLevelsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Subtle ambient glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Deep purple / midnight indigo background gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0B24), // Twilight purple-black top
            Color(0xFF1B123C), // Deep royal violet middle
            Color(0xFF130E2B), // Midnight indigo base
            Color(0xFF0A0718)  // Dark foundation
        )
    )

    val logoGlowRingBrush = Brush.sweepGradient(
        colors = listOf(
            Color(0xFFEC4899), // Pink
            Color(0xFF8B5CF6), // Purple
            Color(0xFF06B6D4), // Cyan
            Color(0xFFF59E0B), // Amber
            Color(0xFFEC4899)  // Loop back
        )
    )

    val playButtonGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF8B5CF6), // Vivid Violet
            Color(0xFFD946EF), // Fuchsia / Magenta
            Color(0xFFF43F5E)  // Rose Red
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .drawBehind {
                if (size.width > 0.5f) {
                    // Ambient background glowing orbs
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF8B5CF6).copy(alpha = 0.18f * glowAlpha),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.22f),
                            radius = size.width * 0.65f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF06B6D4).copy(alpha = 0.12f * glowAlpha),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.85f, size.height * 0.75f),
                            radius = size.width * 0.5f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFEC4899).copy(alpha = 0.10f * glowAlpha),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.15f, size.height * 0.65f),
                            radius = size.width * 0.45f
                        )
                    )
                }
            }
            .testTag("main_menu_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 32.dp)
                .widthIn(max = 440.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Game Logo Badge with glowing border and soft depth
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .shadow(elevation = 16.dp, shape = CircleShape, ambientColor = Color(0xFFC084FC), spotColor = Color(0xFFD946EF))
                    .border(BorderStroke(2.5.dp, logoGlowRingBrush), CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E163B)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_candy_crush_lite),
                    contentDescription = "Candy Crush Lite Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Game Title
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("menu_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle / Tagline
            Text(
                text = "Sweet Match-3 Puzzle Adventure",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.4.sp
                ),
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .testTag("menu_subtitle")
            )

            Spacer(modifier = Modifier.height(44.dp))

            // Main Actions Group
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // PLAY BUTTON (Primary Prominent Action with vivid jewel gradient & rounded shape)
                Button(
                    onClick = onPlayClick,
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 2.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFFF43F5E))
                        .background(playButtonGradient, RoundedCornerShape(22.dp))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)), RoundedCornerShape(22.dp))
                        .testTag("play_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PLAY",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                // LEVELS BUTTON (Secondary Action — Frosted Glass / Violet Card)
                FilledTonalButton(
                    onClick = onLevelsClick,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF2E2254).copy(alpha = 0.85f),
                        contentColor = Color(0xFFE2E8F0)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("levels_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "LEVELS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.0.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                // SETTINGS BUTTON (Tertiary Action — Subtle Glass Border)
                OutlinedButton(
                    onClick = onSettingsClick,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFCBD5E1)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(Color(0xFF1E1738).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .testTag("settings_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SETTINGS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.0.sp
                            ),
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuScreenPreview() {
    PuzzleMasterTheme {
        MainMenuScreen(
            onPlayClick = {},
            onLevelsClick = {},
            onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainMenuScreenDarkPreview() {
    PuzzleMasterTheme(darkTheme = true) {
        MainMenuScreen(
            onPlayClick = {},
            onLevelsClick = {},
            onSettingsClick = {}
        )
    }
}

