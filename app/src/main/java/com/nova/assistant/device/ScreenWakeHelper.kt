package com.nova.assistant.device

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenWakeHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private var partialWakeLock: PowerManager.WakeLock? = null

    @SuppressLint("WakelockTimeout")
    fun acquireCpuWakeLock() {
        if (partialWakeLock?.isHeld == true) return
        try {
            partialWakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "Nova:ContinuousListeningCpuLock"
            ).apply {
                setReferenceCounted(false)
                acquire()
            }
        } catch (_: Exception) {}
    }

    fun releaseCpuWakeLock() {
        try {
            if (partialWakeLock?.isHeld == true) {
                partialWakeLock?.release()
            }
        } catch (_: Exception) {}
        partialWakeLock = null
    }

    @Suppress("DEPRECATION")
    fun wakeUpScreen() {
        try {
            val screenLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "Nova:ScreenTurnOnWakeLock"
            )
            screenLock.acquire(3000)
        } catch (_: Exception) {}
    }
}
