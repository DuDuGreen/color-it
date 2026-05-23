package com.example.colorit.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.ui.theme.*
import com.example.colorit.utils.AudioManager

/**
 * Bubbly, playful Dashboard for ColorIt Kids.
 * Contains large grid cards with rich pastel gradients, illustrative child emojis,
 * tap-responsive bounce mechanics, and standard navigation triggers.
 */
@Composable
fun HomeScreen(
    onNavigateToColoringBook: () -> Unit,
    onNavigateToFreeDraw: () -> Unit,
    onNavigateToGlowDraw: () -> Unit,
    onNavigateToStickers: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhite)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header Panel ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ColorIt Kids! 🧸",
                    color = AccentPurple,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Welcome to your magic studio!",
                    color = TextLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            // Protected Settings Gear
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .background(White, RoundedCornerShape(16.dp))
                    .clickable {
                        AudioManager.playTapSound()
                        onNavigateToSettings()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚙️", fontSize = 26.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Cards Grid Deck ---
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Card 1: Coloring Book
            ActivityCard(
                title = "Coloring Book 🎨",
                subtitle = "Fill cute animals, dinos, and letters!",
                startColor = PastelPink,
                endColor = AccentPink,
                onClick = onNavigateToColoringBook
            )

            // Card 2: Free Draw
            ActivityCard(
                title = "Free Draw ✏️",
                subtitle = "Draw, paint, and express anything!",
                startColor = PastelBlue,
                endColor = AccentBlue,
                onClick = onNavigateToFreeDraw
            )

            // Card 3: Glow Neon Draw
            ActivityCard(
                title = "Neon Glow Draw ✨",
                subtitle = "Shine bright with sparkling lights!",
                startColor = Color(0xFF311B92),
                endColor = NeonPurple,
                titleColor = White,
                subtitleColor = SoftGray,
                onClick = onNavigateToGlowDraw
            )

            // Card 4: Stickers
            ActivityCard(
                title = "Sticker Fun 🦄",
                subtitle = "Place, rotate, and resize cute stickers!",
                startColor = PastelGreen,
                endColor = AccentGreen,
                onClick = onNavigateToStickers
            )

            // Card 5: Gallery
            ActivityCard(
                title = "My Art Gallery 🖼️",
                subtitle = "View and share your saved masterpieces!",
                startColor = PastelYellow,
                endColor = AccentYellow,
                onClick = onNavigateToGallery
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

/**
 * Beautiful, bounce-on-tap Activity card with customizable gradient brush.
 */
@Composable
fun ActivityCard(
    title: String,
    subtitle: String,
    startColor: Color,
    endColor: Color,
    onClick: () -> Unit,
    titleColor: Color = TextDark,
    subtitleColor: Color = TextLight
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "CardTapBounce"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(6.dp, RoundedCornerShape(28.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(startColor, endColor)
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .clip(RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                AudioManager.playTapSound()
                onClick()
            }
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = titleColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = subtitleColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
