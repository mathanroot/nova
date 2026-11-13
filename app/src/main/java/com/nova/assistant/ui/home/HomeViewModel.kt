package com.nova.assistant.ui.home

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nova.assistant.data.datastore.SettingsRepository
import com.nova.assistant.data.models.AssistantState
import com.nova.assistant.data.repository.ConversationRepository
import com.nova.assistant.domain.executor.CommandExecutor
import com.nova.assistant.domain.parser.CommandParser
import com.nova.assistant.service.NovaListeningService
import com.nova.assistant.speech.audio.AudioCaptureManager
import com.nova.assistant.speech.audio.AudioVisualizerProcessor
import com.nova.assistant.speech.recognition.OfflineSpeechEngine
import com.nova.assistant.speech.tts.SpeechOutput
import com.nova.assistant.speech.wakeword.WakeWordEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioCaptureManager: AudioCaptureManager,
    private val visualizerProcessor: AudioVisualizerProcessor,
    private val wakeWordEngine: WakeWordEngine,
    private val offlineSpeechEngine: OfflineSpeechEngine,
    private val speechOutput: SpeechOutput,
    private val commandParser: CommandParser,
    private val commandExecutor: CommandExecutor,
    private val conversationRepository: ConversationRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _lastRecognizedCommand = MutableStateFlow("")
    val lastRecognizedCommand: StateFlow<String> = _lastRecognizedCommand.asStateFlow()

    private val _lastResponse = MutableStateFlow("")
    val lastResponse: StateFlow<String> = _lastResponse.asStateFlow()

    val audioAmplitude: StateFlow<Float> = visualizerProcessor.currentAmplitude

    val isHeyNovaEnabled: StateFlow<Boolean> = settingsRepository.isHeyNovaEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isBatterySaving: StateFlow<Boolean> = settingsRepository.isBatterySavingMode
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    private var speechCollectJob: Job? = null
    private var wakeWordJob: Job? = null

    init {
        observeSpeechEngine()
        observeWakeWord()
    }

    private fun observeSpeechEngine() {
        speechCollectJob = viewModelScope.launch {
            offlineSpeechEngine.finalText.collect { text ->
                if (_assistantState.value == AssistantState.COMMAND_LISTENING ||
                    _assistantState.value == AssistantState.WAKE_WORD_DETECTED) {
                    executeUserCommand(text)
                }
            }
        }
    }

    private fun observeWakeWord() {
        wakeWordJob = viewModelScope.launch {
            wakeWordEngine.wakeWordDetected.collect {
                _assistantState.value = AssistantState.WAKE_WORD_DETECTED
                delay(400)
                _assistantState.value = AssistantState.COMMAND_LISTENING
            }
        }
    }

    fun onMicTapped() {
        when (_assistantState.value) {
            AssistantState.COMMAND_LISTENING, AssistantState.WAKE_WORD_LISTENING -> {
                audioCaptureManager.stopCapture()
                _assistantState.value = AssistantState.IDLE
            }
            AssistantState.SPEAKING -> {
                speechOutput.stop()
                _assistantState.value = AssistantState.IDLE
            }
            else -> {
                startListeningForCommand()
            }
        }
    }

    fun startListeningForCommand() {
        val captureResult = audioCaptureManager.startCapture { buffer, readSize ->
            offlineSpeechEngine.processAudio(buffer, readSize)
        }
        if (captureResult.isSuccess) {
            _assistantState.value = AssistantState.COMMAND_LISTENING
        } else {
            _assistantState.value = AssistantState.ERROR
            _lastResponse.value = "Microphone permission required."
        }
    }

    fun executeUserCommand(commandText: String) {
        viewModelScope.launch {
            _lastRecognizedCommand.value = commandText
            _assistantState.value = AssistantState.PROCESSING
            delay(250)

            val parsedCommands = commandParser.parse(commandText)
            _assistantState.value = AssistantState.EXECUTING

            val (isSuccess, response) = commandExecutor.executeCommands(parsedCommands)
            _lastResponse.value = response

            // Record to Room Database
            val intentId = parsedCommands.firstOrNull()?.matchResult?.intent?.name ?: "UNKNOWN"
            conversationRepository.recordConversation(
                userCommand = commandText,
                assistantResponse = response,
                isSuccess = isSuccess,
                intentId = intentId
            )

            // Speak response via Offline TTS
            _assistantState.value = AssistantState.SPEAKING
            speechOutput.speak(response) {
                _assistantState.value = if (isHeyNovaEnabled.value) AssistantState.WAKE_WORD_LISTENING else AssistantState.IDLE
            }
        }
    }

    fun toggleHeyNovaService(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHeyNovaEnabled(enabled)
            val serviceIntent = Intent(context, NovaListeningService::class.java)
            if (enabled) {
                serviceIntent.action = NovaListeningService.ACTION_START_LISTENING
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                _assistantState.value = AssistantState.WAKE_WORD_LISTENING
            } else {
                serviceIntent.action = NovaListeningService.ACTION_STOP_LISTENING
                context.startService(serviceIntent)
                _assistantState.value = AssistantState.IDLE
            }
        }
    }

    override fun onCleared() {
        speechCollectJob?.cancel()
        wakeWordJob?.cancel()
        super.onCleared()
    }
}
