package com.nova.assistant.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nova.assistant.data.models.AssistantState
import com.nova.assistant.ui.animations.NovaOrbCanvas
import com.nova.assistant.ui.components.GlassmorphicCard
import com.nova.assistant.ui.theme.NovaCardGlassBorder
import com.nova.assistant.ui.theme.NovaCyan
import com.nova.assistant.ui.theme.NovaDeepBlack
import com.nova.assistant.ui.theme.NovaElectricBlue
import com.nova.assistant.ui.theme.NovaGlowError
import com.nova.assistant.ui.theme.NovaMagenta
import com.nova.assistant.ui.theme.NovaSurfaceDark
import com.nova.assistant.ui.theme.NovaTextDimmed
import com.nova.assistant.ui.theme.NovaTextPrimary
import com.nova.assistant.ui.theme.NovaTextSecondary
import com.nova.assistant.ui.theme.NovaViolet

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSettings: () -> Unit
) {
    val assistantState by viewModel.assistantState.collectAsState()
    val lastCommand by viewModel.lastRecognizedCommand.collectAsState()
    val lastResponse by viewModel.lastResponse.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val isHeyNovaEnabled by viewModel.isHeyNovaEnabled.collectAsState()
    val isBatterySaving by viewModel.isBatterySaving.collectAsState()

    val suggestions = listOf(
        "Turn on flashlight",
        "How much battery is left?",
        "Where am I?",
        "Open WhatsApp",
        "Set volume to 70 percent",
        "What time is it?",
        "Calculate 250 multiplied by 5",
        "Check available storage",
        "Turn on flashlight and increase volume"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NovaDeepBlack,
                        Color(0xFF070B1E),
                        NovaDeepBlack
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NovaCyan, NovaViolet))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "NOVA",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = NovaTextPrimary
                    )
                    Text(
                        text = if (isHeyNovaEnabled) "Hey Nova Active" else "Offline Assistant",
                        fontSize = 11.sp,
                        color = if (isHeyNovaEnabled) NovaCyan else NovaTextDimmed
                    )
                }
            }

            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x221E293B))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = NovaTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Centerpiece: Siri-Inspired Animated Glowing Orb
        Box(
            modifier = Modifier
                .size(280.dp),
            contentAlignment = Alignment.Center
        ) {
            NovaOrbCanvas(
                modifier = Modifier.fillMaxSize(),
                assistantState = assistantState,
                audioAmplitude = audioAmplitude,
                isBatterySaving = isBatterySaving
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status indicator pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0x221E293B))
                .border(1.dp, NovaCardGlassBorder, RoundedCornerShape(30.dp))
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Text(
                text = assistantState.displayName,
                color = when (assistantState) {
                    AssistantState.ERROR -> NovaGlowError
                    AssistantState.COMMAND_LISTENING, AssistantState.WAKE_WORD_DETECTED -> NovaCyan
                    AssistantState.PROCESSING, AssistantState.EXECUTING -> NovaViolet
                    AssistantState.SPEAKING -> NovaElectricBlue
                    else -> NovaTextSecondary
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Spoken Command & NOVA Response
        AnimatedVisibility(
            visible = lastCommand.isNotEmpty() || lastResponse.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (lastCommand.isNotEmpty()) {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = "YOU",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NovaCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"$lastCommand\"",
                                fontSize = 15.sp,
                                color = NovaTextPrimary
                            )
                        }
                    }
                }

                if (lastResponse.isNotEmpty()) {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = "NOVA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NovaMagenta
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lastResponse,
                                fontSize = 15.sp,
                                color = NovaTextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Central Microphone Control
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = when (assistantState) {
                            AssistantState.COMMAND_LISTENING -> listOf(NovaCyan, NovaElectricBlue)
                            AssistantState.SPEAKING -> listOf(NovaMagenta, NovaViolet)
                            AssistantState.ERROR -> listOf(NovaGlowError, Color(0xFFB91C1C))
                            else -> listOf(NovaElectricBlue, NovaViolet)
                        }
                    )
                )
                .clickable { viewModel.onMicTapped() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (assistantState) {
                    AssistantState.COMMAND_LISTENING -> Icons.Default.Mic
                    AssistantState.SPEAKING -> Icons.Default.Stop
                    AssistantState.PAUSED -> Icons.Default.MicOff
                    else -> Icons.Default.Mic
                },
                contentDescription = "Microphone",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (assistantState) {
                AssistantState.COMMAND_LISTENING -> "Tap to stop listening"
                AssistantState.SPEAKING -> "Tap to stop speaking"
                else -> "Tap microphone or say \"Hey Nova\""
            },
            fontSize = 12.sp,
            color = NovaTextDimmed,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Suggestion Chips Carousel
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "SUGGESTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = NovaTextDimmed,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (chip in suggestions) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x221E293B))
                            .border(1.dp, NovaCardGlassBorder, RoundedCornerShape(16.dp))
                            .clickable { viewModel.executeUserCommand(chip) }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = chip,
                            fontSize = 13.sp,
                            color = NovaTextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
