package com.nova.assistant.device.volume

import android.content.Context
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VolumeController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun getVolumePercent(streamType: Int = AudioManager.STREAM_MUSIC): Int {
        val current = audioManager.getStreamVolume(streamType)
        val max = audioManager.getStreamMaxVolume(streamType)
        if (max == 0) return 0
        return ((current.toFloat() / max.toFloat()) * 100).toInt()
    }

    fun setVolumePercent(percent: Int, streamType: Int = AudioManager.STREAM_MUSIC): Int {
        val clamped = percent.coerceIn(0, 100)
        val max = audioManager.getStreamMaxVolume(streamType)
        val targetIndex = ((clamped.toFloat() / 100f) * max).toInt()
        audioManager.setStreamVolume(streamType, targetIndex, AudioManager.FLAG_SHOW_UI)
        return getVolumePercent(streamType)
    }

    fun increaseVolume(step: Int = 1, streamType: Int = AudioManager.STREAM_MUSIC): Int {
        audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
        return getVolumePercent(streamType)
    }

    fun decreaseVolume(step: Int = 1, streamType: Int = AudioManager.STREAM_MUSIC): Int {
        audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
        return getVolumePercent(streamType)
    }

    fun mute(streamType: Int = AudioManager.STREAM_MUSIC): Boolean {
        audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
        return true
    }

    fun unmute(streamType: Int = AudioManager.STREAM_MUSIC): Boolean {
        audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI)
        return true
    }
}
