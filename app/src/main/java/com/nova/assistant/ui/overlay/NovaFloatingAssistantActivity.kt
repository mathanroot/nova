package com.nova.assistant.ui.overlay

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nova.assistant.data.models.AssistantState
import com.nova.assistant.data.repository.ConversationRepository
import com.nova.assistant.speech.audio.AudioVisualizerProcessor
import com.nova.assistant.speech.recognition.OfflineSpeechEngine
import com.nova.assistant.speech.tts.SpeechOutput
import com.nova.assistant.ui.animations.NovaOrbCanvas
import com.nova.assistant.ui.components.GlassmorphicCard
import com.nova.assistant.ui.theme.NovaAssistantTheme
import com.nova.assistant.ui.theme.NovaCyan
import com.nova.assistant.ui.theme.NovaMagenta
import com.nova.assistant.ui.theme.NovaTextDimmed
import com.nova.assistant.ui.theme.NovaTextPrimary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NovaFloatingAssistantActivity : ComponentActivity() {

    @Inject lateinit var speechEngine: OfflineSpeechEngine
    @Inject lateinit var visualizerProcessor: AudioVisualizerProcessor
    @Inject lateinit var speechOutput: SpeechOutput
    @Inject lateinit var conversationRepository: ConversationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupScreenWakeAndLockFlags()

        setContent {
            NovaAssistantTheme {
                FloatingSiriOverlayContent(
                    visualizerProcessor = visualizerProcessor,
                    speechOutput = speechOutput,
                    conversationRepository = conversationRepository,
                    onDismiss = { finish() }
                )
            }
        }
    }

    private fun setupScreenWakeAndLockFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }
}

@Composable
fun FloatingSiriOverlayContent(
    visualizerProcessor: AudioVisualizerProcessor,
    speechOutput: SpeechOutput,
    conversationRepository: ConversationRepository,
    onDismiss: () -> Unit
) {
    val amplitude by visualizerProcessor.currentAmplitude.collectAsState()
    val isSpeaking by speechOutput.isSpeaking.collectAsState()
    val conversations by conversationRepository.getAllConversations().collectAsState(initial = emptyList())
    val latestTurn = conversations.firstOrNull()

    // Outer Scrim: Tapping outside dismisses overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3060814)) // Translucent dark blur
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
            .navigationBarsPadding()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Floating Response Card
            AnimatedVisibility(
                visible = latestTurn != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (latestTurn != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(
                                    text = latestTurn.userCommand,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = NovaCyan
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = latestTurn.assistantResponse,
                                    fontSize = 16.sp,
                                    color = NovaTextPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Siri Orb at bottom
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                NovaOrbCanvas(
                    modifier = Modifier.fillMaxSize(),
                    assistantState = if (isSpeaking) AssistantState.SPEAKING else AssistantState.COMMAND_LISTENING,
                    audioAmplitude = amplitude
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isSpeaking) "Speaking..." else "Listening...",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NovaTextDimmed,
                textAlign = TextAlign.Center
            )
        }
    }
}
