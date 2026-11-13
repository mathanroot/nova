package com.nova.assistant.data.models

/**
 * Real device hardware and system status.
 */
data class DeviceStatus(
    val batteryPercentage: Int = 100,
    val isCharging: Boolean = false,
    val isWifiConnected: Boolean = false,
    val wifiSsid: String? = null,
    val isBluetoothEnabled: Boolean = false,
    val isFlashlightOn: Boolean = false,
    val currentVolumePercent: Int = 50,
    val currentBrightnessPercent: Int = 50,
    val availableStorageBytes: Long = 0L,
    val totalStorageBytes: Long = 0L,
    val hasGpsPermission: Boolean = false,
    val isGpsEnabled: Boolean = false,
    val deviceModel: String = "",
    val androidVersion: String = ""
)
