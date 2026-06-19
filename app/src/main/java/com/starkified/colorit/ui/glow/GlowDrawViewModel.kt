package com.starkified.colorit.ui.glow

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starkified.colorit.data.model.BrushStroke
import com.starkified.colorit.data.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class GlowDrawViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository
) : ViewModel() {

    private val _strokes = MutableStateFlow<List<BrushStroke>>(emptyList())
    val strokes: StateFlow<List<BrushStroke>> = _strokes.asStateFlow()

    // Cyan Neon by default
    private val _selectedColor = MutableStateFlow(Color(0xFF00F5FF))
    val selectedColor: StateFlow<Color> = _selectedColor.asStateFlow()

    private val _brushSize = MutableStateFlow(16f)
    val brushSize: StateFlow<Float> = _brushSize.asStateFlow()

    // Undo/Redo Stacks
    private val undoStack = mutableListOf<List<BrushStroke>>()
    private val redoStack = mutableListOf<List<BrushStroke>>()

    fun selectColor(color: Color) {
        _selectedColor.value = color
    }

    fun updateBrushSize(size: Float) {
        _brushSize.value = size
    }

    fun addStroke(stroke: BrushStroke) {
        saveToUndoStack()
        _strokes.value = _strokes.value + stroke
    }

    fun clearAll() {
        saveToUndoStack()
        _strokes.value = emptyList()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(_strokes.value)
            _strokes.value = undoStack.removeAt(undoStack.lastIndex)
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(_strokes.value)
            _strokes.value = redoStack.removeAt(redoStack.lastIndex)
        }
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    private fun saveToUndoStack() {
        undoStack.add(_strokes.value)
        redoStack.clear()
        
        if (undoStack.size > 30) {
            undoStack.removeAt(0)
        }
    }

    /**
     * Renders dark glow canvas to a PNG file, replicating the dual neon glow shader.
     */
    fun saveGlowDrawingToPng(cacheDir: File, width: Int = 1024, height: Int = 1024): File? {
        try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Draw deep night-sky slate background
            canvas.drawColor(Color(0xFF0F172A).toArgb())

            // Setup double neon paints (inner solid and outer blur shadow)
            val glowPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }
            val solidPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                color = android.graphics.Color.WHITE // Solid core is bright white or light-hued neon
                isAntiAlias = true
            }

            for (stroke in _strokes.value) {
                if (stroke.points.isEmpty()) continue

                // 1. Draw outer glowing blur path
                val size = stroke.size
                glowPaint.color = stroke.color.copy(alpha = 0.4f).toArgb()
                glowPaint.strokeWidth = size * 2.2f
                glowPaint.maskFilter = BlurMaskFilter(size * 0.8f, BlurMaskFilter.Blur.NORMAL)

                val path = android.graphics.Path()
                val first = stroke.points.first()
                path.moveTo(first.x, first.y)
                for (i in 1 until stroke.points.size) {
                    path.lineTo(stroke.points[i].x, stroke.points[i].y)
                }
                canvas.drawPath(path, glowPaint)

                // 2. Draw inner neon core path (no blur)
                solidPaint.color = Color.White.copy(alpha = 0.9f).toArgb()
                solidPaint.strokeWidth = size * 0.7f
                canvas.drawPath(path, solidPaint)
                
                // Draw a third layer: thin neon color on top
                val coreNeonPaint = Paint(solidPaint).apply {
                    color = stroke.color.toArgb()
                    strokeWidth = size * 0.5f
                }
                canvas.drawPath(path, coreNeonPaint)
            }

            val file = File(cacheDir, "glow_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Renders to PNG and saves permanently in the Room database gallery folder
     */
    fun saveToGallery(cacheDir: File, width: Int = 1024, height: Int = 1024, onComplete: (File?) -> Unit) {
        val file = saveGlowDrawingToPng(cacheDir, width, height)
        if (file != null) {
            viewModelScope.launch {
                galleryRepository.saveDrawing(file, "Glow Draw")
                val drawingsDir = File(cacheDir.parentFile, "files/drawings")
                val savedFile = File(drawingsDir, file.name)
                onComplete(savedFile)
            }
        } else {
            onComplete(null)
        }
    }
}
