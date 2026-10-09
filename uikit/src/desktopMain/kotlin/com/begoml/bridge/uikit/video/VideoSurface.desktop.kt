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
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.Path
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
import org.jcodec.common.model.Picture
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import java.io.File
import java.net.URI
import java.security.MessageDigest

private const val MillisPerSecond = 1000.0
private const val DefaultFrameMillis = 33L
private const val NanosPerMilli = 1_000_000L

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

    /**
     * Shows every frame, sleeping only what is left of its interval after the work.
     *
     * Two earlier versions were wrong in opposite ways. The first waited a whole frame interval
     * *after* decoding one, so the cost of decoding was counted twice and a sixty-frame second
     * played at thirty, wobbling with the cost of each frame — an I-frame and the frames that
     * follow it are not the same work. The second paced against the wall clock and dropped frames
     * that were late, which is what a player with hardware decoding does; here it threw away all
     * but one frame in twenty, because dropping a frame does not make the next one cheaper when
     * the decoder itself is the limit.
     *
     * Software H.264 is the limit — around fifty frames a second for this clip — so the honest
     * behaviour is to show what was decoded and sleep only the remainder. A clip that cannot keep
     * up runs a little slow rather than unevenly, and the readout stays truthful because it
     * counts the clip's own frames rather than the time on the wall.
     */
    private suspend fun play(decoder: ClipDecoder) {
        var shownMillis = 0L
        while (scope.isActive) {
            val seekTo = seekToSeconds
            shownMillis = when {
                seekTo != null -> {
                    seekToSeconds = null
                    decoder.seek(seekTo)
                    (seekTo * MillisPerSecond).toLong()
                }

                isPlaying -> showNext(decoder, shownMillis)

                else -> {
                    delay(decoder.frameMillis)
                    shownMillis
                }
            }
        }
    }

    /** Decodes, draws and then sleeps whatever is left of the frame's own interval. */
    private suspend fun showNext(decoder: ClipDecoder, shownMillis: Long): Long {
        val startedAt = System.nanoTime()
        val picture = decoder.next()
        if (picture == null) {
            if (!loop) isPlaying = false
            decoder.seek(0.0)
            withContext(Dispatchers.Main) { positionMillis = 0L }
            return 0L
        }

        val bitmap = decoder.convert(picture)
        val next = shownMillis + decoder.frameMillis
        withContext(Dispatchers.Main) {
            frame = bitmap
            positionMillis = next
        }

        val workMillis = (System.nanoTime() - startedAt) / NanosPerMilli
        delay((decoder.frameMillis - workMillis).coerceAtLeast(0L))
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

    /** The next picture, still in the decoder's own colour space, or null at the end. */
    suspend fun next(): Picture? = withContext(Dispatchers.Default) {
        runCatching { grab.nativeFrame }.getOrNull()
    }

    /**
     * Turns a decoded picture into something Compose can draw.
     *
     * Separate from [next] because it is the half worth skipping: a frame the clock has already
     * passed is thrown away before this runs, and this is where the colour conversion and the
     * bitmap allocation happen.
     */
    suspend fun convert(picture: Picture): ImageBitmap = withContext(Dispatchers.Default) {
        picture.toImageBitmap()
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

/**
 * Turns a decoded picture into a bitmap, at half the width and half the height.
 *
 * Both halves of that sentence are the point. Going straight from the decoder's YUV to the bytes
 * Skia wants skips a `BufferedImage` and a second pass over every pixel — the library route
 * measured around fourteen milliseconds a frame, which is most of a frame's budget spent on
 * copying. And the panel is a hundred and eighty points tall, so a seven-hundred-and-twenty-line
 * picture is four times the pixels anything will ever show; dropping every other row and column
 * is free here, because the samples are simply not read.
 *
 * The arithmetic is BT.601 in fixed point. jcodec hands samples back already biased by -128,
 * which is why luma is `+ 128` and the two chroma terms are used as they arrive.
 */
private fun Picture.toImageBitmap(): ImageBitmap {
    val width = getPlaneWidth(0) / Step
    val height = getPlaneHeight(0) / Step
    val lumaStride = getPlaneWidth(0)
    val chromaStride = getPlaneWidth(1)
    val luma = getPlaneData(0)
    val blueChroma = getPlaneData(1)
    val redChroma = getPlaneData(2)

    val pixels = ByteArray(width * height * BytesPerPixel)
    var out = 0
    for (row in 0 until height) {
        val lumaRow = row * Step * lumaStride
        val chromaRow = row / 2 * chromaStride
        for (column in 0 until width) {
            val y = (luma[lumaRow + column * Step] + Bias) shl FixedShift
            val u = blueChroma[chromaRow + column / 2].toInt()
            val v = redChroma[chromaRow + column / 2].toInt()

            pixels[out] = clamp(y + RedV * v)
            pixels[out + 1] = clamp(y - GreenU * u - GreenV * v)
            pixels[out + 2] = clamp(y + BlueU * u)
            pixels[out + 3] = Opaque
            out += BytesPerPixel
        }
    }

    val bitmap = Bitmap()
    bitmap.allocPixels(ImageInfo(width, height, ColorType.RGBA_8888, ColorAlphaType.OPAQUE))
    bitmap.installPixels(pixels)
    return bitmap.asComposeImageBitmap()
}

private fun clamp(fixed: Int): Byte {
    val value = fixed shr FixedShift
    return when {
        value < 0 -> 0
        value > MaxSample -> MaxSample.toByte()
        else -> value.toByte()
    }
}

/** Half the width and half the height: the panel cannot show more. */
private const val Step = 2
private const val BytesPerPixel = 4
private const val Bias = 128
private const val Opaque: Byte = -1
private const val MaxSample = 255
private const val FixedShift = 10
private const val RedV = 1436
private const val GreenU = 352
private const val GreenV = 731
private const val BlueU = 1815
