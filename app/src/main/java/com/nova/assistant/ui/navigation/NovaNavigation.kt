package com.nova.assistant.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nova.assistant.data.datastore.SettingsRepository
import com.nova.assistant.speech.recognition.OfflineSpeechEngine
import com.nova.assistant.speech.tts.SpeechOutput
import com.nova.assistant.ui.dashboard.DashboardScreen
import com.nova.assistant.ui.dashboard.DashboardViewModel
import com.nova.assistant.ui.history.HistoryScreen
import com.nova.assistant.ui.history.HistoryViewModel
import com.nova.assistant.ui.home.HomeScreen
import com.nova.assistant.ui.home.HomeViewModel
import com.nova.assistant.ui.modelsetup.ModelSetupScreen
import com.nova.assistant.ui.permissions.PermissionsScreen
import com.nova.assistant.ui.settings.SettingsScreen
import com.nova.assistant.ui.settings.SettingsViewModel
import com.nova.assistant.ui.theme.NovaCyan
import com.nova.assistant.ui.theme.NovaDeepBlack
import com.nova.assistant.ui.theme.NovaTextDimmed
import com.nova.assistant.ui.voice.VoiceCustomizationScreen

@Composable
fun NovaNavigation(
    settingsRepository: SettingsRepository,
    speechOutput: SpeechOutput,
    offlineSpeechEngine: OfflineSpeechEngine
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Dashboard,
        Screen.History,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            if (bottomNavItems.any { it.route == currentRoute }) {
                NavigationBar(
                    containerColor = NovaDeepBlack,
                    contentColor = NovaCyan
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NovaCyan,
                                selectedTextColor = NovaCyan,
                                unselectedIconColor = NovaTextDimmed,
                                unselectedTextColor = NovaTextDimmed,
                                indicatorColor = Color(0x2200F5D4)
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .padding(paddingValues)
                .background(NovaDeepBlack)
        ) {
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(viewModel = dashboardViewModel)
            }
            composable(Screen.History.route) {
                val historyViewModel: HistoryViewModel = hiltViewModel()
                HistoryScreen(viewModel = historyViewModel)
            }
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToVoice = { navController.navigate(Screen.Voice.route) },
                    onNavigateToPermissions = { navController.navigate(Screen.Permissions.route) },
                    onNavigateToModelSetup = { navController.navigate(Screen.ModelSetup.route) }
                )
            }
            composable(Screen.Voice.route) {
                VoiceCustomizationScreen(
                    settingsRepository = settingsRepository,
                    speechOutput = speechOutput,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Permissions.route) {
                PermissionsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ModelSetup.route) {
                ModelSetupScreen(
                    speechEngine = offlineSpeechEngine,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
