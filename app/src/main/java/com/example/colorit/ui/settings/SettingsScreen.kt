package com.example.colorit.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.ui.components.PastelCard
import com.example.colorit.ui.components.PlayfulButton
import com.example.colorit.ui.theme.*
import com.example.colorit.util.SoundHelper

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val isMusicEnabled by viewModel.isMusicEnabled.collectAsState()

    var showParentalLock by remember { mutableStateOf(false) }
    var mathQuestion by remember { mutableStateOf(Pair(0, 0)) }
    var answerInput by remember { mutableStateOf("") }
    var mathError by remember { mutableStateOf(false) }

    fun generateQuestion() {
        val num1 = (5..15).random()
        val num2 = (3..9).random()
        mathQuestion = Pair(num1, num2)
        answerInput = ""
        mathError = false
    }

    Scaffold(
        topBar = {
            SettingsHeader(
                onBack = {
                    soundHelper.playPopSound()
                    onBack()
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title description
            Text(
                text = "Customize your play settings here! 🎈",
                fontSize = 16.sp,
                color = TextDark.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sound Card
            PastelCard(
                backgroundColor = Color.White,
                borderColor = PastelBlue.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Sound Effects 🔊",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Play bubbly noises on taps",
                            fontSize = 13.sp,
                            color = TextDark.copy(alpha = 0.5f)
                        )
                    }

                    PlayfulButton(
                        onClick = {
                            viewModel.toggleSound()
                            soundHelper.playPopSound()
                        },
                        backgroundColor = if (isSoundEnabled) PastelMint else Color.LightGray.copy(alpha = 0.3f),
                        contentColor = TextDark,
                        shape = CircleShape,
                        border = null,
                        modifier = Modifier.width(90.dp).height(44.dp)
                    ) {
                        Text(
                            text = if (isSoundEnabled) "ON 🌟" else "OFF 💤",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Music Card
            PastelCard(
                backgroundColor = Color.White,
                borderColor = PastelPurple.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Background Music 🎶",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Soft melodies in menus",
                            fontSize = 13.sp,
                            color = TextDark.copy(alpha = 0.5f)
                        )
                    }

                    PlayfulButton(
                        onClick = {
                            viewModel.toggleMusic()
                            soundHelper.playPopSound()
                        },
                        backgroundColor = if (isMusicEnabled) PastelBlue else Color.LightGray.copy(alpha = 0.3f),
                        contentColor = TextDark,
                        shape = CircleShape,
                        border = null,
                        modifier = Modifier.width(90.dp).height(44.dp)
                    ) {
                        Text(
                            text = if (isMusicEnabled) "ON 🌟" else "OFF 💤",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Parental Lock Zone Card
            PastelCard(
                backgroundColor = Color.White,
                borderColor = PastelPink.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Parent Zone 🔒",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                    Text(
                        text = "Reset app data or empty the database gallery. Secure child-lock prevents accidental deletes.",
                        fontSize = 13.sp,
                        color = TextDark.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    PlayfulButton(
                        onClick = {
                            soundHelper.playPopSound()
                            generateQuestion()
                            showParentalLock = true
                        },
                        backgroundColor = PastelPink.copy(alpha = 0.8f),
                        contentColor = TextDark,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Clear Gallery 🗑️", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // Parental Lock Math Gate Dialog
    if (showParentalLock) {
        AlertDialog(
            onDismissRequest = { showParentalLock = false },
            title = {
                Text(
                    text = "Parents Only! 🦊",
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    fontSize = 22.sp
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Please solve this question to unlock:",
                        fontSize = 14.sp,
                        color = TextDark.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "${mathQuestion.first} + ${mathQuestion.second} = ?",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Bubbly virtual numeric entrypad
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (answerInput.isEmpty()) "Type your answer..." else answerInput,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mathError) Color.Red else if (answerInput.isEmpty()) Color.LightGray else TextDark,
                            modifier = Modifier
                                .background(Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }

                    if (mathError) {
                        Text(
                            text = "Incorrect answer, try again!",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Numeric Grid Buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val keys = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("Clear", "0", "⌫")
                        )

                        keys.forEach { rowKeys ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowKeys.forEach { key ->
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(
                                                color = if (key == "Clear" || key == "⌫") PastelPeach.copy(alpha = 0.3f) else PastelYellow.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            )
                                            .clip(CircleShape)
                                            .clickable {
                                                soundHelper.playPopSound()
                                                when (key) {
                                                    "Clear" -> answerInput = ""
                                                    "⌫" -> {
                                                        if (answerInput.isNotEmpty()) {
                                                            answerInput = answerInput.dropLast(1)
                                                        }
                                                    }
                                                    else -> {
                                                        if (answerInput.length < 3) {
                                                            answerInput += key
                                                        }
                                                    }
                                                }
                                                mathError = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = TextDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                PlayfulButton(
                    onClick = {
                        soundHelper.playPopSound()
                        val correctResult = mathQuestion.first + mathQuestion.second
                        if (answerInput == correctResult.toString()) {
                            viewModel.clearAllGalleryDrawings { success ->
                                if (success) {
                                    Toast.makeText(context, "Gallery wiped clean! 🧹", Toast.LENGTH_SHORT).show()
                                    showParentalLock = false
                                } else {
                                    Toast.makeText(context, "Clear failed 😥", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            mathError = true
                            answerInput = ""
                        }
                    },
                    backgroundColor = PastelMint
                ) {
                    Text("Unlock & Wipe 🗑️", color = TextDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                PlayfulButton(
                    onClick = {
                        soundHelper.playPopSound()
                        showParentalLock = false
                    },
                    backgroundColor = Color.LightGray.copy(alpha = 0.2f)
                ) {
                    Text("Cancel", color = TextDark)
                }
            }
        )
    }
}

@Composable
private fun SettingsHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .shadow(elevation = 3.dp, shape = CircleShape)
                .background(PastelPeach, shape = CircleShape)
        ) {
            Text("⬅️", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Settings ⚙️",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
        )
    }
}
