package com.example.colorit.ui.stickers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colorit.data.model.StickerInstance
import com.example.colorit.data.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class StickersViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository
) : ViewModel() {

    private val _stickers = MutableStateFlow<List<StickerInstance>>(emptyList())
    val stickers: StateFlow<List<StickerInstance>> = _stickers.asStateFlow()

    private val _selectedStickerId = MutableStateFlow<String?>(null)
    val selectedStickerId: StateFlow<String?> = _selectedStickerId.asStateFlow()

    // Undo/Redo Stacks
    private val undoStack = mutableListOf<List<StickerInstance>>()
    private val redoStack = mutableListOf<List<StickerInstance>>()

    val stickerCategories = mapOf(
        "Animals 🦊" to listOf(
            "🐶", "🐱", "🦁", "🐰", "🐼", "🐻", "🐨", "🦊", "🐯", "🐸", 
            "🐵", "🐧", "🦖", "🐙", "🐷", "🐮", "🐹", "🐭", "🐔", "🦆", 
            "🦉", "🐝", "🦋", "🐞", "🐠", "🐬", "🐳", "🐊", "🐢", "🐘"
        ),
        "Play & Toys 🧸" to listOf(
            "🧸", "🎈", "🎁", "🎉", "🎨", "🧩", "🎮", "🛹", "🚲", "🛴", 
            "⚽️", "🏀", "🏈", "⚾️", "🎾", "🏐", "🏉", "🎱", "🪀", "🪁"
        ),
        "Shapes & Magic ✨" to listOf(
            "⭐️", "🌟", "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", 
            "🤎", "🍀", "☀️", "🌙", "🌈", "⚡️", "☄️", "🔥", "💧", "🔮", 
            "💎", "🎀", "🪄", "🎩", "👑"
        ),
        "Food & Sweets 🍭" to listOf(
            "🍕", "🍔", "🍟", "🌭", "🥪", "🌮", "🍦", "🍧", "🍨", "🍩", 
            "🍪", "🎂", "🍰", "🧁", "🍫", "🍬", "🍭", "🍮", "🍿", "🥤"
        ),
        "Transport & Space 🚀" to listOf(
            "🚀", "🛸", "🪐", "🌍", "🚗", "🚕", "🚙", "🚌", "🚑", "🚒", 
            "🚓", "🚜", "🚚", "🚛", "🏎", "🛵", "🚲", "✈️", "🚁", "⛵️"
        )
    )

    fun addSticker(emoji: String, position: Offset) {
        saveToUndoStack()
        
        val id = "sticker_${System.currentTimeMillis()}"
        val newSticker = StickerInstance(
            id = id,
            emoji = emoji,
            position = position,
            scale = 1.0f,
            rotation = 0f
        )
        _stickers.value = _stickers.value + newSticker
        _selectedStickerId.value = id
    }

    fun selectSticker(id: String?) {
        _selectedStickerId.value = id
    }

    fun updateSticker(id: String, position: Offset, scale: Float, rotation: Float) {
        // Find and update without saving to undo stack on every drag event (we will save on gesture end in UI)
        _stickers.value = _stickers.value.map {
            if (it.id == id) {
                it.copy(position = position, scale = scale, rotation = rotation)
            } else it
        }
    }

    fun saveHistoryState() {
        saveToUndoStack()
    }

    fun deleteSticker(id: String) {
        saveToUndoStack()
        _stickers.value = _stickers.value.filter { it.id != id }
        if (_selectedStickerId.value == id) {
            _selectedStickerId.value = null
        }
    }

    fun clearAll() {
        saveToUndoStack()
        _stickers.value = emptyList()
        _selectedStickerId.value = null
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(_stickers.value)
            _stickers.value = undoStack.removeAt(undoStack.lastIndex)
            _selectedStickerId.value = null
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(_stickers.value)
            _stickers.value = redoStack.removeAt(redoStack.lastIndex)
            _selectedStickerId.value = null
        }
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    private fun saveToUndoStack() {
        undoStack.add(_stickers.value)
        redoStack.clear()
        
        if (undoStack.size > 20) {
            undoStack.removeAt(0)
        }
    }

    /**
     * Renders background + emoji text stickers to an offscreen PNG file.
     */
    fun saveStickersToPng(cacheDir: File, width: Int = 1024, height: Int = 1024): File? {
        try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Draw creamy canvas background
            canvas.drawColor(Color(0xFFFCFBF7).toArgb())

            val textPaint = Paint().apply {
                textSize = 96f // Base emoji font size
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            for (sticker in _stickers.value) {
                canvas.save()
                
                // Scale coordinate points from design scale to 1024x1024 scale
                // Assuming runtime coordinates are scaled to width/height, we draw text relative
                // For direct export relative, we write matrix translation
                canvas.translate(sticker.position.x, sticker.position.y)
                canvas.rotate(sticker.rotation)
                canvas.scale(sticker.scale, sticker.scale)
                
                // Draw emoji text centered on its alignment
                canvas.drawText(sticker.emoji, 0f, 32f, textPaint)
                
                canvas.restore()
            }

            val file = File(cacheDir, "stickers_${System.currentTimeMillis()}.png")
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
        val file = saveStickersToPng(cacheDir, width, height)
        if (file != null) {
            viewModelScope.launch {
                galleryRepository.saveDrawing(file, "Sticker Art")
                val drawingsDir = File(cacheDir.parentFile, "files/drawings")
                val savedFile = File(drawingsDir, file.name)
                onComplete(savedFile)
            }
        } else {
            onComplete(null)
        }
    }
}
