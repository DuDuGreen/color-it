package com.example.colorit.ui.gallery

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.colorit.data.database.SavedDrawing
import com.example.colorit.ui.components.*
import com.example.colorit.ui.theme.*
import com.example.colorit.util.SoundHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawings by viewModel.savedDrawings.collectAsState()

    var previewDrawing by remember { mutableStateOf<SavedDrawing?>(null) }
    var deleteDrawingTarget by remember { mutableStateOf<SavedDrawing?>(null) }

    // Wrap the entire screen in the interactive BubbleBackground
    BubbleBackground(modifier = modifier) {
        Scaffold(
            topBar = {
                GalleryHeader(
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
            ) {
                if (drawings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🖼️", fontSize = 72.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Your Gallery is Empty!",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextDarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Start coloring or drawing to see your masterpieces here!",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextLightGreen,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(drawings, key = { _, drawing -> drawing.id }) { index, drawing ->
                            DrawingCard(
                                drawing = drawing,
                                onClick = {
                                    soundHelper.playPopSound()
                                    previewDrawing = drawing
                                },
                                onShare = {
                                    soundHelper.playPopSound()
                                    val file = File(drawing.filePath)
                                    if (file.exists()) {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "image/png"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share Drawing"))
                                    }
                                },
                                onDelete = {
                                    soundHelper.playPopSound()
                                    deleteDrawingTarget = drawing
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 1. Full-screen Preview Dialog
    previewDrawing?.let { drawing ->
        val bitmap by produceState<Bitmap?>(initialValue = null) {
            value = withContext(Dispatchers.IO) {
                BitmapFactory.decodeFile(drawing.filePath)
            }
        }

        AlertDialog(
            onDismissRequest = { previewDrawing = null },
            modifier = Modifier.border(3.dp, CountryOutline, MaterialTheme.shapes.large),
            shape = MaterialTheme.shapes.large,
            containerColor = CardYellow,
            titleContentColor = TextDark,
            textContentColor = TextDark.copy(alpha = 0.8f),
            title = {
                Text(
                    text = drawing.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = TextDark
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFCFBF7))
                        .border(3.dp, CountryOutline, RoundedCornerShape(16.dp))
                ) {
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = drawing.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: Text(
                        text = "Loading...",
                        modifier = Modifier.align(Alignment.Center),
                        color = TextDark.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Delete Button
                    PlayfulButton(
                        onClick = {
                            soundHelper.playPopSound()
                            deleteDrawingTarget = drawing
                        },
                        backgroundColor = PastelPink,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }

                    // Share Button
                    PlayfulButton(
                        onClick = {
                            soundHelper.playPopSound()
                            val file = File(drawing.filePath)
                            if (file.exists()) {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Drawing"))
                            }
                        },
                        backgroundColor = PastelBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }

                    // Close Button
                    PlayfulButton(
                        onClick = {
                            soundHelper.playPopSound()
                            previewDrawing = null
                        },
                        backgroundColor = Color.White,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }
            }
        )
    }

    // 2. Delete Confirmation Dialog with Parental Math Gate
    deleteDrawingTarget?.let { drawing ->
        var mathQuestion by remember(drawing.id) {
            val num1 = (5..15).random()
            val num2 = (3..9).random()
            mutableStateOf(Pair(num1, num2))
        }
        var answerInput by remember(drawing.id) { mutableStateOf("") }
        var mathError by remember(drawing.id) { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { deleteDrawingTarget = null },
            modifier = Modifier.border(3.dp, CountryOutline, MaterialTheme.shapes.large),
            shape = MaterialTheme.shapes.large,
            containerColor = CardYellow,
            titleContentColor = TextDark,
            textContentColor = TextDark.copy(alpha = 0.8f),
            title = { Text("Parents Only! 🦊", fontWeight = FontWeight.ExtraBold, color = TextDark) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Solve this question to permanently delete \"${drawing.title}\":",
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
                            text = if (answerInput.isEmpty()) "Type answer..." else answerInput,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mathError) Color.Red else if (answerInput.isEmpty()) Color.LightGray else TextDark,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(2.dp, CountryOutline, RoundedCornerShape(12.dp))
                                .padding(horizontal = 20.dp, vertical = 10.dp)
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
                                            .size(50.dp)
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
                            viewModel.deleteDrawing(drawing)
                            deleteDrawingTarget = null
                            previewDrawing = null
                        } else {
                            soundHelper.playErrorSound()
                            mathError = true
                            answerInput = ""
                        }
                    },
                    backgroundColor = CountrySky,
                    border = BorderStroke(2.dp, CountryOutline)
                ) {
                    Text("Delete Drawing", color = TextDarkGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                PlayfulButton(
                    onClick = {
                        soundHelper.playPopSound()
                        deleteDrawingTarget = null
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
private fun GalleryHeader(
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
            text = "My Gallery 🖼️",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDarkGreen
            )
        )
    }
}

@Composable
private fun DrawingCard(
    drawing: SavedDrawing,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "DrawingCardPress"
    )

    PastelCard(
        backgroundColor = CardYellow,
        borderColor = CountryOutline,
        shadowElevation = if (isPressed) 2.dp else 6.dp,
        contentPadding = 0.dp,
        modifier = modifier
            .aspectRatio(0.9f)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .border(3.dp, CountryOutline, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Thumbnail image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
                    .border(3.dp, CountryOutline, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                        .data(drawing.filePath)
                        .size(200)
                        .crossfade(true)
                        .build(),
                    contentDescription = drawing.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Action Buttons
                // Top-Left: Share
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(PastelBlue.copy(alpha = 0.9f), shape = CircleShape)
                        .border(2.dp, CountryOutline, CircleShape)
                        .clickable { onShare() },
                    contentAlignment = Alignment.Center
                ) {
                    CozyShareIcon(modifier = Modifier.size(15.dp), color = CountryOutline)
                }

                // Top-Right: Delete
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(PastelPink.copy(alpha = 0.9f), shape = CircleShape)
                        .border(2.dp, CountryOutline, CircleShape)
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    CozyTrashIcon(modifier = Modifier.size(15.dp), color = CountryOutline)
                }
            }

            // Title and action buttons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = drawing.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = TextDarkGreen,
                    maxLines = 1
                )
            }
        }
    }
}
