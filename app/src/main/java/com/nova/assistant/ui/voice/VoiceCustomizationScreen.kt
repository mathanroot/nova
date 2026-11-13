package com.nova.assistant.ui.voice

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nova.assistant.data.datastore.SettingsRepository
import com.nova.assistant.speech.tts.SpeechOutput
import com.nova.assistant.ui.components.GlassmorphicCard
import com.nova.assistant.ui.theme.NovaCyan
import com.nova.assistant.ui.theme.NovaDeepBlack
import com.nova.assistant.ui.theme.NovaElectricBlue
import com.nova.assistant.ui.theme.NovaTextDimmed
import com.nova.assistant.ui.theme.NovaTextPrimary
import com.nova.assistant.ui.theme.NovaTextSecondary
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun VoiceCustomizationScreen(
    settingsRepository: SettingsRepository,
    speechOutput: SpeechOutput,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val speed by settingsRepository.speechSpeed.collectAsState(initial = 1.0f)
    val pitch by settingsRepository.speechPitch.collectAsState(initial = 1.0f)

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
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NovaTextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "VOICE CUSTOMIZATION",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary
                )
                Text(
                    text = "Tune local speech synthesis",
                    fontSize = 12.sp,
                    color = NovaTextDimmed
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Speed Card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Speech Rate", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
                    Text(String.format(Locale.US, "%.2fx", speed), fontSize = 15.sp, color = NovaCyan)
                }
                Slider(
                    value = speed,
                    onValueChange = {
                        coroutineScope.launch {
                            settingsRepository.setSpeechSpeed(it)
                            speechOutput.setSpeechRate(it)
                        }
                    },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = NovaCyan,
                        activeTrackColor = NovaCyan,
                        inactiveTrackColor = Color(0x33475569)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pitch Card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Speech Pitch", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
                    Text(String.format(Locale.US, "%.2fx", pitch), fontSize = 15.sp, color = NovaElectricBlue)
                }
                Slider(
                    value = pitch,
                    onValueChange = {
                        coroutineScope.launch {
                            settingsRepository.setSpeechPitch(it)
                            speechOutput.setSpeechPitch(it)
                        }
                    },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = NovaElectricBlue,
                        activeTrackColor = NovaElectricBlue,
                        inactiveTrackColor = Color(0x33475569)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Test Voice Button
        Button(
            onClick = {
                speechOutput.speak("Hello! This is NOVA, your native offline voice assistant.")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = NovaCyan,
                contentColor = NovaDeepBlack
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Test Voice Synthesis", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}
