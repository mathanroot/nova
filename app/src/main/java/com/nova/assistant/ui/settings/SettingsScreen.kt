package com.nova.assistant.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nova.assistant.ui.components.GlassmorphicCard
import com.nova.assistant.ui.theme.NovaCyan
import com.nova.assistant.ui.theme.NovaDeepBlack
import com.nova.assistant.ui.theme.NovaElectricBlue
import com.nova.assistant.ui.theme.NovaGlowError
import com.nova.assistant.ui.theme.NovaMagenta
import com.nova.assistant.ui.theme.NovaTextDimmed
import com.nova.assistant.ui.theme.NovaTextPrimary
import com.nova.assistant.ui.theme.NovaTextSecondary
import com.nova.assistant.ui.theme.NovaViolet

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToVoice: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToModelSetup: () -> Unit
) {
    val isHeyNovaEnabled by viewModel.isHeyNovaEnabled.collectAsState()
    val wakeSensitivity by viewModel.wakeWordSensitivity.collectAsState()
    val isTtsEnabled by viewModel.isTtsEnabled.collectAsState()
    val isBatterySaving by viewModel.isBatterySavingMode.collectAsState()

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
        Text(
            text = "SETTINGS",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextPrimary
        )
        Text(
            text = "Preferences & Offline Voice Configuration",
            fontSize = 12.sp,
            color = NovaTextDimmed
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Hey Nova Wake Word Toggle
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = NovaCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Hey Nova Wake Word", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
                            Text("Background hands-free listening", fontSize = 12.sp, color = NovaTextDimmed)
                        }
                    }
                    Switch(
                        checked = isHeyNovaEnabled,
                        onCheckedChange = { viewModel.setHeyNovaEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NovaCyan,
                            checkedTrackColor = NovaViolet
                        )
                    )
                }

                if (isHeyNovaEnabled) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Wake Word Sensitivity: ${(wakeSensitivity * 100).toInt()}%",
                        fontSize = 13.sp,
                        color = NovaTextSecondary
                    )
                    Slider(
                        value = wakeSensitivity,
                        onValueChange = { viewModel.setWakeWordSensitivity(it) },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = NovaCyan,
                            activeTrackColor = NovaCyan,
                            inactiveTrackColor = Color(0x33475569)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Spoken Responses Toggle
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = NovaMagenta, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Spoken Audio Responses", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
                        Text("Speak responses via offline TTS", fontSize = 12.sp, color = NovaTextDimmed)
                    }
                }
                Switch(
                    checked = isTtsEnabled,
                    onCheckedChange = { viewModel.setTtsEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NovaMagenta,
                        checkedTrackColor = NovaViolet
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Battery Saving Mode
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Battery Saving Mode", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
                    Text("Reduces orb particle density and effects", fontSize = 12.sp, color = NovaTextDimmed)
                }
                Switch(
                    checked = isBatterySaving,
                    onCheckedChange = { viewModel.setBatterySavingMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NovaElectricBlue,
                        checkedTrackColor = NovaViolet
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "CUSTOMIZATION & PRIVACY",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = NovaTextDimmed,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Voice Customization link
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToVoice() }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SettingsVoice, null, tint = NovaCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Voice Customization (Speed & Pitch)", fontSize = 14.sp, color = NovaTextPrimary)
                }
                Icon(Icons.Default.ChevronRight, null, tint = NovaTextDimmed)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Permissions & Privacy link
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToPermissions() }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = NovaElectricBlue, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Permissions & Privacy Dashboard", fontSize = 14.sp, color = NovaTextPrimary)
                }
                Icon(Icons.Default.ChevronRight, null, tint = NovaTextDimmed)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Speech Engine Setup link
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToModelSetup() }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, null, tint = NovaMagenta, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Offline Speech Model Setup", fontSize = 14.sp, color = NovaTextPrimary)
                }
                Icon(Icons.Default.ChevronRight, null, tint = NovaTextDimmed)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Clear History Button
        Button(
            onClick = { viewModel.clearAllHistory() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x33EF4444),
                contentColor = NovaGlowError
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear Conversation History", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
