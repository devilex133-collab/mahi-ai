package com.mahi.ai.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mahi.ai.security.SecureStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsState(
    val darkTheme: Boolean = true,
    val reduceMotion: Boolean = false,
    val hasKey: Boolean = false,
    val model: String = SecureStore.DEFAULT_MODEL
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val store = SecureStore(app)
    private val _state = MutableStateFlow(SettingsState(hasKey = store.hasApiKey(), model = store.getModel()))
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun setDarkTheme(value: Boolean) = _state.update { it.copy(darkTheme = value) }
    fun setReduceMotion(value: Boolean) = _state.update { it.copy(reduceMotion = value) }

    fun saveKey(key: String) {
        store.setApiKey(key)
        _state.update { it.copy(hasKey = store.hasApiKey()) }
    }
    fun saveModel(model: String) {
        store.setModel(model)
        _state.update { it.copy(model = store.getModel()) }
    }
}
