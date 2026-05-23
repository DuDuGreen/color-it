package com.example.colorit.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.colorit.ui.theme.White
import com.example.colorit.utils.AudioManager

/**
 * Highly interactive, colorful, and bubbly button designed for children.
 * Features a playful bounce/scale animation on press, soft shadows, rounded corners,
 * and automatically triggers a synthesized bubble-pop tap sound!
 */
@Composable
fun KidsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF7C4DFF), // Primary playful violet
    borderColor: Color = backgroundColor.copy(alpha = 0.5f),
    contentColor: Color = White,
    fontSize: Int = 18,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth spring-like bounce scale animation on tap
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "ButtonScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false
            )
            .border(4.dp, borderColor, RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.85f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null // remove default grey ripple for customized spring feel
            ) {
                AudioManager.playTapSound()
                onClick()
            }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
