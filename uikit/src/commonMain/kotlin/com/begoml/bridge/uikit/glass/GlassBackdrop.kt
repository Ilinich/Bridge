package com.begoml.bridge.uikit.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.begoml.bridge.uikit.theme.BridgeColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.blur.HazeBlurDefaults
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeSource

/**
 * Pairs a blurred backdrop with the panels drawn over it.
 *
 * The pairing is a type, not a convention, because the ordering it enforces is easy to get wrong
 * and silent when wrong: if a panel carrying `hazeEffect` ends up **inside** the node carrying
 * `hazeSource`, the blur becomes a no-op on iOS with no error and no log. Here the backdrop and
 * the content are siblings by construction, and `Modifier.glass()` exists only inside
 * [GlassScope], so a caller cannot nest one in the other.
 *
 * Where the platform cannot blur — Android before 12 — Haze falls back to a translucent scrim on
 * its own, and the layout is unchanged.
 */
@Composable
fun GlassBackdrop(
    modifier: Modifier = Modifier,
    backdrop: @Composable BoxScope.() -> Unit,
    content: @Composable GlassScope.() -> Unit,
) {
    val hazeState = remember { HazeState() }
    Box(modifier) {
        Box(modifier = Modifier.fillMaxSize().hazeSource(hazeState)) { backdrop() }
        val scope = remember(hazeState, this) { GlassScope(hazeState, this) }
        scope.content()
    }
}

class GlassScope internal constructor(
    private val hazeState: HazeState,
    boxScope: BoxScope,
) : BoxScope by boxScope {

    /**
     * Frosts whatever sits behind this node.
     *
     * [inputScale] is deliberately left alone: measurement on device showed it scales only the
     * convolution while the surrounding layer work stays full size, so lowering it buys nothing.
     * The only real lever is not drawing the effect at all.
     */
    fun Modifier.glass(
        shape: Shape = RoundedCornerShape(GlassCornerRadius),
        tint: Color = BridgeColors.Ground.copy(alpha = GlassTintAlpha),
    ): Modifier = clip(shape).hazeEffect(hazeState) {
        blurEffect {
            blurRadius = GlassBlurRadius
            // An opaque backgroundColor paints a solid rect into the content layer and does not
            // fade with the mask; the darkening is carried by the tint instead.
            backgroundColor = Color.Transparent
            colorEffects = listOf(HazeBlurDefaults.tint(tint))
        }
    }

    private companion object {
        val GlassCornerRadius = 16.dp
        val GlassBlurRadius = 24.dp
        const val GlassTintAlpha = 0.55f
    }
}
