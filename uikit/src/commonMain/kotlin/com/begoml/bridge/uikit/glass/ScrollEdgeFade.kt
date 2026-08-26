package com.begoml.bridge.uikit.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.begoml.bridge.uikit.theme.BridgeColors
import dev.chrisbanes.haze.blur.HazeBlurDefaults
import dev.chrisbanes.haze.blur.HazeProgressive
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect

private val EdgeBlurRadius = 20.dp
private const val EdgeTintAlpha = 0.92f

/**
 * A band at one end of scrolling content that fades what runs under it.
 *
 * Two effects, because one cannot do the job. The progressive gradient fades the *blur*, so rows
 * soften as they approach the edge instead of stopping at a visible seam. The darkening is a
 * separate gradient drawn on top: Haze's tint is applied to the whole layer and does not follow
 * the progressive mask, so relying on it would produce exactly the hard edge this exists to avoid.
 *
 * Below Android 12 there is no blur to fade and Haze draws nothing; the darkening gradient still
 * renders, which is the same shape with less depth rather than a missing element.
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
    val tint = when (edge) {
        ScrollEdge.Top -> listOf(
            BridgeColors.Ground.copy(alpha = EdgeTintAlpha),
            Color.Transparent,
        )
        ScrollEdge.Bottom -> listOf(
            Color.Transparent,
            BridgeColors.Ground.copy(alpha = EdgeTintAlpha),
        )
    }

    Box(
        modifier = modifier
            .align(alignment)
            .fillMaxWidth()
            .height(height)
            .then(edgeBlur(edge))
            .background(Brush.verticalGradient(tint)),
    )
}

@Composable
private fun GlassScope.edgeBlur(edge: ScrollEdge): Modifier = Modifier.hazeEffect(hazeState) {
    blurEffect {
        blurRadius = EdgeBlurRadius
        backgroundColor = Color.Transparent
        colorEffects = listOf(HazeBlurDefaults.tint(Color.Transparent))
        progressive = when (edge) {
            ScrollEdge.Top ->
                HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f)
            ScrollEdge.Bottom ->
                HazeProgressive.verticalGradient(startIntensity = 0f, endIntensity = 1f)
        }
    }
}
