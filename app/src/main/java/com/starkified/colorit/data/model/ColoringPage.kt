package com.starkified.colorit.data.model

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
    val shapes: List<ColoringShape>,
    val imageResName: String? = null
)

data class BrushStroke(
    val points: List<Offset>,
    val color: Color,
    val size: Float,
    val isStraightLine: Boolean = false
)
