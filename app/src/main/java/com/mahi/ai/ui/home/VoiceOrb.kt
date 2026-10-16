package com.mahi.ai.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mahi.ai.ui.theme.Violet
import com.mahi.ai.ui.theme.VioletLight

@Composable
fun VoiceOrb(animate: Boolean, modifier: Modifier = Modifier) {
    val scale: Float
    if (animate) {
        val t = rememberInfiniteTransition(label = "orb")
        val v by t.animateFloat(
            initialValue = 0.92f, targetValue = 1.05f,
            animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "orbScale"
        )
        scale = v
    } else {
        scale = 1f
    }
    Canvas(modifier.size(200.dp)) {
        val c = Offset(size.width / 2, size.height / 2)
        val r = size.minDimension / 2 * 0.6f * scale
        drawCircle(
            Brush.radialGradient(listOf(Violet.copy(alpha = 0.35f), Color.Transparent), c, r * 1.6f),
            radius = r * 1.6f, center = c
        )
        drawCircle(
            Brush.radialGradient(listOf(VioletLight, Violet, Color(0xFF4C1D95)), c, r),
            radius = r, center = c
        )
    }
}
