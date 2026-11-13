package com.nova.assistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NovaCyan,
    secondary = NovaElectricBlue,
    tertiary = NovaMagenta,
    background = NovaDeepBlack,
    surface = NovaSurfaceDark,
    onPrimary = NovaDeepBlack,
    onSecondary = NovaDeepBlack,
    onTertiary = NovaDeepBlack,
    onBackground = NovaTextPrimary,
    onSurface = NovaTextPrimary
)

@Composable
fun NovaAssistantTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
