package com.begoml.bridge.uikit.video

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import bridge.uikit.generated.resources.Res
import bridge.uikit.generated.resources.video_mute
import bridge.uikit.generated.resources.video_pause
import bridge.uikit.generated.resources.video_play
import bridge.uikit.generated.resources.video_unmute
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.FigureStyle
import org.jetbrains.compose.resources.stringResource

private const val MillisPerSecond = 1000L
private const val SecondsPerMinute = 60L

/**
 * Transport controls for a [VideoPlayback], written once for both platforms.
 *
 * While the user drags the scrubber the displayed position comes from the drag rather than from
 * the clip, because a clip that is still seeking would otherwise snap the thumb back under the
 * finger on every frame.
 */
@Composable
fun VideoControls(playback: VideoPlayback, modifier: Modifier = Modifier) {
    var scrubbingFraction by remember { mutableStateOf<Float?>(null) }
    val duration = playback.durationMillis
    val fraction = scrubbingFraction
        ?: if (duration > 0L) playback.positionMillis.toFloat() / duration else 0f
    val shownPosition = scrubbingFraction?.let { (it * duration).toLong() } ?: playback.positionMillis

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TransportButton(
            playing = playback.isPlaying,
            onClick = { if (playback.isPlaying) playback.pause() else playback.play() },
        )
        Text(
            text = formatClock(shownPosition),
            style = FigureStyle,
            color = BridgeColors.TextPrimary,
        )
        Slider(
            value = fraction.coerceIn(0f, 1f),
            onValueChange = { scrubbingFraction = it },
            onValueChangeFinished = {
                scrubbingFraction?.let { playback.seekTo((it * duration).toLong()) }
                scrubbingFraction = null
            },
            enabled = duration > 0L,
            colors = SliderDefaults.colors(
                thumbColor = BridgeColors.TextPrimary,
                activeTrackColor = BridgeColors.Club,
                inactiveTrackColor = Color.White.copy(alpha = 0.22f),
            ),
            modifier = Modifier.weight(1f).height(28.dp),
        )
        Text(
            text = formatClock(duration),
            style = FigureStyle,
            color = BridgeColors.TextMuted,
        )
        MuteButton(muted = playback.isMuted, onClick = { playback.mute(!playback.isMuted) })
    }
}

@Composable
private fun TransportButton(playing: Boolean, onClick: () -> Unit) {
    val label = stringResource(if (playing) Res.string.video_pause else Res.string.video_play)

    Box(
        modifier = Modifier
            .size(34.dp)
            .background(BridgeColors.Club, CircleShape)
            .clickable(onClickLabel = label, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        if (playing) PauseGlyph() else PlayGlyph()
    }
}

@Composable
private fun MuteButton(muted: Boolean, onClick: () -> Unit) {
    val label = stringResource(if (muted) Res.string.video_unmute else Res.string.video_mute)

    Box(
        modifier = Modifier
            .size(30.dp)
            .clickable(onClickLabel = label, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        SpeakerGlyph(muted = muted)
    }
}

private fun formatClock(millis: Long): String {
    val totalSeconds = (millis / MillisPerSecond).coerceAtLeast(0L)
    val minutes = totalSeconds / SecondsPerMinute
    val seconds = totalSeconds % SecondsPerMinute
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

@Composable
private fun PlayGlyph() {
    Canvas(modifier = Modifier.size(12.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.18f, 0f)
            lineTo(size.width, size.height * 0.5f)
            lineTo(size.width * 0.18f, size.height)
            close()
        }
        drawPath(path = path, color = BridgeColors.TextPrimary)
    }
}

@Composable
private fun PauseGlyph() {
    Canvas(modifier = Modifier.size(12.dp)) {
        val barWidth = size.width * 0.30f
        drawRect(color = BridgeColors.TextPrimary, size = androidx.compose.ui.geometry.Size(barWidth, size.height))
        drawRect(
            color = BridgeColors.TextPrimary,
            topLeft = Offset(size.width - barWidth, 0f),
            size = androidx.compose.ui.geometry.Size(barWidth, size.height),
        )
    }
}

@Composable
private fun SpeakerGlyph(muted: Boolean) {
    val tint = if (muted) BridgeColors.TextMuted else BridgeColors.TextPrimary

    Canvas(modifier = Modifier.size(15.dp)) {
        val cone = Path().apply {
            moveTo(0f, size.height * 0.34f)
            lineTo(size.width * 0.26f, size.height * 0.34f)
            lineTo(size.width * 0.52f, size.height * 0.08f)
            lineTo(size.width * 0.52f, size.height * 0.92f)
            lineTo(size.width * 0.26f, size.height * 0.66f)
            close()
        }
        drawPath(path = cone, color = tint)
        if (muted) {
            drawLine(
                color = tint,
                start = Offset(size.width * 0.64f, size.height * 0.30f),
                end = Offset(size.width * 0.96f, size.height * 0.70f),
                strokeWidth = size.width * 0.10f,
            )
            drawLine(
                color = tint,
                start = Offset(size.width * 0.96f, size.height * 0.30f),
                end = Offset(size.width * 0.64f, size.height * 0.70f),
                strokeWidth = size.width * 0.10f,
            )
        } else {
            drawArc(
                color = tint,
                startAngle = -55f,
                sweepAngle = 110f,
                useCenter = false,
                topLeft = Offset(size.width * 0.34f, size.height * 0.18f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.62f, size.height * 0.64f),
                style = Stroke(width = size.width * 0.09f),
            )
        }
    }
}
