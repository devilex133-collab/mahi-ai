package com.mahi.ai.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.mahi.ai.command.ActionValidator
import com.mahi.ai.data.MessageEntity
import com.mahi.ai.voice.VoiceController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Dashboard(nav: NavController, vm: AppViewModel) {
    val s by vm.settings.collectAsState()
    val status by vm.status.collectAsState()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        MahiOrb()
        Text(
            if (s.useNickname) "Hi ${s.nickname}, ami Mahi 💜" else "Hi, ami Mahi 💜",
            style = MaterialTheme.typography.headlineSmall
        )
        GlassCard(Modifier.fillMaxWidth()) { Text("AI connection: $status") }
        Button({ nav.navigate("voice") }, Modifier.fillMaxWidth()) { Text("Start Voice") }
        Button({ nav.navigate("chat") }, Modifier.fillMaxWidth()) { Text("Chat") }
        Button({ nav.navigate("launcher") }, Modifier.fillMaxWidth()) { Text("App Launcher") }
        Button({ nav.navigate("settings") }, Modifier.fillMaxWidth()) { Text("Settings") }
        OutlinedButton({ nav.navigate("coding") }, Modifier.fillMaxWidth()) { Text("Coding Workspace (soon)") }
        OutlinedButton({ nav.navigate("automation") }, Modifier.fillMaxWidth()) { Text("Automation Center (soon)") }
        OutlinedButton({ nav.navigate("memory") }, Modifier.fillMaxWidth()) { Text("Memory Manager (soon)") }
    }
}

@Composable
fun Bubble(m: MessageEntity) {
    val mine = m.role == "user"
    Box(Modifier.fillMaxWidth(), contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart) {
        GlassCard(Modifier.widthIn(max = 300.dp)) { Text(m.text) }
    }
}

@Composable
fun PendingCard(vm: AppViewModel) {
    val p by vm.pending.collectAsState()
    p?.let {
        GlassCard(Modifier.fillMaxWidth()) {
            Text(ActionValidator.describe(it))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(vm::confirmPending) { Text("Confirm") }
                OutlinedButton(vm::cancelPending) { Text("Cancel") }
            }
        }
    }
}

@Composable
fun ErrorCard(vm: AppViewModel) {
    val err by vm.error.collectAsState()
    err?.let {
        GlassCard(Modifier.fillMaxWidth()) {
            Text(it, color = MaterialTheme.colorScheme.error)
            Row {
                TextButton(vm::retry) { Text("Retry") }
                TextButton(vm::dismissError) { Text("Dismiss") }
            }
        }
    }
}

@Composable
fun ChatScreen(vm: AppViewModel) {
    val msgs by vm.messages.collectAsState()
    val busy by vm.busy.collectAsState()
    var input by rememberSaveable { mutableStateOf("") }
    val list = rememberLazyListState()
    LaunchedEffect(msgs.size) { if (msgs.isNotEmpty()) list.animateScrollToItem(msgs.lastIndex) }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Mahi", style = MaterialTheme.typography.titleLarge)
        if (msgs.isEmpty()) Text("Say hi to Mahi 💜", Modifier.weight(1f))
        else LazyColumn(Modifier.weight(1f), state = list, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(msgs, key = { it.id }) { Bubble(it) }
        }
        if (busy) Text("Mahi is typing…")
        ErrorCard(vm)
        PendingCard(vm)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(input, { input = it }, Modifier.weight(1f), placeholder = { Text("Message Mahi…") })
            Button({ vm.send(input); input = "" }, enabled = !busy) { Text("Send") }
        }
    }
}

@Composable
fun VoiceScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    val voice = remember { VoiceController(ctx) }
    DisposableEffect(Unit) { onDispose { voice.shutdown() } }
    val state by voice.state.collectAsState()
    val s by vm.settings.collectAsState()
    val msgs by vm.messages.collectAsState()
    val busy by vm.busy.collectAsState()
    var note by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.replies.collect { if (s.autoSpeak) voice.speak(it, s.speechLang, s.speechRate) }
    }

    fun startListening() {
        note = null
        voice.listen(s.speechLang, onResult = { vm.send(it) }, onError = { note = it })
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) startListening() else note = "Microphone permission denied. Enable it in system settings to use voice."
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MahiOrb(active = state != VoiceController.State.Idle)
        Text(
            when {
                state == VoiceController.State.Listening -> "Listening…"
                state == VoiceController.State.Speaking -> "Speaking…"
                busy -> "Thinking…"
                else -> "Tap to talk (${s.speechLang})"
            }
        )
        note?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        ErrorCard(vm)
        PendingCard(vm)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                    startListening()
                else permission.launch(Manifest.permission.RECORD_AUDIO)
            }, enabled = state == VoiceController.State.Idle && !busy) { Text("Listen") }
            OutlinedButton({ voice.stop() }, enabled = state != VoiceController.State.Idle) { Text("Stop") }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(msgs.takeLast(6), key = { it.id }) { Bubble(it) }
        }
    }
}

@Composable
fun LauncherScreen(vm: AppViewModel) {
    var apps by remember { mutableStateOf<List<com.mahi.ai.launcher.AppInfo>?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { apps = withContext(Dispatchers.IO) { vm.appList() } }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("App Launcher", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), placeholder = { Text("Search apps") })
        msg?.let { Text(it) }
        val all = apps
        if (all == null) CircularProgressIndicator()
        else {
            val shown = all.filter { it.label.contains(query, ignoreCase = true) }
            if (shown.isEmpty()) Text("No apps found.")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(shown, key = { it.pkg }) { a ->
                    GlassCard(Modifier.fillMaxWidth()) {
                        TextButton({ msg = if (vm.launchApp(a.pkg)) null else "${a.label} can't be opened." }) { Text(a.label) }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val s by vm.settings.collectAsState()
    val status by vm.status.collectAsState()
    var url by remember(s.backendUrl) { mutableStateOf(s.backendUrl) }
    var token by remember { mutableStateOf("") }
    var model by remember(s.model) { mutableStateOf(s.model) }
    var nick by remember(s.nickname) { mutableStateOf(s.nickname) }
    var confirmClear by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)
        Text("AI (Gemini via your backend)", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(url, { url = it }, Modifier.fillMaxWidth(), label = { Text("Backend URL (https://…/api)") }, singleLine = true)
        OutlinedTextField(
            token, { token = it }, Modifier.fillMaxWidth(),
            label = { Text(if (s.token.isBlank()) "App token" else "App token (saved — type to replace)") },
            visualTransformation = PasswordVisualTransformation(), singleLine = true
        )
        OutlinedTextField(model, { model = it }, Modifier.fillMaxWidth(), label = { Text("Model") }, singleLine = true)
        Text("Suggested: gemini-2.5-flash, gemini-2.5-pro, gemini-2.5-flash-lite", style = MaterialTheme.typography.bodySmall)
        Text("Companion", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(nick, { nick = it }, Modifier.fillMaxWidth(), label = { Text("Nickname") }, singleLine = true)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(s.useNickname, { v -> vm.update { it.copy(useNickname = v) } }); Spacer(Modifier.width(8.dp)); Text("Use nickname")
        }
        Button({
            vm.update { it.copy(backendUrl = url.trim(), model = model.trim(), nickname = nick.trim(), token = token.ifBlank { it.token }) }
            token = ""
        }) { Text("Save") }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ vm.testConnection() }) { Text("Test connection") }
            Text(status)
        }
        Text("Voice", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(s.speechLang == "bn-BD", { vm.update { it.copy(speechLang = "bn-BD") } }, { Text("বাংলা") })
            FilterChip(s.speechLang == "en-US", { vm.update { it.copy(speechLang = "en-US") } }, { Text("English") })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(s.autoSpeak, { v -> vm.update { it.copy(autoSpeak = v) } }); Spacer(Modifier.width(8.dp)); Text("Auto-speak replies")
        }
        Text("Speech speed: ${"%.1f".format(s.speechRate)}x")
        Slider(s.speechRate, { v -> vm.update { it.copy(speechRate = v) } }, valueRange = 0.5f..1.5f)
        Text("Privacy", style = MaterialTheme.typography.titleMedium)
        OutlinedButton({ confirmClear = true }) { Text("Delete conversation history") }
        Text("Mahi AI v0.1.0", style = MaterialTheme.typography.bodySmall)
    }
    if (confirmClear) AlertDialog(
        onDismissRequest = { confirmClear = false },
        title = { Text("Delete all history?") },
        text = { Text("This can't be undone.") },
        confirmButton = { TextButton({ vm.clearHistory(); confirmClear = false }) { Text("Delete") } },
        dismissButton = { TextButton({ confirmClear = false }) { Text("Cancel") } }
    )
}

@Composable
fun ComingSoon(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        GlassCard { Text(title, style = MaterialTheme.typography.titleLarge); Text("Not implemented yet.") }
    }
}
