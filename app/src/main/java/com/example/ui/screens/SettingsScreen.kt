package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.model.GameSettings
import com.example.game.viewmodel.SettingsViewModel
import com.example.ui.theme.PuzzleMasterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by settingsViewModel.settingsState.collectAsStateWithLifecycle()

    SettingsScreenContent(
        settings = settings,
        onToggleSound = { settingsViewModel.toggleSound() },
        onToggleMusic = { settingsViewModel.toggleMusic() },
        onToggleVibration = { settingsViewModel.toggleVibration() },
        onResetSettings = { settingsViewModel.resetSettingsToDefault() },
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    settings: GameSettings,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit = {},
    onToggleVibration: () -> Unit,
    onResetSettings: () -> Unit = {},
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Ambient pulsating glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "settings_ambient_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Candy Crush Lite deep twilight canvas
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0B24), // Twilight purple-black top
            Color(0xFF1B123C), // Deep royal violet middle
            Color(0xFF130E2B), // Midnight indigo base
            Color(0xFF0A0718)  // Dark foundation
        )
    )

    val headerGlowRingBrush = Brush.sweepGradient(
        colors = listOf(
            Color(0xFFEC4899), // Fuchsia / Pink
            Color(0xFF8B5CF6), // Purple
            Color(0xFF06B6D4), // Cyan
            Color(0xFFF59E0B), // Amber
            Color(0xFFEC4899)  // Loop back
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.testTag("settings_screen_title")
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0B24).copy(alpha = 0.92f),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F0B24),
        modifier = modifier.testTag("settings_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundBrush)
                .drawBehind {
                    // Ambient radial glow effects matching Candy Crush Lite atmosphere
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF8B5CF6).copy(alpha = 0.16f * glowAlpha),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.15f),
                            radius = size.width * 0.65f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFEC4899).copy(alpha = 0.10f * glowAlpha),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.85f, size.height * 0.65f),
                            radius = size.width * 0.5f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF06B6D4).copy(alpha = 0.08f * glowAlpha),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.15f, size.height * 0.80f),
                            radius = size.width * 0.45f
                        )
                    )
                },
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .widthIn(max = 480.dp)
            ) {
                // Header Icon Badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(elevation = 12.dp, shape = CircleShape, ambientColor = Color(0xFFC084FC), spotColor = Color(0xFFD946EF))
                        .border(BorderStroke(2.dp, headerGlowRingBrush), CircleShape)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E163B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Preferences Gear Icon",
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Audio & Feedback",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    ),
                    color = Color.White
                )

                Text(
                    text = "Customize sound effects, music soundtrack, and tactile haptic vibration.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Sound Effects Setting Item
                SettingSwitchCard(
                    icon = if (settings.soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    iconTint = if (settings.soundEnabled) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                    iconContainerColor = if (settings.soundEnabled) Color(0xFF0369A1).copy(alpha = 0.35f) else Color(0xFF334155).copy(alpha = 0.35f),
                    title = "Sound Effects",
                    subtitle = "Audio cues for candy swaps, matches, combos & buttons",
                    checked = settings.soundEnabled,
                    onCheckedChange = { onToggleSound() },
                    switchTag = "sound_switch",
                    cardTag = "sound_setting_item",
                    statusLabel = if (settings.soundEnabled) "ON" else "OFF"
                )

                // Music Setting Item
                SettingSwitchCard(
                    icon = if (settings.musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                    iconTint = if (settings.musicEnabled) Color(0xFFF472B6) else Color(0xFF94A3B8),
                    iconContainerColor = if (settings.musicEnabled) Color(0xFFBE185D).copy(alpha = 0.35f) else Color(0xFF334155).copy(alpha = 0.35f),
                    title = "Music",
                    subtitle = "Background soundtrack and ambient audio",
                    checked = settings.musicEnabled,
                    onCheckedChange = { onToggleMusic() },
                    switchTag = "music_switch",
                    cardTag = "music_setting_item",
                    statusLabel = if (settings.musicEnabled) "ON" else "OFF"
                )

                // Vibration Setting Item
                SettingSwitchCard(
                    icon = Icons.Default.Vibration,
                    iconTint = if (settings.vibrationEnabled) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                    iconContainerColor = if (settings.vibrationEnabled) Color(0xFF15803D).copy(alpha = 0.35f) else Color(0xFF334155).copy(alpha = 0.35f),
                    title = "Vibration",
                    subtitle = "Tactile physical feedback on moves and cascade combos",
                    checked = settings.vibrationEnabled,
                    onCheckedChange = { onToggleVibration() },
                    switchTag = "vibration_switch",
                    cardTag = "vibration_setting_item",
                    statusLabel = if (settings.vibrationEnabled) "ON" else "OFF"
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Reset Preferences to Default Button
                OutlinedButton(
                    onClick = onResetSettings,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE2E8F0)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.30f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFF1E1738).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .testTag("reset_settings_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reset Preferences to Default",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }

                // Back Button
                OutlinedButton(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color(0xFF2E2254).copy(alpha = 0.65f), RoundedCornerShape(18.dp))
                        .testTag("settings_back_button")
                ) {
                    Text(
                        text = "Back to Main Menu",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Footer Game Info
                Text(
                    text = "Candy Crush Lite • v1.0.0\nOffline Match-3 Adventure",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SettingSwitchCard(
    icon: ImageVector,
    iconTint: Color,
    iconContainerColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchTag: String,
    cardTag: String,
    statusLabel: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1738).copy(alpha = 0.85f)
        ),
        border = BorderStroke(
            1.dp,
            if (checked) Color(0xFF818CF8).copy(alpha = 0.35f)
            else Color(0xFF475569).copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(cardTag)
            .semantics(mergeDescendants = true) {
                contentDescription = "$title, currently $statusLabel. Tap switch to toggle."
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconContainerColor)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (checked) iconTint.copy(alpha = 0.4f)
                                else Color.Transparent
                            ),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Status pill badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (checked) Color(0xFF8B5CF6).copy(alpha = 0.30f)
                                    else Color(0xFF475569).copy(alpha = 0.30f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (checked) Color(0xFFC084FC) else Color(0xFF94A3B8)
                            )
                        }
                    }

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(switchTag),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF8B5CF6),
                    checkedBorderColor = Color(0xFFC084FC),
                    uncheckedThumbColor = Color(0xFF94A3B8),
                    uncheckedTrackColor = Color(0xFF334155).copy(alpha = 0.6f),
                    uncheckedBorderColor = Color(0xFF475569)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    PuzzleMasterTheme {
        SettingsScreenContent(
            settings = GameSettings(soundEnabled = true, musicEnabled = true, vibrationEnabled = true),
            onToggleSound = {},
            onToggleMusic = {},
            onToggleVibration = {},
            onResetSettings = {},
            onBackClick = {}
        )
    }
}
