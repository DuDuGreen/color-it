package com.example.colorit.ui.gallery

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.colorit.data.database.SavedDrawing
import com.example.colorit.ui.components.PastelCard
import com.example.colorit.ui.components.PlayfulButton
import com.example.colorit.ui.theme.PastelBlue
import com.example.colorit.ui.theme.PastelMint
import com.example.colorit.ui.theme.PastelPeach
import com.example.colorit.ui.theme.PastelPink
import com.example.colorit.ui.theme.PastelYellow
import com.example.colorit.ui.theme.TextDark
import com.example.colorit.util.SoundHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    soundHelper: SoundHelper,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawings by viewModel.savedDrawings.collectAsState()

    var previewDrawing by remember { mutableStateOf<SavedDrawing?>(null) }
    var deleteDrawingTarget by remember { mutableStateOf<SavedDrawing?>(null) }

    Scaffold(
        topBar = {
            GalleryHeader(
                onBack = {
                    soundHelper.playPopSound()
                    onBack()
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
            if (drawings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🖼️", fontSize = 72.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your Gallery is Empty!",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start coloring or drawing to see your masterpieces here!",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = TextDark.copy(alpha = 0.6f),
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(drawings) { drawing ->
                        DrawingCard(
                            drawing = drawing,
                            onClick = {
                                soundHelper.playPopSound()
                                previewDrawing = drawing
                            },
                            onShare = {
                                soundHelper.playPopSound()
                                val file = File(drawing.filePath)
                                if (file.exists()) {
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
                                    context.startActivity(Intent.createChooser(intent, "Share Drawing"))
                                }
                            },
                            onDelete = {
                                soundHelper.playPopSound()
                                deleteDrawingTarget = drawing
                            }
                        )
                    }
                }
            }
        }
    }

    // 1. Full-screen Preview Dialog
    previewDrawing?.let { drawing ->
        val bitmap by produceState<Bitmap?>(initialValue = null) {
            value = withContext(Dispatchers.IO) {
                BitmapFactory.decodeFile(drawing.filePath)
            }
        }

        AlertDialog(
            onDismissRequest = { previewDrawing = null },
            title = {
                Text(
                    text = drawing.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFCFBF7))
                ) {
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = drawing.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: Text(
                        text = "Loading...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            },
            confirmButton = {
                PlayfulButton(onClick = { previewDrawing = null }) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // 2. Delete Confirmation Dialog
    deleteDrawingTarget?.let { drawing ->
        AlertDialog(
            onDismissRequest = { deleteDrawingTarget = null },
            title = { Text("Delete Masterpiece? 🗑️", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete \"${drawing.title}\"?") },
            confirmButton = {
                PlayfulButton(
                    onClick = {
                        soundHelper.playPopSound()
                        viewModel.deleteDrawing(drawing)
                        deleteDrawingTarget = null
                    },
                    backgroundColor = PastelPink
                ) {
                    Text("Yes, Delete!", color = TextDark)
                }
            },
            dismissButton = {
                PlayfulButton(
                    onClick = {
                        soundHelper.playPopSound()
                        deleteDrawingTarget = null
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
private fun GalleryHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
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
            text = "My Gallery 🖼️",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
        )
    }
}

@Composable
private fun DrawingCard(
    drawing: SavedDrawing,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Asynchronously load the local file bitmap off-thread
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = drawing.filePath) {
        value = withContext(Dispatchers.IO) {
            BitmapFactory.decodeFile(drawing.filePath)
        }
    }

    PastelCard(
        backgroundColor = Color.White,
        borderColor = PastelMint.copy(alpha = 0.5f),
        shadowElevation = 4.dp,
        contentPadding = 0.dp,
        modifier = modifier
            .aspectRatio(0.9f)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Thumbnail image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFFCFBF7))
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = drawing.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Text(
                    text = "🎨",
                    fontSize = 32.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Title and action buttons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = drawing.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextDark,
                    modifier = Modifier.weight(1f)
                )

                // Share
                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(32.dp)
                        .background(PastelBlue, shape = CircleShape)
                ) {
                    Text("📤", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .background(PastelPink, shape = CircleShape)
                ) {
                    Text("🗑️", fontSize = 12.sp)
                }
            }
        }
    }
}
