package com.starkified.colorit.ui.glow

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.Toast
import androidx.compose.runtime.withFrameMillis
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starkified.colorit.data.model.BrushStroke
import com.starkified.colorit.ui.components.*
import com.starkified.colorit.ui.theme.PastelBlue
import com.starkified.colorit.ui.theme.PastelMint
import com.starkified.colorit.ui.theme.PastelPeach
import com.starkified.colorit.ui.theme.PastelPink
import com.starkified.colorit.ui.theme.PastelPurple
import com.starkified.colorit.ui.theme.PastelYellow
import com.starkified.colorit.ui.theme.TextDark
import com.starkified.colorit.ui.theme.CozyRose
import com.starkified.colorit.ui.theme.CozyBlush
import com.starkified.colorit.ui.theme.CozyBlushLight
import com.starkified.colorit.ui.theme.CozyCreamBackground
import com.starkified.colorit.ui.theme.AppColorSpectrum
import com.starkified.colorit.ui.theme.CountryOutline
import com.starkified.colorit.ui.theme.CardYellow
import androidx.compose.ui.graphics.Brush
import com.starkified.colorit.util.SoundHelper

data class Sparkle(
    val position: Offset,
    val color: Color,
    val size: Float,
    val alpha: Float,
    val velocity: Offset,
    val life: Float
)

@Composable
fun GlowDrawScreen(
    viewModel: GlowDrawViewModel,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strokes by viewModel.strokes.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val brushSize by viewModel.brushSize.collectAsState()

    var activeStrokePoints by remember { mutableStateOf<List<Offset>?>(null) }
    var sparkles by remember { mutableStateOf<List<Sparkle>>(emptyList()) }

    var canvasWidth by remember { mutableStateOf(0) }
    var canvasHeight by remember { mutableStateOf(0) }
    var cachedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Dialog states
    var showSavedDialog by remember { mutableStateOf(false) }
    var savedFilePath by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showSpectrumDialog by remember { mutableStateOf(false) }

    val canvasBgColor = Color(0xFF0F172A) // Deep night slate

    // Neon glowing paint helpers
    val glowPaint = remember {
        Paint().apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
    }
    val solidPaint = remember {
        Paint().apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
    }
    val corePaint = remember {
        Paint().apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
    }

    // 1. Sparkle update loop (Fades and moves sparkles at 60 FPS)
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { _ ->
                sparkles = sparkles.mapNotNull { sparkle ->
                    if (sparkle.life <= 0.05f) null
                    else {
                        sparkle.copy(
                            position = sparkle.position + sparkle.velocity,
                            alpha = sparkle.life - 0.04f,
                            life = sparkle.life - 0.04f
                        )
                    }
                }
            }
        }
    }

    // 2. Persistent offscreen Bitmap cache updates
    LaunchedEffect(strokes, canvasWidth, canvasHeight) {
        if (canvasWidth > 0 && canvasHeight > 0) {
            val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(canvasBgColor.toArgb())

            for (stroke in strokes) {
                if (stroke.points.isEmpty()) continue

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

                // Draw outer neon glow shadow
                val size = stroke.size
                glowPaint.color = stroke.color.copy(alpha = 0.35f).toArgb()
                glowPaint.strokeWidth = size * 2.2f
                glowPaint.maskFilter = BlurMaskFilter(size * 0.8f, BlurMaskFilter.Blur.NORMAL)
                canvas.drawPath(path, glowPaint)

                // Draw inner solid white core
                solidPaint.color = Color.White.copy(alpha = 0.9f).toArgb()
                solidPaint.strokeWidth = size * 0.7f
                canvas.drawPath(path, solidPaint)

                // Draw neon thin color on top
                corePaint.color = stroke.color.toArgb()
                corePaint.strokeWidth = size * 0.4f
                canvas.drawPath(path, corePaint)
            }
            cachedBitmap = bitmap
        }
    }

    Scaffold(
        topBar = {
            GlowHeader(
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
                        Toast.makeText(context, "Draw something first! ✨", Toast.LENGTH_SHORT).show()
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
            // Dark Neon Canvas drawing board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(MaterialTheme.shapes.large)
                    .background(canvasBgColor)
                    .border(4.dp, CozyRose.copy(alpha = 0.5f), MaterialTheme.shapes.large)
                    .padding(8.dp)
                    .onSizeChanged { size ->
                        canvasWidth = size.width
                        canvasHeight = size.height
                    }
                    .pointerInput(selectedColor, brushSize) {
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                activeStrokePoints = listOf(startOffset)
                            },
                            onDragEnd = {
                                activeStrokePoints?.let { points ->
                                    if (points.isNotEmpty()) {
                                        viewModel.addStroke(
                                            BrushStroke(points, selectedColor, brushSize)
                                        )
                                    }
                                }
                                activeStrokePoints = null
                            }
                        ) { change, _ ->
                            change.consume()
                            val currentPoints = activeStrokePoints ?: emptyList()
                            val touchPos = change.position
                            activeStrokePoints = currentPoints + touchPos

                            // Spawn trailing particle sparkles
                            val count = (1..3).random()
                            val newSparkles = mutableListOf<Sparkle>()
                            for (j in 0 until count) {
                                val offsetVel = Offset(
                                    ((Math.random() - 0.5) * 6).toFloat(),
                                    ((Math.random() - 0.5) * 6).toFloat()
                                )
                                newSparkles.add(
                                    Sparkle(
                                        position = touchPos + Offset(
                                            ((Math.random() - 0.5) * 20).toFloat(),
                                            ((Math.random() - 0.5) * 20).toFloat()
                                        ),
                                        color = selectedColor,
                                        size = (4..12).random().toFloat(),
                                        alpha = 1.0f,
                                        velocity = offsetVel,
                                        life = 1.0f
                                    )
                                )
                            }
                            sparkles = sparkles + newSparkles
                        }
                    }
            ) {
                // Interactive draw Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // 1. Draw static cached neon bitmap (O(1) drawing complexity)
                    cachedBitmap?.let { bitmap ->
                        drawImage(bitmap.asImageBitmap())
                    }

                    // 2. Draw active neon stroke double-pass in real-time
                    activeStrokePoints?.let { points ->
                        if (points.isNotEmpty()) {
                            val path = android.graphics.Path()
                            path.moveTo(points.first().x, points.first().y)
                            
                            if (points.size > 1) {
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    path.quadTo(prev.x, prev.y, (prev.x + curr.x) / 2, (prev.y + curr.y) / 2)
                                }
                                path.lineTo(points.last().x, points.last().y)
                            } else {
                                path.lineTo(points.first().x, points.first().y)
                            }

                            drawIntoCanvas { canvas ->
                                val nativeCanvas = canvas.nativeCanvas

                                // Pass 1: Glowing blur shadow
                                glowPaint.color = selectedColor.copy(alpha = 0.35f).toArgb()
                                glowPaint.strokeWidth = brushSize * 2.2f
                                glowPaint.maskFilter = BlurMaskFilter(brushSize * 0.8f, BlurMaskFilter.Blur.NORMAL)
                                nativeCanvas.drawPath(path, glowPaint)

                                // Pass 2: White inner core
                                solidPaint.color = Color.White.copy(alpha = 0.9f).toArgb()
                                solidPaint.strokeWidth = brushSize * 0.7f
                                nativeCanvas.drawPath(path, solidPaint)

                                // Pass 3: Thin color core
                                corePaint.color = selectedColor.toArgb()
                                corePaint.strokeWidth = brushSize * 0.4f
                                nativeCanvas.drawPath(path, corePaint)
                            }
                        }
                    }

                    // 3. Draw sparkles particles on top
                    for (sparkle in sparkles) {
                        drawCircle(
                            color = sparkle.color.copy(alpha = sparkle.alpha),
                            radius = sparkle.size,
                            center = sparkle.position
                        )
                    }
                }
            }

            // Controls panel
            GlowToolbar(
                selectedColor = selectedColor,
                brushSize = brushSize,
                onColorSelected = { viewModel.selectColor(it) },
                onBrushSizeChanged = { viewModel.updateBrushSize(it) },
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
            title = { Text("Drawing Saved! ✨", fontWeight = FontWeight.ExtraBold, color = TextDark) },
            text = { Text("Your neon creation has been successfully saved to your device's Gallery! ✨", fontWeight = FontWeight.Medium, color = TextDark.copy(alpha = 0.8f)) },
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
            title = { Text("Start Over? 🧹", fontWeight = FontWeight.ExtraBold, color = TextDark) },
            text = { Text("Do you want to clear your current drawing and start fresh?", fontWeight = FontWeight.Medium, color = TextDark.copy(alpha = 0.8f)) },
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
private fun GlowHeader(
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
                text = "Glow Draw 🌌",
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
private fun GlowToolbar(
    selectedColor: Color,
    brushSize: Float,
    onColorSelected: (Color) -> Unit,
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
            // Glow Size and Spectrum row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Glow Size: ", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = TextDark)
                    Slider(
                        value = brushSize,
                        onValueChange = onBrushSizeChanged,
                        valueRange = 6f..60f,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Spectrum selector button
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

            Spacer(modifier = Modifier.height(14.dp))

            // Neon Palette Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(colors) { color ->
                    val isSelected = color == selectedColor
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 46.dp else 38.dp)
                            .shadow(elevation = if (isSelected) 6.dp else 1.dp, shape = CircleShape)
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
