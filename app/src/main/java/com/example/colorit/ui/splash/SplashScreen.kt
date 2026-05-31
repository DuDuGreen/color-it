package com.example.colorit.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.ui.theme.PastelBlue
import com.example.colorit.ui.theme.PastelPink
import com.example.colorit.ui.theme.PastelYellow
import com.example.colorit.ui.theme.TextDark
import com.example.colorit.util.SoundHelper
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    soundHelper: SoundHelper,
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        soundHelper.playChimeSound()
        
        // Bounce animation
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
        
        // Hold for 1.5 seconds, then transition
        delay(1500)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PastelPink.copy(alpha = 0.6f),
                        PastelBlue.copy(alpha = 0.6f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Splash bubbly circle logo
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale.value)
                    .background(PastelYellow, shape = CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.White, Color.Transparent),
                            radius = 180f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Simple brush emoji/text representation as placeholder icon
                Text(
                    text = "🎨",
                    fontSize = 72.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name text
            Text(
                text = "Color It!",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                ),
                modifier = Modifier.scale(scale.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Play & Color",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark.copy(alpha = 0.6f)
                ),
                modifier = Modifier.scale(alpha.value)
            )
        }
    }
}
