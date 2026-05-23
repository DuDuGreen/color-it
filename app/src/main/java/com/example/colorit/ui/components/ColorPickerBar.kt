package com.example.colorit.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.colorit.ui.theme.PlayfulPalette
import com.example.colorit.ui.theme.White
import com.example.colorit.utils.AudioManager

/**
 * Scrollable list of vibrant paint pots.
 * Supports custom palettes (pastels, standards, or neon glow) and scales up the selected
 * color circle with a checkmark indicator for delightful, child-friendly feedback.
 */
@Composable
fun ColorPickerBar(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    palette: List<Color> = PlayfulPalette
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(palette) { color ->
            val isSelected = color == selectedColor
            val scale by animateFloatAsState(if (isSelected) 1.25f else 1.0f, label = "ColorPotScale")

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .scale(scale)
                    .shadow(
                        elevation = if (isSelected) 8.dp else 4.dp,
                        shape = CircleShape
                    )
                    .border(
                        width = if (isSelected) 4.dp else 2.dp,
                        color = if (isSelected) White else color.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .background(color, CircleShape)
                    .clickable {
                        AudioManager.playTapSound()
                        onColorSelected(color)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = if (isLightColor(color)) Color.Black else White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Quick helper to determine color luminance for visual contrast
 */
fun isLightColor(color: Color): Boolean {
    val r = color.red
    val g = color.green
    val b = color.blue
    val luminance = 0.299 * r + 0.587 * g + 0.114 * b
    return luminance > 0.6
}
