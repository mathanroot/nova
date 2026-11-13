package com.nova.assistant.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nova.assistant.data.datastore.SettingsRepository
import com.nova.assistant.data.repository.ConversationRepository
import com.nova.assistant.service.NovaListeningService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    val isHeyNovaEnabled: StateFlow<Boolean> = settingsRepository.isHeyNovaEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val wakeWordSensitivity: StateFlow<Float> = settingsRepository.wakeWordSensitivity
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.6f)

    val isTtsEnabled: StateFlow<Boolean> = settingsRepository.isTtsEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val isBatterySavingMode: StateFlow<Boolean> = settingsRepository.isBatterySavingMode
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun setHeyNovaEnabled(enabled: Boolean) {
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
            } else {
                serviceIntent.action = NovaListeningService.ACTION_STOP_LISTENING
                context.startService(serviceIntent)
            }
        }
    }

    fun setWakeWordSensitivity(sensitivity: Float) {
        viewModelScope.launch {
            settingsRepository.setWakeWordSensitivity(sensitivity)
        }
    }

    fun setTtsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTtsEnabled(enabled)
        }
    }

    fun setBatterySavingMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBatterySavingMode(enabled)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            conversationRepository.clearHistory()
        }
    }
}
