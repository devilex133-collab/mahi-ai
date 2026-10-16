package com.mahi.ai.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val Purple = Color(0xFFB388FF)

@Composable
fun MahiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Purple, onPrimary = Color.Black,
            secondary = Color(0xFF7C4DFF), background = Color(0xFF0A0612),
            surface = Color(0xFF140C24), onSurface = Color(0xFFEDE7F6),
            onBackground = Color(0xFFEDE7F6)
        ),
        content = content
    )
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color(0x33B388FF), RoundedCornerShape(20.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun MahiOrb(orbSize: Dp = 160.dp, active: Boolean = false) {
    val t = rememberInfiniteTransition(label = "orb")
    val scale by t.animateFloat(
        initialValue = 0.9f, targetValue = if (active) 1.12f else 1.04f,
        animationSpec = infiniteRepeatable(
            tween(if (active) 600 else 2200, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "scale"
    )
    Canvas(Modifier.size(orbSize)) {
        val r = size.minDimension / 2 * scale
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFE0B3FF), Color(0xFF7C4DFF), Color(0x007C4DFF)),
                center = Offset(size.width / 2, size.height / 2), radius = r
            ),
            radius = r
        )
    }
}
