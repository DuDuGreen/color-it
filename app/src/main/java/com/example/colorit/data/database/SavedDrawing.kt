package com.example.colorit.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_drawings")
data class SavedDrawing(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val filePath: String,
    val title: String,
    val timestamp: Long
)
