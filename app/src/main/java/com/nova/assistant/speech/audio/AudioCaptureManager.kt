package com.nova.assistant.speech.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.nova.assistant.device.ScreenWakeHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioCaptureManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val visualizerProcessor: AudioVisualizerProcessor,
    private val screenWakeHelper: ScreenWakeHelper
) {
    companion object {
        const val SAMPLE_RATE = 16000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    private var captureJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startCapture(onBufferReady: (ShortArray, Int) -> Unit): Result<Boolean> {
        if (!hasRecordPermission()) {
            return Result.failure(SecurityException("RECORD_AUDIO permission is not granted"))
        }

        if (_isRecording.value) {
            return Result.success(true)
        }

        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            .coerceAtLeast(SAMPLE_RATE * 2)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return Result.failure(IllegalStateException("AudioRecord failed to initialize"))
            }

            // Acquire CPU wake lock so recording continues seamlessly when screen turns off
            screenWakeHelper.acquireCpuWakeLock()

            audioRecord?.startRecording()
            _isRecording.value = true

            captureJob = coroutineScope.launch {
                val buffer = ShortArray(1024)
                while (isActive && _isRecording.value) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (read > 0) {
                        visualizerProcessor.processAudioBuffer(buffer, read)
                        onBufferReady(buffer, read)
                    }
                }
            }

            return Result.success(true)
        } catch (e: Exception) {
            stopCapture()
            return Result.failure(e)
        }
    }

    fun stopCapture() {
        _isRecording.value = false
        captureJob?.cancel()
        captureJob = null

        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        screenWakeHelper.releaseCpuWakeLock()
        visualizerProcessor.reset()
    }
}
