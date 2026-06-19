package com.starkified.colorit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artworks")
data class ArtworkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val filePath: String,
    val category: String,
    val canvasType: String, // COLORING_BOOK, FREE_DRAW, GLOW_DRAW, STICKER
    val timestamp: Long = System.currentTimeMillis()
)
