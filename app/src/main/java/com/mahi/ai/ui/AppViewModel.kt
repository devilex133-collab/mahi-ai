package com.mahi.ai.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mahi.ai.MahiApplication
import com.mahi.ai.ai.ChatTurn
import com.mahi.ai.ai.PersonaPrompt
import com.mahi.ai.command.ActionValidator
import com.mahi.ai.command.AssistantAction
import com.mahi.ai.command.CommandParser
import com.mahi.ai.data.MessageEntity
import com.mahi.ai.data.Settings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val g = app as MahiApplication
    private val dao = g.db.messages()

    val messages = dao.observe().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val settings = g.prefs.flow.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private val _pending = MutableStateFlow<AssistantAction?>(null)
    val pending: StateFlow<AssistantAction?> = _pending
    private val _status = MutableStateFlow("Not tested")
    val status: StateFlow<String> = _status

    /** Assistant replies, so the voice screen can speak them. */
    private val _replies = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val replies: SharedFlow<String> = _replies

    fun send(text: String) {
        val t = text.trim()
        if (t.isEmpty() || _busy.value) return
        viewModelScope.launch {
            dao.insert(MessageEntity(role = "user", text = t))
            val action = CommandParser.parse(t)
            if (action != null && ActionValidator.isAllowed(action)) {
                _pending.value = action
                reply("${ActionValidator.describe(action)} — confirm?")
            } else askAI()
        }
    }

    fun retry() { if (!_busy.value) viewModelScope.launch { askAI() } }
    fun dismissError() { _error.value = null }

    fun confirmPending() {
        val a = _pending.value ?: return
        _pending.value = null
        viewModelScope.launch { reply(g.executor.execute(a)) }
    }

    fun cancelPending() {
        _pending.value = null
        viewModelScope.launch { reply("Okay, cancelled.") }
    }

    private suspend fun askAI() {
        _busy.value = true
        _error.value = null
        val s = settings.value
        val history = dao.last(20).reversed().map { ChatTurn(it.role, it.text) }
        g.provider.chat(history, PersonaPrompt.build(s), s.model)
            .onSuccess { reply(it) }
            .onFailure { _error.value = it.message ?: "Something went wrong." }
        _busy.value = false
    }

    private suspend fun reply(text: String) {
        dao.insert(MessageEntity(role = "assistant", text = text))
        _replies.emit(text)
    }

    fun update(block: (Settings) -> Settings) { viewModelScope.launch { g.prefs.update(block) } }
    fun clearHistory() { viewModelScope.launch { dao.clear() } }

    fun testConnection() {
        viewModelScope.launch {
            _status.value = "Testing…"
            _status.value = g.provider.health().fold({ it }, { "Failed: ${it.message}" })
        }
    }

    fun appList() = g.launcher.list()
    fun launchApp(pkg: String): Boolean {
        val i = g.launcher.launchIntent(pkg) ?: return false
        getApplication<Application>().startActivity(i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        return true
    }
}
