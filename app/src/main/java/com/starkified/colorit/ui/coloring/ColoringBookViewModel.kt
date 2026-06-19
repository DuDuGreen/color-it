package com.starkified.colorit.ui.coloring

import androidx.lifecycle.ViewModel
import com.starkified.colorit.data.repository.ColoringRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ColoringBookViewModel @Inject constructor(
    private val repository: ColoringRepository
) : ViewModel() {

    val categories = listOf("All", "Animals", "Nature", "Vehicles", "Space", "Dinosaurs", "Birds", "Numbers")

    fun getPagesByCategory(category: String) = repository.getPagesByCategory(category)
}
