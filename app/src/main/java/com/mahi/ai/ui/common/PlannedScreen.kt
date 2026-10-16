package com.mahi.ai.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mahi.ai.R

@Composable
fun PlannedScreen(feature: String, phase: String, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().systemBarsPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("← " + stringResource(R.string.back)) }
        Text(stringResource(R.string.planned_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.planned_body, feature, phase))
    }
}
