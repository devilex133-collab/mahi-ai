package com.mahi.ai.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mahi.ai.ai.ChatMessage
import com.mahi.ai.ai.GeminiClient
import com.mahi.ai.ai.GeminiException
import com.mahi.ai.security.SecureStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val noKey: Boolean = false
)

class ChatViewModel(app: Application) : AndroidViewModel(app) {
    private val store = SecureStore(app)
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    private var job: Job? = null

    fun send(text: String) {
        val t = text.trim()
        if (t.isEmpty() || _state.value.loading) return
        val key = store.getApiKey()
        if (key == null) { _state.update { it.copy(noKey = true, error = null) }; return }
        val withUser = _state.value.messages + ChatMessage("user", t)
        _state.update { it.copy(messages = withUser, loading = true, error = null, noKey = false) }
        val context = withUser.takeLast(MAX_CONTEXT).dropWhile { it.role != "user" }
        job = viewModelScope.launch {
            try {
                val reply = GeminiClient.generate(key, store.getModel(), context)
                _state.update { it.copy(messages = it.messages + ChatMessage("model", reply), loading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: GeminiException) {
                _state.update { it.copy(loading = false, error = e.message) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = "Something went wrong.") }
            }
        }
    }

    fun cancel() { job?.cancel(); _state.update { it.copy(loading = false) } }
    fun clear() { job?.cancel(); _state.value = ChatState() }

    companion object { const val MAX_CONTEXT = 20 }
}
