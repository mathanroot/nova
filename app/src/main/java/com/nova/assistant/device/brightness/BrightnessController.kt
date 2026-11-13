package com.nova.assistant.device.brightness

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrightnessController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun hasWriteSettingsPermission(): Boolean {
        return Settings.System.canWrite(context)
    }

    fun requestWriteSettingsIntent(): Intent {
        return Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:" + context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getBrightnessPercent(): Int {
        return try {
            val raw = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            ((raw.toFloat() / 255f) * 100).toInt()
        } catch (_: Exception) {
            50
        }
    }

    fun setBrightnessPercent(percent: Int): Result<Int> {
        if (!hasWriteSettingsPermission()) {
            return Result.failure(SecurityException("WRITE_SETTINGS permission is required to adjust screen brightness"))
        }
        val clamped = percent.coerceIn(0, 100)
        val rawValue = ((clamped.toFloat() / 100f) * 255).toInt()
        return try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                rawValue
            )
            Result.success(clamped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun adjustBrightness(deltaPercent: Int): Result<Int> {
        val current = getBrightnessPercent()
        return setBrightnessPercent(current + deltaPercent)
    }
}
