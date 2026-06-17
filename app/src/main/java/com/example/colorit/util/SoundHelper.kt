package com.example.colorit.util

import android.content.Context
import com.example.colorit.utils.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Delegates to AudioManager for high-pitched bubble pop sound.
     */
    fun playPopSound() {
        AudioManager.playTapSound()
    }

    /**
     * Delegates to AudioManager for warm chime sound on canvas save.
     */
    fun playChimeSound() {
        AudioManager.playSaveSound()
    }

    /**
     * Delegates to AudioManager for warm, cozy music-box arpeggio.
     */
    fun playCozySound() {
        AudioManager.playCozySound()
    }

    /**
     * Delegates to AudioManager for drawing/rustle noise.
     */
    fun playBrushSound() {
        AudioManager.playBrushSound()
    }

    /**
     * Delegates to AudioManager for star-filled success arpeggios.
     */
    fun playSuccessSound() {
        AudioManager.playSuccessSound()
    }

    /**
     * Delegates to AudioManager for warning buzzes.
     */
    fun playErrorSound() {
        AudioManager.playErrorSound()
    }
}
