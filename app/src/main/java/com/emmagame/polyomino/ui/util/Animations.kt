package com.emmagame.polyomino.ui.util

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import android.provider.Settings

@Composable
fun rememberAnimationsEnabled(): Boolean {
    val view = LocalView.current
    return remember {
        try {
            val scale = Settings.Global.getFloat(
                view.context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            scale > 0f
        } catch (_: Exception) {
            true
        }
    }
}

// Placement overshoot: scale 0.8 → 1.08 → 1.0 over 400ms
fun Modifier.placementAnimation(trigger: Boolean): Modifier = composed {
    val animationsEnabled = rememberAnimationsEnabled()
    if (!animationsEnabled) return@composed this

    val scale by animateFloatAsState(
        targetValue = if (trigger) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "placement_scale"
    )
    this.scale(scale)
}

// Invalid shake: horizontal offset ±4dp over 400ms
@Composable
fun rememberShakeState(): MutableState<Boolean> = remember { mutableStateOf(false) }

fun Modifier.shakeAnimation(shaking: Boolean): Modifier = composed {
    val animationsEnabled = rememberAnimationsEnabled()
    if (!animationsEnabled || !shaking) return@composed this

    val offsetX by animateFloatAsState(
        targetValue = 0f,
        animationSpec = keyframes {
            durationMillis = 400
            0f at 0
            8f at 50
            -8f at 100
            6f at 150
            -6f at 200
            4f at 250
            -4f at 300
            2f at 350
            0f at 400
        },
        label = "shake_offset"
    )
    this.offset(x = offsetX.dp)
}

// Active player pulse: 1500ms infinite alpha oscillation
@Composable
fun rememberPulseAlpha(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    return alpha
}

// AI thinking dots animation
@Composable
fun rememberThinkingDots(): String {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking")
    val dotCount by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinking_dots"
    )
    return ".".repeat(dotCount.toInt().coerceIn(0, 3))
}

// Ghost appear: alpha 0 → 1 over 150ms
fun Modifier.ghostAppearAnimation(visible: Boolean): Modifier = composed {
    val animationsEnabled = rememberAnimationsEnabled()
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = if (animationsEnabled) tween(150, easing = FastOutSlowInEasing) else snap(),
        label = "ghost_alpha"
    )
    this.alpha(alpha)
}
