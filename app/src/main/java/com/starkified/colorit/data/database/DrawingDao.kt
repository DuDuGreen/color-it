package com.starkified.colorit.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawingDao {

    @Query("SELECT * FROM saved_drawings ORDER BY timestamp DESC")
    fun getAllDrawings(): Flow<List<SavedDrawing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrawing(drawing: SavedDrawing)

    @Delete
    suspend fun deleteDrawing(drawing: SavedDrawing)

    @Query("DELETE FROM saved_drawings")
    suspend fun deleteAll()
}
