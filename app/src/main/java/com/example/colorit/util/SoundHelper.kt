package com.example.colorit.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sin

@Singleton
class SoundHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Synthesizes and plays a soft, bubbly pop sound in the background
     */
    fun playPopSound() {
        if (!com.example.colorit.utils.AudioManager.isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = 120
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val sample = DoubleArray(numSamples)
                val buffer = ShortArray(numSamples)

                val startFreq = 320.0
                val endFreq = 720.0

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    // Frequency linear sweep going upwards
                    val freq = startFreq + (endFreq - startFreq) * (i.toDouble() / numSamples)
                    val angle = 2.0 * Math.PI * freq * t
                    
                    // Volume envelope to prevent popping/clicking sounds
                    val envelope = if (i < numSamples * 0.15) {
                        i.toDouble() / (numSamples * 0.15) // Quick fade-in
                    } else if (i > numSamples * 0.6) {
                        1.0 - (i.toDouble() - numSamples * 0.6) / (numSamples * 0.4) // Fade-out
                    } else {
                        1.0
                    }
                    sample[i] = sin(angle) * envelope
                    buffer[i] = (sample[i] * 20000).toInt().toShort() // Moderate volume
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, numSamples)
                audioTrack.play()

                // Free resources after playing
                scope.launch {
                    kotlinx.coroutines.delay(durationMs.toLong() + 50)
                    audioTrack.stop()
                    audioTrack.release()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Synthesizes and plays a welcome double chime chord
     */
    fun playChimeSound() {
        scope.launch {
            playPopSound()
            kotlinx.coroutines.delay(120)
            playPopSound()
        }
    }
}
