package com.nova.assistant.device

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenWakeHelper(
    private val context: Context?
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(context as Context?)

    companion object {
        fun createForTesting(): ScreenWakeHelper = ScreenWakeHelper(null)
    }

    private val powerManager: PowerManager? by lazy {
        context?.getSystemService(Context.POWER_SERVICE) as? PowerManager
    }

    private var partialWakeLock: PowerManager.WakeLock? = null

    @SuppressLint("WakelockTimeout")
    fun acquireCpuWakeLock() {
        val pm = powerManager ?: return
        if (partialWakeLock?.isHeld == true) return
        try {
            partialWakeLock = pm.newWakeLock(
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
        val pm = powerManager ?: return
        try {
            val screenLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "Nova:ScreenTurnOnWakeLock"
            )
            screenLock.acquire(3000)
        } catch (_: Exception) {}
    }
}
