package com.example.colorit.ui.stickers

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.data.model.StickerInstance
import com.example.colorit.ui.components.PastelCard
import com.example.colorit.ui.components.PlayfulButton
import com.example.colorit.ui.theme.PastelBlue
import com.example.colorit.ui.theme.PastelMint
import com.example.colorit.ui.theme.PastelPeach
import com.example.colorit.ui.theme.PastelPink
import com.example.colorit.ui.theme.PastelPurple
import com.example.colorit.ui.theme.PastelYellow
import com.example.colorit.ui.theme.TextDark
import com.example.colorit.util.SoundHelper
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun StickersScreen(
    viewModel: StickersViewModel,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stickers by viewModel.stickers.collectAsState()
    val selectedId by viewModel.selectedStickerId.collectAsState()

    var selectedTab by remember { mutableStateOf("Animals 🦊") }
    var canvasWidth by remember { mutableStateOf(0) }
    var canvasHeight by remember { mutableStateOf(0) }

    // Dialog states
    var showSavedDialog by remember { mutableStateOf(false) }
    var savedFilePath by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            StickersHeader(
                canUndo = viewModel.canUndo(),
                canRedo = viewModel.canRedo(),
                onBack = {
                    soundHelper.playPopSound()
                    onBack()
                },
                onUndo = {
                    soundHelper.playPopSound()
                    viewModel.undo()
                },
                onRedo = {
                    soundHelper.playPopSound()
                    viewModel.redo()
                },
                onClearAll = {
                    if (stickers.isNotEmpty()) {
                        soundHelper.playPopSound()
                        showClearConfirm = true
                    }
                },
                onSave = {
                    if (stickers.isEmpty()) {
                        Toast.makeText(context, "Add some stickers first! 🎈", Toast.LENGTH_SHORT).show()
                    } else {
                        soundHelper.playChimeSound()
                        viewModel.saveToGallery(context.cacheDir, canvasWidth, canvasHeight) { savedFile ->
                            if (savedFile != null) {
                                savedFilePath = savedFile.absolutePath
                                showSavedDialog = true
                            } else {
                                Toast.makeText(context, "Failed to save composition 😥", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
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
        ) {
            // Main canvas workspace area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(MaterialTheme.shapes.large)
                    .background(Color(0xFFFCFBF7))
                    .border(4.dp, PastelMint.copy(alpha = 0.5f), MaterialTheme.shapes.large)
                    .padding(8.dp)
                    .onSizeChanged { size ->
                        canvasWidth = size.width
                        canvasHeight = size.height
                    }
                    .clickable { viewModel.selectSticker(null) } // Tap empty space to deselect
            ) {
                // Draw added stickers
                stickers.forEach { sticker ->
                    val isSelected = sticker.id == selectedId
                    StickerComponent(
                        sticker = sticker,
                        isSelected = isSelected,
                        onSelect = {
                            viewModel.selectSticker(sticker.id)
                        },
                        onDelete = {
                            viewModel.deleteSticker(sticker.id)
                        },
                        onTransformEnd = {
                            viewModel.saveHistoryState() // Save historical action snapshot
                        },
                        onUpdate = { pos, scale, rot ->
                            viewModel.updateSticker(sticker.id, pos, scale, rot)
                        }
                    )
                }
            }

            // Bottom drawer: categorised stickers
            StickerDrawer(
                categories = viewModel.stickerCategories,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onStickerSelected = { emoji ->
                    soundHelper.playPopSound()
                    // Center sticker in canvas
                    viewModel.addSticker(emoji)
                    // Place sticker at center
                    viewModel.updateSticker(
                        id = stickers.lastOrNull()?.id ?: "",
                        position = Offset(canvasWidth / 2f, canvasHeight / 2f),
                        scale = 1.0f,
                        rotation = 0f
                    )
                }
            )
        }
    }

    // Save dialog
    if (showSavedDialog) {
        AlertDialog(
            onDismissRequest = { showSavedDialog = false },
            title = { Text("Composition Saved! 🎨", fontWeight = FontWeight.Bold) },
            text = { Text("Your stickers masterpiece has been saved to:\n$savedFilePath") },
            confirmButton = {
                PlayfulButton(onClick = { showSavedDialog = false }) {
                    Text("Super!", color = Color.White)
                }
            }
        )
    }

    // Clear confirmation
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Start Over? 🧹", fontWeight = FontWeight.Bold) },
            text = { Text("Clear all stickers from the workspace?") },
            confirmButton = {
                PlayfulButton(
                    onClick = {
                        soundHelper.playPopSound()
                        viewModel.clearAll()
                        showClearConfirm = false
                    },
                    backgroundColor = PastelPink
                ) {
                    Text("Yes, Clear!", color = TextDark)
                }
            },
            dismissButton = {
                PlayfulButton(
                    onClick = {
                        soundHelper.playPopSound()
                        showClearConfirm = false
                    },
                    backgroundColor = Color.LightGray.copy(alpha = 0.2f)
                ) {
                    Text("No", color = TextDark)
                }
            }
        )
    }
}

@Composable
private fun StickerComponent(
    sticker: StickerInstance,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onTransformEnd: () -> Unit,
    onUpdate: (Offset, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Math reference values for calculations
    val baseHandleDistance = 80f // Anchor radius 80 pixels

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    sticker.position.x.roundToInt() - 50.dp.roundToPx(),
                    sticker.position.y.roundToInt() - 50.dp.roundToPx()
                )
            }
            .size(100.dp)
            .graphicsLayer(
                scaleX = sticker.scale,
                scaleY = sticker.scale,
                rotationZ = sticker.rotation
            )
            .drawBehind {
                if (isSelected) {
                    // Draw a dashed selection frame around sticker
                    drawRect(
                        color = PastelPink,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        )
                    )
                }
            }
            .pointerInput(Unit) {
                // Drag sticker
                detectDragGestures(
                    onDragStart = { onSelect() },
                    onDragEnd = { onTransformEnd() }
                ) { change, dragAmount ->
                    change.consume()
                    onUpdate(sticker.position + dragAmount, sticker.scale, sticker.rotation)
                }
            }
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        // Drawing Sticker Text Emoji
        Text(
            text = sticker.emoji,
            fontSize = 58.sp,
            modifier = Modifier.padding(12.dp)
        )

        // Overlay Action Handles
        if (isSelected) {
            // 1. Delete button bubble (Top-Right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(10.dp, (-10).dp)
                    .size(24.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .background(PastelPink, shape = CircleShape)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Text("❌", fontSize = 11.sp)
            }

            // 2. Transform handle (Bottom-Right) (Drag to scale and rotate)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(10.dp, 10.dp)
                    .size(26.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .background(PastelBlue, shape = CircleShape)
                    .pointerInput(sticker.position) {
                        detectDragGestures(
                            onDragEnd = { onTransformEnd() }
                        ) { change, _ ->
                            change.consume()
                            
                            val touchPos = change.position
                            // Center of box relative to drag coordinates is (width/2, height/2) in local space
                            // Transform handle dragging relative to sticker position center
                            val dx = touchPos.x
                            val dy = touchPos.y
                            val distance = sqrt(dx * dx + dy * dy)
                            
                            // Scale factor
                            val calculatedScale = (distance / baseHandleDistance).coerceIn(0.5f, 3.5f)
                            // Rotation angle relative to center anchor
                            val angleRad = atan2(dy, dx)
                            val calculatedRotation = Math.toDegrees(angleRad.toDouble()).toFloat() - 45f // Adjust base handle offset
                            
                            onUpdate(sticker.position, calculatedScale, calculatedRotation)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("🔄", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun StickersHeader(
    canUndo: Boolean,
    canRedo: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClearAll: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
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
            text = "Sticker Mode 🧸",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            ),
            modifier = Modifier.weight(1f)
        )

        // Clear Trash
        IconButton(
            onClick = onClearAll,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(40.dp)
                .background(PastelPink.copy(alpha = 0.2f), shape = CircleShape)
        ) {
            Text("🗑️", fontSize = 16.sp)
        }

        // Undo
        IconButton(
            onClick = onUndo,
            enabled = canUndo,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(40.dp)
                .background(if (canUndo) PastelYellow else Color.LightGray.copy(alpha = 0.3f), shape = CircleShape)
        ) {
            Text("↩️", fontSize = 16.sp)
        }

        // Redo
        IconButton(
            onClick = onRedo,
            enabled = canRedo,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(40.dp)
                .background(if (canRedo) PastelYellow else Color.LightGray.copy(alpha = 0.3f), shape = CircleShape)
        ) {
            Text("↪️", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Save
        PlayfulButton(
            onClick = onSave,
            backgroundColor = PastelMint,
            contentColor = TextDark,
            modifier = Modifier.height(40.dp)
        ) {
            Text("Save 💾", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StickerDrawer(
    categories: Map<String, List<String>>,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    onStickerSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(12.dp, shape = MaterialTheme.shapes.extraLarge)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Tab row selections
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories.keys.toList()) { category ->
                    val isSelected = category == selectedTab
                    val bgColor = when (category) {
                        "Animals 🦊" -> PastelPink
                        "Emojis 🎈" -> PastelBlue
                        else -> PastelYellow
                    }

                    Box(
                        modifier = Modifier
                            .shadow(elevation = if (isSelected) 4.dp else 1.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(if (isSelected) bgColor else Color.LightGray.copy(alpha = 0.15f))
                            .clickable { onTabSelected(category) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sticker Emojis Grid
            val stickers = categories[selectedTab] ?: emptyList()
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 48.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                items(stickers) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.LightGray.copy(alpha = 0.15f), shape = MaterialTheme.shapes.medium)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onStickerSelected(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 28.sp)
                    }
                }
            }
        }
    }
}
