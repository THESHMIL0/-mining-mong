package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundEffects {

    var isSfxEnabled: Boolean = true
    var volume: Float = 0.8f

    private val sampleRate = 22050
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playTapGold() {
        if (!isSfxEnabled) return
        scope.launch {
            // High pitch short retro coin clink
            val frequencies = floatArrayOf(987.77f, 1318.51f) // B5 to E6
            val durationMs = 60
            playArpeggio(frequencies, durationMs)
        }
    }

    fun playPickaxeHit() {
        if (!isSfxEnabled) return
        scope.launch {
            // Low thud + metallic scrape
            val frequencies = floatArrayOf(220f, 180f, 150f)
            val durationMs = 40
            playArpeggio(frequencies, durationMs)
        }
    }

    fun playLevelUp() {
        if (!isSfxEnabled) return
        scope.launch {
            // Classic 8-bit fanfare chime (C5 -> E5 -> G5 -> C6)
            val frequencies = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f)
            val durationMs = 80
            playArpeggio(frequencies, durationMs)
        }
    }

    fun playPrestige() {
        if (!isSfxEnabled) return
        scope.launch {
            // Deep descending cosmic rumble
            val frequencies = floatArrayOf(880f, 660f, 440f, 220f, 110f)
            val durationMs = 120
            playArpeggio(frequencies, durationMs)
        }
    }

    fun playCollectCart() {
        if (!isSfxEnabled) return
        scope.launch {
            val frequencies = floatArrayOf(587.33f, 880f) // D5 to A5
            val durationMs = 50
            playArpeggio(frequencies, durationMs)
        }
    }

    private fun playArpeggio(freqs: FloatArray, noteDurationMs: Int) {
        try {
            val totalSamples = (sampleRate * (freqs.size * noteDurationMs) / 1000)
            val buffer = ShortArray(totalSamples)

            var sampleIdx = 0
            val noteSamples = (sampleRate * noteDurationMs) / 1000

            for (freq in freqs) {
                for (i in 0 until noteSamples) {
                    if (sampleIdx >= buffer.size) break
                    val t = i.toDouble() / sampleRate
                    // Square wave / Sine wave hybrid for retro synth tone
                    val sine = sin(2.0 * Math.PI * freq * t)
                    val square = if (sine > 0) 0.8 else -0.8
                    val envelope = 1.0 - (i.toDouble() / noteSamples) // Fade out
                    val value = (square * envelope * Short.MAX_VALUE * volume * 0.35).toInt()
                    buffer[sampleIdx++] = value.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
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
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            // Release after playing
            Thread.sleep((freqs.size * noteDurationMs + 100).toLong())
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore audio hardware exception
        }
    }
}
