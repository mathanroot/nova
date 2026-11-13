package com.nova.assistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nova.assistant.R
import com.nova.assistant.data.models.AssistantState
import com.nova.assistant.data.repository.ConversationRepository
import com.nova.assistant.domain.executor.CommandExecutor
import com.nova.assistant.domain.parser.CommandParser
import com.nova.assistant.speech.audio.AudioCaptureManager
import com.nova.assistant.speech.recognition.OfflineSpeechEngine
import com.nova.assistant.speech.tts.SpeechOutput
import com.nova.assistant.speech.wakeword.WakeWordEngine
import com.nova.assistant.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NovaListeningService : Service() {

    companion object {
        const val CHANNEL_ID = "nova_listening_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_LISTENING = "com.nova.assistant.START_LISTENING"
        const val ACTION_STOP_LISTENING = "com.nova.assistant.STOP_LISTENING"
    }

    @Inject lateinit var audioCaptureManager: AudioCaptureManager
    @Inject lateinit var wakeWordEngine: WakeWordEngine
    @Inject lateinit var offlineSpeechEngine: OfflineSpeechEngine
    @Inject lateinit var speechOutput: SpeechOutput
    @Inject lateinit var commandParser: CommandParser
    @Inject lateinit var commandExecutor: CommandExecutor
    @Inject lateinit var conversationRepository: ConversationRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var wakeWordJob: Job? = null
    private var speechCollectJob: Job? = null

    private val _serviceAssistantState = MutableStateFlow(AssistantState.IDLE)
    val serviceAssistantState: StateFlow<AssistantState> = _serviceAssistantState.asStateFlow()

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): NovaListeningService = this@NovaListeningService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_LISTENING -> {
                stopForegroundListening()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START_LISTENING, null -> {
                startForegroundListening()
            }
        }
        return START_STICKY
    }

    private fun startForegroundListening() {
        val notification = buildPersistentNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        _serviceAssistantState.value = AssistantState.WAKE_WORD_LISTENING

        // Start capturing audio
        audioCaptureManager.startCapture { buffer, readSize ->
            offlineSpeechEngine.processAudio(buffer, readSize)
        }

        // Listen for wake word detection
        wakeWordJob?.cancel()
        wakeWordJob = serviceScope.launch {
            wakeWordEngine.wakeWordDetected.collect {
                onWakeWordTriggered()
            }
        }

        // Listen for speech recognition results
        speechCollectJob?.cancel()
        speechCollectJob = serviceScope.launch {
            offlineSpeechEngine.finalText.collect { text ->
                if (_serviceAssistantState.value == AssistantState.COMMAND_LISTENING ||
                    _serviceAssistantState.value == AssistantState.WAKE_WORD_DETECTED) {
                    processSpokenCommand(text)
                }
            }
        }
    }

    private fun onWakeWordTriggered() {
        serviceScope.launch {
            _serviceAssistantState.value = AssistantState.WAKE_WORD_DETECTED
            delay(400)
            _serviceAssistantState.value = AssistantState.COMMAND_LISTENING
        }
    }

    private fun processSpokenCommand(rawCommand: String) {
        serviceScope.launch {
            _serviceAssistantState.value = AssistantState.PROCESSING
            val parsedCommands = commandParser.parse(rawCommand)

            _serviceAssistantState.value = AssistantState.EXECUTING
            val (isSuccess, response) = commandExecutor.executeCommands(parsedCommands)

            // Save to database
            val intentId = parsedCommands.firstOrNull()?.matchResult?.intent?.name ?: "UNKNOWN"
            conversationRepository.recordConversation(
                userCommand = rawCommand,
                assistantResponse = response,
                isSuccess = isSuccess,
                intentId = intentId
            )

            // Speak response
            _serviceAssistantState.value = AssistantState.SPEAKING
            speechOutput.speak(response) {
                _serviceAssistantState.value = AssistantState.WAKE_WORD_LISTENING
            }
        }
    }

    private fun stopForegroundListening() {
        audioCaptureManager.stopCapture()
        wakeWordJob?.cancel()
        speechCollectJob?.cancel()
        _serviceAssistantState.value = AssistantState.PAUSED
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun buildPersistentNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, NovaListeningService::class.java).apply {
            action = ACTION_STOP_LISTENING
        }
        val pendingStop = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nova_logo)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingOpen)
            .addAction(0, getString(R.string.notification_stop), pendingStop)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopForegroundListening()
        super.onDestroy()
    }
}
