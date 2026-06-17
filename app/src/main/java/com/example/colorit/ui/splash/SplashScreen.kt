package com.example.colorit.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.R
import com.example.colorit.ui.components.BubbleBackground
import com.example.colorit.ui.theme.*
import com.example.colorit.util.SoundHelper
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    soundHelper: SoundHelper,
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CardYellow)
    ) {
        // 1. Full-screen background illustration
        AsyncImage(
            model = R.drawable.landing_screen,
            contentDescription = "Welcome Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Overlaid UI Controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Header Subtitle
            Text(
                text = "Where Creative Kids Color",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDarkGreen.copy(alpha = 0.8f)
                ),
                modifier = Modifier.padding(top = 24.dp)
            )

            // 2. Large Brand Title: "COLOR IT!"
            BubblyBrandTitle()

            // 3. Spacer to let central illustration show through
            Spacer(modifier = Modifier.weight(1f))

            // 4. Play button: "Let's Color"
            LetsColorButton(
                onClick = {
                    soundHelper.playCozySound()
                    onSplashFinished()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BubblyBrandTitle() {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val word1 = listOf(
            Pair("C", GlowPurple),
            Pair("O", GlowCyan),
            Pair("L", GlowPink),
            Pair("O", GlowYellow),
            Pair("R", GlowMint)
        )
        val word2 = listOf(
            Pair("I", GlowPurple),
            Pair("T", GlowCyan)
        )

        // Draw COLOR
        word1.forEachIndexed { idx, pair ->
            val rotation = if (idx % 2 == 0) -8f else 8f
            LetterCard(char = pair.first, color = pair.second, rotation = rotation)
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Draw IT
        word2.forEachIndexed { idx, pair ->
            val rotation = if (idx % 2 == 0) 8f else -8f
            LetterCard(char = pair.first, color = pair.second, rotation = rotation)
        }
    }
}

@Composable
private fun LetterCard(char: String, color: Color, rotation: Float) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .graphicsLayer { rotationZ = rotation }
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .background(color, RoundedCornerShape(10.dp))
            .border(3.dp, CountryOutline, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

/**
 * Large bubbly play button: orange background, thick dark outline, play icon.
 */

/**
 * Large bubbly play button: orange background, thick dark outline, play icon.
 */
@Composable
private fun LetsColorButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "PlayButtonScale"
    )

    Row(
        modifier = Modifier
            .scale(scale)
            .shadow(6.dp, RoundedCornerShape(30.dp))
            .background(ButtonOrange, RoundedCornerShape(30.dp))
            .border(3.dp, CountryOutline, RoundedCornerShape(30.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 32.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Let's Color",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        // Play chevron inside white circle
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color.White, CircleShape)
                .border(2.dp, CountryOutline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▶",
                color = ButtonOrange,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}
