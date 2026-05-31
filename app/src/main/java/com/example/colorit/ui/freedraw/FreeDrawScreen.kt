package com.example.colorit.ui.freedraw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.data.model.BrushStroke
import com.example.colorit.ui.components.PlayfulButton
import com.example.colorit.ui.theme.PastelBlue
import com.example.colorit.ui.theme.PastelMint
import com.example.colorit.ui.theme.PastelPeach
import com.example.colorit.ui.theme.PastelPink
import com.example.colorit.ui.theme.PastelPurple
import com.example.colorit.ui.theme.PastelYellow
import com.example.colorit.ui.theme.TextDark
import com.example.colorit.util.SoundHelper

@Composable
fun FreeDrawScreen(
    viewModel: FreeDrawViewModel,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strokes by viewModel.strokes.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val selectedTool by viewModel.selectedTool.collectAsState()
    val brushSize by viewModel.brushSize.collectAsState()
    val opacity by viewModel.opacity.collectAsState()

    var activeStrokePoints by remember { mutableStateOf<List<Offset>?>(null) }
    
    // Bounding dimensions for Bitmap caching
    var canvasWidth by remember { mutableStateOf(0) }
    var canvasHeight by remember { mutableStateOf(0) }
    var cachedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Dialog states
    var showSavedDialog by remember { mutableStateOf(false) }
    var savedFilePath by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }

    val canvasBgColor = Color(0xFFFCFBF7)

    // Redraw offscreen Bitmap whenever finalized strokes change or canvas size changes
    LaunchedEffect(strokes, canvasWidth, canvasHeight) {
        if (canvasWidth > 0 && canvasHeight > 0) {
            val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(canvasBgColor.toArgb())

            val paint = Paint().apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }

            for (stroke in strokes) {
                if (stroke.points.isEmpty()) continue
                paint.color = stroke.color.toArgb()
                paint.strokeWidth = stroke.size

                val path = android.graphics.Path()
                val first = stroke.points.first()
                path.moveTo(first.x, first.y)
                
                if (stroke.points.size > 1) {
                    for (i in 1 until stroke.points.size) {
                        val prev = stroke.points[i - 1]
                        val curr = stroke.points[i]
                        val midX = (prev.x + curr.x) / 2
                        val midY = (prev.y + curr.y) / 2
                        if (i == 1) {
                            path.lineTo(midX, midY)
                        } else {
                            path.quadTo(prev.x, prev.y, midX, midY)
                        }
                    }
                    path.lineTo(stroke.points.last().x, stroke.points.last().y)
                } else {
                    path.lineTo(first.x, first.y)
                }
                canvas.drawPath(path, paint)
            }
            cachedBitmap = bitmap
        }
    }

    Scaffold(
        topBar = {
            FreeDrawHeader(
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
                    if (strokes.isNotEmpty()) {
                        soundHelper.playPopSound()
                        showClearConfirm = true
                    }
                },
                onSave = {
                    if (strokes.isEmpty()) {
                        Toast.makeText(context, "Draw something first! 🎨", Toast.LENGTH_SHORT).show()
                    } else {
                        soundHelper.playChimeSound()
                        viewModel.saveToGallery(context.cacheDir, canvasWidth, canvasHeight) { savedFile ->
                            if (savedFile != null) {
                                savedFilePath = savedFile.absolutePath
                                showSavedDialog = true
                            } else {
                                Toast.makeText(context, "Failed to save drawing 😥", Toast.LENGTH_SHORT).show()
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
            // Drawing Workspace
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(MaterialTheme.shapes.large)
                    .background(canvasBgColor)
                    .border(4.dp, PastelBlue.copy(alpha = 0.5f), MaterialTheme.shapes.large)
                    .padding(8.dp)
                    .onSizeChanged { size ->
                        canvasWidth = size.width
                        canvasHeight = size.height
                    }
                    .pointerInput(selectedTool, selectedColor, brushSize, opacity) {
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                activeStrokePoints = listOf(startOffset)
                            },
                            onDragEnd = {
                                activeStrokePoints?.let { points ->
                                    if (points.isNotEmpty()) {
                                        val colorWithOpacity = if (selectedTool == FreeDrawTool.ERASER) {
                                            canvasBgColor
                                        } else {
                                            selectedColor.copy(alpha = opacity)
                                        }
                                        viewModel.addStroke(
                                            BrushStroke(points, colorWithOpacity, brushSize)
                                        )
                                    }
                                }
                                activeStrokePoints = null
                            }
                        ) { change, _ ->
                            change.consume()
                            val currentPoints = activeStrokePoints ?: emptyList()
                            activeStrokePoints = currentPoints + change.position
                        }
                    }
            ) {
                // Drawing Canvas Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // 1. Draw static cached Bitmap background (O(1) rendering)
                    cachedBitmap?.let { bitmap ->
                        drawImage(bitmap.asImageBitmap())
                    }

                    // 2. Draw active local stroke on top in real-time
                    activeStrokePoints?.let { points ->
                        if (points.isNotEmpty()) {
                            val activeColor = if (selectedTool == FreeDrawTool.ERASER) {
                                canvasBgColor
                            } else {
                                selectedColor.copy(alpha = opacity)
                            }

                            val path = androidx.compose.ui.graphics.Path().apply {
                                val first = points.first()
                                moveTo(first.x, first.y)
                                if (points.size > 1) {
                                    for (i in 1 until points.size) {
                                        val prev = points[i - 1]
                                        val curr = points[i]
                                        val mid = Offset((prev.x + curr.x) / 2, (prev.y + curr.y) / 2)
                                        if (i == 1) {
                                            lineTo(mid.x, mid.y)
                                        } else {
                                            quadraticTo(prev.x, prev.y, mid.x, mid.y)
                                        }
                                    }
                                    lineTo(points.last().x, points.last().y)
                                } else {
                                    lineTo(first.x, first.y)
                                }
                            }

                            drawPath(
                                path = path,
                                color = activeColor,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = brushSize,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }

            // Draw controls
            FreeDrawToolbar(
                selectedColor = selectedColor,
                selectedTool = selectedTool,
                brushSize = brushSize,
                opacity = opacity,
                onColorSelected = { viewModel.selectColor(it) },
                onToolSelected = { viewModel.selectTool(it) },
                onBrushSizeChanged = { viewModel.updateBrushSize(it) },
                onOpacityChanged = { viewModel.updateOpacity(it) }
            )
        }
    }

    // Save dialog
    if (showSavedDialog) {
        AlertDialog(
            onDismissRequest = { showSavedDialog = false },
            title = { Text("Drawing Saved! 🎨", fontWeight = FontWeight.Bold) },
            text = { Text("Your masterpiece has been successfully saved to:\n$savedFilePath") },
            confirmButton = {
                PlayfulButton(onClick = { showSavedDialog = false }) {
                    Text("Super!", color = Color.White)
                }
            }
        )
    }

    // Clear confirmation dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Start Over? 🧹", fontWeight = FontWeight.Bold) },
            text = { Text("Do you want to clear your current drawing and start fresh?") },
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
private fun FreeDrawHeader(
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
            text = "Free Draw 🎨",
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

        // Save Button
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
private fun FreeDrawToolbar(
    selectedColor: Color,
    selectedTool: FreeDrawTool,
    brushSize: Float,
    opacity: Float,
    onColorSelected: (Color) -> Unit,
    onToolSelected: (FreeDrawTool) -> Unit,
    onBrushSizeChanged: (Float) -> Unit,
    onOpacityChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        PastelPink, PastelBlue, PastelYellow, PastelMint, PastelPurple, PastelPeach,
        Color(0xFFFFB7B2), Color(0xFFFFDAC1), Color(0xFFE2F0CB), Color(0xFFB5EAD7), Color(0xFFC7CEEA),
        Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Black, Color.White
    )

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
            // Row 1: Tool Selection and Parameter sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tools List
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1.1f)
                ) {
                    items(FreeDrawTool.values()) { tool ->
                        val isSelected = tool == selectedTool
                        val bgColor = when (tool) {
                            FreeDrawTool.PENCIL -> PastelPink
                            FreeDrawTool.MARKER -> PastelBlue
                            FreeDrawTool.BRUSH -> PastelPurple
                            FreeDrawTool.ERASER -> PastelYellow
                        }

                        PlayfulButton(
                            onClick = { onToolSelected(tool) },
                            backgroundColor = if (isSelected) bgColor else Color.LightGray.copy(alpha = 0.2f),
                            contentColor = TextDark,
                            shape = CircleShape,
                            border = null,
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(
                                text = when (tool) {
                                    FreeDrawTool.PENCIL -> "Pencil ✏️"
                                    FreeDrawTool.MARKER -> "Marker 🖍️"
                                    FreeDrawTool.BRUSH -> "Brush 🖌️"
                                    FreeDrawTool.ERASER -> "Eraser 🧼"
                                },
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Adjustment Sliders (Size & Opacity)
                Column(
                    modifier = Modifier.weight(0.9f),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Size
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Size: ", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp), color = TextDark)
                        Slider(
                            value = brushSize,
                            onValueChange = onBrushSizeChanged,
                            valueRange = 2f..60f,
                            modifier = Modifier.height(30.dp)
                        )
                    }

                    // Opacity (only for non-erasers)
                    if (selectedTool != FreeDrawTool.ERASER) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Opacity: ", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp), color = TextDark)
                            Slider(
                                value = opacity,
                                onValueChange = onOpacityChanged,
                                valueRange = 0.1f..1f,
                                modifier = Modifier.height(30.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 2: Color Palette Picker
            if (selectedTool != FreeDrawTool.ERASER) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colors) { color ->
                        val isSelected = color == selectedColor
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 44.dp else 36.dp)
                                .shadow(elevation = if (isSelected) 5.dp else 1.dp, shape = CircleShape)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 4.dp else 1.dp,
                                    color = if (isSelected) TextDark else Color.LightGray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable { onColorSelected(color) }
                        )
                    }
                }
            } else {
                Text(
                    text = "Eraser active - click another tool to restore color palette.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
