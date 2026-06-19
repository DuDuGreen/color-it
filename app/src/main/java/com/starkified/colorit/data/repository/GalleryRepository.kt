package com.starkified.colorit.data.repository

import android.content.Context
import com.starkified.colorit.data.database.DrawingDao
import com.starkified.colorit.data.database.SavedDrawing
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import android.content.ContentValues
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.FileInputStream
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
     * and inserts the metadata entry in Room database. Also saves a copy of the
     * drawing to the device's public MediaStore (Gallery).
     */
    suspend fun saveDrawing(tempFile: File, title: String) = withContext(Dispatchers.IO) {
        if (!tempFile.exists()) return@withContext
        
        try {
            val destFile = File(drawingsDir, tempFile.name)
            tempFile.copyTo(destFile, overwrite = true)
            
            // Delete temp file after copy
            tempFile.delete()

            // Save copy to the system gallery
            saveImageToSystemGallery(context, destFile, title)

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

    private fun saveImageToSystemGallery(context: Context, file: File, title: String): Uri? {
        if (!file.exists()) return null
        
        val contentResolver = context.contentResolver
        val filename = "${title.replace(" ", "_")}_${System.currentTimeMillis()}.png"
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val currentTime = System.currentTimeMillis() / 1000
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Colorit/")
                put(MediaStore.Images.Media.IS_PENDING, 1)
                put(MediaStore.Images.Media.DATE_ADDED, currentTime)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.Images.Media.DATE_MODIFIED, currentTime)
            }
            
            val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                try {
                    contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                        FileInputStream(file).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(imageUri, contentValues, null, null)
                    imageUri
                } catch (e: Exception) {
                    e.printStackTrace()
                    contentResolver.delete(imageUri, null, null)
                    null
                }
            } else {
                null
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(imagesDir, "Colorit")
            if (!appDir.exists()) appDir.mkdirs()
            
            val destFile = File(appDir, filename)
            try {
                FileInputStream(file).use { inputStream ->
                    destFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                MediaScannerConnection.scanFile(context, arrayOf(destFile.absolutePath), arrayOf("image/png"), null)
                Uri.fromFile(destFile)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
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
