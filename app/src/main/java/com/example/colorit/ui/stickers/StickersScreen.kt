package com.example.colorit.ui.stickers

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.colorit.ui.components.*
import com.example.colorit.ui.theme.PastelBlue
import com.example.colorit.ui.theme.PastelMint
import com.example.colorit.ui.theme.PastelPeach
import com.example.colorit.ui.theme.PastelPink
import com.example.colorit.ui.theme.PastelPurple
import com.example.colorit.ui.theme.PastelYellow
import com.example.colorit.ui.theme.TextDark
import com.example.colorit.ui.theme.CozyRose
import com.example.colorit.ui.theme.CozyBlush
import com.example.colorit.ui.theme.CozyBlushLight
import com.example.colorit.ui.theme.CozyCreamBackground
import androidx.compose.ui.graphics.Brush
import com.example.colorit.ui.theme.CountryOutline
import com.example.colorit.ui.theme.CardYellow
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
        containerColor = Color.Transparent,
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CozyCreamBackground,
                        CozyBlushLight.copy(alpha = 0.5f)
                    )
                )
            )
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
                    .background(CozyCreamBackground)
                    .border(4.dp, CozyRose.copy(alpha = 0.5f), MaterialTheme.shapes.large)
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
                    viewModel.addSticker(emoji, Offset(canvasWidth / 2f, canvasHeight / 2f))
                }
            )
        }
    }

    // Save dialog
    if (showSavedDialog) {
        AlertDialog(
            onDismissRequest = { showSavedDialog = false },
            modifier = Modifier.border(3.dp, CountryOutline, MaterialTheme.shapes.large),
            shape = MaterialTheme.shapes.large,
            containerColor = CardYellow,
            titleContentColor = TextDark,
            textContentColor = TextDark.copy(alpha = 0.8f),
            title = { Text("Composition Saved! 🎨", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Your stickers masterpiece has been saved to:\n$savedFilePath", fontWeight = FontWeight.Medium) },
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
            modifier = Modifier.border(3.dp, CountryOutline, MaterialTheme.shapes.large),
            shape = MaterialTheme.shapes.large,
            containerColor = CardYellow,
            titleContentColor = TextDark,
            textContentColor = TextDark.copy(alpha = 0.8f),
            title = { Text("Start Over? 🧹", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Clear all stickers from the workspace?", fontWeight = FontWeight.Medium) },
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
    // Keep reference to the latest sticker properties to avoid stale state capture inside pointerInput
    val currentSticker by rememberUpdatedState(sticker)

    // Math reference values for calculations
    val baseHandleDistance = 80f // Anchor radius 80 pixels
    var currentHandleOffset by remember { mutableStateOf(Offset.Zero) }
    var dragStartStickerPos by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    sticker.position.x.roundToInt() - 60.dp.roundToPx(),
                    sticker.position.y.roundToInt() - 60.dp.roundToPx()
                )
            }
            .size(120.dp)
            .pointerInput(sticker.id) {
                detectDragGestures(
                    onDragStart = {
                        dragStartStickerPos = currentSticker.position
                        onSelect()
                    },
                    onDragEnd = { onTransformEnd() }
                ) { change, dragAmount ->
                    change.consume()
                    dragStartStickerPos = dragStartStickerPos + dragAmount
                    onUpdate(dragStartStickerPos, currentSticker.scale, currentSticker.rotation)
                }
            }
            .graphicsLayer(
                scaleX = sticker.scale,
                scaleY = sticker.scale,
                rotationZ = sticker.rotation
            )
            .drawBehind {
                if (isSelected) {
                    // Draw a dashed selection frame around the inner 100.dp sticker area
                    val padding = 10.dp.toPx()
                    drawRect(
                        color = CozyRose,
                        topLeft = Offset(padding, padding),
                        size = Size(size.width - 2 * padding, size.height - 2 * padding),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        )
                    )
                }
            },
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
                    .offset((-4).dp, 4.dp)
                    .size(24.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .background(CozyBlush, shape = CircleShape)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Text("❌", fontSize = 11.sp)
            }

            // 2. Transform handle (Bottom-Right) (Drag to scale and rotate)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset((-4).dp, (-4).dp)
                    .size(26.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .background(CozyBlush, shape = CircleShape)
                    .pointerInput(sticker.id) {
                        detectDragGestures(
                            onDragStart = {
                                val initialDistance = baseHandleDistance * currentSticker.scale
                                val initialAngleRad = Math.toRadians((currentSticker.rotation + 45f).toDouble())
                                currentHandleOffset = Offset(
                                    (initialDistance * kotlin.math.cos(initialAngleRad)).toFloat(),
                                    (initialDistance * kotlin.math.sin(initialAngleRad)).toFloat()
                                )
                            },
                            onDragEnd = { onTransformEnd() }
                        ) { change, dragAmount ->
                            change.consume()
                            
                            // Rotate local dragAmount to parent space and scale it
                            val parentDragAmount = dragAmount.rotateBy(currentSticker.rotation) * currentSticker.scale
                            currentHandleOffset += parentDragAmount
                            val newDistance = currentHandleOffset.getDistance()
                            
                            // Scale factor
                            val calculatedScale = (newDistance / baseHandleDistance).coerceIn(0.5f, 3.5f)
                            // Rotation angle relative to center anchor
                            val newAngleRad = atan2(currentHandleOffset.y, currentHandleOffset.x)
                            val calculatedRotation = Math.toDegrees(newAngleRad.toDouble()).toFloat() - 45f // Adjust base handle offset
                            
                            onUpdate(currentSticker.position, calculatedScale, calculatedRotation)
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Row 1: Back Button + Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayfulIconButton(
                onClick = onBack,
                backgroundColor = PastelPeach,
                modifier = Modifier.size(44.dp)
            ) {
                CozyBackIcon(modifier = Modifier.size(20.dp), color = TextDark)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "Sticker Mode 🧸",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: Actions (Trash on the left, Undo + Redo + Save on the right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clear Trash
            PlayfulIconButton(
                onClick = onClearAll,
                backgroundColor = PastelPink.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            ) {
                CozyTrashIcon(modifier = Modifier.size(18.dp), color = TextDark)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Undo
                PlayfulButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    backgroundColor = PastelYellow,
                    contentColor = TextDark,
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Undo", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Redo
                PlayfulButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    backgroundColor = PastelYellow,
                    contentColor = TextDark,
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Redo", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Save
                PlayfulButton(
                    onClick = onSave,
                    backgroundColor = PastelMint,
                    contentColor = TextDark,
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        CozySaveIcon(modifier = Modifier.size(16.dp), color = TextDark)
                        Text("Save", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
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
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(12.dp, shape = MaterialTheme.shapes.extraLarge)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories.keys.toList()) { category ->
                    val isSelected = category == selectedTab
                    val bgColor = if (isSelected) CozyRose else Color.LightGray.copy(alpha = 0.2f)
                    val textColor = if (isSelected) Color.White else TextDark.copy(alpha = 0.8f)
                    val borderModifier = if (isSelected) {
                        Modifier.border(2.dp, TextDark, CircleShape)
                    } else {
                        Modifier.border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
                    }

                    Box(
                        modifier = Modifier
                            .then(borderModifier)
                            .shadow(elevation = if (isSelected) 4.dp else 0.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(bgColor)
                            .clickable { onTabSelected(category) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                color = textColor,
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
                columns = GridCells.Adaptive(minSize = 44.dp),
                contentPadding = PaddingValues(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                items(stickers) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.LightGray.copy(alpha = 0.15f), shape = MaterialTheme.shapes.medium)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onStickerSelected(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

private fun Offset.rotateBy(degrees: Float): Offset {
    val rad = Math.toRadians(degrees.toDouble())
    val cos = kotlin.math.cos(rad).toFloat()
    val sin = kotlin.math.sin(rad).toFloat()
    return Offset(
        x * cos - y * sin,
        x * sin + y * cos
    )
}
