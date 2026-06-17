package com.example.colorit.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.R
import com.example.colorit.ui.components.*
import com.example.colorit.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToColoringBook: () -> Unit,
    onNavigateToFreeDraw: () -> Unit,
    onNavigateToGlowDraw: () -> Unit,
    onNavigateToStickers: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CardYellow)
    ) {
        // Full-screen background image
        AsyncImage(
            model = R.drawable.home_background,
            contentDescription = "Home Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            topBar = {
                // Header (Matching circular avatar + Coins + Settings/Gallery)
                HomeHeader(
                    onBack = onBack,
                    onGalleryClick = {
                        viewModel.playClickSound()
                        onNavigateToGallery()
                    },
                    onSettingsClick = {
                        viewModel.playClickSound()
                        onNavigateToSettings()
                    }
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BannerAd()
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 2. 2x2 Grid of game cards with horizontal padding
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            CountrysideMenuCard(
                                title = "Coloring Book",
                                labelBgColor = CountryGrassDark,
                                labelTextColor = Color.White,
                                illustration = { ColoringBookIcon(Modifier.size(80.dp)) },
                                onClick = {
                                    viewModel.playClickSound()
                                    onNavigateToColoringBook()
                                }
                            )
                        }
                        item {
                            CountrysideMenuCard(
                                title = "Free Draw",
                                labelBgColor = ButtonOrange,
                                labelTextColor = Color.White,
                                illustration = { FreeDrawIcon(Modifier.size(80.dp)) },
                                onClick = {
                                    viewModel.playClickSound()
                                    onNavigateToFreeDraw()
                                }
                            )
                        }
                        item {
                            CountrysideMenuCard(
                                title = "Glow Draw",
                                labelBgColor = GlowYellow,
                                labelTextColor = TextDarkGreen,
                                illustration = { GlowDrawIcon(Modifier.size(80.dp)) },
                                onClick = {
                                    viewModel.playClickSound()
                                    onNavigateToGlowDraw()
                                }
                            )
                        }
                        item {
                            CountrysideMenuCard(
                                title = "Stickers",
                                labelBgColor = PastelBlueDark,
                                labelTextColor = Color.White,
                                illustration = { StickersIcon(Modifier.size(80.dp)) },
                                onClick = {
                                    viewModel.playClickSound()
                                    onNavigateToStickers()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top Header matching the first reference UI screen:
 * - Left: Circular fox avatar
 * - Middle: Round star badge ("10")
 * - Right: Gallery and Settings buttons
 */
@Composable
private fun HomeHeader(
    onBack: () -> Unit,
    onGalleryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Back button
        PlayfulIconButton(
            onClick = onBack,
            backgroundColor = PastelPeach,
            modifier = Modifier.size(42.dp)
        ) {
            CozyBackIcon(modifier = Modifier.size(18.dp), color = TextDark)
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Middle: App Title
        Text(
            text = "Colorit 🎨",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDarkGreen
            ),
            modifier = Modifier.weight(1f)
        )

        // Right: Gallery and Settings
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery
            PlayfulIconButton(
                onClick = onGalleryClick,
                backgroundColor = Color.White,
                modifier = Modifier.size(42.dp)
            ) {
                CozyGalleryIcon(modifier = Modifier.size(20.dp), color = CountryOutline)
            }

            // Settings
            PlayfulIconButton(
                onClick = onSettingsClick,
                backgroundColor = Color.White,
                modifier = Modifier.size(42.dp)
            ) {
                CozySettingsIcon(modifier = Modifier.size(20.dp), color = CountryOutline)
            }
        }
    }
}

/**
 * Hand-drawn rounded menu card.
 * Bottom 25% of the card is a colored banner container carrying the text label.
 */
@Composable
private fun CountrysideMenuCard(
    title: String,
    labelBgColor: Color,
    labelTextColor: Color,
    illustration: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "CardScale"
    )

    Box(
        modifier = Modifier
            .aspectRatio(0.9f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(6.dp, RoundedCornerShape(24.dp))
            .background(CardYellow, RoundedCornerShape(24.dp))
            .border(3.dp, CountryOutline, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper: Illustration
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                illustration()
            }

            // Bottom: Bubbly text label container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(labelBgColor)
                    .border(3.dp, CountryOutline),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = labelTextColor
                )
            }
        }
    }
}



/**
 * Character group graphic drawing (fox, cat, rabbit, frog) standing in front of green hills.
 */
/**
 * Character group graphic drawing (fox, cat, rabbit, frog) standing in front of green hills.
 */
