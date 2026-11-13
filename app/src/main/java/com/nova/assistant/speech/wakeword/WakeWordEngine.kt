package com.nova.assistant.speech.wakeword

import com.nova.assistant.device.ScreenWakeHelper
import com.nova.assistant.speech.audio.ChimePlayer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WakeWordEngine private constructor(
    private val chimePlayer: ChimePlayer?,
    private val screenWakeHelper: ScreenWakeHelper?
) {
    // Exactly ONE constructor annotated with @Inject for Dagger/Hilt
    @Inject
    constructor(
        chimePlayer: ChimePlayer,
        screenWakeHelper: ScreenWakeHelper
    ) : this(chimePlayer as ChimePlayer?, screenWakeHelper as ScreenWakeHelper?)

    // Secondary constructor for unit testing without @Inject
    constructor() : this(null, null)

    private val _wakeWordDetected = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val wakeWordDetected: SharedFlow<Unit> = _wakeWordDetected.asSharedFlow()

    private var sensitivity: Float = 0.6f

    private val targetPhrases = listOf(
        "hey nova",
        "nova",
        "hi nova",
        "ok nova",
        "hello nova",
        "hey noah",
        "hey know",
        "a nova",
        "hay nova"
    )

    fun setSensitivity(value: Float) {
        sensitivity = value.coerceIn(0.1f, 1.0f)
    }

    fun processRecognizedText(text: String): Boolean {
        val clean = text.lowercase(Locale.ROOT).trim()
        val isMatch = targetPhrases.any { phrase ->
            clean.contains(phrase)
        }

        if (isMatch) {
            triggerWakeWord()
            return true
        }
        return false
    }

    fun triggerWakeWord() {
        // Immediate Siri-style audio earcon & wake screen
        chimePlayer?.playWakeChime()
        screenWakeHelper?.wakeUpScreen()
        _wakeWordDetected.tryEmit(Unit)
    }
}
