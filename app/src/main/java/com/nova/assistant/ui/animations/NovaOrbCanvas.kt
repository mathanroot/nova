package com.nova.assistant.ui.animations

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.nova.assistant.data.models.AssistantState
import com.nova.assistant.ui.theme.NovaCyan
import com.nova.assistant.ui.theme.NovaElectricBlue
import com.nova.assistant.ui.theme.NovaGlowError
import com.nova.assistant.ui.theme.NovaGlowPaused
import com.nova.assistant.ui.theme.NovaMagenta
import com.nova.assistant.ui.theme.NovaPurpleDeep
import com.nova.assistant.ui.theme.NovaViolet
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NovaOrbCanvas(
    modifier: Modifier = Modifier,
    assistantState: AssistantState,
    audioAmplitude: Float = 0f,
    isBatterySaving: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NovaOrbInfinite")

    // Slow organic breathing
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingScale"
    )

    // Swirling rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (assistantState) {
                    AssistantState.PROCESSING -> 1400
                    AssistantState.EXECUTING -> 2000
                    AssistantState.COMMAND_LISTENING -> 2800
                    AssistantState.SPEAKING -> 2400
                    AssistantState.WAKE_WORD_DETECTED -> 1000
                    else -> 6000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    // Fluid wave oscillation phase
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (assistantState) {
                    AssistantState.COMMAND_LISTENING, AssistantState.SPEAKING -> 1800
                    AssistantState.PROCESSING -> 1200
                    else -> 4000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // Smooth state-driven target scale
    val stateTargetScale = when (assistantState) {
        AssistantState.WAKE_WORD_DETECTED -> 1.25f
        AssistantState.COMMAND_LISTENING -> 1.15f
        AssistantState.SPEAKING -> 1.18f
        AssistantState.PROCESSING -> 1.08f
        AssistantState.EXECUTING -> 1.10f
        AssistantState.ERROR -> 0.96f
        AssistantState.PAUSED -> 0.88f
        else -> 1.0f
    }
    val animatedStateScale by animateFloatAsState(
        targetValue = stateTargetScale,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "stateScale"
    )

    // Audio-reactive expansion
    val reactiveAmplitudeScale = 1.0f + (audioAmplitude * 0.45f)

    val particleField = remember { ParticleField(if (isBatterySaving) 12 else 24) }
    val particleColors = listOf(NovaCyan, NovaElectricBlue, NovaViolet, NovaMagenta)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2.7f) * breathingScale * animatedStateScale * reactiveAmplitudeScale

            if (particleField.particles.isEmpty()) {
                particleField.init(size.width, size.height, particleColors)
            }
            particleField.update(center.x, center.y, if (assistantState == AssistantState.PAUSED) 0.2f else 1.0f)

            // 1. Draw Subtle Floating Ambient Particles
            if (!isBatterySaving) {
                for (p in particleField.particles) {
                    drawCircle(
                        color = p.color.copy(alpha = p.alpha * if (assistantState == AssistantState.PAUSED) 0.2f else 0.7f),
                        radius = p.radius,
                        center = Offset(p.x, p.y)
                    )
                }
            }

            // State-based Primary Gradient Colors
            val (coreColor1, coreColor2, outerHaloColor) = when (assistantState) {
                AssistantState.ERROR -> Triple(NovaGlowError, Color(0xFFB91C1C), NovaGlowError.copy(alpha = 0.3f))
                AssistantState.PAUSED -> Triple(NovaGlowPaused, NovaSurfaceDark, NovaGlowPaused.copy(alpha = 0.15f))
                AssistantState.PROCESSING -> Triple(NovaViolet, NovaMagenta, NovaElectricBlue.copy(alpha = 0.4f))
                AssistantState.EXECUTING -> Triple(NovaMagenta, NovaViolet, NovaCyan.copy(alpha = 0.5f))
                AssistantState.WAKE_WORD_DETECTED -> Triple(NovaCyan, NovaElectricBlue, NovaCyan.copy(alpha = 0.6f))
                AssistantState.COMMAND_LISTENING -> Triple(NovaCyan, NovaElectricBlue, NovaViolet.copy(alpha = 0.45f))
                AssistantState.SPEAKING -> Triple(NovaCyan, NovaMagenta, NovaElectricBlue.copy(alpha = 0.5f))
                else -> Triple(NovaElectricBlue, NovaViolet, NovaCyan.copy(alpha = 0.35f))
            }

            // 2. Soft Outer Bloom Halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        outerHaloColor,
                        outerHaloColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.55f
                ),
                radius = baseRadius * 1.55f,
                center = center
            )

            // 3. Fluid Organic Deformed Waves (Layer 1 - Deep Violet / Magenta)
            drawDeformedWaveLayer(
                center = center,
                radius = baseRadius * 0.95f,
                phase = wavePhase,
                harmonicFreq = 3,
                amplitude = if (assistantState == AssistantState.COMMAND_LISTENING || assistantState == AssistantState.SPEAKING) 18f + (audioAmplitude * 25f) else 10f,
                brush = Brush.radialGradient(
                    colors = listOf(coreColor2.copy(alpha = 0.85f), NovaPurpleDeep.copy(alpha = 0.6f), Color.Transparent),
                    center = center,
                    radius = baseRadius
                )
            )

            // 4. Fluid Organic Deformed Waves (Layer 2 - Electric Blue / Cyan)
            drawDeformedWaveLayer(
                center = center,
                radius = baseRadius * 0.82f,
                phase = wavePhase + 1.8f,
                harmonicFreq = 4,
                amplitude = if (assistantState == AssistantState.COMMAND_LISTENING || assistantState == AssistantState.SPEAKING) 14f + (audioAmplitude * 20f) else 8f,
                brush = Brush.radialGradient(
                    colors = listOf(coreColor1.copy(alpha = 0.9f), coreColor2.copy(alpha = 0.7f), Color.Transparent),
                    center = center,
                    radius = baseRadius * 0.85f
                )
            )

            // 5. Pulsating Inner Glowing Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (assistantState == AssistantState.WAKE_WORD_DETECTED) 0.95f else 0.75f),
                        coreColor1.copy(alpha = 0.85f),
                        coreColor2.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 0.65f
                ),
                radius = baseRadius * 0.65f,
                center = center,
                blendMode = BlendMode.Screen
            )
        }
    }
}

private fun DrawScope.drawDeformedWaveLayer(
    center: Offset,
    radius: Float,
    phase: Float,
    harmonicFreq: Int,
    amplitude: Float,
    brush: Brush
) {
    val path = Path()
    val steps = 60
    val angleStep = (2 * PI / steps).toFloat()

    for (i in 0 until steps) {
        val angle = i * angleStep
        // Multi-frequency harmonic distortion
        val wave1 = sin(angle * harmonicFreq + phase) * amplitude
        val wave2 = cos(angle * 2 - phase * 0.7f) * (amplitude * 0.5f)
        val r = radius + wave1 + wave2

        val x = center.x + (cos(angle) * r)
        val y = center.y + (sin(angle) * r)

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    drawPath(path = path, brush = brush)
}
