package com.mahi.ai.ui.home

import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mahi.ai.R
import com.mahi.ai.navigation.Routes
import com.mahi.ai.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(settingsViewModel: SettingsViewModel, onNavigate: (String) -> Unit) {
    val s by settingsViewModel.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val systemAnimationsOn = remember {
        Settings.Global.getFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
    Column(
        Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VoiceOrb(animate = systemAnimationsOn && !s.reduceMotion)
        Text(stringResource(R.string.greeting), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        GlassCard {
            Text(stringResource(R.string.session_idle), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.status_ai), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.status_mic), style = MaterialTheme.typography.bodySmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onNavigate(Routes.VOICE) }, Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.mic)) }
            FilledTonalButton(onClick = { onNavigate(Routes.CHAT) }, Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.chat)) }
        }
        OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.stop_session))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = { onNavigate(Routes.CODING) }, label = { Text(stringResource(R.string.coding)) })
            AssistChip(onClick = { onNavigate(Routes.MEMORY) }, label = { Text(stringResource(R.string.memory)) })
            AssistChip(onClick = { onNavigate(Routes.SETTINGS) }, label = { Text(stringResource(R.string.settings)) })
        }
        GlassCard {
            Text(stringResource(R.string.recent), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.recent_empty), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
    }
}
