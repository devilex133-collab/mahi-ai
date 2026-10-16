package com.mahi.ai.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mahi.ai.R

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val s by viewModel.state.collectAsStateWithLifecycle()
    var keyInput by remember { mutableStateOf("") }
    var modelInput by remember(s.model) { mutableStateOf(s.model) }
    Column(
        Modifier.fillMaxSize().systemBarsPadding().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("← " + stringResource(R.string.back)) }
        Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium)
        SwitchRow(stringResource(R.string.dark_theme), s.darkTheme, viewModel::setDarkTheme)
        SwitchRow(stringResource(R.string.reduced_motion), s.reduceMotion, viewModel::setReduceMotion)
        HorizontalDivider()
        Text(stringResource(R.string.api_key), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(if (s.hasKey) R.string.api_key_saved else R.string.api_key_missing),
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedTextField(
            value = keyInput, onValueChange = { keyInput = it },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            label = { Text(stringResource(R.string.api_key)) }
        )
        Button(
            onClick = { viewModel.saveKey(keyInput); keyInput = "" },
            enabled = keyInput.isNotBlank(), modifier = Modifier.heightIn(min = 48.dp)
        ) { Text(stringResource(R.string.save)) }
        OutlinedTextField(
            value = modelInput, onValueChange = { modelInput = it },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            label = { Text(stringResource(R.string.model_name)) }
        )
        Button(onClick = { viewModel.saveModel(modelInput) }, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.save))
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
