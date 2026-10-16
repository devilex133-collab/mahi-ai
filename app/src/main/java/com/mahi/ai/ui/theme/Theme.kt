package com.mahi.ai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Charcoal = Color(0xFF0E0B16)
val Surface1 = Color(0xFF1A1526)
val Violet = Color(0xFF8B5CF6)
val VioletLight = Color(0xFFC4B5FD)

private val DarkScheme = darkColorScheme(
    primary = Violet, secondary = VioletLight,
    background = Charcoal, surface = Surface1,
    onBackground = Color(0xFFF3EFFA), onSurface = Color(0xFFF3EFFA)
)
private val LightScheme = lightColorScheme(
    primary = Color(0xFF6D3FD9), secondary = Color(0xFF8B5CF6),
    background = Color(0xFFF7F4FD), surface = Color.White
)

@Composable
fun MahiTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkScheme else LightScheme, content = content)
}
