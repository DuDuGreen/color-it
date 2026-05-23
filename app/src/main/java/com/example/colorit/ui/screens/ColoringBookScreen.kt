package com.example.colorit.ui.screens

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asAndroidPath as toAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.data.AppDatabase
import com.example.colorit.data.ArtworkEntity
import com.example.colorit.model.ColorableRegion
import com.example.colorit.model.ColoringPage
import com.example.colorit.ui.components.ColorPickerBar
import com.example.colorit.ui.components.KidsButton
import com.example.colorit.ui.theme.*
import com.example.colorit.utils.AudioManager
import com.example.colorit.utils.SampleColoringPages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Full Coloring Book system supporting Category selection, page grids,
 * custom vector outlines, zoom/pan gesture handlers, enclosed region tap-to-fill detection
 * using native Android Region bounds, Undo/Redo operations, and local Room saving.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColoringBookScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    // State definitions
    val categories = listOf("Animals", "Dinosaurs", "Vehicles", "Nature", "Space", "Alphabets", "Numbers")
    var selectedCategory by remember { mutableStateOf("Animals") }
    var selectedPage by remember { mutableStateOf<ColoringPage?>(null) }

    // Selected draw configurations
    var selectedColor by remember { mutableStateOf(PlayfulPalette.first()) }
    var isBucketMode by remember { mutableStateOf(true) } // true: tap to fill, false: outline paint

    // Canvas manipulation variables
    var zoomScale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Loaded coloring outlines
    val allPages = remember { SampleColoringPages.getPages() }
    val filteredPages = allPages.filter { it.category == selectedCategory }

    // Undo / Redo Stacks for vector region coloring
    val undoStack = remember { mutableStateListOf<List<Color>>() }
    val redoStack = remember { mutableStateListOf<List<Color>>() }

    // Save success alert dialog
    var showSaveSuccess by remember { mutableStateOf(false) }

    if (selectedPage == null) {
        // --- 1. Selection & Category Grid Mode ---
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(OffWhite)
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        AudioManager.playTapSound()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(White, CircleShape)
                        .shadow(2.dp, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Coloring Book 🎨",
                    color = AccentPurple,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Categories horizontal bar
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {},
                indicator = {}
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                            .shadow(if (isSelected) 4.dp else 1.dp, RoundedCornerShape(20.dp))
                            .background(
                                color = if (isSelected) AccentPurple else White,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                AudioManager.playTapSound()
                                selectedCategory = cat
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) White else TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pages Grid
            if (filteredPages.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "More pages coming soon! 🧸", color = TextLight)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredPages) { page ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .shadow(4.dp, RoundedCornerShape(24.dp))
                                .background(White, RoundedCornerShape(24.dp))
                                .clickable {
                                    AudioManager.playTapSound()
                                    // Make deep copy of the regions so coloring states are isolated
                                    val clonedRegions = page.regions.map {
                                        ColorableRegion(it.id, it.label, Color.White, it.drawCommand)
                                    }
                                    selectedPage = page.copy(regions = clonedRegions)
                                    undoStack.clear()
                                    redoStack.clear()
                                }
                                .padding(12.dp)
                        ) {
                            // Micro Outline Canvas Preview
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(OffWhite)
                                    .padding(8.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    page.regions.forEach { region ->
                                        val path = androidx.compose.ui.graphics.Path()
                                        region.drawCommand(path)
                                        drawPath(
                                            path = path,
                                            color = Color.Black,
                                            style = Stroke(width = 2f)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = page.name,
                                color = TextDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    } else {
        // --- 2. Interactive Coloring Canvas Mode ---
        val activePage = selectedPage!!
        
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(OffWhite)
        ) {
            // Upper HUD actions panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        AudioManager.playTapSound()
                        selectedPage = null
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(White, CircleShape)
                        .shadow(2.dp, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }

                // Control Tools (Undo / Redo / Reset / Save)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Undo
                    IconButton(
                        onClick = {
                            if (undoStack.isNotEmpty()) {
                                AudioManager.playTapSound()
                                // Capture current state for redo
                                val currentState = activePage.regions.map { it.color }
                                redoStack.add(currentState)
                                
                                val previousState = undoStack.removeLast()
                                activePage.regions.forEachIndexed { i, reg ->
                                    reg.color = previousState[i]
                                }
                            }
                        },
                        enabled = undoStack.isNotEmpty(),
                        modifier = Modifier
                            .size(44.dp)
                            .background(if (undoStack.isNotEmpty()) White else SoftGray, CircleShape)
                    ) {
                        Text(text = "↩️", fontSize = 18.sp)
                    }

                    // Redo
                    IconButton(
                        onClick = {
                            if (redoStack.isNotEmpty()) {
                                AudioManager.playTapSound()
                                val currentState = activePage.regions.map { it.color }
                                undoStack.add(currentState)

                                val nextState = redoStack.removeLast()
                                activePage.regions.forEachIndexed { i, reg ->
                                    reg.color = nextState[i]
                                }
                            }
                        },
                        enabled = redoStack.isNotEmpty(),
                        modifier = Modifier
                            .size(44.dp)
                            .background(if (redoStack.isNotEmpty()) White else SoftGray, CircleShape)
                    ) {
                        Text(text = "↪️", fontSize = 18.sp)
                    }

                    // Brush vs Bucket
                    IconButton(
                        onClick = {
                            AudioManager.playTapSound()
                            isBucketMode = !isBucketMode
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(if (isBucketMode) PastelPink else PastelBlue, CircleShape)
                            .border(2.dp, AccentPink, CircleShape)
                    ) {
                        Text(text = if (isBucketMode) "🪣" else "🖌️", fontSize = 20.sp)
                    }

                    // Reset
                    IconButton(
                        onClick = {
                            AudioManager.playErrorSound()
                            // Reset regions color
                            activePage.regions.forEach { it.color = Color.White }
                            undoStack.clear()
                            redoStack.clear()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(White, CircleShape)
                    ) {
                        Text(text = "🧽", fontSize = 18.sp)
                    }
                }
            }

            // Central Canvas Area supporting Tap-Fill and Zoom
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(4.dp, RoundedCornerShape(32.dp))
                    .background(White, RoundedCornerShape(32.dp))
                    .clip(RoundedCornerShape(32.dp))
                    .pointerInput(Unit) {
                        // Detect Zoom and Pan
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomScale = (zoomScale * zoom).coerceIn(1f, 4f)
                            offset = if (zoomScale == 1f) Offset.Zero else offset + pan
                        }
                    }
                    .pointerInput(activePage) {
                        detectTapGestures { tapOffset ->
                            // Scale tap position to local coordinates inside standard 1000x1000 bounds
                            val sizeWidth = size.width
                            val sizeHeight = size.height

                            val localX = ((tapOffset.x - offset.x) / zoomScale) * (1000f / sizeWidth)
                            val localY = ((tapOffset.y - offset.y) / zoomScale) * (1000f / sizeHeight)

                            // Hit testing: Iterate backwards (top-most layers first)
                            for (region in activePage.regions.asReversed()) {
                                val composePath = androidx.compose.ui.graphics.Path()
                                region.drawCommand(composePath)
                                val androidPath = composePath.asAndroidPath()
                                
                                val rectF = RectF()
                                androidPath.computeBounds(rectF, true)
                                val graphicsRegion = android.graphics.Region()
                                graphicsRegion.setPath(
                                    androidPath,
                                    android.graphics.Region(
                                        rectF.left.toInt(),
                                        rectF.top.toInt(),
                                        rectF.right.toInt(),
                                        rectF.bottom.toInt()
                                    )
                                )

                                if (graphicsRegion.contains(localX.toInt(), localY.toInt())) {
                                    // Save state for undo stack
                                    val previousState = activePage.regions.map { it.color }
                                    undoStack.add(previousState)
                                    redoStack.clear()

                                    // Color the region!
                                    region.color = selectedColor
                                    AudioManager.playTapSound()
                                    break
                                }
                            }
                        }
                    }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = zoomScale,
                            scaleY = zoomScale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                ) {
                    val scaleX = size.width / 1000f
                    val scaleY = size.height / 1000f

                    // Draw regions in ordered layers
                    activePage.regions.forEach { region ->
                        val path = androidx.compose.ui.graphics.Path()
                        region.drawCommand(path)

                        // Scale the path to fit actual canvas size
                        val scaledPath = androidx.compose.ui.graphics.Path()
                        scaledPath.addPath(path, Offset.Zero)
                        
                        // Scale canvas matrix appropriately
                        withTransform({
                            scale(scaleX, scaleY, Offset.Zero)
                        }) {
                            // 1. Fill region with current color state
                            drawPath(
                                path = path,
                                color = region.color
                            )
                            // 2. Stroke outline
                            drawPath(
                                path = path,
                                color = TextDark,
                                style = Stroke(width = 6f)
                            )
                        }
                    }
                }

                // Floating Save Button
                FloatingActionButton(
                    onClick = {
                        AudioManager.playSaveSound()
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                // Programmatically render and save the drawing as a PNG bitmap
                                val previewBitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
                                val androidCanvas = android.graphics.Canvas(previewBitmap)
                                androidCanvas.drawColor(android.graphics.Color.WHITE)

                                val scaleX = 800f / 1000f
                                val scaleY = 800f / 1000f

                                activePage.regions.forEach { region ->
                                    val path = androidx.compose.ui.graphics.Path()
                                    region.drawCommand(path)
                                    val paint = android.graphics.Paint().apply {
                                        style = android.graphics.Paint.Style.FILL
                                        color = android.graphics.Color.argb(
                                            (region.color.alpha * 255).toInt(),
                                            (region.color.red * 255).toInt(),
                                            (region.color.green * 255).toInt(),
                                            (region.color.blue * 255).toInt()
                                        )
                                    }
                                    val strokePaint = android.graphics.Paint().apply {
                                        style = android.graphics.Paint.Style.STROKE
                                        strokeWidth = 6f
                                        color = android.graphics.Color.parseColor("#263238")
                                    }

                                    val androidPath = path.asAndroidPath()
                                    val matrix = android.graphics.Matrix()
                                    matrix.postScale(scaleX, scaleY)
                                    androidPath.transform(matrix)

                                    androidCanvas.drawPath(androidPath, paint)
                                    androidCanvas.drawPath(androidPath, strokePaint)
                                }

                                // Write bitmap to file
                                val fileName = "artwork_${UUID.randomUUID()}.png"
                                val file = File(context.filesDir, fileName)
                                val fos = FileOutputStream(file)
                                previewBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                                fos.close()

                                // Register file record in Room DB
                                val entity = ArtworkEntity(
                                    filePath = file.absolutePath,
                                    category = activePage.category,
                                    canvasType = "COLORING_BOOK"
                                )
                                db.artworkDao().insertArtwork(entity)

                                withContext(Dispatchers.Main) {
                                    showSaveSuccess = true
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    containerColor = AccentPink,
                    contentColor = White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text(text = "💾 Save", color = White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                }
            }

            // Bottom Palette Selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(bottom = 16.dp)
            ) {
                ColorPickerBar(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }
        }
    }

    // Success dialog
    if (showSaveSuccess) {
        AlertDialog(
            onDismissRequest = { showSaveSuccess = false },
            title = { Text("Fantastic! 🎉", fontWeight = FontWeight.Bold) },
            text = { Text("Your masterpiece has been saved safely in your gallery!") },
            confirmButton = {
                KidsButton(
                    text = "Cool!",
                    onClick = {
                        showSaveSuccess = false
                        selectedPage = null // exit coloring view
                    }
                )
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = White
        )
    }
}
