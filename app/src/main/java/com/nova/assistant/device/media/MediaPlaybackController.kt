package com.nova.assistant.device.media

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaPlaybackController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun playPause(): Boolean {
        return sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    }

    fun play(): Boolean {
        return sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY)
    }

    fun pause(): Boolean {
        return sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE)
    }

    fun next(): Boolean {
        return sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    fun previous(): Boolean {
        return sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    private fun sendMediaKeyEvent(keyCode: Int): Boolean {
        return try {
            val down = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val up = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            audioManager.dispatchMediaKeyEvent(down)
            audioManager.dispatchMediaKeyEvent(up)
            true
        } catch (_: Exception) {
            false
        }
    }
}
