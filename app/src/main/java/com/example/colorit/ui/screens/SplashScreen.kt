package com.example.colorit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.ui.theme.*
import com.example.colorit.utils.AudioManager
import kotlinx.coroutines.delay

/**
 * Splash Screen featuring playful bouncy character branding, circular pastel blobs,
 * a loading bar, and triggering the success chord melody on creation before routing to Home.
 */
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Start sound on load
    LaunchedEffect(Unit) {
        AudioManager.playSuccessSound()
        delay(2500) // 2.5s delay
        onNavigateToHome()
    }

    // Cute bouncing scale animation
    val infiniteTransition = rememberInfiniteTransition(label = "SplashBounce")
    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoBounce"
    )

    // Animated loading progress dot row
    val dots = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) }
    )

    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay(index * 150L)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PastelPink, PastelBlue)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Mascot / Logo Icon Shape
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(bounceScale)
                    .background(White, CircleShape)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎨",
                    fontSize = 80.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Playful Title
            Text(
                text = "ColorIt Kids!",
                color = AccentPurple,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.scale(bounceScale)
            )

            Text(
                text = "Paint, Draw & Sparkle! ✨",
                color = AccentPink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Loading dot indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dots.forEach { anim ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .scale(anim.value)
                            .background(
                                color = if (anim.value > 0.5f) AccentPurple else AccentPink,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}
