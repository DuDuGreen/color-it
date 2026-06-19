package com.starkified.colorit.ui.coloring

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.starkified.colorit.ui.components.*
import com.starkified.colorit.ui.theme.*
import com.starkified.colorit.util.SoundHelper

@Composable
fun CategorySelectionScreen(
    viewModel: ColoringBookViewModel,
    soundHelper: SoundHelper,
    onPageSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    val headerTitle = when (selectedCategory) {
        null -> "Coloring Book 📚"
        "All" -> "All Pages 🌈"
        "Animals" -> "Animals 🐱"
        "Nature" -> "Nature 🌸"
        "Vehicles" -> "Vehicles 🚗"
        "Space" -> "Space 🚀"
        "Dinosaurs" -> "Dinosaurs 🦖"
        "Birds" -> "Birds 🐦"
        else -> "$selectedCategory 🔢"
    }

    // Wrap the entire screen in the interactive BubbleBackground
    BubbleBackground(modifier = modifier) {
        Scaffold(
            topBar = {
                CategoryHeader(
                    title = headerTitle,
                    onBack = {
                        soundHelper.playPopSound()
                        if (selectedCategory != null) {
                            selectedCategory = null
                        } else {
                            onBack()
                        }
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
            containerColor = Color.Transparent // Allow BubbleBackground to show through
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (selectedCategory == null) {
                    // Category Selection Grid View (showing all categories in a 2-column grid layout)
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(viewModel.categories) { index, category ->
                            val popInScale = remember(category) { Animatable(0.9f) }
                            LaunchedEffect(key1 = category) {
                                popInScale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = 0.7f,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }

                            val pageCount = viewModel.getPagesByCategory(category).size
                            CategoryCard(
                                name = category,
                                pageCount = pageCount,
                                onClick = {
                                    soundHelper.playPopSound()
                                    selectedCategory = category
                                },
                                modifier = Modifier.graphicsLayer {
                                    scaleX = popInScale.value
                                    scaleY = popInScale.value
                                }
                            )
                        }
                    }
                } else {
                    // Pages selection view
                    val pages = remember(selectedCategory) {
                        viewModel.getPagesByCategory(selectedCategory!!)
                    }

                    if (pages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No pages here yet! 🎨",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkGreen
                                )
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(pages, key = { _, page -> page.id }) { index, page ->
                                PageCard(
                                    title = page.title,
                                    category = page.category,
                                    imageResName = page.imageResName,
                                    onClick = {
                                        soundHelper.playPopSound()
                                        onPageSelected(page.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayfulIconButton(
            onClick = onBack,
            backgroundColor = Color.White,
            contentColor = TextDarkGreen,
            modifier = Modifier.size(44.dp)
        ) {
            CozyBackIcon(modifier = Modifier.size(20.dp), color = TextDarkGreen)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDarkGreen
            )
        )
    }
}

@Composable
private fun CategoryCard(
    name: String,
    pageCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (emoji, cardColor) = when (name) {
        "All" -> Pair("🌈", CozyRose)
        "Animals" -> Pair("🐱", CountryGrassDark)
        "Nature" -> Pair("🌸", GlowMint)
        "Vehicles" -> Pair("🚗", ButtonOrange)
        "Space" -> Pair("🚀", GlowPink)
        "Dinosaurs" -> Pair("🦖", PastelPurple)
        "Birds" -> Pair("🐦", CountrySky)
        else -> Pair("🔢", GlowYellow)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "CategoryCardPress"
    )

    PastelCard(
        backgroundColor = CardYellow,
        borderColor = CountryOutline,
        shadowElevation = if (isPressed) 2.dp else 6.dp,
        contentPadding = 0.dp,
        modifier = modifier
            .aspectRatio(1.1f)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .border(3.dp, CountryOutline, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
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
                    .size(64.dp)
                    .background(cardColor.copy(alpha = 0.15f), shape = CircleShape)
                    .border(2.dp, CountryOutline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 36.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDarkGreen
                )
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = if (pageCount == 1) "1 page" else "$pageCount pages",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLightGreen
                )
            )
        }
    }
}

@Composable
private fun PageCard(
    title: String,
    category: String,
    imageResName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = when (category) {
        "Animals" -> CountryGrassDark
        "Nature" -> GlowMint
        "Vehicles" -> ButtonOrange
        "Space" -> GlowPink
        else -> GlowYellow
    }

    val context = LocalContext.current
    val imageResId = remember(imageResName) {
        if (imageResName != null) {
            context.resources.getIdentifier(imageResName, "drawable", context.packageName)
        } else {
            0
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "PageCardPress"
    )

    PastelCard(
        backgroundColor = CardYellow,
        borderColor = CountryOutline,
        shadowElevation = if (isPressed) 2.dp else 6.dp,
        contentPadding = 0.dp,
        modifier = modifier
            .aspectRatio(1.0f)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .border(3.dp, CountryOutline, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .border(2.dp, CountryOutline, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (imageResId != 0) {
                    AsyncImage(
                        model = imageResId,
                        contentDescription = title,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = when (category) {
                            "Animals" -> "🐱"
                            "Nature" -> "🌸"
                            "Vehicles" -> "🚗"
                            "Space" -> "🚀"
                            else -> "🔢"
                        },
                        fontSize = 32.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDarkGreen
                ),
                maxLines = 1
            )
        }
    }
}
