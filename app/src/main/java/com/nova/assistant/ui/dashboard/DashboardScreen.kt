package com.nova.assistant.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nova.assistant.ui.components.GlassmorphicCard
import com.nova.assistant.ui.theme.NovaCyan
import com.nova.assistant.ui.theme.NovaDeepBlack
import com.nova.assistant.ui.theme.NovaElectricBlue
import com.nova.assistant.ui.theme.NovaMagenta
import com.nova.assistant.ui.theme.NovaTextDimmed
import com.nova.assistant.ui.theme.NovaTextPrimary
import com.nova.assistant.ui.theme.NovaTextSecondary
import com.nova.assistant.ui.theme.NovaViolet

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val status by viewModel.deviceStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NovaDeepBlack, Color(0xFF070B1E), NovaDeepBlack)
                )
            )
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DEVICE CONTROL",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary
                )
                Text(
                    text = "Live Hardware & System Metrics",
                    fontSize = 12.sp,
                    color = NovaTextDimmed
                )
            }
            IconButton(
                onClick = { viewModel.refreshDashboard() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x221E293B))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = NovaCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Device Info Card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = NovaCyan,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = status.deviceModel.ifEmpty { "Android Device" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NovaTextPrimary
                    )
                    Text(
                        text = status.androidVersion,
                        fontSize = 13.sp,
                        color = NovaTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Battery & Flashlight Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Battery
            GlassmorphicCard(modifier = Modifier.weight(1f)) {
                Column {
                    Icon(
                        imageVector = if (status.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        contentDescription = null,
                        tint = if (status.isCharging) NovaCyan else NovaElectricBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${status.batteryPercentage}%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaTextPrimary
                    )
                    Text(
                        text = if (status.isCharging) "Charging" else "On Battery",
                        fontSize = 12.sp,
                        color = NovaTextDimmed
                    )
                }
            }

            // Flashlight Toggle
            GlassmorphicCard(modifier = Modifier.weight(1f)) {
                Column {
                    Icon(
                        imageVector = if (status.isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = null,
                        tint = if (status.isFlashlightOn) NovaCyan else NovaTextDimmed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (status.isFlashlightOn) "Torch ON" else "Torch OFF",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NovaTextPrimary
                        )
                        Switch(
                            checked = status.isFlashlightOn,
                            onCheckedChange = { viewModel.toggleFlashlight() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NovaCyan,
                                checkedTrackColor = NovaViolet
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Storage & Location Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Storage
            GlassmorphicCard(modifier = Modifier.weight(1f)) {
                val availableGb = status.availableStorageBytes / (1024.0 * 1024.0 * 1024.0)
                Column {
                    Icon(
                        imageVector = Icons.Default.SdStorage,
                        contentDescription = null,
                        tint = NovaMagenta,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = String.format("%.1f GB", availableGb),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaTextPrimary
                    )
                    Text(
                        text = "Available Storage",
                        fontSize = 12.sp,
                        color = NovaTextDimmed
                    )
                }
            }

            // GPS
            GlassmorphicCard(modifier = Modifier.weight(1f)) {
                Column {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (status.isGpsEnabled) NovaCyan else NovaTextDimmed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (status.isGpsEnabled) "GPS Active" else "GPS Off",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaTextPrimary
                    )
                    Text(
                        text = if (status.hasGpsPermission) "Permission Granted" else "Permission Required",
                        fontSize = 11.sp,
                        color = NovaTextDimmed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Connectivity (Wi-Fi & Bluetooth)
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = if (status.isWifiConnected) NovaCyan else NovaTextDimmed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Wi-Fi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = NovaTextPrimary
                        )
                        Text(
                            text = status.wifiSsid ?: if (status.isWifiConnected) "Connected" else "Disconnected",
                            fontSize = 12.sp,
                            color = NovaTextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = if (status.isBluetoothEnabled) NovaElectricBlue else NovaTextDimmed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Bluetooth",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = NovaTextPrimary
                        )
                        Text(
                            text = if (status.isBluetoothEnabled) "Enabled" else "Disabled",
                            fontSize = 12.sp,
                            color = NovaTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Volume Slider Card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, null, tint = NovaCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Volume", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = NovaTextPrimary)
                    }
                    Text("${status.currentVolumePercent}%", fontSize = 14.sp, color = NovaCyan)
                }
                Slider(
                    value = status.currentVolumePercent.toFloat(),
                    onValueChange = { viewModel.setVolume(it.toInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = NovaCyan,
                        activeTrackColor = NovaCyan,
                        inactiveTrackColor = Color(0x33475569)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Brightness Slider Card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BrightnessMedium, null, tint = NovaElectricBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Brightness", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = NovaTextPrimary)
                    }
                    Text("${status.currentBrightnessPercent}%", fontSize = 14.sp, color = NovaElectricBlue)
                }
                Slider(
                    value = status.currentBrightnessPercent.toFloat(),
                    onValueChange = { viewModel.setBrightness(it.toInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = NovaElectricBlue,
                        activeTrackColor = NovaElectricBlue,
                        inactiveTrackColor = Color(0x33475569)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
