package com.example.colorit.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class ColoringShape(
    val id: String,
    val pathData: String // SVG Path data (e.g., "M 10 10 L 90 10...")
)

data class ColoringPage(
    val id: String,
    val title: String,
    val category: String,
    val shapes: List<ColoringShape>
)

data class BrushStroke(
    val points: List<Offset>,
    val color: Color,
    val size: Float
)
