package com.begoml.bridge.uikit.video

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.begoml.bridge.uikit.theme.BridgeColors
import java.awt.Desktop
import java.net.URI

/**
 * The clip, handed to the system player.
 *
 * The JVM decodes no video. Everything that could change that is a liability a supporter app has
 * no business carrying: VLCJ needs VLC installed on the machine, JavaFX brings tens of megabytes
 * of platform natives and a Swing panel inside a Compose window. So the desktop keeps the panel
 * the design asks for and gives the clip to whatever the person already plays video with — which
 * is also what a desktop app is expected to do with media it does not own.
 *
 * The transport below it stays honest: nothing is playing here, so nothing claims to be.
 */
private class HandoffPlayback(private val url: String) : VideoPlayback {

    override val isPlaying = false
    override val isBuffering = false
    override val positionMillis = 0L
    override val durationMillis = 0L
    override val isMuted = true

    override fun play() {
        runCatching {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) desktop.browse(URI(url))
        }
    }

    override fun pause() = Unit

    override fun seekTo(millis: Long) = Unit

    override fun mute(muted: Boolean) = Unit
}

@Composable
actual fun rememberVideoPlayback(
    url: String,
    autoPlay: Boolean,
    loop: Boolean,
    muted: Boolean,
): VideoPlayback = remember(url) { HandoffPlayback(url) }

@Composable
actual fun VideoSurface(playback: VideoPlayback, modifier: Modifier) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(listOf(BridgeColors.Club, BridgeColors.Ground)),
        ),
        contentAlignment = Alignment.Center,
    ) {
        PlayMark()
    }
}

/** The same triangle the transport carries, at the size a poster wants it. */
@Composable
private fun PlayMark() {
    Canvas(modifier = Modifier.size(34.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.22f, 0f)
            lineTo(size.width, size.height * 0.5f)
            lineTo(size.width * 0.22f, size.height)
            close()
        }
        drawPath(path = path, color = BridgeColors.TextPrimary.copy(alpha = 0.82f))
    }
}
