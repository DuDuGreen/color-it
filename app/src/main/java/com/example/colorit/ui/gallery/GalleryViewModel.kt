package com.example.colorit.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colorit.data.database.SavedDrawing
import com.example.colorit.data.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository
) : ViewModel() {

    // Load drawings dynamically from Room
    val savedDrawings: StateFlow<List<SavedDrawing>> = galleryRepository.getAllDrawings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteDrawing(drawing: SavedDrawing) {
        viewModelScope.launch {
            galleryRepository.deleteDrawing(drawing)
        }
    }
}
