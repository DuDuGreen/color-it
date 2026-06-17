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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.PathParser
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
import com.example.colorit.ui.theme.SoftGray
import com.example.colorit.ui.theme.CozyDeskSurface
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.Brush
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
    val drawingBitmap by viewModel.drawingBitmap.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val selectedTool by viewModel.selectedTool.collectAsState()
    val brushSize by viewModel.brushSize.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    // Screen scale and pan values
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    // Resolve sketch bitmap from resources
    val pageImageBitmap = page?.imageResName?.let { resName ->
        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
        if (resId != 0) {
            val drawable = androidx.core.content.res.ResourcesCompat.getDrawable(context.resources, resId, null)
            (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap?.asImageBitmap()
        } else null
    }

    // Active brush stroke local drawing state
    var activeStrokePoints by remember { mutableStateOf<List<Offset>?>(null) }

    // Save dialog state
    var showSavedDialog by remember { mutableStateOf(false) }
    var showSpectrumDialog by remember { mutableStateOf(false) }
    var savedFilePath by remember { mutableStateOf("") }

    LaunchedEffect(pageId) {
        viewModel.loadPage(context, pageId)
    }

    Scaffold(
        topBar = {
            ColoringHeader(
                title = page?.title ?: "Color It",
                canUndo = canUndo,
                canRedo = canRedo,
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
                    viewModel.saveToGallery(context, context.cacheDir) { savedFile ->
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
            // Interactive drawing canvas area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(MaterialTheme.shapes.large)
                    .background(CozyDeskSurface) // Cozy desk surface background
                    .border(4.dp, CozyBlush.copy(alpha = 0.5f), MaterialTheme.shapes.large)
                    .padding(16.dp) // padding around the drawing sheet
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
                        .pointerInput(selectedTool) {
                            if (selectedTool == ColoringTool.FILL) {
                                detectTapGestures { touchOffset ->
                                    val side = min(size.width, size.height)
                                    val xOffset = (size.width - side) / 2f
                                    val yOffset = (size.height - side) / 2f
                                    val fitScale = side / 800f
                                    
                                    // Scale and transform coordinate to original 800x800 design grid
                                    val canvasX = (touchOffset.x - xOffset) / fitScale
                                    val canvasY = (touchOffset.y - yOffset) / fitScale

                                    if (page?.imageResName != null) {
                                        viewModel.fillBitmap(canvasX, canvasY, selectedColor)
                                    } else {
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
                                }
                            } else {
                                // Brush Mode
                                detectDragGestures(
                                    onDragStart = { startOffset ->
                                        val side = min(size.width, size.height)
                                        val xOffset = (size.width - side) / 2f
                                        val yOffset = (size.height - side) / 2f
                                        val fitScale = side / 800f
                                        
                                        val canvasX = (startOffset.x - xOffset) / fitScale
                                        val canvasY = (startOffset.y - yOffset) / fitScale
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
                                    val side = min(size.width, size.height)
                                    val xOffset = (size.width - side) / 2f
                                    val yOffset = (size.height - side) / 2f
                                    val fitScale = side / 800f
                                    
                                    val canvasX = (change.position.x - xOffset) / fitScale
                                    val canvasY = (change.position.y - yOffset) / fitScale
                                    activeStrokePoints = currentPoints + Offset(canvasX, canvasY)
                                }
                            }
                        }
                ) {
                    val side = min(size.width, size.height)
                    val xOffset = (size.width - side) / 2f
                    val yOffset = (size.height - side) / 2f

                    // Draw soft paper shadow under the sheet
                    drawRect(
                        color = Color.Black.copy(alpha = 0.08f),
                        topLeft = Offset(xOffset + 6.dp.toPx(), yOffset + 6.dp.toPx()),
                        size = Size(side, side)
                    )

                    if (pageImageBitmap != null && drawingBitmap != null) {
                        clipRect(
                            left = xOffset,
                            top = yOffset,
                            right = xOffset + side,
                            bottom = yOffset + side
                        ) {
                            // 1. Draw solid white background only inside the centered page sheet boundaries
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(xOffset, yOffset),
                                size = Size(side, side)
                            )

                            // 2. Draw user's colored strokes bitmap
                            drawImage(
                                image = drawingBitmap!!.bitmap.asImageBitmap(),
                                dstOffset = androidx.compose.ui.unit.IntOffset(xOffset.toInt(), yOffset.toInt()),
                                dstSize = androidx.compose.ui.unit.IntSize(side.toInt(), side.toInt())
                            )

                            // 3. Draw active local stroke in real-time on top
                            if (selectedTool == ColoringTool.BRUSH) {
                                val fitScale = side / 800f
                                drawIntoCanvas { canvas ->
                                    val nativeCanvas = canvas.nativeCanvas
                                    nativeCanvas.save()
                                    nativeCanvas.translate(xOffset, yOffset)
                                    nativeCanvas.scale(fitScale, fitScale)

                                    val brushPaint = Paint().apply {
                                        style = Paint.Style.STROKE
                                        strokeCap = Paint.Cap.ROUND
                                        strokeJoin = Paint.Join.ROUND
                                        color = selectedColor.toArgb()
                                        strokeWidth = brushSize
                                        isAntiAlias = true
                                    }
                                    activeStrokePoints?.let { points ->
                                        if (points.isNotEmpty()) {
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
                                    nativeCanvas.restore()
                                }
                            }

                            // 4. Draw outline sketch bitmap on top using Multiply blend mode
                            drawImage(
                                image = pageImageBitmap,
                                dstOffset = androidx.compose.ui.unit.IntOffset(xOffset.toInt(), yOffset.toInt()),
                                dstSize = androidx.compose.ui.unit.IntSize(side.toInt(), side.toInt()),
                                blendMode = BlendMode.Multiply
                            )
                        }

                        // 5. Draw clean page border around the drawing sheet
                        drawRect(
                            color = CountryOutline,
                            topLeft = Offset(xOffset, yOffset),
                            size = Size(side, side),
                            style = Stroke(width = 4.dp.toPx())
                        )
                    } else {
                        // Original SVG path rendering fallback
                        clipRect(
                            left = xOffset,
                            top = yOffset,
                            right = xOffset + side,
                            bottom = yOffset + side
                        ) {
                            // 1. Draw solid white background only inside the centered page sheet boundaries
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(xOffset, yOffset),
                                size = Size(side, side)
                            )

                            val fitScale = side / 800f

                            drawIntoCanvas { canvas ->
                                val nativeCanvas = canvas.nativeCanvas
                                nativeCanvas.save()
                                nativeCanvas.translate(xOffset, yOffset)
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

                                // 2. Draw Brush Strokes
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

                                // 3. Draw Active Local Stroke
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

                                // 4. Draw Outlines
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

                        // 5. Draw clean page border around the drawing sheet
                        drawRect(
                            color = CountryOutline,
                            topLeft = Offset(xOffset, yOffset),
                            size = Size(side, side),
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
                }
            }

            // Bottom control bar panel (palette + tool toggles)
            BottomControlBar(
                selectedColor = selectedColor,
                selectedTool = selectedTool,
                brushSize = brushSize,
                isBitmapPage = page?.imageResName != null,
                onColorSelected = { viewModel.selectColor(it) },
                onToolSelected = { viewModel.selectTool(it) },
                onBrushSizeChanged = { viewModel.updateBrushSize(it) },
                onSpectrumClick = { showSpectrumDialog = true }
            )
        }
    }

    // Success dialog showing saved drawing path
    if (showSavedDialog) {
        AlertDialog(
            onDismissRequest = { showSavedDialog = false },
            modifier = Modifier.border(3.dp, CountryOutline, MaterialTheme.shapes.large),
            shape = MaterialTheme.shapes.large,
            containerColor = CardYellow,
            titleContentColor = TextDark,
            textContentColor = TextDark.copy(alpha = 0.8f),
            title = { Text("Drawing Saved! 🎨", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Your coloring book creation has been successfully saved to:\n$savedFilePath", fontWeight = FontWeight.Medium) },
            confirmButton = {
                PlayfulButton(
                    onClick = { showSavedDialog = false }
                ) {
                    Text("Super!", color = Color.White)
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
                text = title,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: Actions (Undo, Redo, Save)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
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

@Composable
private fun BottomControlBar(
    selectedColor: Color,
    selectedTool: ColoringTool,
    brushSize: Float,
    isBitmapPage: Boolean,
    onColorSelected: (Color) -> Unit,
    onToolSelected: (ColoringTool) -> Unit,
    onBrushSizeChanged: (Float) -> Unit,
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
            // Row 1: Tool Selection and Spectrum Button
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
                        backgroundColor = if (selectedTool == ColoringTool.FILL) CozyRose else Color.LightGray.copy(alpha = 0.15f),
                        contentColor = if (selectedTool == ColoringTool.FILL) Color.White else TextDark,
                        shape = CircleShape,
                        border = null,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            CozyFillIcon(modifier = Modifier.size(16.dp), color = if (selectedTool == ColoringTool.FILL) Color.White else TextDark)
                            Text("Fill", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    PlayfulButton(
                        onClick = { onToolSelected(ColoringTool.BRUSH) },
                        backgroundColor = if (selectedTool == ColoringTool.BRUSH) CozyBlush else Color.LightGray.copy(alpha = 0.15f),
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
                            CozyBrushIcon(modifier = Modifier.size(16.dp), color = TextDark)
                            Text("Brush", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Spectrum Selector Button (Far Right)
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
                        Text("Spectrum", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Row 2: Brush Size Slider (Only visible in brush mode, takes full width below the tools)
            if (selectedTool == ColoringTool.BRUSH) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Size: ", style = MaterialTheme.typography.bodyMedium, color = TextDark, modifier = Modifier.padding(end = 4.dp))
                    Slider(
                        value = brushSize,
                        onValueChange = onBrushSizeChanged,
                        valueRange = 4f..40f,
                        modifier = Modifier.weight(1f)
                    )
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


