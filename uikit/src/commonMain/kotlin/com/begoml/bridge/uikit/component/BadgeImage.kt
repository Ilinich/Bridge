package com.begoml.bridge.uikit.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter

/**
 * A club badge where the feed supplies one, and the monogram everywhere else.
 *
 * The monogram also covers loading and failure, so a slow or missing image never leaves a hole in
 * a row of fixtures.
 */
@Composable
fun BadgeImage(
    url: String?,
    code: String,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    highlighted: Boolean = false,
) {
    if (url.isNullOrBlank()) {
        TeamMonogram(code = code, modifier = modifier, size = size, highlighted = highlighted)
        return
    }

    val painter = rememberAsyncImagePainter(model = url)
    val state by painter.state.collectAsState()

    Box(modifier = modifier.size(size)) {
        if (state is AsyncImagePainter.State.Success) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(size).clip(CircleShape),
            )
        } else {
            TeamMonogram(code = code, size = size, highlighted = highlighted)
        }
    }
}
