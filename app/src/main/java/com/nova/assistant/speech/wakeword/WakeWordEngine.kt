package com.nova.assistant.speech.wakeword

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WakeWordEngine @Inject constructor() {

    private val _wakeWordDetected = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val wakeWordDetected: SharedFlow<Unit> = _wakeWordDetected.asSharedFlow()

    private var sensitivity: Float = 0.6f

    fun setSensitivity(value: Float) {
        sensitivity = value.coerceIn(0.1f, 1.0f)
    }

    /**
     * Inspects partial recognizer text or phoneme sequences for the wake phrase "hey nova" or "nova".
     */
    fun processRecognizedText(text: String): Boolean {
        val clean = text.lowercase(Locale.ROOT)
        if (clean.contains("hey nova") || clean.contains("hi nova") || clean.contains("ok nova") || clean.contains("nova")) {
            _wakeWordDetected.tryEmit(Unit)
            return true
        }
        return false
    }

    /**
     * Manual trigger (e.g. from UI mic tap or test).
     */
    fun triggerWakeWord() {
        _wakeWordDetected.tryEmit(Unit)
    }
}
