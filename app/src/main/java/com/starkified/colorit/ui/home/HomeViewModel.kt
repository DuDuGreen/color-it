package com.starkified.colorit.ui.home

import androidx.lifecycle.ViewModel
import com.starkified.colorit.util.SoundHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val soundHelper: SoundHelper
) : ViewModel() {

    fun playClickSound() {
        soundHelper.playPopSound()
    }

    fun playWelcomeSound() {
        soundHelper.playChimeSound()
    }
}
