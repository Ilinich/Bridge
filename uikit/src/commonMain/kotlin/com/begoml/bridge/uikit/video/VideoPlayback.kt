package com.begoml.bridge.uikit.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

/**
 * A clip the caller can drive.
 *
 * Every member is Compose state, so a control that reads [positionMillis] recomposes as the clip
 * advances without the caller polling anything. [durationMillis] is zero until the platform has
 * parsed the header, which is why controls must treat zero as unknown rather than as an empty clip.
 */
@Stable
interface VideoPlayback {

    val isPlaying: Boolean

    val isBuffering: Boolean

    val positionMillis: Long

    val durationMillis: Long

    val isMuted: Boolean

    fun play()

    fun pause()

    fun seekTo(millis: Long)

    fun mute(muted: Boolean)
}

/**
 * Creates a playback bound to the calling composition.
 *
 * The clip is released when the composition leaves, and playback follows the host lifecycle: a
 * screen the user has left decodes nothing, and a clip the user had paused stays paused when they
 * come back.
 */
@Composable
expect fun rememberVideoPlayback(
    url: String,
    autoPlay: Boolean = true,
    loop: Boolean = true,
    muted: Boolean = true,
): VideoPlayback

/** Draws the frames of [playback]. Carries no controls; compose it with [VideoControls]. */
@Composable
expect fun VideoSurface(playback: VideoPlayback, modifier: Modifier)

/**
 * A silent looping clip, used as a moving backdrop.
 *
 * There is no error surface on purpose — the caller draws a still image underneath, so a clip that
 * fails to load is invisible rather than broken.
 */
@Composable
fun VideoSurface(url: String, modifier: Modifier) {
    VideoSurface(playback = rememberVideoPlayback(url), modifier = modifier)
}
