package com.nova.assistant.device.deviceinfo

import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton

data class DeviceDetails(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkInt: Int
)

@Singleton
class DeviceInfoController @Inject constructor() {
    fun getDeviceDetails(): DeviceDetails {
        return DeviceDetails(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT
        )
    }
}
