package com.nova.assistant.speech.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Computes real-time normalized audio amplitude (0.0 to 1.0)
 * from PCM 16-bit audio buffers to feed the audio-reactive Siri orb.
 */
@Singleton
class AudioVisualizerProcessor @Inject constructor() {

    private val _currentAmplitude = MutableStateFlow(0f)
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    fun processAudioBuffer(buffer: ShortArray, readSize: Int) {
        if (readSize <= 0) return

        var sumSquare = 0.0
        for (i in 0 until readSize) {
            val sample = buffer[i].toDouble()
            sumSquare += sample * sample
        }

        val rms = sqrt(sumSquare / readSize)
        // Normalize 16-bit PCM RMS (max 32767) with smoothing
        val normalized = (rms / 8000.0).toFloat().coerceIn(0f, 1f)

        // Smooth decaying response
        val smoothed = (_currentAmplitude.value * 0.4f) + (normalized * 0.6f)
        _currentAmplitude.value = smoothed
    }

    fun setSimulatedSpeechAmplitude(amplitude: Float) {
        _currentAmplitude.value = amplitude.coerceIn(0f, 1f)
    }

    fun reset() {
        _currentAmplitude.value = 0f
    }
}
