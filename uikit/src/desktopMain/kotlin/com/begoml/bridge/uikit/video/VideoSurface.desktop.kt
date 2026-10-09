// Two rules are suppressed for this file, and both are about where it sits rather than about what
// it does. `InjectDispatcher` keeps dispatchers out of code that has to be testable; this is the
// platform's own decoder and the platform's own pool is the answer, which is why the iOS
// dispatcher actual carries the same suppression. `RedundantSuspendModifier` misfires here: every
// function it names suspends through `withContext`, which detekt cannot see without a classpath
// for this source set.
@file:Suppress("InjectDispatcher", "RedundantSuspendModifier")

package com.begoml.bridge.uikit.video

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.begoml.bridge.uikit.theme.BridgeColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jcodec.api.FrameGrab
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.io.SeekableByteChannel
import org.jcodec.scale.AWTUtil
import java.io.File
import java.net.URI
import java.security.MessageDigest

private const val MillisPerSecond = 1000.0
private const val DefaultFrameMillis = 33L

/**
 * The clip decoded in the JVM and drawn by Compose.
 *
 * The alternatives were an interop surface — JavaFX in a Swing panel, or VLC through JNA — and
 * both put a native view over the window rather than inside the composition: it would not clip to
 * the scroll, would not round its corners and would float over the tab bar. A decoded frame is an
 * `ImageBitmap` like any other, so the panel behaves like the rest of the page.
 *
 * What that costs is honest and bounded: software H.264, which measures around fifty frames a
 * second for this clip on an ordinary machine, and no sound — there is no AAC decoder here, which
 * is why [hasAudio] is false and the controls drop the mute button.
 */
private class JvmPlayback(
    private val url: String,
    private val loop: Boolean,
    autoPlay: Boolean,
) : VideoPlayback, RememberObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    var frame by mutableStateOf<ImageBitmap?>(null)
        private set

    override var isPlaying by mutableStateOf(autoPlay)
        private set

    override var isBuffering by mutableStateOf(true)
        private set

    override var positionMillis by mutableLongStateOf(0L)
        private set

    override var durationMillis by mutableLongStateOf(0L)
        private set

    override val isMuted = true

    override val hasAudio = false

    private var seekToSeconds: Double? = null

    override fun play() {
        isPlaying = true
    }

    override fun pause() {
        isPlaying = false
    }

    override fun seekTo(millis: Long) {
        seekToSeconds = millis / MillisPerSecond
        positionMillis = millis
    }

    override fun mute(muted: Boolean) = Unit

    override fun onRemembered() {
        scope.launch { run() }
    }

    /**
     * Decoding is a resource the composition owns, so it ends on both exits. `onAbandoned` is the
     * one that matters: a remembered value whose composition is discarded never reaches a
     * DisposableEffect, and the decoder would keep a thread and a file handle for nothing.
     */
    override fun onForgotten() = scope.cancel()

    override fun onAbandoned() = scope.cancel()

    private suspend fun run() {
        val decoder = open() ?: return
        durationMillis = decoder.durationMillis
        isBuffering = false
        try {
            play(decoder)
        } finally {
            decoder.close()
        }
    }

    private suspend fun open(): ClipDecoder? {
        val file = runCatching { download(url) }.getOrNull()
        return file?.let { cached -> runCatching { ClipDecoder.open(cached) }.getOrNull() }
    }

    private suspend fun play(decoder: ClipDecoder) {
        var shown = 0L
        while (scope.isActive) {
            seekToSeconds?.let { seconds ->
                seekToSeconds = null
                decoder.seek(seconds)
                shown = (seconds * MillisPerSecond).toLong()
            }
            if (isPlaying) {
                shown = advance(decoder, shown)
            }
            delay(decoder.frameMillis)
        }
    }

    /** One frame on, or back to the start — and, for a clip that does not loop, a stop with it. */
    private suspend fun advance(decoder: ClipDecoder, shown: Long): Long {
        val bitmap = decoder.next()
        if (bitmap == null) {
            if (!loop) isPlaying = false
            decoder.seek(0.0)
            withContext(Dispatchers.Main) { positionMillis = 0L }
            return 0L
        }
        val next = shown + decoder.frameMillis
        withContext(Dispatchers.Main) {
            frame = bitmap
            positionMillis = next
        }
        return next
    }

    /**
     * The clip, kept in the temp directory between runs.
     *
     * A decoder needs a file it can seek in, and the file is a few megabytes of public footage —
     * cache, not data, so it goes where the system clears things rather than into the app's own
     * directory next to what a person chose.
     */
    private suspend fun download(from: String): File = withContext(Dispatchers.IO) {
        val name = MessageDigest.getInstance("SHA-1")
            .digest(from.encodeToByteArray())
            .joinToString("") { byte -> "%02x".format(byte) }
        val cache = File(System.getProperty("java.io.tmpdir"), "bridge-media").apply { mkdirs() }
        val file = File(cache, "$name.mp4")
        if (!file.exists() || file.length() == 0L) {
            URI(from).toURL().openStream().use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
        }
        file
    }
}

/** The decoder and the two numbers the player needs from the file it opened. */
private class ClipDecoder private constructor(
    private val channel: SeekableByteChannel,
    private val grab: FrameGrab,
    val frameMillis: Long,
    val durationMillis: Long,
) {

    /** The next picture, or null at the end of the stream. */
    suspend fun next(): ImageBitmap? = withContext(Dispatchers.Default) {
        val picture = runCatching { grab.nativeFrame }.getOrNull()
        picture?.let { decoded -> AWTUtil.toBufferedImage(decoded).toComposeImageBitmap() }
    }

    suspend fun seek(seconds: Double) {
        withContext(Dispatchers.Default) { runCatching { grab.seekToSecondPrecise(seconds) } }
    }

    fun close() {
        runCatching { channel.close() }
    }

    companion object {

        suspend fun open(file: File): ClipDecoder = withContext(Dispatchers.IO) {
            val channel = NIOUtils.readableChannel(file)
            val grab = FrameGrab.createFrameGrab(channel)
            val meta = grab.videoTrack.meta
            val frames = meta.totalFrames
            ClipDecoder(
                channel = channel,
                grab = grab,
                frameMillis = if (frames > 0) {
                    (meta.totalDuration * MillisPerSecond / frames).toLong()
                } else {
                    DefaultFrameMillis
                },
                durationMillis = (meta.totalDuration * MillisPerSecond).toLong(),
            )
        }
    }
}

@Composable
actual fun rememberVideoPlayback(
    url: String,
    autoPlay: Boolean,
    loop: Boolean,
    muted: Boolean,
): VideoPlayback = remember(url) { JvmPlayback(url = url, loop = loop, autoPlay = autoPlay) }

@Composable
actual fun VideoSurface(playback: VideoPlayback, modifier: Modifier) {
    val jvm = playback as JvmPlayback
    val frame = jvm.frame

    Box(
        // The surface is a play control as well as a picture, the way a player on a page is.
        modifier = modifier.clickable { if (jvm.isPlaying) jvm.pause() else jvm.play() },
        contentAlignment = Alignment.Center,
    ) {
        if (frame == null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Brush.verticalGradient(listOf(BridgeColors.Club, BridgeColors.Ground))),
            )
            PlayMark()
        } else {
            Image(
                bitmap = frame,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
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
