package com.example.colorit.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.media.AudioManager as AndroidAudioManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Lightweight, self-contained programmatic sound synthesizer for playful kids interactions.
 * Generates custom synthesized waveforms (bubble pops, chimes, brush sweeps) dynamically
 * so that no asset files are required, ensuring 100% build reliability and instant responsiveness.
 */
object AudioManager {
    private val scope = CoroutineScope(Dispatchers.Default)

    // User settings toggles
    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true

    // System tone generator for simple beeps
    private val toneGen = ToneGenerator(AndroidAudioManager.STREAM_MUSIC, 80)

    /**
     * Programmatically synthesize and play a wave sound in a background coroutine
     */
    private fun playSynthSound(
        frequencies: List<Float>,
        durationsMs: List<Int>,
        waveType: String = "sine"
    ) {
        if (!isSoundEnabled) return

        scope.launch {
            try {
                val sampleRate = 22050
                var totalSamples = 0
                val sampleSizes = durationsMs.map { (it * sampleRate) / 1000 }
                totalSamples = sampleSizes.sum()

                if (totalSamples <= 0) return@launch

                val buffer = ShortArray(totalSamples)
                var currentOffset = 0

                for (i in frequencies.indices) {
                    val freq = frequencies[i]
                    val size = sampleSizes[i]
                    val durationMs = durationsMs[i]
                    
                    for (sampleIndex in 0 until size) {
                        val t = sampleIndex.toDouble() / sampleRate
                        
                        // Waveform selection
                        val waveVal = when (waveType) {
                            "triangle" -> {
                                // Triangle wave
                                val period = 1.0 / freq
                                val phase = (t % period) / period
                                val raw = if (phase < 0.5) 4 * phase - 1 else 3 - 4 * phase
                                raw
                            }
                            "noise" -> {
                                // Pseudo-white noise
                                Math.random() * 2.0 - 1.0
                            }
                            else -> {
                                // Standard smooth sine wave
                                sin(2.0 * Math.PI * freq * t)
                            }
                        }

                        // Apply fade-in and fade-out envelope to avoid clicks
                        val attackPercent = 0.1
                        val decayPercent = 0.2
                        val progress = sampleIndex.toDouble() / size
                        val envelope = when {
                            progress < attackPercent -> progress / attackPercent
                            progress > (1.0 - decayPercent) -> (1.0 - progress) / decayPercent
                            else -> 1.0
                        }

                        val amplitude = 12000.0 // volume range
                        buffer[currentOffset + sampleIndex] = (waveVal * amplitude * envelope).toInt().toShort()
                    }
                    currentOffset += size
                }

                // Create and write to AudioTrack
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
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                // Release AudioTrack after duration
                val totalDuration = durationsMs.sum()
                Thread.sleep(totalDuration.toLong() + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Play a high-pitched playful bubble-pop tap sound.
     */
    fun playTapSound() {
        // High bubble-pop frequency sweep: 600Hz -> 1200Hz
        playSynthSound(listOf(880f, 1200f), listOf(35, 45), "sine")
    }

    /**
     * Play a soft crayon/brush rustle sound.
     */
    fun playBrushSound() {
        // Low amplitude rustle noise
        playSynthSound(listOf(100f), listOf(60), "noise")
    }

    /**
     * Play a star-filled bubble success arpeggio.
     */
    fun playSuccessSound() {
        // Playful rising major chord arpeggio
        playSynthSound(
            frequencies = listOf(523.25f, 659.25f, 783.99f, 1046.50f), // C5 -> E5 -> G5 -> C6
            durationsMs = listOf(100, 100, 100, 250),
            waveType = "triangle"
        )
    }

    /**
     * Play a warm chime sound on canvas save.
     */
    fun playSaveSound() {
        // High soft chime chord: E6 -> G6 -> C7
        playSynthSound(
            frequencies = listOf(1318.51f, 1567.98f, 2093.00f),
            durationsMs = listOf(80, 80, 250),
            waveType = "sine"
        )
    }

    /**
     * Play a low buzz warning sound.
     */
    fun playErrorSound() {
        playSynthSound(listOf(180f, 120f), listOf(100, 150), "triangle")
    }
}
