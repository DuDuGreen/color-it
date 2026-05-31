package com.example.colorit.ui.freedraw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colorit.data.model.BrushStroke
import com.example.colorit.data.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

enum class FreeDrawTool {
    PENCIL, MARKER, BRUSH, ERASER
}

@HiltViewModel
class FreeDrawViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository
) : ViewModel() {

    private val _strokes = MutableStateFlow<List<BrushStroke>>(emptyList())
    val strokes: StateFlow<List<BrushStroke>> = _strokes.asStateFlow()

    private val _selectedColor = MutableStateFlow(Color(0xFFFFC6FF)) // Default PastelPink
    val selectedColor: StateFlow<Color> = _selectedColor.asStateFlow()

    private val _selectedTool = MutableStateFlow(FreeDrawTool.PENCIL)
    val selectedTool: StateFlow<FreeDrawTool> = _selectedTool.asStateFlow()

    private val _brushSize = MutableStateFlow(12f)
    val brushSize: StateFlow<Float> = _brushSize.asStateFlow()

    private val _opacity = MutableStateFlow(1f)
    val opacity: StateFlow<Float> = _opacity.asStateFlow()

    // Undo/Redo history stacks
    private val undoStack = mutableListOf<List<BrushStroke>>()
    private val redoStack = mutableListOf<List<BrushStroke>>()

    fun selectColor(color: Color) {
        _selectedColor.value = color
    }

    fun selectTool(tool: FreeDrawTool) {
        _selectedTool.value = tool
        // Set standard baseline options for tools
        when (tool) {
            FreeDrawTool.PENCIL -> {
                _brushSize.value = 6f
                _opacity.value = 1.0f
            }
            FreeDrawTool.MARKER -> {
                _brushSize.value = 16f
                _opacity.value = 1.0f
            }
            FreeDrawTool.BRUSH -> {
                _brushSize.value = 28f
                _opacity.value = 0.5f
            }
            FreeDrawTool.ERASER -> {
                _brushSize.value = 32f
                _opacity.value = 1.0f
            }
        }
    }

    fun updateBrushSize(size: Float) {
        _brushSize.value = size
    }

    fun updateOpacity(opacity: Float) {
        _opacity.value = opacity
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
        redoStack.clear() // Clear redo stack on new actions
        
        // Cap history length to 30 strokes to prevent OOM
        if (undoStack.size > 30) {
            undoStack.removeAt(0)
        }
    }

    /**
     * Renders free draw canvas strokes to a PNG file
     */
    fun saveDrawingToPng(cacheDir: File, width: Int = 1024, height: Int = 1024): File? {
        try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Draw creamy canvas background
            canvas.drawColor(Color(0xFFFCFBF7).toArgb())

            val paint = Paint().apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }

            // Normalization scaling factor (mapping runtime canvas sizes back to fixed dimensions)
            // For simple free draw export, we will map strokes drawn in relative ratios or scale according to design.
            for (stroke in _strokes.value) {
                if (stroke.points.isEmpty()) continue
                
                paint.color = stroke.color.toArgb()
                paint.strokeWidth = stroke.size

                val path = android.graphics.Path()
                val first = stroke.points.first()
                path.moveTo(first.x, first.y)
                
                for (i in 1 until stroke.points.size) {
                    val p = stroke.points[i]
                    path.lineTo(p.x, p.y)
                }
                canvas.drawPath(path, paint)
            }

            val file = File(cacheDir, "drawing_${System.currentTimeMillis()}.png")
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
        val file = saveDrawingToPng(cacheDir, width, height)
        if (file != null) {
            viewModelScope.launch {
                galleryRepository.saveDrawing(file, "Free Draw")
                val drawingsDir = File(cacheDir.parentFile, "files/drawings")
                val savedFile = File(drawingsDir, file.name)
                onComplete(savedFile)
            }
        } else {
            onComplete(null)
        }
    }
}
