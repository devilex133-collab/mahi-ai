package com.mahi.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mahi.ai.navigation.MahiNavHost
import com.mahi.ai.ui.settings.SettingsViewModel
import com.mahi.ai.ui.theme.MahiTheme

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.state.collectAsStateWithLifecycle()
            MahiTheme(darkTheme = settings.darkTheme) {
                MahiNavHost(settingsViewModel = settingsViewModel)
            }
        }
    }
}
