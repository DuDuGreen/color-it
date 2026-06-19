package com.starkified.colorit.data.model

import androidx.compose.ui.geometry.Offset

data class StickerInstance(
    val id: String,
    val emoji: String,
    val position: Offset,
    val scale: Float = 1.0f,
    val rotation: Float = 0f
)
