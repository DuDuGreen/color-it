package com.example.colorit.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.data.AppDatabase
import com.example.colorit.data.ArtworkEntity
import com.example.colorit.model.StickerItem
import com.example.colorit.ui.components.KidsButton
import com.example.colorit.ui.theme.*
import com.example.colorit.utils.AudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Fun Draggable Sticker Decorator Screen.
 * Allows child users to add cute stickers, drag/scale/rotate them using multi-touch gestures,
 * duplicate/delete active stickers, and export custom scenes to their local gallery.
 */
@Composable
fun StickerScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    // Selected canvas backing color
    val canvasBackings = listOf(White, PastelBlue, PastelPink, PastelYellow, PastelGreen)
    var activeCanvasBacking by remember { mutableStateOf(White) }

    // List of active stickers spawned on canvas
    val activeStickers = remember { mutableStateListOf<StickerItem>() }
    var selectedStickerId by remember { mutableStateOf<String?>(null) }

    // Categories
    val categories = listOf("Animals", "Emojis", "Stars", "Vehicles", "Shapes")
    var selectedCategory by remember { mutableStateOf("Animals") }

    val stickersData = mapOf(
        "Animals" to listOf("🦁", "🐯", "🐻", "🐼", "🐨", "🦊", "🐰", "🐸", "🐙", "🐬", "🦄", "🦖"),
        "Emojis" to listOf("😃", "😎", "🤪", "🥳", "😍", "👻", "🤖", "🤡", "👽", "💩"),
        "Stars" to listOf("⭐", "🌟", "✨", "💫", "🌙", "☀️", "☁️", "🌈", "🔥", "⚡"),
        "Vehicles" to listOf("🚗", "🚓", "🏎️", "🚀", "🛸", "✈️", "🚢", "🚒", "🚲", "🚂"),
        "Shapes" to listOf("🔴", "🔵", "🟡", "🟢", "💜", "🧡", "💖", "🟥", "🔺", "🔶", "⭐")
    )

    var showSaveSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        // --- 1. Top Control Bar HUD ---
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
                    .background(White, CircleShape)
                    .shadow(2.dp, CircleShape)
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Sticker Fun 🦄",
                color = AccentPurple,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // Wipe / Delete active selections
            IconButton(
                onClick = {
                    if (activeStickers.isNotEmpty()) {
                        AudioManager.playErrorSound()
                        activeStickers.clear()
                        selectedStickerId = null
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(White, CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear All", tint = AccentPink)
            }
        }

        // --- 2. Sticker Scene Canvas Board ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(4.dp, RoundedCornerShape(32.dp))
                .background(activeCanvasBacking, RoundedCornerShape(32.dp))
                .border(2.dp, AccentPurple.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .clickable {
                    // Tap background to deselect active sticker selection
                    selectedStickerId = null
                }
        ) {
            // Render stickers
            activeStickers.forEach { sticker ->
                val isSelected = sticker.id == selectedStickerId

                Box(
                    modifier = Modifier
                        .offset { IntOffset(sticker.x.toInt(), sticker.y.toInt()) }
                        .graphicsLayer(
                            scaleX = sticker.scale,
                            scaleY = sticker.scale,
                            rotationZ = sticker.rotation
                        )
                        .pointerInput(sticker) {
                            // Touch drag, zoom, and rotate gesture listener
                            detectTransformGestures { _, pan, zoom, rotation ->
                                sticker.x += pan.x
                                sticker.y += pan.y
                                sticker.scale = (sticker.scale * zoom).coerceIn(0.5f, 4.0f)
                                sticker.rotation += rotation
                                selectedStickerId = sticker.id
                            }
                        }
                        .clickable {
                            AudioManager.playTapSound()
                            selectedStickerId = sticker.id
                        }
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) AccentPink else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = sticker.label,
                        fontSize = 56.sp
                    )

                    // Overlay Delete/Duplicate controls for active selection
                    if (isSelected) {
                        // Small overlay ➕ button to duplicate
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(AccentGreen, CircleShape)
                                .align(Alignment.TopEnd)
                                .clickable {
                                    AudioManager.playTapSound()
                                    val cloned = sticker.copy(
                                        id = UUID.randomUUID().toString(),
                                        x = sticker.x + 60,
                                        y = sticker.y + 60
                                    )
                                    activeStickers.add(cloned)
                                    selectedStickerId = cloned.id
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "➕", fontSize = 10.sp, color = White)
                        }

                        // Small overlay ❌ button to delete
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(AccentPink, CircleShape)
                                .align(Alignment.TopStart)
                                .clickable {
                                    AudioManager.playErrorSound()
                                    activeStickers.remove(sticker)
                                    selectedStickerId = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "❌", fontSize = 10.sp, color = White)
                        }
                    }
                }
            }

            // Save Scene Button
            FloatingActionButton(
                onClick = {
                    AudioManager.playSaveSound()
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            // Export stickers layer to Bitmap PNG
                            val bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bitmap)
                            
                            val canvasColorInt = android.graphics.Color.argb(
                                255,
                                (activeCanvasBacking.red * 255).toInt(),
                                (activeCanvasBacking.green * 255).toInt(),
                                (activeCanvasBacking.blue * 255).toInt()
                            )
                            canvas.drawColor(canvasColorInt)

                            // Render text characters
                            val paint = android.graphics.Paint().apply {
                                isAntiAlias = true
                                textSize = 80f // standard character draw size
                                textAlign = android.graphics.Paint.Align.CENTER
                            }

                            activeStickers.forEach { sticker ->
                                canvas.save()
                                
                                // Translate to sticker offset and scale proportionally
                                // Map coordinates properly to 800x800 preview scale
                                canvas.translate(sticker.x + 40f, sticker.y + 40f)
                                canvas.rotate(sticker.rotation)
                                canvas.scale(sticker.scale, sticker.scale)

                                canvas.drawText(sticker.label, 0f, 0f, paint)
                                canvas.restore()
                            }

                            val fileName = "sticker_${UUID.randomUUID()}.png"
                            val file = File(context.filesDir, fileName)
                            val fos = FileOutputStream(file)
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                            fos.close()

                            val entity = ArtworkEntity(
                                filePath = file.absolutePath,
                                category = "Sticker Fun",
                                canvasType = "STICKER"
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
                Text(
                    text = "💾 Save Scene",
                    color = White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. Custom Canvas Backing colors row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Paper Color 📜", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
            
            canvasBackings.forEach { backing ->
                val isSelected = activeCanvasBacking == backing
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .shadow(2.dp, CircleShape)
                        .background(backing, CircleShape)
                        .border(if (isSelected) 3.dp else 1.dp, if (isSelected) AccentPurple else Color.Gray, CircleShape)
                        .clickable {
                            AudioManager.playTapSound()
                            activeCanvasBacking = backing
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 4. Bottom Sticker Drawer Selection Panel ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(vertical = 12.dp)
        ) {
            // Category scrollbar
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) AccentPurple else SoftGray,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                AudioManager.playTapSound()
                                selectedCategory = cat
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) White else TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sticker items horizontal list
            val itemsList = stickersData[selectedCategory] ?: emptyList()
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(itemsList) { symbol ->
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .background(OffWhite, RoundedCornerShape(16.dp))
                            .clickable {
                                AudioManager.playTapSound()
                                // Spawn in center with default scale and rotation
                                val newItem = StickerItem(
                                    id = UUID.randomUUID().toString(),
                                    category = selectedCategory,
                                    label = symbol,
                                    x = 300f,
                                    y = 200f
                                )
                                activeStickers.add(newItem)
                                selectedStickerId = newItem.id
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = symbol, fontSize = 36.sp)
                    }
                }
            }
        }
    }

    // Save alert dialog
    if (showSaveSuccess) {
        AlertDialog(
            onDismissRequest = { showSaveSuccess = false },
            title = { Text("Fabulous Scene! 🦄", fontWeight = FontWeight.Bold) },
            text = { Text("Your sticker poster was saved successfully!") },
            confirmButton = {
                KidsButton(
                    text = "Wonderful!",
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
