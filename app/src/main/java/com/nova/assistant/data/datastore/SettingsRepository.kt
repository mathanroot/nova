package com.nova.assistant.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "nova_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val HEY_NOVA_ENABLED = booleanPreferencesKey("hey_nova_enabled")
        val WAKE_WORD_SENSITIVITY = floatPreferencesKey("wake_word_sensitivity")
        val SPEECH_SPEED = floatPreferencesKey("speech_speed")
        val SPEECH_PITCH = floatPreferencesKey("speech_pitch")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val BATTERY_SAVING_MODE = booleanPreferencesKey("battery_saving_mode")
        val OFFLINE_MODEL_INSTALLED = booleanPreferencesKey("offline_model_installed")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val SELECTED_VOICE_NAME = stringPreferencesKey("selected_voice_name")
    }

    val isHeyNovaEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.HEY_NOVA_ENABLED] ?: false
    }

    val wakeWordSensitivity: Flow<Float> = context.dataStore.data.map {
        it[Keys.WAKE_WORD_SENSITIVITY] ?: 0.6f
    }

    val speechSpeed: Flow<Float> = context.dataStore.data.map {
        it[Keys.SPEECH_SPEED] ?: 1.0f
    }

    val speechPitch: Flow<Float> = context.dataStore.data.map {
        it[Keys.SPEECH_PITCH] ?: 1.0f
    }

    val isTtsEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.TTS_ENABLED] ?: true
    }

    val isBatterySavingMode: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.BATTERY_SAVING_MODE] ?: false
    }

    val isOfflineModelInstalled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.OFFLINE_MODEL_INSTALLED] ?: false
    }

    val preferredLanguage: Flow<String> = context.dataStore.data.map {
        it[Keys.PREFERRED_LANGUAGE] ?: "en-US"
    }

    val selectedVoiceName: Flow<String?> = context.dataStore.data.map {
        it[Keys.SELECTED_VOICE_NAME]
    }

    suspend fun setHeyNovaEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HEY_NOVA_ENABLED] = enabled }
    }

    suspend fun setWakeWordSensitivity(sensitivity: Float) {
        context.dataStore.edit { it[Keys.WAKE_WORD_SENSITIVITY] = sensitivity.coerceIn(0.1f, 1.0f) }
    }

    suspend fun setSpeechSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.SPEECH_SPEED] = speed.coerceIn(0.5f, 2.0f) }
    }

    suspend fun setSpeechPitch(pitch: Float) {
        context.dataStore.edit { it[Keys.SPEECH_PITCH] = pitch.coerceIn(0.5f, 2.0f) }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.TTS_ENABLED] = enabled }
    }

    suspend fun setBatterySavingMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BATTERY_SAVING_MODE] = enabled }
    }

    suspend fun setOfflineModelInstalled(installed: Boolean) {
        context.dataStore.edit { it[Keys.OFFLINE_MODEL_INSTALLED] = installed }
    }

    suspend fun setPreferredLanguage(language: String) {
        context.dataStore.edit { it[Keys.PREFERRED_LANGUAGE] = language }
    }

    suspend fun setSelectedVoiceName(voiceName: String?) {
        context.dataStore.edit {
            if (voiceName == null) {
                it.remove(Keys.SELECTED_VOICE_NAME)
            } else {
                it[Keys.SELECTED_VOICE_NAME] = voiceName
            }
        }
    }
}
