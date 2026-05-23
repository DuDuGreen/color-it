package com.example.colorit.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.colorit.data.AppDatabase
import com.example.colorit.data.ArtworkEntity
import com.example.colorit.ui.components.KidsButton
import com.example.colorit.ui.theme.*
import com.example.colorit.utils.AudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Visual Showcase Gallery displaying saved kids artworks.
 * Connects to Room DB to query all saved images, handles loading bitmaps from
 * internal storage programmatically, displays a full-screen preview popup modal,
 * and allows kids/parents to delete or share drawings.
 */
@Composable
fun GalleryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    // Live Room DB query state
    var artworks by remember { mutableStateOf<List<ArtworkEntity>>(emptyList()) }
    var selectedArtwork by remember { mutableStateOf<ArtworkEntity?>(null) }

    // Query helper
    fun reloadArtworks() {
        coroutineScope.launch(Dispatchers.IO) {
            val list = db.artworkDao().getAllArtworks()
            withContext(Dispatchers.Main) {
                artworks = list
            }
        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        reloadArtworks()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(16.dp)
    ) {
        // --- 1. Top HUD Header ---
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
                text = "My Gallery 🖼️",
                color = AccentPurple,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. Artworks Grid Deck ---
        if (artworks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🎨✨", fontSize = 72.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your studio is empty!",
                        color = TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Go color some pages or draw in the canvas!",
                        color = TextLight,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(artworks) { artwork ->
                    ArtworkThumbnailItem(
                        artwork = artwork,
                        onClick = {
                            AudioManager.playTapSound()
                            selectedArtwork = artwork
                        }
                    )
                }
            }
        }
    }

    // --- 3. Full Screen Artwork Preview Overlay Modal ---
    selectedArtwork?.let { artwork ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { selectedArtwork = null }, // tap outer to close
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                modifier = Modifier
                    .width(360.dp)
                    .padding(16.dp)
                    .clickable(enabled = false) {} // prevent closing
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Title header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = artwork.category,
                            color = AccentPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(SoftGray, CircleShape)
                                .clickable {
                                    AudioManager.playTapSound()
                                    selectedArtwork = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "❌", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Large rendered image
                    val bitmap = remember(artwork.filePath) {
                        BitmapFactory.decodeFile(artwork.filePath)?.asImageBitmap()
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(OffWhite)
                            .border(1.dp, SoftGray, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Artwork Preview",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(text = "Loading preview...", color = TextLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action controllers (Share / Delete)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Share Button
                        KidsButton(
                            text = "📤 Share",
                            backgroundColor = AccentPurple,
                            onClick = {
                                try {
                                    val file = File(artwork.filePath)
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Artwork"))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        )

                        // Delete Button
                        KidsButton(
                            text = "🗑️ Delete",
                            backgroundColor = AccentPink,
                            onClick = {
                                AudioManager.playErrorSound()
                                coroutineScope.launch(Dispatchers.IO) {
                                    // Remove file
                                    val file = File(artwork.filePath)
                                    if (file.exists()) {
                                        file.delete()
                                    }
                                    // Remove from DB
                                    db.artworkDao().deleteArtwork(artwork)
                                    
                                    withContext(Dispatchers.Main) {
                                        selectedArtwork = null
                                        reloadArtworks()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Thumbnail item listing that handles background IO image fetching
 */
@Composable
fun ArtworkThumbnailItem(
    artwork: ArtworkEntity,
    onClick: () -> Unit
) {
    val bitmapState = remember(artwork.filePath) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(artwork.filePath) {
        withContext(Dispatchers.IO) {
            val bitmap = BitmapFactory.decodeFile(artwork.filePath)
            bitmapState.value = bitmap
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .background(White, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(OffWhite),
            contentAlignment = Alignment.Center
        ) {
            val bmp = bitmapState.value
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = artwork.category,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(text = "✏️ Preview", color = TextLight, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = artwork.category,
            color = TextDark,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
