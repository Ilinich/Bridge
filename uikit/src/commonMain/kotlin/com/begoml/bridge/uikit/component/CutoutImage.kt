package com.begoml.bridge.uikit.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * A player cut out of their background.
 *
 * The feed supplies these as transparent PNGs, so they are drawn unscaled and bottom-aligned —
 * resizing eats the edges that make the cut-out read as a cut-out.
 */
@Composable
fun CutoutImage(url: String?, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomCenter,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
