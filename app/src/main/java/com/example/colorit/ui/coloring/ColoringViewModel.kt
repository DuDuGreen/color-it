package com.example.colorit.ui.coloring

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
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
import java.util.BitSet
import java.util.LinkedList
import javax.inject.Inject

enum class ColoringTool {
    FILL, BRUSH
}

data class BoardState(
    val shapeColors: Map<String, Color> = emptyMap(),
    val brushStrokes: List<BrushStroke> = emptyList(),
    val bitmapState: Bitmap? = null
)

data class DrawingBitmapState(
    val bitmap: Bitmap,
    val revision: Int
)

@HiltViewModel
class ColoringViewModel @Inject constructor(
    private val repository: ColoringRepository,
    private val galleryRepository: GalleryRepository
) : ViewModel() {

    private var currentOutlineBitmap: Bitmap? = null

    private val _page = MutableStateFlow<ColoringPage?>(null)
    val page: StateFlow<ColoringPage?> = _page.asStateFlow()

    private val _shapeColors = MutableStateFlow<Map<String, Color>>(emptyMap())
    val shapeColors: StateFlow<Map<String, Color>> = _shapeColors.asStateFlow()

    private val _brushStrokes = MutableStateFlow<List<BrushStroke>>(emptyList())
    val brushStrokes: StateFlow<List<BrushStroke>> = _brushStrokes.asStateFlow()

    private val _drawingBitmap = MutableStateFlow<DrawingBitmapState?>(null)
    val drawingBitmap: StateFlow<DrawingBitmapState?> = _drawingBitmap.asStateFlow()

    private val _selectedColor = MutableStateFlow(Color(0xFFFFC6FF)) // PastelPink default
    val selectedColor: StateFlow<Color> = _selectedColor.asStateFlow()

    private val _selectedTool = MutableStateFlow(ColoringTool.FILL)
    val selectedTool: StateFlow<ColoringTool> = _selectedTool.asStateFlow()

    private val _brushSize = MutableStateFlow(12f)
    val brushSize: StateFlow<Float> = _brushSize.asStateFlow()

    // Undo/Redo Stacks
    private val undoStack = mutableListOf<BoardState>()
    private val redoStack = mutableListOf<BoardState>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    fun loadPage(context: android.content.Context, pageId: String) {
        val loadedPage = repository.getPageById(pageId)
        _page.value = loadedPage
        
        // Reset state
        _shapeColors.value = emptyMap()
        _brushStrokes.value = emptyList()
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        currentOutlineBitmap = null
        
        _selectedTool.value = ColoringTool.FILL // Default to Paint Bucket tool!

        if (loadedPage?.imageResName != null) {
            val resId = context.resources.getIdentifier(loadedPage.imageResName, "drawable", context.packageName)
            if (resId != 0) {
                val drawable = androidx.core.content.res.ResourcesCompat.getDrawable(context.resources, resId, null)
                val outlineBitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                if (outlineBitmap != null) {
                    currentOutlineBitmap = outlineBitmap
                    val w = outlineBitmap.width
                    val h = outlineBitmap.height
                    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(android.graphics.Color.TRANSPARENT)
                    _drawingBitmap.value = DrawingBitmapState(bmp, 0)
                }
            }
        } else {
            _drawingBitmap.value = null
        }
    }

    private fun updateDrawingBitmap(bitmap: Bitmap) {
        val current = _drawingBitmap.value
        val nextRevision = (current?.revision ?: 0) + 1
        _drawingBitmap.value = DrawingBitmapState(bitmap, nextRevision)
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
        
        val drawingState = _drawingBitmap.value
        if (drawingState != null) {
            val bitmap = drawingState.bitmap
            val canvas = Canvas(bitmap)
            val scaleX = bitmap.width / 800f
            val scaleY = bitmap.height / 800f
            
            val paint = Paint().apply {
                style = Paint.Style.STROKE
                color = stroke.color.toArgb()
                strokeWidth = stroke.size * scaleX
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }
            
            val path = android.graphics.Path()
            if (stroke.points.isNotEmpty()) {
                val first = stroke.points.first()
                path.moveTo(first.x * scaleX, first.y * scaleY)
                for (i in 1 until stroke.points.size) {
                    val p = stroke.points[i]
                    path.lineTo(p.x * scaleX, p.y * scaleY)
                }
                canvas.drawPath(path, paint)
            }
            // Trigger state change update
            updateDrawingBitmap(bitmap)
        } else {
            _brushStrokes.value = _brushStrokes.value + stroke
        }
    }

    /**
     * Queue-based 2D flood fill algorithm
     */
    private fun floodFill(
        outlineBitmap: Bitmap,
        colorBitmap: Bitmap,
        startX: Int,
        startY: Int,
        fillColor: Int
    ) {
        val width = outlineBitmap.width
        val height = outlineBitmap.height
        if (startX !in 0 until width || startY !in 0 until height) return

        val targetColor = colorBitmap.getPixel(startX, startY)
        if (targetColor == fillColor) return

        val outlinePixels = IntArray(width * height)
        outlineBitmap.getPixels(outlinePixels, 0, width, 0, 0, width, height)

        fun isWall(x: Int, y: Int): Boolean {
            val pixel = outlinePixels[y * width + x]
            val alpha = (pixel shr 24) and 0xff
            val r = (pixel shr 16) and 0xff
            val g = (pixel shr 8) and 0xff
            val b = pixel and 0xff
            if (alpha < 50) return false
            return (r + g + b) < 300 // Dark outline is a wall
        }

        if (isWall(startX, startY)) return

        val visited = BitSet(width * height)
        val pixels = IntArray(width * height)
        colorBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val queue = IntArray(width * height)
        var head = 0
        var tail = 0

        queue[tail++] = startY * width + startX
        visited.set(startY * width + startX)

        while (head < tail) {
            val index = queue[head++]
            val x = index % width
            val y = index / width

            pixels[index] = fillColor

            // Check 4 neighbors
            if (x + 1 < width) {
                val nextIndex = index + 1
                if (!visited.get(nextIndex) && !isWall(x + 1, y)) {
                    if (pixels[nextIndex] == targetColor) {
                        visited.set(nextIndex)
                        queue[tail++] = nextIndex
                    }
                }
            }
            if (x - 1 >= 0) {
                val nextIndex = index - 1
                if (!visited.get(nextIndex) && !isWall(x - 1, y)) {
                    if (pixels[nextIndex] == targetColor) {
                        visited.set(nextIndex)
                        queue[tail++] = nextIndex
                    }
                }
            }
            if (y + 1 < height) {
                val nextIndex = index + width
                if (!visited.get(nextIndex) && !isWall(x, y + 1)) {
                    if (pixels[nextIndex] == targetColor) {
                        visited.set(nextIndex)
                        queue[tail++] = nextIndex
                    }
                }
            }
            if (y - 1 >= 0) {
                val nextIndex = index - width
                if (!visited.get(nextIndex) && !isWall(x, y - 1)) {
                    if (pixels[nextIndex] == targetColor) {
                        visited.set(nextIndex)
                        queue[tail++] = nextIndex
                    }
                }
            }
        }

        colorBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    /**
     * Executes flood-fill on the drawing bitmap asynchronously
     */
    fun fillBitmap(startX: Float, startY: Float, color: Color) {
        val drawingState = _drawingBitmap.value ?: return
        val bitmap = drawingState.bitmap
        val outlineBitmap = currentOutlineBitmap ?: return
        
        saveToUndoStack()
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val scaleX = bitmap.width / 800f
            val scaleY = bitmap.height / 800f
            val px = (startX * scaleX).toInt()
            val py = (startY * scaleY).toInt()
            
            floodFill(outlineBitmap, bitmap, px, py, color.toArgb())
            
            updateDrawingBitmap(bitmap)
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val bmpCopy = _drawingBitmap.value?.bitmap?.let { it.copy(it.config ?: Bitmap.Config.ARGB_8888, true) }
            val currentState = BoardState(_shapeColors.value, _brushStrokes.value, bmpCopy)
            redoStack.add(currentState)
            
            val previousState = undoStack.removeAt(undoStack.lastIndex)
            _shapeColors.value = previousState.shapeColors
            _brushStrokes.value = previousState.brushStrokes
            
            previousState.bitmapState?.let {
                val restoredBmp = it.copy(it.config ?: Bitmap.Config.ARGB_8888, true)
                updateDrawingBitmap(restoredBmp)
            }
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = true
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val bmpCopy = _drawingBitmap.value?.bitmap?.let { it.copy(it.config ?: Bitmap.Config.ARGB_8888, true) }
            val currentState = BoardState(_shapeColors.value, _brushStrokes.value, bmpCopy)
            undoStack.add(currentState)
            
            val nextState = redoStack.removeAt(redoStack.lastIndex)
            _shapeColors.value = nextState.shapeColors
            _brushStrokes.value = nextState.brushStrokes
            
            nextState.bitmapState?.let {
                val restoredBmp = it.copy(it.config ?: Bitmap.Config.ARGB_8888, true)
                updateDrawingBitmap(restoredBmp)
            }
            _canUndo.value = true
            _canRedo.value = redoStack.isNotEmpty()
        }
    }

    private fun saveToUndoStack() {
        val bmpCopy = _drawingBitmap.value?.bitmap?.let { it.copy(it.config ?: Bitmap.Config.ARGB_8888, true) }
        undoStack.add(BoardState(_shapeColors.value, _brushStrokes.value, bmpCopy))
        redoStack.clear() // Clear redo on new actions
        _canUndo.value = true
        _canRedo.value = false
        
        // Cap undo history to 20 actions for performance
        if (undoStack.size > 20) {
            val oldest = undoStack.removeAt(0)
            oldest.bitmapState?.recycle()
        }
    }

    /**
     * Renders current state to a Bitmap offscreen and saves it as a PNG file
     */
    fun savePageToPng(context: android.content.Context, cacheDir: File, width: Int = 1024, height: Int = 1024): File? {
        val currentPage = _page.value ?: return null
        
        try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Draw pure white background for bitmap coloring
            canvas.drawColor(android.graphics.Color.WHITE)

            // Setup paint for brush strokes
            val brushPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }

            // Coordinate grid resolution scaling (strokes are normalized to 800f grid)
            val scaleX = width / 800f
            val scaleY = height / 800f

            // 1. Draw user's colored strokes and fills from drawingBitmap
            val drawBmp = _drawingBitmap.value?.bitmap
            if (drawBmp != null) {
                val srcRect = android.graphics.Rect(0, 0, drawBmp.width, drawBmp.height)
                val dstRect = android.graphics.Rect(0, 0, width, height)
                canvas.drawBitmap(drawBmp, srcRect, dstRect, null)
            } else {
                // Draw brush strokes (fallback for SVG paths version, if any)
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
            }

            // 2. Draw outline sketch bitmap on top with Multiply blend mode
            if (currentPage.imageResName != null) {
                val resId = context.resources.getIdentifier(currentPage.imageResName, "drawable", context.packageName)
                if (resId != 0) {
                    val drawable = androidx.core.content.res.ResourcesCompat.getDrawable(context.resources, resId, null)
                    val outlineBitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                    if (outlineBitmap != null) {
                        val multiplyPaint = Paint().apply {
                            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.MULTIPLY)
                            isAntiAlias = true
                        }
                        val srcRect = android.graphics.Rect(0, 0, outlineBitmap.width, outlineBitmap.height)
                        val dstRect = android.graphics.Rect(0, 0, width, height)
                        canvas.drawBitmap(outlineBitmap, srcRect, dstRect, multiplyPaint)
                    }
                }
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
    fun saveToGallery(context: android.content.Context, cacheDir: File, width: Int = 1024, height: Int = 1024, onComplete: (File?) -> Unit) {
        val file = savePageToPng(context, cacheDir, width, height)
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
