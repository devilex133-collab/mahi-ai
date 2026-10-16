package com.mahi.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNav(vm: AppViewModel = viewModel()) {
    val nav = rememberNavController()
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0A0612), Color(0xFF1B1030))))) {
        NavHost(nav, startDestination = "dashboard") {
            composable("dashboard") { Dashboard(nav, vm) }
            composable("chat") { ChatScreen(vm) }
            composable("voice") { VoiceScreen(vm) }
            composable("launcher") { LauncherScreen(vm) }
            composable("settings") { SettingsScreen(vm) }
            composable("coding") { ComingSoon("Coding Workspace") }
            composable("automation") { ComingSoon("Automation Center") }
            composable("memory") { ComingSoon("Memory Manager") }
        }
    }
}
