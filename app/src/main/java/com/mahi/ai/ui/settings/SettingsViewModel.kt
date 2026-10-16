package com.mahi.ai.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsState(val darkTheme: Boolean = true, val reduceMotion: Boolean = false)

class SettingsViewModel : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun setDarkTheme(value: Boolean) = _state.update { it.copy(darkTheme = value) }
    fun setReduceMotion(value: Boolean) = _state.update { it.copy(reduceMotion = value) }
}
