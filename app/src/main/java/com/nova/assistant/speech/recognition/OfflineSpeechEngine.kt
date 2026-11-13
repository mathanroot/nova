package com.nova.assistant.speech.recognition

import android.content.Context
import com.nova.assistant.speech.wakeword.WakeWordEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

enum class RecognizerMode {
    KEYWORD_SPOTTING,
    COMMAND_LISTENING
}

@Singleton
class OfflineSpeechEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wakeWordEngine: WakeWordEngine
) {
    companion object {
        // Constrained phonetic grammar for instant, high-accuracy keyword detection
        const val WAKE_WORD_GRAMMAR = "[\"hey nova\", \"hi nova\", \"ok nova\", \"hello nova\", \"nova\", \"hey noah\", \"hey know\", \"a nova\", \"hay nova\", \"[unk]\"]"
    }

    private var voskModel: Model? = null
    private var voskRecognizer: Recognizer? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded.asStateFlow()

    private val _currentMode = MutableStateFlow(RecognizerMode.KEYWORD_SPOTTING)
    val currentMode: StateFlow<RecognizerMode> = _currentMode.asStateFlow()

    private val _partialText = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val partialText: SharedFlow<String> = _partialText.asSharedFlow()

    private val _finalText = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val finalText: SharedFlow<String> = _finalText.asSharedFlow()

    init {
        initModelIfAvailable()
    }

    fun getModelDirectory(): File {
        val externalDir = context.getExternalFilesDir("model")
        return externalDir ?: File(context.filesDir, "model")
    }

    fun isModelInstalled(): Boolean {
        val dir = getModelDirectory()
        return dir.exists() && dir.isDirectory && (File(dir, "am").exists() || File(dir, "conf").exists() || dir.list()?.isNotEmpty() == true)
    }

    fun initModelIfAvailable(): Boolean {
        if (_isModelLoaded.value) return true
        val dir = getModelDirectory()
        if (isModelInstalled()) {
            return try {
                voskModel = Model(dir.absolutePath)
                setRecognizerMode(RecognizerMode.KEYWORD_SPOTTING)
                _isModelLoaded.value = true
                true
            } catch (e: Exception) {
                _isModelLoaded.value = false
                false
            }
        }
        return false
    }

    fun setRecognizerMode(mode: RecognizerMode) {
        val model = voskModel ?: return
        try {
            voskRecognizer?.close()
            voskRecognizer = if (mode == RecognizerMode.KEYWORD_SPOTTING) {
                Recognizer(model, 16000.0f, WAKE_WORD_GRAMMAR)
            } else {
                Recognizer(model, 16000.0f)
            }
            _currentMode.value = mode
        } catch (_: Exception) {}
    }

    fun processAudio(buffer: ShortArray, readSize: Int) {
        val recognizer = voskRecognizer ?: return
        if (recognizer.acceptWaveForm(buffer, readSize)) {
            val resultJson = recognizer.result
            val text = extractTextFromJson(resultJson)
            if (text.isNotEmpty()) {
                handleRecognizedText(text, isFinal = true)
            }
        } else {
            val partialJson = recognizer.partialResult
            val partial = extractPartialFromJson(partialJson)
            if (partial.isNotEmpty()) {
                handleRecognizedText(partial, isFinal = false)
            }
        }
    }

    private fun handleRecognizedText(text: String, isFinal: Boolean) {
        if (_currentMode.value == RecognizerMode.KEYWORD_SPOTTING) {
            val detected = wakeWordEngine.processRecognizedText(text)
            if (detected) {
                // Instantly switch to command listening mode
                setRecognizerMode(RecognizerMode.COMMAND_LISTENING)
            }
        } else {
            if (isFinal) {
                _finalText.tryEmit(text)
            } else {
                _partialText.tryEmit(text)
            }
        }
    }

    fun resetToWakeWordMode() {
        setRecognizerMode(RecognizerMode.KEYWORD_SPOTTING)
    }

    fun reset() {
        voskRecognizer?.reset()
    }

    fun injectSimulatedCommand(text: String) {
        coroutineScope.launch {
            _finalText.emit(text)
        }
    }

    private fun extractTextFromJson(jsonStr: String): String {
        return try {
            val obj = JSONObject(jsonStr)
            obj.optString("text", "")
        } catch (_: Exception) {
            ""
        }
    }

    private fun extractPartialFromJson(jsonStr: String): String {
        return try {
            val obj = JSONObject(jsonStr)
            obj.optString("partial", "")
        } catch (_: Exception) {
            ""
        }
    }
}
