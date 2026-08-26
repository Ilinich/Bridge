package com.begoml.bridge.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.begoml.bridge.uikit.glass.ScrollEdge
import com.begoml.bridge.uikit.theme.BridgeColors

private const val EdgeTintAlpha = 0.94f

/**
 * A band at one end of scrolling content that sinks what runs under it into the background.
 *
 * [solid] is held at full strength and only [fade] is graded. A single linear ramp across the whole
 * band is already half transparent a few dp in, which leaves the clock sitting on whatever scrolls
 * under it; keeping the inset opaque is what makes the system bars readable.
 *
 * Only a gradient. An earlier version also faded a blur through Haze's progressive mask, which
 * looked much the same and cost a blur pass on every frame of every scroll; a ground-coloured
 * gradient buys the same separation in one draw, on every platform and API level, with no
 * fallback path.
 */
@Composable
fun BoxScope.ScrollEdgeFade(
    edge: ScrollEdge,
    solid: Dp,
    fade: Dp,
    modifier: Modifier = Modifier,
) {
    val height = solid + fade
    val solidStop = if (height.value > 0f) (solid / height).coerceIn(0f, 1f) else 0f
    val alignment = when (edge) {
        ScrollEdge.Top -> Alignment.TopCenter
        ScrollEdge.Bottom -> Alignment.BottomCenter
    }
    val opaque = BridgeColors.Ground.copy(alpha = EdgeTintAlpha)
    val brush = when (edge) {
        ScrollEdge.Top -> Brush.verticalGradient(
            0f to opaque,
            solidStop to opaque,
            1f to Color.Transparent,
        )
        ScrollEdge.Bottom -> Brush.verticalGradient(
            0f to Color.Transparent,
            (1f - solidStop) to opaque,
            1f to opaque,
        )
    }

    Box(
        modifier = modifier
            .align(alignment)
            .fillMaxWidth()
            .height(height)
            .background(brush),
    )
}
