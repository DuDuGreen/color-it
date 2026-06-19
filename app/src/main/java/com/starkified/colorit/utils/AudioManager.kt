package com.starkified.colorit.utils

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
import kotlin.math.exp

/**
 * Lightweight, self-contained programmatic sound synthesizer for playful kids interactions.
 * Generates custom synthesized waveforms (bubble pops, chimes, brush sweeps) dynamically
 * so that no asset files are required, ensuring 100% build reliability and instant responsiveness.
 */
object AudioManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var prefs: android.content.SharedPreferences? = null

    private var musicJob: kotlinx.coroutines.Job? = null
    private var isPausedInternal = false
    private var streamingAudioTrack: AudioTrack? = null

    // User settings toggles
    var isSoundEnabled: Boolean = true
        set(value) {
            field = value
            prefs?.edit()?.putBoolean("sound_enabled", value)?.apply()
        }

    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            prefs?.edit()?.putBoolean("music_enabled", value)?.apply()
        }

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("colorit_settings", Context.MODE_PRIVATE)
        isSoundEnabled = prefs?.getBoolean("sound_enabled", true) ?: true
        isMusicEnabled = prefs?.getBoolean("music_enabled", true) ?: true
        startMusicLoop() // Start background music loop!
    }

    fun pauseMusic() {
        isPausedInternal = true
    }

    fun resumeMusic() {
        isPausedInternal = false
    }

    private fun startMusicLoop() {
        if (musicJob != null) return
        
        musicJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                
                streamingAudioTrack = track
                track.play()

                // Data class to hold currently playing note state
                class ActiveNote(
                    val freq: Float,
                    val startSample: Long,
                    val durationSamples: Int,
                    val amplitude: Float,
                    val isChord: Boolean
                )

                val activeNotes = mutableListOf<ActiveNote>()
                
                // Beautiful 64-beat melody frequencies (0f represents rest)
                val melodyFrequencies = FloatArray(64)
                // Bar 0 (C)
                melodyFrequencies[0] = 523.25f // C5
                melodyFrequencies[2] = 587.33f // D5
                // Bar 1 (G)
                melodyFrequencies[4] = 659.25f // E5
                melodyFrequencies[6] = 783.99f // G5
                // Bar 2 (Am)
                melodyFrequencies[8] = 880.00f // A5
                melodyFrequencies[10] = 783.99f // G5
                // Bar 3 (F)
                melodyFrequencies[12] = 659.25f // E5
                // Bar 4 (C)
                melodyFrequencies[16] = 523.25f // C5
                melodyFrequencies[18] = 587.33f // D5
                // Bar 5 (G)
                melodyFrequencies[20] = 659.25f // E5
                melodyFrequencies[22] = 392.00f // G4 (drop down)
                // Bar 6 (Am)
                melodyFrequencies[24] = 440.00f // A4
                melodyFrequencies[26] = 523.25f // C5
                // Bar 7 (F)
                melodyFrequencies[28] = 293.66f // D4
                // Bar 8 (Em)
                melodyFrequencies[32] = 659.25f // E5
                melodyFrequencies[34] = 783.99f // G5
                // Bar 9 (F)
                melodyFrequencies[36] = 880.00f // A5
                melodyFrequencies[38] = 1046.50f // C6
                // Bar 10 (C)
                melodyFrequencies[40] = 987.77f // B5
                melodyFrequencies[42] = 783.99f // G5
                // Bar 11 (G)
                melodyFrequencies[44] = 587.33f // D5
                // Bar 12 (Am)
                melodyFrequencies[48] = 880.00f // A5
                melodyFrequencies[50] = 783.99f // G5
                // Bar 13 (F)
                melodyFrequencies[52] = 659.25f // E5
                melodyFrequencies[54] = 523.25f // C5
                // Bar 14 (Dm)
                melodyFrequencies[56] = 587.33f // D5
                melodyFrequencies[58] = 659.25f // E5
                // Bar 15 (G)
                melodyFrequencies[60] = 392.00f // G4

                // Chord definitions (arpeggiated later)
                val chordC = listOf(130.81f, 196.00f, 261.63f, 329.63f)
                val chordG = listOf(196.00f, 246.94f, 293.66f, 392.00f)
                val chordAm = listOf(220.00f, 261.63f, 329.63f, 440.00f)
                val chordF = listOf(174.61f, 220.00f, 261.63f, 349.23f)
                val chordEm = listOf(164.81f, 246.94f, 329.63f, 392.00f)
                val chordDm = listOf(146.83f, 220.00f, 293.66f, 349.23f)

                val beatDurationMs = 600
                val beatDurationSamples = (beatDurationMs * sampleRate) / 1000 // 13230 samples
                val chunkSize = 512
                val chunkBuffer = ShortArray(chunkSize)
                
                // Echo/Delay ring buffer (feedback loop)
                // 8820 samples is 400ms at 22050Hz
                val delayLine = FloatArray(8820)
                var delayIndex = 0
                val delayFeedback = 0.35f

                var sampleCount: Long = 0
                var lastTriggeredBeat: Long = -1

                while (true) {
                    // Check if music should be muted or paused
                    if (!isMusicEnabled || isPausedInternal) {
                        if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                            track.pause()
                        }
                        kotlinx.coroutines.delay(200)
                        continue
                    } else if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                        track.play()
                    }

                    // Determine the current beat index
                    val currentBeat = sampleCount / beatDurationSamples
                    val beatIndex = (currentBeat % 64).toInt()

                    // Trigger new notes if we just crossed into a new beat
                    if (currentBeat > lastTriggeredBeat) {
                        lastTriggeredBeat = currentBeat
                        val startSampleOffset = currentBeat * beatDurationSamples

                        // 1. Trigger Chord Accompaniment on the start of each bar (every 4 beats)
                        if (beatIndex % 4 == 0) {
                            val activeChord = when (beatIndex / 4) {
                                0, 4, 10 -> chordC
                                1, 5, 11 -> chordG
                                2, 6, 12 -> chordAm
                                3, 7, 13 -> chordF
                                8 -> chordEm
                                9 -> chordF
                                14 -> chordDm
                                15 -> chordG
                                else -> chordC
                            }
                            // Spawn chord notes with arpeggiation strum (staggered by 40ms)
                            activeChord.forEachIndexed { idx, freq ->
                                val strumDelayMs = idx * 40
                                val strumDelaySamples = (strumDelayMs * sampleRate) / 1000
                                activeNotes.add(
                                    ActiveNote(
                                        freq = freq,
                                        startSample = startSampleOffset + strumDelaySamples,
                                        durationSamples = beatDurationSamples * 4, // Ring out for 4 beats (2.4s)
                                        amplitude = 400f, // Soft background accompaniment
                                        isChord = true
                                    )
                                )
                            }
                        }

                        // 2. Trigger Melody Note
                        val melodyFreq = melodyFrequencies[beatIndex]
                        if (melodyFreq > 0f) {
                            activeNotes.add(
                                ActiveNote(
                                    freq = melodyFreq,
                                    startSample = startSampleOffset,
                                    durationSamples = beatDurationSamples * 2, // Melody notes hold for 2 beats (1.2s)
                                    amplitude = 750f, // Slightly louder melody, but balanced
                                    isChord = false
                                )
                            )
                        }
                    }

                    // Synthesize chunk of samples
                    for (i in 0 until chunkSize) {
                        val tGlobal = sampleCount + i
                        var sampleVal = 0.0

                        // Sum all active voices
                        for (note in activeNotes) {
                            val relSample = tGlobal - note.startSample
                            if (relSample in 0 until note.durationSamples) {
                                val progress = relSample.toDouble() / note.durationSamples
                                
                                // Exponential decay envelope
                                val decaySpeed = if (note.isChord) 3.0 else 4.5
                                val env = exp(-progress * decaySpeed)

                                val timeSeconds = relSample.toDouble() / sampleRate
                                // Standard sine + harmonics (warm, rich music box chime timbre)
                                val f = note.freq.toDouble()
                                val tone = sin(2.0 * Math.PI * f * timeSeconds) +
                                           0.25 * sin(2.0 * Math.PI * (f * 2.0) * timeSeconds) +
                                           0.10 * sin(2.0 * Math.PI * (f * 3.01) * timeSeconds) +
                                           0.05 * sin(2.0 * Math.PI * (f * 5.0) * timeSeconds)
                                
                                sampleVal += tone * note.amplitude * env
                            }
                        }

                        // Apply echo delay line feedback effect
                        val dryVal = sampleVal
                        val delayVal = delayLine[delayIndex]
                        val wetVal = dryVal + delayVal * delayFeedback
                        
                        // Save to delay line buffer
                        delayLine[delayIndex] = wetVal.toFloat()
                        delayIndex = (delayIndex + 1) % delayLine.size

                        // Clip protection
                        chunkBuffer[i] = wetVal.coerceIn(-32768.0, 32767.0).toInt().toShort()
                    }

                    // Write chunk to AudioTrack
                    track.write(chunkBuffer, 0, chunkSize)
                    sampleCount += chunkSize

                    // Clean up completed notes from active notes list
                    activeNotes.removeAll { note ->
                        sampleCount > note.startSample + note.durationSamples
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    streamingAudioTrack?.stop()
                    streamingAudioTrack?.release()
                } catch (e: Exception) {}
                streamingAudioTrack = null
            }
        }
    }

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
     * Play a warm, cozy music-box arpeggio.
     */
    fun playCozySound() {
        // Soft C Major 7 arpeggio: C4 (261.63Hz) -> E4 (329.63Hz) -> G4 (392Hz) -> B4 (493.88Hz) -> C5 (523.25Hz)
        playSynthSound(
            frequencies = listOf(261.63f, 329.63f, 392.00f, 493.88f, 523.25f),
            durationsMs = listOf(110, 110, 110, 110, 350),
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
