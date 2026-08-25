package com.begoml.bridge.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.begoml.bridge.uikit.theme.BridgeColors

/**
 * A full-bleed photograph with the scrim that makes text on top of it readable.
 *
 * Image loading lives here rather than in the screens, so a feature module never depends on the
 * loader directly and swapping it stays a change to one file.
 */
@Composable
fun BackdropImage(url: String?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(BridgeColors.Ground)) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.45f to BridgeColors.Ground.copy(alpha = 0.65f),
                    1f to BridgeColors.Ground,
                ),
            ),
        )
    }
}
