package com.example.colorit.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.data.AppDatabase
import com.example.colorit.data.ArtworkEntity
import com.example.colorit.model.DrawPath
import com.example.colorit.model.ToolType
import com.example.colorit.ui.components.ColorPickerBar
import com.example.colorit.ui.components.KidsButton
import com.example.colorit.ui.theme.*
import com.example.colorit.utils.AudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Particle system model for star sparkle trails
 */
data class SparkleParticle(
    val id: String = UUID.randomUUID().toString(),
    val position: Offset,
    val color: Color,
    val maxRadius: Float = (10..22).random().toFloat(),
    var currentRadius: Float = 2f,
    var alpha: Float = 1f,
    val speedX: Float = (-5..5).random().toFloat(),
    val speedY: Float = (-5..5).random().toFloat()
)

/**
 * Magical Neon Glow Drawing Screen.
 * Renders glowing neon lines using layered gradient composite passes (outer glow, inner flame, white core),
 * spawns real-time animated sparkle trails, and records gorgeous glowing PNG artwork.
 */
@Composable
fun GlowDrawScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    // Glow neon paint pots
    val glowPalette = listOf(NeonPink, NeonCyan, NeonYellow, NeonGreen, NeonPurple, NeonOrange)
    var selectedColor by remember { mutableStateOf(glowPalette.first()) }
    var brushSize by remember { mutableStateOf(24f) }

    // Path histories
    val paths = remember { mutableStateListOf<DrawPath>() }
    val undonePaths = remember { mutableStateListOf<DrawPath>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }

    // Sparkling Particle Trail System
    val particles = remember { mutableStateListOf<SparkleParticle>() }

    // Animation frame ticks for particle simulation
    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60fps updates
            val iterator = particles.iterator()
            while (iterator.hasNext()) {
                val particle = iterator.next()
                particle.currentRadius += 0.4f
                particle.alpha -= 0.04f
                
                // Move particle slightly
                val updatedPos = Offset(
                    particle.position.x + particle.speedX,
                    particle.position.y + particle.speedY
                )
                // We recreate or mutate particles in-place
                if (particle.alpha <= 0f || particle.currentRadius >= particle.maxRadius) {
                    iterator.remove()
                }
            }
        }
    }

    var showSaveSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F021B)) // Magical ultra-dark violet background
    ) {
        // --- 1. Top HUD Header ---
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
                    onNavigateBack()
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF220E3E), CircleShape)
                    .border(2.dp, NeonCyan, CircleShape)
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = NeonCyan)
            }

            Text(
                text = "Magic Glow Draw ✨",
                color = NeonCyan,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // Undo / Redo / Reset
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Undo
                IconButton(
                    onClick = {
                        if (paths.isNotEmpty()) {
                            AudioManager.playTapSound()
                            undonePaths.add(paths.removeLast())
                        }
                    },
                    enabled = paths.isNotEmpty(),
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF220E3E), CircleShape)
                ) {
                    Text(text = "↩️", fontSize = 16.sp)
                }

                // Clear
                IconButton(
                    onClick = {
                        if (paths.isNotEmpty()) {
                            AudioManager.playErrorSound()
                            paths.clear()
                            undonePaths.clear()
                            particles.clear()
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF220E3E), CircleShape)
                ) {
                    Text(text = "🧽", fontSize = 16.sp)
                }
            }
        }

        // --- 2. Magic Canvas Deck ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(8.dp, RoundedCornerShape(32.dp))
                .background(Color(0xFF07000E), RoundedCornerShape(32.dp))
                .border(3.dp, NeonPurple.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .pointerInput(selectedColor, brushSize) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            undonePaths.clear()
                            currentPoints.clear()
                            currentPoints.add(startOffset)

                            // Spawn initial sparkles
                            repeat(6) {
                                particles.add(SparkleParticle(position = startOffset, color = selectedColor))
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            currentPoints.add(change.position)

                            // Sparkle spawn on trail
                            if (currentPoints.size % 2 == 0) {
                                repeat(3) {
                                    particles.add(SparkleParticle(position = change.position, color = selectedColor))
                                }
                                AudioManager.playBrushSound()
                            }
                        },
                        onDragEnd = {
                            if (currentPoints.isNotEmpty()) {
                                paths.add(
                                    DrawPath(
                                        points = currentPoints.toList(),
                                        color = selectedColor,
                                        size = brushSize,
                                        tool = ToolType.GLOW
                                    )
                                )
                                currentPoints.clear()
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Renders completed glow paths
                paths.forEach { drawPath ->
                    drawGlowPath(drawPath)
                }

                // Renders active glow path
                if (currentPoints.isNotEmpty()) {
                    drawGlowPath(
                        DrawPath(
                            points = currentPoints.toList(),
                            color = selectedColor,
                            size = brushSize,
                            tool = ToolType.GLOW
                        )
                    )
                }

                // Renders animated sparkles
                particles.forEach { particle ->
                    drawCircle(
                        color = particle.color.copy(alpha = particle.alpha),
                        radius = particle.currentRadius,
                        center = particle.position
                    )
                }
            }

            // Save Neon Floating Button
            FloatingActionButton(
                onClick = {
                    AudioManager.playSaveSound()
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            // Render glowing paths onto a dark-background Bitmap image
                            val bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bitmap)
                            canvas.drawColor(android.graphics.Color.parseColor("#07000E"))

                            paths.forEach { drawPath ->
                                val rgb = drawPath.color
                                val androidColor = android.graphics.Color.argb(255, (rgb.red * 255).toInt(), (rgb.green * 255).toInt(), (rgb.blue * 255).toInt())

                                // 1. Large thick fuzzy glow layer
                                val paintOuter = android.graphics.Paint().apply {
                                    isAntiAlias = true
                                    style = android.graphics.Paint.Style.STROKE
                                    strokeCap = android.graphics.Paint.Cap.ROUND
                                    strokeJoin = android.graphics.Paint.Join.ROUND
                                    strokeWidth = drawPath.size * 2.2f
                                    color = android.graphics.Color.argb(60, (rgb.red * 255).toInt(), (rgb.green * 255).toInt(), (rgb.blue * 255).toInt())
                                }

                                // 2. Sharp neon layer
                                val paintMid = android.graphics.Paint().apply {
                                    isAntiAlias = true
                                    style = android.graphics.Paint.Style.STROKE
                                    strokeCap = android.graphics.Paint.Cap.ROUND
                                    strokeJoin = android.graphics.Paint.Join.ROUND
                                    strokeWidth = drawPath.size * 1.1f
                                    color = androidColor
                                }

                                // 3. White center flame layer
                                val paintInner = android.graphics.Paint().apply {
                                    isAntiAlias = true
                                    style = android.graphics.Paint.Style.STROKE
                                    strokeCap = android.graphics.Paint.Cap.ROUND
                                    strokeJoin = android.graphics.Paint.Join.ROUND
                                    strokeWidth = drawPath.size * 0.35f
                                    color = android.graphics.Color.WHITE
                                }

                                if (drawPath.points.isNotEmpty()) {
                                    val path = android.graphics.Path()
                                    path.moveTo(drawPath.points.first().x, drawPath.points.first().y)
                                    for (i in 1 until drawPath.points.size) {
                                        val p1 = drawPath.points[i - 1]
                                        val p2 = drawPath.points[i]
                                        val midX = (p1.x + p2.x) / 2
                                        val midY = (p1.y + p2.y) / 2
                                        if (i == 1) {
                                            path.lineTo(midX, midY)
                                        } else {
                                            path.quadTo(p1.x, p1.y, midX, midY)
                                        }
                                    }
                                    path.lineTo(drawPath.points.last().x, drawPath.points.last().y)

                                    canvas.drawPath(path, paintOuter)
                                    canvas.drawPath(path, paintMid)
                                    canvas.drawPath(path, paintInner)
                                }
                            }

                            // Write to file
                            val fileName = "glow_draw_${UUID.randomUUID()}.png"
                            val file = File(context.filesDir, fileName)
                            val fos = FileOutputStream(file)
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                            fos.close()

                            // Room Database entry
                            val entity = ArtworkEntity(
                                filePath = file.absolutePath,
                                category = "Glow Draw",
                                canvasType = "GLOW_DRAW"
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
                containerColor = NeonPurple,
                contentColor = White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text(
                    text = "💾 Save",
                    color = White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. Neon styling selector row ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF220E3E))
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ColorPickerBar(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it },
                palette = glowPalette
            )

            // Custom thick brush size slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Glow Size ☄️",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    modifier = Modifier.width(96.dp)
                )
                Slider(
                    value = brushSize,
                    onValueChange = { brushSize = it },
                    valueRange = 8f..60f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan.copy(alpha = 0.5f),
                        inactiveTrackColor = Color(0xFF0F021B)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Success dialog
    if (showSaveSuccess) {
        AlertDialog(
            onDismissRequest = { showSaveSuccess = false },
            title = { Text("Magic Done! ✨", fontWeight = FontWeight.Bold) },
            text = { Text("Your glowing neon picture has been saved safely!") },
            confirmButton = {
                KidsButton(
                    text = "Wow!",
                    onClick = {
                        showSaveSuccess = false
                        onNavigateBack()
                    }
                )
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = White
        )
    }
}

/**
 * Composite layered stroke rendering to simulate a real physical high-intensity glowing tube.
 */
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGlowPath(
    drawPath: DrawPath
) {
    if (drawPath.points.size < 2) return

    val path = Path()
    path.moveTo(drawPath.points.first().x, drawPath.points.first().y)
    for (i in 1 until drawPath.points.size) {
        val p1 = drawPath.points[i - 1]
        val p2 = drawPath.points[i]
        val midX = (p1.x + p2.x) / 2
        val midY = (p1.y + p2.y) / 2
        if (i == 1) {
            path.lineTo(midX, midY)
        } else {
            path.quadraticBezierTo(p1.x, p1.y, midX, midY)
        }
    }
    path.lineTo(drawPath.points.last().x, drawPath.points.last().y)

    // Layer 1: Wide, high-diffuse transparent outer-edge glow halo
    drawPath(
        path = path,
        color = drawPath.color.copy(alpha = 0.22f),
        style = Stroke(
            width = drawPath.size * 2.3f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Layer 2: Medium glowing core base
    drawPath(
        path = path,
        color = drawPath.color,
        style = Stroke(
            width = drawPath.size * 1.1f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Layer 3: High-intensity white center flame line
    drawPath(
        path = path,
        color = White,
        style = Stroke(
            width = drawPath.size * 0.35f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
