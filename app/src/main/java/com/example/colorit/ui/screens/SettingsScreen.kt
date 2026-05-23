package com.example.colorit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.data.AppDatabase
import com.example.colorit.ui.components.KidsButton
import com.example.colorit.ui.theme.*
import com.example.colorit.utils.AudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Settings Screen with playful controls and kid-safe Parental Lock gate.
 * Manages audio toggle configurations, allows full gallery clean wipes, and features
 * a math gate puzzle (e.g. "solve 9 + 5 = ?") to prevent toddlers from making edits.
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    // Toggle states
    var soundOn by remember { mutableStateOf(AudioManager.isSoundEnabled) }
    var musicOn by remember { mutableStateOf(AudioManager.isMusicEnabled) }

    // Parental Lock dialog states
    var showParentalGate by remember { mutableStateOf(false) }
    var parentalGateTargetAction by remember { mutableStateOf<String?>(null) } // "CLEAN" or "ENTER_PAGE"
    var numA by remember { mutableStateOf(0) }
    var numB by remember { mutableStateOf(0) }
    var gateInputAnswer by remember { mutableStateOf("") }
    var gateError by remember { mutableStateOf(false) }

    // Helper to trigger Parental lock math equation
    fun launchGate(action: String) {
        AudioManager.playTapSound()
        numA = (5..15).random()
        numB = (3..9).random()
        gateInputAnswer = ""
        gateError = false
        parentalGateTargetAction = action
        showParentalGate = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    AudioManager.playTapSound()
                    onNavigateBack()
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(White, CircleShape)
                    .shadow(2.dp, CircleShape)
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Settings ⚙️",
                color = AccentPurple,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // --- Settings Card Deck ---
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Sound effects toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Sound Effects 🔊", color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Play bubbly bubble pop audio sweeps", color = TextLight, fontSize = 11.sp)
                    }
                    Switch(
                        checked = soundOn,
                        onCheckedChange = {
                            AudioManager.playTapSound()
                            soundOn = it
                            AudioManager.isSoundEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentPurple,
                            checkedTrackColor = PastelPurple
                        )
                    )
                }

                Divider(color = SoftGray)

                // 2. Playful background music toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "App Music 🎵", color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Play beautiful background kid melodies", color = TextLight, fontSize = 11.sp)
                    }
                    Switch(
                        checked = musicOn,
                        onCheckedChange = {
                            AudioManager.playTapSound()
                            musicOn = it
                            AudioManager.isMusicEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentPurple,
                            checkedTrackColor = PastelPurple
                        )
                    )
                }

                Divider(color = SoftGray)

                // 3. Dark Theme toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Glowing Dark Mode 🌃", color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Glow draw in neon or dark templates", color = TextLight, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = {
                            AudioManager.playTapSound()
                            onToggleTheme()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentPurple,
                            checkedTrackColor = PastelPurple
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Parental Wipe Card
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Parental Zone 🚸",
                    color = AccentPink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Requires solving a math equation to wipe the drawings database gallery.",
                    color = TextLight,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                KidsButton(
                    text = "🧹 Wipe Gallery",
                    backgroundColor = AccentPink,
                    onClick = {
                        launchGate("CLEAN")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Info credits
        Text(text = "ColorIt Kids! version 1.0.0", color = TextLight, fontSize = 12.sp)
        Text(
            text = "Made programmatically with 💖 for kids!",
            color = TextLight,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }

    // --- Parental Math Puzzle Overlay ---
    if (showParentalGate) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { showParentalGate = false },
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                modifier = Modifier
                    .width(320.dp)
                    .padding(16.dp)
                    .clickable(enabled = false) {}
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Parents Only! 🔐",
                        color = AccentPurple,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Please solve the math puzzle to verify you are a parent:",
                        color = TextDark,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Text(
                        text = "$numA + $numB = ?",
                        color = AccentPink,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Text Input field
                    OutlinedTextField(
                        value = gateInputAnswer,
                        onValueChange = { gateInputAnswer = it },
                        label = { Text("Your answer") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (gateError) {
                        Text(
                            text = "Incorrect, try again! 🧸",
                            color = AccentPink,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        KidsButton(
                            text = "Submit",
                            onClick = {
                                val expected = numA + numB
                                val input = gateInputAnswer.toIntOrNull()
                                if (input == expected) {
                                    AudioManager.playSuccessSound()
                                    showParentalGate = false
                                    // Gate verified! Perform action
                                    if (parentalGateTargetAction == "CLEAN") {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            // Wipe internal database records
                                            db.artworkDao().clearAllArtworks()
                                            
                                            // Delete all physical files in background
                                            val dir = context.filesDir
                                            dir.listFiles()?.forEach { file ->
                                                if (file.name.endsWith(".png")) {
                                                    file.delete()
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    AudioManager.playErrorSound()
                                    gateError = true
                                }
                            }
                        )

                        KidsButton(
                            text = "Cancel",
                            backgroundColor = SoftGray,
                            contentColor = TextDark,
                            onClick = {
                                AudioManager.playTapSound()
                                showParentalGate = false
                            }
                        )
                    }
                }
            }
        }
    }
}
