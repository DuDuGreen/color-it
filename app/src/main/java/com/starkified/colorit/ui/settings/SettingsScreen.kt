package com.starkified.colorit.ui.settings

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starkified.colorit.ui.components.*
import com.starkified.colorit.ui.theme.*
import com.starkified.colorit.util.SoundHelper
import kotlinx.coroutines.launch

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

    // Staggered pop-in animations for setting cards
    val soundCardScale = remember { Animatable(0f) }
    val musicCardScale = remember { Animatable(0f) }
    val parentCardScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            soundCardScale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow))
        }
        kotlinx.coroutines.delay(60)
        launch {
            musicCardScale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow))
        }
        kotlinx.coroutines.delay(60)
        launch {
            parentCardScale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow))
        }
    }

    // Wrap the entire screen in the interactive BubbleBackground
    BubbleBackground(modifier = modifier) {
        Scaffold(
            topBar = {
                SettingsHeader(
                    onBack = {
                        soundHelper.playPopSound()
                        onBack()
                    }
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BannerAd()
                }
            },
            containerColor = Color.Transparent // Allow BubbleBackground to show through
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
                    color = TextDarkGreen,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sound Card
                PastelCard(
                    backgroundColor = CardYellow,
                    borderColor = CountryOutline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = soundCardScale.value
                            scaleY = soundCardScale.value
                        }
                        .border(3.dp, CountryOutline, RoundedCornerShape(24.dp))
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
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkGreen
                            )
                            Text(
                                text = "Play bubbly noises on taps",
                                fontSize = 13.sp,
                                color = TextLightGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        PlayfulButton(
                            onClick = {
                                viewModel.toggleSound()
                                soundHelper.playPopSound()
                            },
                            backgroundColor = if (isSoundEnabled) CountryGrass else Color.White,
                            contentColor = TextDarkGreen,
                            shape = CircleShape,
                            border = BorderStroke(2.dp, CountryOutline),
                            modifier = Modifier
                                .width(90.dp)
                                .height(44.dp)
                        ) {
                            Text(
                                text = if (isSoundEnabled) "ON 🌟" else "OFF 💤",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Music Card
                PastelCard(
                    backgroundColor = CardYellow,
                    borderColor = CountryOutline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = musicCardScale.value
                            scaleY = musicCardScale.value
                        }
                        .border(3.dp, CountryOutline, RoundedCornerShape(24.dp))
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
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkGreen
                            )
                            Text(
                                text = "Soft melodies in menus",
                                fontSize = 13.sp,
                                color = TextLightGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        PlayfulButton(
                            onClick = {
                                viewModel.toggleMusic()
                                soundHelper.playPopSound()
                            },
                            backgroundColor = if (isMusicEnabled) CountryGrass else Color.White,
                            contentColor = TextDarkGreen,
                            shape = CircleShape,
                            border = BorderStroke(2.dp, CountryOutline),
                            modifier = Modifier
                                .width(90.dp)
                                .height(44.dp)
                        ) {
                            Text(
                                text = if (isMusicEnabled) "ON 🌟" else "OFF 💤",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Parental Lock Zone Card
                PastelCard(
                    backgroundColor = CardYellow,
                    borderColor = CountryOutline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = parentCardScale.value
                            scaleY = parentCardScale.value
                        }
                        .border(3.dp, CountryOutline, RoundedCornerShape(24.dp))
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
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkGreen
                            )
                        }
                        Text(
                            text = "Reset app data or empty the database gallery. Secure child-lock prevents accidental deletes.",
                            fontSize = 13.sp,
                            color = TextLightGreen,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        PlayfulButton(
                            onClick = {
                                soundHelper.playPopSound()
                                generateQuestion()
                                showParentalLock = true
                            },
                            backgroundColor = ButtonOrange,
                            contentColor = Color.White,
                            border = BorderStroke(2.dp, CountryOutline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CozyTrashIcon(modifier = Modifier.size(16.dp), color = Color.White)
                                Text("Clear Gallery", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Parental Lock Math Gate Dialog
    if (showParentalLock) {
        AlertDialog(
            onDismissRequest = { showParentalLock = false },
            modifier = Modifier.border(3.dp, CountryOutline, MaterialTheme.shapes.large),
            shape = MaterialTheme.shapes.large,
            containerColor = CardYellow,
            titleContentColor = TextDark,
            textContentColor = TextDark.copy(alpha = 0.8f),
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
                        color = TextDark.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (answerInput.isEmpty()) "Type your answer..." else answerInput,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mathError) Color.Red else if (answerInput.isEmpty()) Color.LightGray else TextDark,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(2.dp, CountryOutline, RoundedCornerShape(12.dp))
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
                                    val isClearDelete = key == "Clear" || key == "⌫"
                                    val keyBgColor = if (isClearDelete) ButtonOrange.copy(alpha = 0.2f) else Color.White
                                    val keyBorder = if (isClearDelete) ButtonOrange else CountryOutline
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(keyBgColor, shape = CircleShape)
                                            .border(2.dp, keyBorder, CircleShape)
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
                        val correctResult = mathQuestion.first + mathQuestion.second
                        if (answerInput == correctResult.toString()) {
                            soundHelper.playSuccessSound()
                            viewModel.clearAllGalleryDrawings { success ->
                                if (success) {
                                    Toast.makeText(context, "Gallery wiped clean! 🧹", Toast.LENGTH_SHORT).show()
                                    showParentalLock = false
                                } else {
                                    Toast.makeText(context, "Clear failed 😥", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            soundHelper.playErrorSound()
                            mathError = true
                            answerInput = ""
                        }
                    },
                    backgroundColor = CountryGrass,
                    border = BorderStroke(2.dp, CountryOutline)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CozyTrashIcon(modifier = Modifier.size(16.dp), color = TextDarkGreen)
                        Text("Unlock & Wipe", color = TextDarkGreen, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                PlayfulButton(
                    onClick = {
                        soundHelper.playPopSound()
                        showParentalLock = false
                    },
                    backgroundColor = Color.White,
                    border = BorderStroke(2.dp, CountryOutline)
                ) {
                    Text("Cancel", color = TextDarkGreen)
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
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayfulIconButton(
            onClick = onBack,
            backgroundColor = Color.White,
            contentColor = TextDarkGreen,
            modifier = Modifier.size(44.dp)
        ) {
            CozyBackIcon(modifier = Modifier.size(20.dp), color = TextDarkGreen)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Settings ⚙️",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDarkGreen
            )
        )
    }
}
