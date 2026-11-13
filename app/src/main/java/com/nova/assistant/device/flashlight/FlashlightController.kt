package com.nova.assistant.device.flashlight

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashlightController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private var cameraIdWithFlash: String? = null

    init {
        findCameraWithFlash()
        registerTorchCallback()
    }

    private fun findCameraWithFlash() {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            return
        }
        try {
            cameraManager?.cameraIdList?.forEach { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraIdWithFlash = id
                    return
                }
            }
        } catch (_: Exception) {}
    }

    private fun registerTorchCallback() {
        try {
            cameraManager?.registerTorchCallback(object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    if (cameraId == cameraIdWithFlash) {
                        _isFlashlightOn.value = enabled
                    }
                }
            }, null)
        } catch (_: Exception) {}
    }

    fun isSupported(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) && cameraIdWithFlash != null
    }

    fun setFlashlight(enabled: Boolean): Result<Boolean> {
        if (!isSupported()) {
            return Result.failure(IllegalStateException("Flashlight hardware is unavailable on this device"))
        }
        val targetId = cameraIdWithFlash ?: return Result.failure(IllegalStateException("No flash camera ID found"))
        return try {
            cameraManager?.setTorchMode(targetId, enabled)
            _isFlashlightOn.value = enabled
            Result.success(enabled)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun toggleFlashlight(): Result<Boolean> {
        val newState = !_isFlashlightOn.value
        return setFlashlight(newState)
    }
}
