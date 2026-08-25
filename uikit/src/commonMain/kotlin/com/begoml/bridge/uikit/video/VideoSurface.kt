package com.begoml.bridge.uikit.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A silent looping clip, used as a moving backdrop.
 *
 * Playback follows the host lifecycle: a screen the user has left decodes nothing. There is no
 * error surface on purpose — the caller draws a still image underneath, so a clip that fails to
 * load is invisible rather than broken.
 */
@Composable
expect fun VideoSurface(url: String, modifier: Modifier)
