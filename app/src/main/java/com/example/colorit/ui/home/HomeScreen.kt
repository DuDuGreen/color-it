package com.example.colorit.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.ui.components.PastelCard
import com.example.colorit.ui.theme.PastelBlue
import com.example.colorit.ui.theme.PastelMint
import com.example.colorit.ui.theme.PastelPeach
import com.example.colorit.ui.theme.PastelPink
import com.example.colorit.ui.theme.PastelPurple
import com.example.colorit.ui.theme.PastelYellow
import com.example.colorit.ui.theme.TextDark

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToColoringBook: () -> Unit,
    onNavigateToFreeDraw: () -> Unit,
    onNavigateToGlowDraw: () -> Unit,
    onNavigateToStickers: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            HomeHeader(
                onSettingsClicked = {
                    viewModel.playClickSound()
                    onNavigateToSettings()
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
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Pick a Game! 🎈",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = TextDark
                ),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Responsive grid for phones and tablets
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    MenuCard(
                        title = "Coloring Book",
                        icon = "📚",
                        gradientColors = listOf(PastelPink, PastelYellow),
                        onClick = {
                            viewModel.playClickSound()
                            onNavigateToColoringBook()
                        }
                    )
                }
                item {
                    MenuCard(
                        title = "Free Draw",
                        icon = "✏️",
                        gradientColors = listOf(PastelBlue, PastelMint),
                        onClick = {
                            viewModel.playClickSound()
                            onNavigateToFreeDraw()
                        }
                    )
                }
                item {
                    MenuCard(
                        title = "Glow Draw",
                        icon = "✨",
                        gradientColors = listOf(Color(0xFF2C3E50), PastelPurple),
                        onClick = {
                            viewModel.playClickSound()
                            onNavigateToGlowDraw()
                        }
                    )
                }
                item {
                    MenuCard(
                        title = "Stickers",
                        icon = "🧸",
                        gradientColors = listOf(PastelPurple, PastelPink),
                        onClick = {
                            viewModel.playClickSound()
                            onNavigateToStickers()
                        }
                    )
                }
                item {
                    MenuCard(
                        title = "Gallery",
                        icon = "🖼️",
                        gradientColors = listOf(PastelPeach, PastelMint),
                        onClick = {
                            viewModel.playClickSound()
                            onNavigateToGallery()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(PastelYellow, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🎨", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Color It!",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
            )
        }

        // Settings Bubble Button
        IconButton(
            onClick = onSettingsClicked,
            modifier = Modifier
                .size(48.dp)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .background(PastelBlue, shape = CircleShape)
        ) {
            Text("⚙️", fontSize = 22.sp)
        }
    }
}

@Composable
private fun MenuCard(
    title: String,
    icon: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Premium animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_shift")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val animatedBrush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(offset, 0f),
        end = Offset(offset + 300f, 300f)
    )

    PastelCard(
        backgroundColor = Color.Transparent, // Managed by gradient
        borderColor = Color.White.copy(alpha = 0.4f),
        shadowElevation = 6.dp,
        contentPadding = 0.dp,
        modifier = modifier
            .aspectRatio(1.1f)
            .clip(MaterialTheme.shapes.large)
            .background(animatedBrush)
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color.White.copy(alpha = 0.25f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 42.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (gradientColors.first() == Color(0xFF2C3E50)) Color.White else TextDark
                )
            )
        }
    }
}
