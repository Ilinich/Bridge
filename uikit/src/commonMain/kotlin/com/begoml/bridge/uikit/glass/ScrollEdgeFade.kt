package com.begoml.bridge.uikit.glass

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
import com.begoml.bridge.uikit.theme.BridgeColors

private const val PeakAlpha = 0.88f

/**
 * A dark translucent band at one end of scrolling content, with a soft edge.
 *
 * It dims what runs under it and nothing more: the content itself is never blurred. An earlier
 * version faded a blur through Haze's progressive mask, which softened the faces of the top row
 * rather than the boundary of the band.
 *
 * The ramp is a many-stop gradient on an eased curve. Density sits against the edge and the tail
 * runs long and thin, so there is no distance at which the band reads as having a border, and the
 * extra stops keep an 8-bit ramp from holding one value long enough to show as a step.
 */
@Composable
fun BoxScope.ScrollEdgeFade(
    edge: ScrollEdge,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val alignment = when (edge) {
        ScrollEdge.Top -> Alignment.TopCenter
        ScrollEdge.Bottom -> Alignment.BottomCenter
    }
    val stops = fadeStops(edge)

    Box(
        modifier = modifier
            .align(alignment)
            .fillMaxWidth()
            .height(height)
            .background(Brush.verticalGradient(colorStops = stops)),
    )
}

private const val StopCount = 12

private fun fadeStops(edge: ScrollEdge): Array<Pair<Float, Color>> = Array(StopCount) { index ->
    val t = index.toFloat() / (StopCount - 1)
    val distanceFromEdge = if (edge == ScrollEdge.Top) t else 1f - t
    val alpha = PeakAlpha * easeOut(1f - distanceFromEdge)
    t to BridgeColors.Ground.copy(alpha = alpha)
}

/** Holds density near the edge, then falls away — a shadow rather than a linear ramp. */
private fun easeOut(value: Float): Float = value * value * (3f - 2f * value)
