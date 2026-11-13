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

@Singleton
class OfflineSpeechEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wakeWordEngine: WakeWordEngine
) {
    private var voskModel: Model? = null
    private var voskRecognizer: Recognizer? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded.asStateFlow()

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
        // Vosk model directory typically contains 'am' and 'conf'
        return dir.exists() && dir.isDirectory && (File(dir, "am").exists() || File(dir, "conf").exists() || dir.list()?.isNotEmpty() == true)
    }

    fun initModelIfAvailable(): Boolean {
        if (_isModelLoaded.value) return true
        val dir = getModelDirectory()
        if (isModelInstalled()) {
            return try {
                voskModel = Model(dir.absolutePath)
                voskRecognizer = Recognizer(voskModel, 16000.0f)
                _isModelLoaded.value = true
                true
            } catch (e: Exception) {
                _isModelLoaded.value = false
                false
            }
        }
        return false
    }

    fun processAudio(buffer: ShortArray, readSize: Int) {
        val recognizer = voskRecognizer ?: return
        if (recognizer.acceptWaveForm(buffer, readSize)) {
            val resultJson = recognizer.result
            val text = extractTextFromJson(resultJson)
            if (text.isNotEmpty()) {
                _finalText.tryEmit(text)
                wakeWordEngine.processRecognizedText(text)
            }
        } else {
            val partialJson = recognizer.partialResult
            val partial = extractPartialFromJson(partialJson)
            if (partial.isNotEmpty()) {
                _partialText.tryEmit(partial)
                wakeWordEngine.processRecognizedText(partial)
            }
        }
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
