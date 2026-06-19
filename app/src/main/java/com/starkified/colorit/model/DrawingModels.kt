package com.starkified.colorit.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

enum class ToolType {
    PENCIL,
    MARKER,
    CRAYON,
    BRUSH,
    ERASER,
    GLOW
}

data class DrawPath(
    val points: List<Offset>,
    val color: Color,
    val size: Float,
    val opacity: Float = 1.0f,
    val tool: ToolType = ToolType.BRUSH
)

data class StickerItem(
    val id: String,
    val category: String, // Animals, Emojis, Stars, Vehicles, Shapes
    val label: String,    // Unicode symbol or icon path name
    var x: Float,
    var y: Float,
    var scale: Float = 1.0f,
    var rotation: Float = 0.0f
)

data class ColorableRegion(
    val id: Int,
    val label: String, // descriptive name like "Head", "Wing", "Star"
    var color: Color = Color.White,
    val drawCommand: (Path) -> Unit // block to generate the Path vector outline
)

data class ColoringPage(
    val id: Int,
    val name: String,
    val category: String, // Animals, Dinosaurs, Vehicles, Nature, Space, Alphabets, Numbers
    val regions: List<ColorableRegion>
)
