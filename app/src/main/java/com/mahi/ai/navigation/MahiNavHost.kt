package com.mahi.ai.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mahi.ai.ui.common.PlannedScreen
import com.mahi.ai.ui.home.HomeScreen
import com.mahi.ai.ui.settings.SettingsScreen
import com.mahi.ai.ui.settings.SettingsViewModel

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val CHAT = "chat"
    const val VOICE = "voice"
    const val CODING = "coding"
    const val MEMORY = "memory"
}

@Composable
fun MahiNavHost(settingsViewModel: SettingsViewModel) {
    val nav = rememberNavController()
    val back: () -> Unit = { nav.popBackStack() }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        NavHost(nav, startDestination = Routes.HOME) {
            composable(Routes.HOME) {
                HomeScreen(settingsViewModel = settingsViewModel, onNavigate = { nav.navigate(it) })
            }
            composable(Routes.SETTINGS) { SettingsScreen(settingsViewModel, back) }
            composable(Routes.CHAT) { PlannedScreen("Chat", "Phase 2", back) }
            composable(Routes.VOICE) { PlannedScreen("Real-time voice", "Phase 3", back) }
            composable(Routes.MEMORY) { PlannedScreen("Memory", "Phase 4", back) }
            composable(Routes.CODING) { PlannedScreen("Coding Studio", "Phase 7", back) }
        }
    }
}
