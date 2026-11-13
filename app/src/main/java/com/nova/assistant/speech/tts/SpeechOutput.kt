package com.nova.assistant.speech.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.nova.assistant.speech.audio.AudioVisualizerProcessor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechOutput @Inject constructor(
    @ApplicationContext private val context: Context,
    private val visualizerProcessor: AudioVisualizerProcessor
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var speakingPulseJob: Job? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var speechRate = 1.0f
    private var speechPitch = 1.0f
    private var isMuted = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
                tts?.setSpeechRate(speechRate)
                tts?.setPitch(speechPitch)
            }
        }
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(speechRate)
    }

    fun setSpeechPitch(pitch: Float) {
        speechPitch = pitch.coerceIn(0.5f, 2.0f)
        tts?.setPitch(speechPitch)
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        if (muted && _isSpeaking.value) {
            stop()
        }
    }

    fun getAvailableVoices(): List<Voice> {
        return try {
            tts?.voices?.filter { it.locale.language == Locale.US.language }?.toList() ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun setVoice(voice: Voice) {
        try {
            tts?.voice = voice
        } catch (_: Exception) {}
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (isMuted || text.isBlank()) {
            onComplete?.invoke()
            return
        }

        val utteranceId = "NOVA_UTTERANCE_${System.currentTimeMillis()}"

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                _isSpeaking.value = true
                startSpeakingVisualizerAnimation()
            }

            override fun onDone(id: String?) {
                stopSpeakingVisualizerAnimation()
                _isSpeaking.value = false
                coroutineScope.launch {
                    onComplete?.invoke()
                }
            }

            override fun onError(id: String?) {
                stopSpeakingVisualizerAnimation()
                _isSpeaking.value = false
                coroutineScope.launch {
                    onComplete?.invoke()
                }
            }
        })

        val params = Bundle()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    private fun startSpeakingVisualizerAnimation() {
        speakingPulseJob?.cancel()
        speakingPulseJob = coroutineScope.launch {
            var phase = 0.0
            while (isActive && _isSpeaking.value) {
                phase += 0.3
                // Synthesizes a natural voice fluctuation amplitude for the orb
                val wave = (kotlin.math.sin(phase) * 0.35 + kotlin.math.cos(phase * 1.7) * 0.25 + 0.4).toFloat()
                visualizerProcessor.setSimulatedSpeechAmplitude(wave.coerceIn(0.15f, 0.95f))
                delay(60)
            }
        }
    }

    private fun stopSpeakingVisualizerAnimation() {
        speakingPulseJob?.cancel()
        speakingPulseJob = null
        visualizerProcessor.reset()
    }

    fun stop() {
        tts?.stop()
        stopSpeakingVisualizerAnimation()
        _isSpeaking.value = false
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
