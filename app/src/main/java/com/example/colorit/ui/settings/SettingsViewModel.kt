package com.example.colorit.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colorit.data.repository.GalleryRepository
import com.example.colorit.utils.AudioManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository
) : ViewModel() {

    private val _isSoundEnabled = MutableStateFlow(AudioManager.isSoundEnabled)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _isMusicEnabled = MutableStateFlow(AudioManager.isMusicEnabled)
    val isMusicEnabled: StateFlow<Boolean> = _isMusicEnabled.asStateFlow()

    fun toggleSound() {
        val newValue = !_isSoundEnabled.value
        AudioManager.isSoundEnabled = newValue
        _isSoundEnabled.value = newValue
    }

    fun toggleMusic() {
        val newValue = !_isMusicEnabled.value
        AudioManager.isMusicEnabled = newValue
        _isMusicEnabled.value = newValue
    }

    fun clearAllGalleryDrawings(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                galleryRepository.clearAll()
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }
}
