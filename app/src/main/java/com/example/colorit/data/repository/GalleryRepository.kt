package com.example.colorit.data.repository

import android.content.Context
import com.example.colorit.data.database.DrawingDao
import com.example.colorit.data.database.SavedDrawing
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GalleryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val drawingDao: DrawingDao
) {
    private val drawingsDir = File(context.filesDir, "drawings").apply {
        if (!exists()) mkdirs()
    }

    fun getAllDrawings(): Flow<List<SavedDrawing>> = drawingDao.getAllDrawings()

    /**
     * Copies drawing file from temporary cache to permanent app files storage
     * and inserts the metadata entry in Room database.
     */
    suspend fun saveDrawing(tempFile: File, title: String) = withContext(Dispatchers.IO) {
        if (!tempFile.exists()) return@withContext
        
        try {
            val destFile = File(drawingsDir, tempFile.name)
            tempFile.copyTo(destFile, overwrite = true)
            
            // Delete temp file after copy
            tempFile.delete()

            val drawingRecord = SavedDrawing(
                filePath = destFile.absolutePath,
                title = title,
                timestamp = System.currentTimeMillis()
            )
            drawingDao.insertDrawing(drawingRecord)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Deletes drawing file from disk and deletes database record
     */
    suspend fun deleteDrawing(drawing: SavedDrawing) = withContext(Dispatchers.IO) {
        try {
            val file = File(drawing.filePath)
            if (file.exists()) {
                file.delete()
            }
            drawingDao.deleteDrawing(drawing)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Clear all drawings from gallery
     */
    suspend fun clearAll() = withContext(Dispatchers.IO) {
        try {
            val files = drawingsDir.listFiles()
            files?.forEach { it.delete() }
            drawingDao.deleteAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
