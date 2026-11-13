package com.nova.assistant.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Assistant", Icons.Default.GraphicEq)
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Speed)
    data object History : Screen("history", "History", Icons.Default.History)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Voice : Screen("voice", "Voice", Icons.Default.GraphicEq)
    data object Permissions : Screen("permissions", "Privacy", Icons.Default.Security)
    data object ModelSetup : Screen("model_setup", "Speech Engine", Icons.Default.Settings)
}
