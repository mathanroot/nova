package com.nova.assistant.ui.modelsetup

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nova.assistant.speech.recognition.OfflineSpeechEngine
import com.nova.assistant.ui.components.GlassmorphicCard
import com.nova.assistant.ui.theme.NovaCyan
import com.nova.assistant.ui.theme.NovaDeepBlack
import com.nova.assistant.ui.theme.NovaGlowError
import com.nova.assistant.ui.theme.NovaTextDimmed
import com.nova.assistant.ui.theme.NovaTextPrimary
import com.nova.assistant.ui.theme.NovaTextSecondary

@Composable
fun ModelSetupScreen(
    speechEngine: OfflineSpeechEngine,
    onNavigateBack: () -> Unit
) {
    val isModelLoaded by speechEngine.isModelLoaded.collectAsState()
    var verificationStatus by remember { mutableStateOf<String?>(null) }
    val modelDir = speechEngine.getModelDirectory()

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
                    text = "SPEECH MODEL SETUP",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary
                )
                Text(
                    text = "Vosk Offline English Acoustic Engine",
                    fontSize = 12.sp,
                    color = NovaTextDimmed
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Status Card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Model Engine Status",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NovaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isModelLoaded) "Acoustic model is active & ready" else "Offline model not detected",
                        fontSize = 12.sp,
                        color = NovaTextSecondary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isModelLoaded) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isModelLoaded) NovaCyan else NovaGlowError,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isModelLoaded) "ACTIVE" else "NOT LOADED",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isModelLoaded) NovaCyan else NovaGlowError
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Directory Path Card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, null, tint = NovaCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Model Storage Location", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NovaTextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = modelDir.absolutePath,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NovaTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Setup Instructions Card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Offline Installation Instructions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaTextPrimary
                )
                Text(
                    text = "1. Download the lightweight offline English model from Alphacephei / Vosk (e.g. vosk-model-small-en-us-0.15.zip).",
                    fontSize = 13.sp,
                    color = NovaTextSecondary
                )
                Text(
                    text = "2. Unzip and copy the model folder into the storage location shown above.",
                    fontSize = 13.sp,
                    color = NovaTextSecondary
                )
                Text(
                    text = "3. Tap 'Verify & Load Model' below. Once loaded, NOVA provides 100% offline speech recognition without any internet connection.",
                    fontSize = 13.sp,
                    color = NovaTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (verificationStatus != null) {
            Text(
                text = verificationStatus!!,
                fontSize = 13.sp,
                color = if (isModelLoaded) NovaCyan else NovaGlowError,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Button(
            onClick = {
                val success = speechEngine.initModelIfAvailable()
                verificationStatus = if (success) {
                    "Model loaded successfully! Offline recognition is active."
                } else {
                    "No valid model found in folder. Please ensure files are placed in the directory."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = NovaCyan,
                contentColor = NovaDeepBlack
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Verify & Load Model", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
