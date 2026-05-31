package com.example.colorit.ui.coloring

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.PathParser
import androidx.lifecycle.ViewModel
import com.example.colorit.data.model.BrushStroke
import com.example.colorit.data.model.ColoringPage
import com.example.colorit.data.repository.ColoringRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.colorit.data.repository.GalleryRepository
import javax.inject.Inject

enum class ColoringTool {
    FILL, BRUSH
}

data class BoardState(
    val shapeColors: Map<String, Color>,
    val brushStrokes: List<BrushStroke>
)

@HiltViewModel
class ColoringViewModel @Inject constructor(
    private val repository: ColoringRepository,
    private val galleryRepository: GalleryRepository
) : ViewModel() {

    private val _page = MutableStateFlow<ColoringPage?>(null)
    val page: StateFlow<ColoringPage?> = _page.asStateFlow()

    private val _shapeColors = MutableStateFlow<Map<String, Color>>(emptyMap())
    val shapeColors: StateFlow<Map<String, Color>> = _shapeColors.asStateFlow()

    private val _brushStrokes = MutableStateFlow<List<BrushStroke>>(emptyList())
    val brushStrokes: StateFlow<List<BrushStroke>> = _brushStrokes.asStateFlow()

    private val _selectedColor = MutableStateFlow(Color(0xFFFFC6FF)) // PastelPink default
    val selectedColor: StateFlow<Color> = _selectedColor.asStateFlow()

    private val _selectedTool = MutableStateFlow(ColoringTool.FILL)
    val selectedTool: StateFlow<ColoringTool> = _selectedTool.asStateFlow()

    private val _brushSize = MutableStateFlow(12f)
    val brushSize: StateFlow<Float> = _brushSize.asStateFlow()

    // Undo/Redo Stacks
    private val undoStack = mutableListOf<BoardState>()
    private val redoStack = mutableListOf<BoardState>()

    fun loadPage(pageId: String) {
        val loadedPage = repository.getPageById(pageId)
        _page.value = loadedPage
        
        // Reset state
        _shapeColors.value = emptyMap()
        _brushStrokes.value = emptyList()
        undoStack.clear()
        redoStack.clear()
    }

    fun selectColor(color: Color) {
        _selectedColor.value = color
    }

    fun selectTool(tool: ColoringTool) {
        _selectedTool.value = tool
    }

    fun updateBrushSize(size: Float) {
        _brushSize.value = size
    }

    /**
     * Records a shape fill action and updates undo stack
     */
    fun fillShape(shapeId: String) {
        val currentColor = _shapeColors.value[shapeId]
        val newColor = _selectedColor.value
        
        if (currentColor == newColor) return // No change
        
        saveToUndoStack()
        
        val updatedMap = _shapeColors.value.toMutableMap()
        updatedMap[shapeId] = newColor
        _shapeColors.value = updatedMap
    }

    /**
     * Adds a new completed brush stroke and updates undo stack
     */
    fun addBrushStroke(stroke: BrushStroke) {
        saveToUndoStack()
        _brushStrokes.value = _brushStrokes.value + stroke
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val currentState = BoardState(_shapeColors.value, _brushStrokes.value)
            redoStack.add(currentState)
            
            val previousState = undoStack.removeAt(undoStack.lastIndex)
            _shapeColors.value = previousState.shapeColors
            _brushStrokes.value = previousState.brushStrokes
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val currentState = BoardState(_shapeColors.value, _brushStrokes.value)
            undoStack.add(currentState)
            
            val nextState = redoStack.removeAt(redoStack.lastIndex)
            _shapeColors.value = nextState.shapeColors
            _brushStrokes.value = nextState.brushStrokes
        }
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    private fun saveToUndoStack() {
        undoStack.add(BoardState(_shapeColors.value, _brushStrokes.value))
        redoStack.clear() // Clear redo on new actions
        
        // Cap undo history to 20 actions for performance
        if (undoStack.size > 20) {
            undoStack.removeAt(0)
        }
    }

    /**
     * Renders current state to a Bitmap offscreen and saves it as a PNG file
     */
    fun savePageToPng(cacheDir: File, width: Int = 1024, height: Int = 1024): File? {
        val currentPage = _page.value ?: return null
        
        try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Draw creamy kids background
            canvas.drawColor(Color(0xFFFCFBF7).toArgb())

            // Setup paints
            val fillPaint = Paint().apply {
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val outlinePaint = Paint().apply {
                style = Paint.Style.STROKE
                color = android.graphics.Color.BLACK
                strokeWidth = 6f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }
            val brushPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }

            // Path translation scaling factors from original 200x200 design grid
            val scaleX = width / 200f
            val scaleY = height / 200f

            // 1. Draw shapes
            for (shape in currentPage.shapes) {
                val androidPath = PathParser.createPathFromPathData(shape.pathData)
                
                // Scale path to fit offscreen output
                val matrix = android.graphics.Matrix()
                matrix.postScale(scaleX, scaleY)
                androidPath.transform(matrix)

                // Fill color
                val color = _shapeColors.value[shape.id] ?: Color.White
                fillPaint.color = color.toArgb()
                canvas.drawPath(androidPath, fillPaint)

                // Outline
                canvas.drawPath(androidPath, outlinePaint)
            }

            // 2. Draw brush strokes
            for (stroke in _brushStrokes.value) {
                if (stroke.points.isEmpty()) continue
                
                brushPaint.color = stroke.color.toArgb()
                brushPaint.strokeWidth = stroke.size * scaleX // Scale brush thickness

                val path = android.graphics.Path()
                val firstPoint = stroke.points.first()
                path.moveTo(firstPoint.x * scaleX, firstPoint.y * scaleY)
                
                for (i in 1 until stroke.points.size) {
                    val p = stroke.points[i]
                    path.lineTo(p.x * scaleX, p.y * scaleY)
                }
                canvas.drawPath(path, brushPaint)
            }

            // Write to file
            val file = File(cacheDir, "colorit_${currentPage.id}_${System.currentTimeMillis()}.png")
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
        val file = savePageToPng(cacheDir, width, height)
        if (file != null) {
            val currentPage = _page.value
            val title = currentPage?.title ?: "Coloring Page"
            viewModelScope.launch {
                galleryRepository.saveDrawing(file, title)
                val drawingsDir = File(cacheDir.parentFile, "files/drawings")
                val savedFile = File(drawingsDir, file.name)
                onComplete(savedFile)
            }
        } else {
            onComplete(null)
        }
    }
}
