package com.example.colorit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.colorit.model.ToolType
import com.example.colorit.ui.theme.AccentPink
import com.example.colorit.ui.theme.AccentPurple
import com.example.colorit.ui.theme.PastelPink
import com.example.colorit.ui.theme.PlayfulPalette
import com.example.colorit.ui.theme.SoftGray
import com.example.colorit.ui.theme.White
import com.example.colorit.utils.AudioManager

/**
 * Custom toolbar for selecting drawing utensils (pencil, marker, crayon, brush, eraser)
 * and sliders for brush sizing/opacity that feature large, kid-friendly touch regions.
 */
@Composable
fun ToolSelector(
    selectedTool: ToolType,
    onToolSelected: (ToolType) -> Unit,
    brushSize: Float,
    onBrushSizeChange: (Float) -> Unit,
    brushOpacity: Float,
    onBrushOpacityChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = AccentPurple,
    showOpacity: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .shadow(16.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Tool selection buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tools = listOf(
                ToolOption(ToolType.PENCIL, "✏️", "Pencil"),
                ToolOption(ToolType.MARKER, "🖋️", "Marker"),
                ToolOption(ToolType.CRAYON, "🖍️", "Crayon"),
                ToolOption(ToolType.BRUSH, "🖌️", "Brush"),
                ToolOption(ToolType.ERASER, "🧽", "Eraser")
            )

            tools.forEach { toolOpt ->
                val isSelected = toolOpt.type == selectedTool
                val scale by animateFloatAsState(if (isSelected) 1.2f else 1.0f, label = "ToolButtonScale")
                val bgCol by animateColorAsState(if (isSelected) accentColor else SoftGray, label = "ToolButtonBg")

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.scale(scale)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(if (isSelected) 6.dp else 2.dp, CircleShape)
                            .background(bgCol, CircleShape)
                            .border(3.dp, if (isSelected) White else Color.Transparent, CircleShape)
                            .clickable {
                                AudioManager.playTapSound()
                                onToolSelected(toolOpt.type)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = toolOpt.emoji,
                            fontSize = 26.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = toolOpt.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) accentColor else Color.Gray
                    )
                }
            }
        }

        // 2. Sliders Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Brush Size Slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Size 🔘",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.width(64.dp)
                )
                Slider(
                    value = brushSize,
                    onValueChange = onBrushSizeChange,
                    valueRange = 5f..80f,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor.copy(alpha = 0.5f),
                        inactiveTrackColor = SoftGray
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            // Brush Opacity Slider
            if (showOpacity) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Paint 💨",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.width(64.dp)
                    )
                    Slider(
                        value = brushOpacity,
                        onValueChange = onBrushOpacityChange,
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor.copy(alpha = 0.5f),
                            inactiveTrackColor = SoftGray
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private data class ToolOption(
    val type: ToolType,
    val emoji: String,
    val label: String
)
