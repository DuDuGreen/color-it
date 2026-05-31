package com.example.colorit.ui.coloring

import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Region
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.PathParser
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
import java.io.File
import kotlin.math.min

@Composable
fun ColoringScreen(
    viewModel: ColoringViewModel,
    soundHelper: SoundHelper,
    pageId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val page by viewModel.page.collectAsState()
    val shapeColors by viewModel.shapeColors.collectAsState()
    val brushStrokes by viewModel.brushStrokes.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val selectedTool by viewModel.selectedTool.collectAsState()
    val brushSize by viewModel.brushSize.collectAsState()

    // Screen scale and pan values
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Active brush stroke local drawing state
    var activeStrokePoints by remember { mutableStateOf<List<Offset>?>(null) }

    // Save dialog state
    var showSavedDialog by remember { mutableStateOf(false) }
    var savedFilePath by remember { mutableStateOf("") }

    LaunchedEffect(pageId) {
        viewModel.loadPage(pageId)
    }

    Scaffold(
        topBar = {
            ColoringHeader(
                title = page?.title ?: "Color It",
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
                onSave = {
                    soundHelper.playChimeSound()
                    viewModel.saveToGallery(context.cacheDir) { savedFile ->
                        if (savedFile != null) {
                            savedFilePath = savedFile.absolutePath
                            showSavedDialog = true
                        } else {
                            Toast.makeText(context, "Failed to save drawing 😥", Toast.LENGTH_SHORT).show()
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
            // Interactive drawing canvas area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(MaterialTheme.shapes.large)
                    .background(Color(0xFFFCFBF7)) // Bubbly off-white background
                    .border(4.dp, PastelPeach.copy(alpha = 0.5f), MaterialTheme.shapes.large)
                    .padding(8.dp)
                    // Manage multi-touch gestures (zoom & pan)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offset += pan
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    }
                    // Tap to Fill or Drag to Draw
                    .pointerInput(selectedTool) {
                        if (selectedTool == ColoringTool.FILL) {
                            detectTapGestures { touchOffset ->
                                val fitScale = min(size.width, size.height) / 200f
                                // Scale and transform coordinate to original 200x200 design grid
                                val canvasX = ((touchOffset.x - offset.x) / scale) / fitScale
                                val canvasY = ((touchOffset.y - offset.y) / scale) / fitScale

                                page?.let { currentPage ->
                                    // Detect which path contains touch
                                    for (shape in currentPage.shapes.reversed()) {
                                        val path = PathParser.createPathFromPathData(shape.pathData)
                                        val bounds = RectF()
                                        path.computeBounds(bounds, true)

                                        val region = Region()
                                        region.setPath(
                                            path,
                                            Region(
                                                bounds.left.toInt(),
                                                bounds.top.toInt(),
                                                bounds.right.toInt(),
                                                bounds.bottom.toInt()
                                            )
                                        )

                                        if (region.contains(canvasX.toInt(), canvasY.toInt())) {
                                            viewModel.fillShape(shape.id)
                                            break
                                        }
                                    }
                                }
                            }
                        } else {
                            // Brush Mode
                            detectDragGestures(
                                onDragStart = { startOffset ->
                                    val fitScale = min(size.width, size.height) / 200f
                                    val canvasX = ((startOffset.x - offset.x) / scale) / fitScale
                                    val canvasY = ((startOffset.y - offset.y) / scale) / fitScale
                                    activeStrokePoints = listOf(Offset(canvasX, canvasY))
                                },
                                onDragEnd = {
                                    activeStrokePoints?.let { points ->
                                        if (points.isNotEmpty()) {
                                            viewModel.addBrushStroke(
                                                BrushStroke(points, selectedColor, brushSize)
                                            )
                                        }
                                    }
                                    activeStrokePoints = null
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                val currentPoints = activeStrokePoints ?: emptyList()
                                val fitScale = min(size.width, size.height) / 200f
                                val canvasX = ((change.position.x - offset.x) / scale) / fitScale
                                val canvasY = ((change.position.y - offset.y) / scale) / fitScale
                                activeStrokePoints = currentPoints + Offset(canvasX, canvasY)
                            }
                        }
                    }
            ) {
                // Drawing Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                ) {
                    val fitScale = min(size.width, size.height) / 200f

                    drawIntoCanvas { canvas ->
                        val nativeCanvas = canvas.nativeCanvas
                        nativeCanvas.save()
                        nativeCanvas.scale(fitScale, fitScale)

                        // 1. Draw SVG Fills
                        val fillPaint = Paint().apply {
                            style = Paint.Style.FILL
                            isAntiAlias = true
                        }
                        page?.let { currentPage ->
                            for (shape in currentPage.shapes) {
                                val androidPath = PathParser.createPathFromPathData(shape.pathData)
                                val color = shapeColors[shape.id] ?: Color.White
                                fillPaint.color = color.toArgb()
                                nativeCanvas.drawPath(androidPath, fillPaint)
                            }
                        }

                        // 2. Draw Brush Strokes (Static Viewmodel)
                        val brushPaint = Paint().apply {
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                            isAntiAlias = true
                        }
                        for (stroke in brushStrokes) {
                            if (stroke.points.isEmpty()) continue
                            brushPaint.color = stroke.color.toArgb()
                            brushPaint.strokeWidth = stroke.size

                            val strokePath = android.graphics.Path()
                            val first = stroke.points.first()
                            strokePath.moveTo(first.x, first.y)
                            for (i in 1 until stroke.points.size) {
                                val p = stroke.points[i]
                                strokePath.lineTo(p.x, p.y)
                            }
                            nativeCanvas.drawPath(strokePath, brushPaint)
                        }

                        // 3. Draw Active Local Stroke (lag free feedback)
                        activeStrokePoints?.let { points ->
                            if (points.isNotEmpty()) {
                                brushPaint.color = selectedColor.toArgb()
                                brushPaint.strokeWidth = brushSize

                                val activePath = android.graphics.Path()
                                val first = points.first()
                                activePath.moveTo(first.x, first.y)
                                for (i in 1 until points.size) {
                                    val p = points[i]
                                    activePath.lineTo(p.x, p.y)
                                }
                                nativeCanvas.drawPath(activePath, brushPaint)
                            }
                        }

                        // 4. Draw Outlines on Top
                        val outlinePaint = Paint().apply {
                            style = Paint.Style.STROKE
                            color = android.graphics.Color.BLACK
                            strokeWidth = 2.5f
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                            isAntiAlias = true
                        }
                        page?.let { currentPage ->
                            for (shape in currentPage.shapes) {
                                val androidPath = PathParser.createPathFromPathData(shape.pathData)
                                nativeCanvas.drawPath(androidPath, outlinePaint)
                            }
                        }

                        nativeCanvas.restore()
                    }
                }
            }

            // Bottom control bar panel (palette + tool toggles)
            BottomControlBar(
                selectedColor = selectedColor,
                selectedTool = selectedTool,
                brushSize = brushSize,
                onColorSelected = { viewModel.selectColor(it) },
                onToolSelected = { viewModel.selectTool(it) },
                onBrushSizeChanged = { viewModel.updateBrushSize(it) }
            )
        }
    }

    // Success dialog showing saved drawing path
    if (showSavedDialog) {
        AlertDialog(
            onDismissRequest = { showSavedDialog = false },
            title = { Text("Drawing Saved! 🎨", fontWeight = FontWeight.Bold) },
            text = { Text("Your coloring book creation has been successfully saved to:\n$savedFilePath") },
            confirmButton = {
                PlayfulButton(
                    onClick = { showSavedDialog = false }
                ) {
                    Text("Super!", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun ColoringHeader(
    title: String,
    canUndo: Boolean,
    canRedo: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
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
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            ),
            modifier = Modifier.weight(1f)
        )

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
private fun BottomControlBar(
    selectedColor: Color,
    selectedTool: ColoringTool,
    brushSize: Float,
    onColorSelected: (Color) -> Unit,
    onToolSelected: (ColoringTool) -> Unit,
    onBrushSizeChanged: (Float) -> Unit,
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
            // Row 1: Brush Size and Tool Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tool Toggle (Fill vs Brush)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayfulButton(
                        onClick = { onToolSelected(ColoringTool.FILL) },
                        backgroundColor = if (selectedTool == ColoringTool.FILL) PastelPink else Color.LightGray.copy(alpha = 0.2f),
                        contentColor = TextDark,
                        shape = CircleShape,
                        border = null,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("Fill 🪣", fontSize = 13.sp)
                    }

                    PlayfulButton(
                        onClick = { onToolSelected(ColoringTool.BRUSH) },
                        backgroundColor = if (selectedTool == ColoringTool.BRUSH) PastelBlue else Color.LightGray.copy(alpha = 0.2f),
                        contentColor = TextDark,
                        shape = CircleShape,
                        border = null,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("Brush 🖌️", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Brush Size Slider (Only visible in brush mode)
                if (selectedTool == ColoringTool.BRUSH) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Size: ", style = MaterialTheme.typography.bodyLarge, color = TextDark)
                        Slider(
                            value = brushSize,
                            onValueChange = onBrushSizeChanged,
                            valueRange = 4f..40f,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row 2: Scrolling Palette selection
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
        }
    }
}
