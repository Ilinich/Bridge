package com.begoml.bridge.uikit.shader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ShaderBrush
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState

internal expect fun isRuntimeShaderSupported(): Boolean

private const val MillisPerSecond = 1000f

/**
 * A compiled shader and the clock that drives it, held apart from the nodes that draw it.
 *
 * Splitting the two is what lets many nodes share one program: a grid of cards draws the same
 * instance instead of compiling a program per cell.
 */
@Stable
class AnimatedShader internal constructor(
    internal val spec: ShaderSpec,
    internal val program: ShaderProgram?,
    internal val timeSeconds: MutableFloatState,
)

/**
 * Compiles [spec] once and advances its clock while the host lifecycle is at least RESUMED.
 *
 * Returns a handle rather than a `Brush` on purpose. `ShaderBrush` caches the shader it built and
 * rebuilds it only when the draw size changes, so an animated shader handed out as a brush renders
 * its first frame forever.
 */
@Composable
fun rememberAnimatedShader(spec: ShaderSpec): AnimatedShader {
    val program = remember(spec.source) {
        if (isRuntimeShaderSupported()) ShaderProgram(spec.source) else null
    }
    val timeSeconds = remember { mutableFloatStateOf(0f) }
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()

    LaunchedEffect(program, lifecycleState) {
        if (program == null) return@LaunchedEffect
        if (!lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) return@LaunchedEffect
        var startMillis = 0L
        while (true) {
            withFrameMillis { frameMillis ->
                if (startMillis == 0L) startMillis = frameMillis
                timeSeconds.floatValue = (frameMillis - startMillis) / MillisPerSecond
            }
        }
    }

    return remember(program, spec) { AnimatedShader(spec, program, timeSeconds) }
}

/**
 * Fills this node with [shader], or with the spec's fallback where no runtime shader exists.
 *
 * The clock is read inside the draw lambda, so a new frame invalidates drawing only — reading it
 * in composition would recompose the whole calling screen sixty times a second.
 */
fun Modifier.shaded(shader: AnimatedShader): Modifier = drawBehind {
    val program = shader.program
    if (program == null) {
        drawRect(brush = shader.spec.fallback)
    } else {
        drawRect(brush = ShaderBrush(program.shader(shader.timeSeconds.floatValue, size)))
    }
}
