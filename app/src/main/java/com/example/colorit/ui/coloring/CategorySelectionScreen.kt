package com.example.colorit.ui.coloring

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.ui.components.PastelCard
import com.example.colorit.ui.components.PlayfulButton
import com.example.colorit.ui.theme.PastelBlue
import com.example.colorit.ui.theme.PastelMint
import com.example.colorit.ui.theme.PastelPeach
import com.example.colorit.ui.theme.PastelPink
import com.example.colorit.ui.theme.PastelPurple
import com.example.colorit.ui.theme.PastelYellow
import com.example.colorit.ui.theme.TextDark
import com.example.colorit.util.SoundHelper

@Composable
fun CategorySelectionScreen(
    viewModel: ColoringBookViewModel,
    soundHelper: SoundHelper,
    onPageSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("Animals") }

    Scaffold(
        topBar = {
            CategoryHeader(
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
            // Category Capsule List Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(viewModel.categories) { category ->
                    val isSelected = category == selectedCategory
                    val bgColor = when (category) {
                        "Animals" -> PastelPink
                        "Nature" -> PastelMint
                        "Vehicles" -> PastelBlue
                        "Space" -> PastelPurple
                        else -> PastelYellow
                    }

                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = if (isSelected) 6.dp else 2.dp,
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .background(if (isSelected) bgColor else Color.White)
                            .clickable {
                                soundHelper.playPopSound()
                                selectedCategory = category
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pages List Grid
            val pages = viewModel.getPagesByCategory(selectedCategory)
            
            if (pages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No pages here yet! 🎨", color = TextDark)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pages) { page ->
                        PageCard(
                            title = page.title,
                            category = page.category,
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

@Composable
private fun CategoryHeader(
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
            text = "Coloring Book 📚",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
        )
    }
}

@Composable
private fun PageCard(
    title: String,
    category: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = when (category) {
        "Animals" -> PastelPink
        "Nature" -> PastelMint
        "Vehicles" -> PastelBlue
        "Space" -> PastelPurple
        else -> PastelYellow
    }

    PastelCard(
        backgroundColor = Color.White,
        borderColor = cardColor.copy(alpha = 0.5f),
        shadowElevation = 4.dp,
        contentPadding = 0.dp,
        modifier = modifier
            .aspectRatio(1.0f)
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Draw a cute outline icon box as placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(cardColor.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            )
        }
    }
}
