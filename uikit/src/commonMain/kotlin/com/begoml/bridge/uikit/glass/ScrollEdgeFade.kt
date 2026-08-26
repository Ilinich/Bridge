package com.begoml.bridge.uikit.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.begoml.bridge.uikit.shader.EdgeFadeShader
import com.begoml.bridge.uikit.shader.rememberStaticShaderBrush
import dev.chrisbanes.haze.blur.HazeBlurDefaults
import dev.chrisbanes.haze.blur.HazeProgressive
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect

private val EdgeBlurRadius = 24.dp

/**
 * A translucent band at one end of scrolling content.
 *
 * Two effects, and neither can be dropped. The progressive mask fades the *blur*, so rows soften
 * as they approach the edge; the tint is a separate dithered gradient drawn on top, because Haze
 * applies its own tint to the whole layer and it does not follow that mask — leaning on it puts a
 * visible border exactly where this is meant to have none.
 *
 * The tint stays translucent on purpose: content is meant to be visible through it, blurred, in
 * the way a bar sits over a list rather than covering it.
 */
@Composable
fun GlassScope.ScrollEdgeFade(
    edge: ScrollEdge,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val alignment = when (edge) {
        ScrollEdge.Top -> Alignment.TopCenter
        ScrollEdge.Bottom -> Alignment.BottomCenter
    }
    val tint = rememberStaticShaderBrush(EdgeFadeShader)

    Box(
        modifier = modifier
            .align(alignment)
            .fillMaxWidth()
            .height(height)
            // The shader and the mask both run top-down; the other end is the same pair flipped.
            .graphicsLayer { rotationX = if (edge == ScrollEdge.Bottom) 180f else 0f }
            .then(progressiveBlur())
            .background(tint),
    )
}

@Composable
private fun GlassScope.progressiveBlur(): Modifier = Modifier.hazeEffect(hazeState) {
    blurEffect {
        blurRadius = EdgeBlurRadius
        backgroundColor = Color.Transparent
        colorEffects = listOf(HazeBlurDefaults.tint(Color.Transparent))
        progressive = HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f)
    }
}
