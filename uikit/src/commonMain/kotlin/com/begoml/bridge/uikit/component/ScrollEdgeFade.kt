package com.begoml.bridge.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.begoml.bridge.uikit.glass.ScrollEdge
import com.begoml.bridge.uikit.shader.EdgeFadeShader
import com.begoml.bridge.uikit.shader.rememberStaticShaderBrush
import com.begoml.bridge.uikit.theme.BridgeColors

/**
 * A band at one end of scrolling content that sinks what runs under it into the background.
 *
 * Two pieces rather than one gradient. [solid] is flat colour, where no ramp exists to band, and
 * only [fade] carries the ramp — a single gradient across the whole band is already half
 * transparent a few dp in, which left the clock sitting on whatever scrolled under it.
 *
 * The ramp is a dithered shader, not a [androidx.compose.ui.graphics.Brush]: over this distance an
 * 8-bit gradient holds each value for several rows and reads as visible bands. Nothing here
 * animates, so it costs one draw and never asks for a frame. Where no runtime shader exists the
 * spec falls back to a plain gradient, which bands a little and is otherwise the same picture.
 */
@Composable
fun BoxScope.ScrollEdgeFade(
    edge: ScrollEdge,
    solid: Dp,
    fade: Dp,
    modifier: Modifier = Modifier,
) {
    val alignment = when (edge) {
        ScrollEdge.Top -> Alignment.TopCenter
        ScrollEdge.Bottom -> Alignment.BottomCenter
    }
    val brush = rememberStaticShaderBrush(EdgeFadeShader)

    Column(modifier = modifier.align(alignment).fillMaxWidth()) {
        val solidBand = @Composable {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(solid)
                    .background(BridgeColors.Ground),
            )
        }
        val fadeBand = @Composable {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fade)
                    // The shader always fades downwards; the bottom edge is the same ramp flipped.
                    .graphicsLayer { rotationX = if (edge == ScrollEdge.Bottom) 180f else 0f }
                    .background(brush),
            )
        }

        when (edge) {
            ScrollEdge.Top -> {
                solidBand()
                fadeBand()
            }
            ScrollEdge.Bottom -> {
                fadeBand()
                solidBand()
            }
        }
    }
}
