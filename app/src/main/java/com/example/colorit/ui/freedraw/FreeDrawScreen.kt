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
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.example.colorit.ui.theme.AppColorSpectrum
import com.example.colorit.ui.theme.CountryOutline
import com.example.colorit.ui.theme.CardYellow
import androidx.compose.ui.graphics.Brush
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
    var showSpectrumDialog by remember { mutableStateOf(false) }

    val canvasBgColor = CozyCreamBackground

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
            // Drawing Workspace
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(MaterialTheme.shapes.large)
                    .background(canvasBgColor)
                    .border(4.dp, CozyBlush.copy(alpha = 0.5f), MaterialTheme.shapes.large)
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
                                        val isStraightLine = selectedTool == FreeDrawTool.STRAIGHT_LINE
                                        viewModel.addStroke(
                                            BrushStroke(
                                                points = if (isStraightLine) listOf(points.first(), points.last()) else points,
                                                color = colorWithOpacity,
                                                size = brushSize,
                                                isStraightLine = isStraightLine
                                            )
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

                            if (selectedTool == FreeDrawTool.STRAIGHT_LINE && points.size >= 2) {
                                // Straight line preview: just draw from first to current
                                drawLine(
                                    color = activeColor,
                                    start = points.first(),
                                    end = points.last(),
                                    strokeWidth = brushSize,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            } else {
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
                onOpacityChanged = { viewModel.updateOpacity(it) },
                onSpectrumClick = { showSpectrumDialog = true }
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
            title = { Text("Drawing Saved! 🎨", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Your masterpiece has been successfully saved to:\n$savedFilePath", fontWeight = FontWeight.Medium) },
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
            modifier = Modifier.border(3.dp, CountryOutline, MaterialTheme.shapes.large),
            shape = MaterialTheme.shapes.large,
            containerColor = CardYellow,
            titleContentColor = TextDark,
            textContentColor = TextDark.copy(alpha = 0.8f),
            title = { Text("Start Over? 🧹", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Do you want to clear your current drawing and start fresh?", fontWeight = FontWeight.Medium) },
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

    if (showSpectrumDialog) {
        ColorSpectrumDialog(
            initialColor = selectedColor,
            onColorSelected = { viewModel.selectColor(it) },
            onDismiss = { showSpectrumDialog = false }
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
                text = "Free Draw 🎨",
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

                // Save Button
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
private fun FreeDrawToolbar(
    selectedColor: Color,
    selectedTool: FreeDrawTool,
    brushSize: Float,
    opacity: Float,
    onColorSelected: (Color) -> Unit,
    onToolSelected: (FreeDrawTool) -> Unit,
    onBrushSizeChanged: (Float) -> Unit,
    onOpacityChanged: (Float) -> Unit,
    onSpectrumClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = remember { AppColorSpectrum }

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
            // Row 1: Spectrum Selector (placed a bit higher)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Custom Color:",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                )

                PlayfulButton(
                    onClick = onSpectrumClick,
                    backgroundColor = PastelPurple,
                    contentColor = TextDark,
                    shape = CircleShape,
                    border = null,
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        CozySpectrumIcon(modifier = Modifier.size(16.dp))
                        Text("Spectrum", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Tools List (taking full width)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(FreeDrawTool.values()) { tool ->
                    val isSelected = tool == selectedTool
                    val bgColor = when (tool) {
                        FreeDrawTool.PENCIL -> CozyRose
                        FreeDrawTool.MARKER -> CozyBlush
                        FreeDrawTool.BRUSH -> PastelPurple
                        FreeDrawTool.ERASER -> CozyBlushLight
                        FreeDrawTool.STRAIGHT_LINE -> PastelBlue
                    }

                    PlayfulButton(
                        onClick = { onToolSelected(tool) },
                        backgroundColor = if (isSelected) bgColor else Color.LightGray.copy(alpha = 0.15f),
                        contentColor = if (isSelected && tool == FreeDrawTool.PENCIL) Color.White else TextDark,
                        shape = CircleShape,
                        border = null,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            when (tool) {
                                FreeDrawTool.PENCIL -> {
                                    CozyPencilIcon(modifier = Modifier.size(15.dp), color = if (isSelected) Color.White else TextDark)
                                    Text("Pencil", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                FreeDrawTool.MARKER -> {
                                    CozyMarkerIcon(modifier = Modifier.size(15.dp), color = TextDark)
                                    Text("Marker", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                FreeDrawTool.BRUSH -> {
                                    CozyBrushIcon(modifier = Modifier.size(15.dp), color = TextDark)
                                    Text("Brush", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                FreeDrawTool.ERASER -> {
                                    CozyEraserIcon(modifier = Modifier.size(15.dp), color = TextDark)
                                    Text("Eraser", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                FreeDrawTool.STRAIGHT_LINE -> {
                                    CozyLineIcon(modifier = Modifier.size(15.dp), color = if (isSelected) Color.White else TextDark)
                                    Text("Line", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Adjustment Sliders (stacked vertically for readability on mobile)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Size
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Size: ", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = TextDark, modifier = Modifier.width(55.dp))
                    Slider(
                        value = brushSize,
                        onValueChange = onBrushSizeChanged,
                        valueRange = 2f..60f,
                        modifier = Modifier.weight(1f).height(30.dp)
                    )
                }

                // Opacity (only for non-erasers)
                if (selectedTool != FreeDrawTool.ERASER) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Opacity: ", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = TextDark, modifier = Modifier.width(55.dp))
                        Slider(
                            value = opacity,
                            onValueChange = onOpacityChanged,
                            valueRange = 0.1f..1f,
                            modifier = Modifier.weight(1f).height(30.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Color Palette Picker
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
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
