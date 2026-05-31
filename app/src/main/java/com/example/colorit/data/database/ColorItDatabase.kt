package com.example.colorit.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SavedDrawing::class], version = 1, exportSchema = false)
abstract class ColorItDatabase : RoomDatabase() {
    abstract fun drawingDao(): DrawingDao
}
