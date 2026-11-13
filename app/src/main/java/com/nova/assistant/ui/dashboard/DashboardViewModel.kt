package com.nova.assistant.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nova.assistant.data.models.DeviceStatus
import com.nova.assistant.device.battery.BatteryController
import com.nova.assistant.device.brightness.BrightnessController
import com.nova.assistant.device.connectivity.ConnectivityController
import com.nova.assistant.device.deviceinfo.DeviceInfoController
import com.nova.assistant.device.flashlight.FlashlightController
import com.nova.assistant.device.location.LocationController
import com.nova.assistant.device.storage.StorageController
import com.nova.assistant.device.volume.VolumeController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val batteryController: BatteryController,
    private val flashlightController: FlashlightController,
    private val volumeController: VolumeController,
    private val brightnessController: BrightnessController,
    private val connectivityController: ConnectivityController,
    private val storageController: StorageController,
    private val deviceInfoController: DeviceInfoController,
    private val locationController: LocationController
) : ViewModel() {

    private val _deviceStatus = MutableStateFlow(DeviceStatus())
    val deviceStatus: StateFlow<DeviceStatus> = _deviceStatus.asStateFlow()

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            val bat = batteryController.getBatteryInfo()
            val storage = storageController.getInternalStorageInfo()
            val details = deviceInfoController.getDeviceDetails()
            val isWifi = connectivityController.isWifiConnected()
            val ssid = connectivityController.getWifiSsid()
            val isBt = connectivityController.isBluetoothEnabled()
            val isFlash = flashlightController.isFlashlightOn.value
            val vol = volumeController.getVolumePercent()
            val bright = brightnessController.getBrightnessPercent()
            val hasLocPerm = locationController.hasLocationPermission()
            val isGps = locationController.isGpsEnabled()

            _deviceStatus.value = DeviceStatus(
                batteryPercentage = bat.percentage,
                isCharging = bat.isCharging,
                isWifiConnected = isWifi,
                wifiSsid = ssid,
                isBluetoothEnabled = isBt,
                isFlashlightOn = isFlash,
                currentVolumePercent = vol,
                currentBrightnessPercent = bright,
                availableStorageBytes = storage.availableBytes,
                totalStorageBytes = storage.totalBytes,
                hasGpsPermission = hasLocPerm,
                isGpsEnabled = isGps,
                deviceModel = "${details.manufacturer} ${details.model}",
                androidVersion = "Android ${details.androidVersion}"
            )
        }
    }

    fun toggleFlashlight() {
        flashlightController.toggleFlashlight()
        refreshDashboard()
    }

    fun setVolume(percent: Int) {
        volumeController.setVolumePercent(percent)
        refreshDashboard()
    }

    fun setBrightness(percent: Int) {
        brightnessController.setBrightnessPercent(percent)
        refreshDashboard()
    }
}
