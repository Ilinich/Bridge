package com.begoml.bridge.uikit.shader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState

internal expect fun isRuntimeShaderSupported(): Boolean

private const val MillisPerSecond = 1000f

/**
 * A brush that animates a runtime shader, or the spec's fallback where none is available.
 *
 * Callers do not branch: both outcomes are a [Brush], and the fallback is a still gradient rather
 * than an error. The program is compiled once per [spec]; only uniforms change per frame. Frames
 * are requested only while the host lifecycle is at least RESUMED, so a screen the user has left
 * costs nothing.
 */
@Composable
fun rememberAnimatedShaderBrush(spec: ShaderSpec, enabled: Boolean = true): Brush {
    if (!enabled || !isRuntimeShaderSupported()) return spec.fallback

    val program = remember(spec.source) { ShaderProgram(spec.source) }
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val timeSeconds = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(lifecycleState) {
        if (!lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) return@LaunchedEffect
        var startMillis = 0L
        while (true) {
            withFrameMillis { frameMillis ->
                if (startMillis == 0L) startMillis = frameMillis
                timeSeconds.floatValue = (frameMillis - startMillis) / MillisPerSecond
            }
        }
    }

    // The clock is read inside createShader, which the draw phase calls, and never in composition.
    // Reading it here instead would put a per-frame snapshot read into the caller's restart scope
    // and recompose the whole calling screen sixty times a second.
    return remember(program) {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader =
                program.shader(timeSeconds.floatValue, size)
        }
    }
}
