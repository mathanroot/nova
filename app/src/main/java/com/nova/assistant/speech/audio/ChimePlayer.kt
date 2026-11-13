package com.nova.assistant.speech.audio

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays a clean, native Siri-inspired double-chime when "Hey Nova" is detected.
 */
@Singleton
class ChimePlayer @Inject constructor() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun playWakeChime() {
        coroutineScope.launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
                // Ascending tones mimicking Siri's activation earcon
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                delay(95)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
                delay(140)
                toneGen.release()
            } catch (_: Exception) {}
        }
    }
}
