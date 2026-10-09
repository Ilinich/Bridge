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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.begoml.bridge.uikit.theme.BridgeColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jcodec.api.FrameGrab
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.io.SeekableByteChannel
import org.jcodec.common.model.Picture
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import java.io.File
import java.net.URI
import java.security.MessageDigest

private const val MillisPerSecond = 1000.0
private const val DefaultFrameNanos = 33_000_000L
private const val NanosPerSecond = 1_000_000_000.0
private const val NanosPerMilli = 1_000_000L

/** Deep enough to cover an expensive frame, short enough that a pause is felt at once. */
private const val QueuedFrames = 3

/** How far behind the clip may fall before it stops catching up and resynchronises instead. */
private const val MaxCatchUp = 2

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
    private val frames = Channel<ImageBitmap>(capacity = QueuedFrames)
    private var frameNanos = 0L
    private var shownIndex = 0L
    private var anchorNanos = 0L

    /**
     * Takes the clip one step on, called once per *display* frame.
     *
     * This is the part three earlier versions got wrong, each in a smaller way than the last.
     * They all drove the picture from a timer of their own — and a timer that runs at the clip's
     * rate is never in phase with the screen's. At sixty-one frames a second against a sixty-hertz
     * display, two frames land in one refresh about once a second and one of them is never seen:
     * the motion hitches, however even the numbers look from inside the player. Asking the
     * composition for its frame time instead means one decoded frame is chosen per refresh, by
     * when it is due rather than by when it was ready.
     *
     * A starved queue keeps the picture it has. Showing nothing would be a flash, and the clip
     * catches up on the next frame that arrives.
     */
    fun step(frameTimeNanos: Long) {
        if (frameNanos == 0L) return
        if (!isPlaying) {
            anchorNanos = frameTimeNanos - shownIndex * frameNanos
            return
        }
        if (anchorNanos == 0L) anchorNanos = frameTimeNanos - shownIndex * frameNanos

        val due = (frameTimeNanos - anchorNanos) / frameNanos
        if (due - shownIndex > MaxCatchUp) {
            // The composition stopped drawing for a while — the window went behind another, or a
            // frame was simply missed. Catching up would mean emptying the queue in one refresh
            // and then starving for several, which is a jump followed by a freeze. Resynchronise
            // instead: the clip carries on from where the picture is.
            anchorNanos = frameTimeNanos - shownIndex * frameNanos
            return
        }

        var taken: ImageBitmap? = null
        var starved = false
        while (shownIndex < due && !starved) {
            val next = frames.tryReceive().getOrNull()
            if (next == null) {
                starved = true
            } else {
                taken = next
                shownIndex++
            }
        }
        if (taken != null) {
            frame = taken
            positionMillis = shownIndex * frameNanos / NanosPerMilli
        }
    }

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
        frameNanos = decoder.frameNanos
        isBuffering = false
        try {
            produce(decoder)
        } finally {
            decoder.close()
        }
    }

    private suspend fun open(): ClipDecoder? {
        val file = runCatching { download(url) }.getOrNull()
        return file?.let { cached -> runCatching { ClipDecoder.open(cached) }.getOrNull() }
    }

    /**
     * Decodes ahead into a short queue, and stops when it is full.
     *
     * Pausing needs no flag of its own: nothing is taken from the queue, so it fills and the
     * decoder waits at the far end of it.
     */
    private suspend fun produce(decoder: ClipDecoder) {
        while (scope.isActive) {
            val seekTo = seekToSeconds
            if (seekTo != null) {
                seekToSeconds = null
                decoder.seek(seekTo)
                while (frames.tryReceive().isSuccess) Unit
                shownIndex = (seekTo * MillisPerSecond * NanosPerMilli / frameNanos).toLong()
            }

            val picture = decoder.next()
            when {
                picture != null -> frames.send(decoder.convert(picture))
                loop -> decoder.seek(0.0)
                else -> {
                    isPlaying = false
                    return
                }
            }
        }
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
    /**
     * How long one frame lasts, in nanoseconds.
     *
     * Not milliseconds: sixty frames a second is 16.67 of them, and the rounded 16 ran the clip
     * four percent fast — which on a display refreshing at its own rate is exactly the kind of
     * mismatch that reads as judder.
     */
    val frameNanos: Long,
    val durationMillis: Long,
) {

    private var scratch = ByteArray(0)

    /** The next picture, still in the decoder's own colour space, or null at the end. */
    suspend fun next(): Picture? = withContext(Dispatchers.Default) {
        runCatching { grab.nativeFrame }.getOrNull()
    }

    /**
     * Turns a decoded picture into something Compose can draw, at half width and half height.
     *
     * Separate from [next] so the two can be timed apart, and the one place a frame's pixels are
     * touched. The panel is a hundred and eighty points tall, so a seven-hundred-and-twenty-line
     * picture is four times what anything will show; the dropped rows and columns are never read.
     *
     * The byte buffer is reused across frames. A fresh one would be nine hundred kilobytes sixty
     * times a second, and the collector answering that is what the shaking was: a pause past a
     * hundred milliseconds every few frames. Only this coroutine writes it, and `makeRaster`
     * copies out of it, so the frame already on screen is nobody's business but Skia's.
     */
    suspend fun convert(picture: Picture): ImageBitmap = withContext(Dispatchers.Default) {
        val width = picture.getPlaneWidth(0) / Step
        val height = picture.getPlaneHeight(0) / Step
        val needed = width * height * BytesPerPixel
        if (scratch.size != needed) scratch = ByteArray(needed)

        picture.writeRgba(scratch, width, height)

        val info = ImageInfo(width, height, ColorType.RGBA_8888, ColorAlphaType.OPAQUE)
        Image.makeRaster(info, scratch, width * BytesPerPixel).toComposeImageBitmap()
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
                frameNanos = if (frames > 0) {
                    (meta.totalDuration * NanosPerSecond / frames).toLong()
                } else {
                    DefaultFrameNanos
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

    // The clip advances on the composition's own frame clock, so a decoded frame is chosen per
    // refresh rather than per tick of a timer that is never quite in phase with the screen.
    LaunchedEffect(jvm) {
        while (true) {
            withFrameNanos { frameTimeNanos -> jvm.step(frameTimeNanos) }
        }
    }

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
 * Writes the picture into [out] as RGBA at half size, averaging each 2×2 block of luma.
 *
 * The averaging is the difference between a downscale and a decimation. Taking every other pixel
 * instead — which is what the first version did — throws away the detail rather than folding it
 * in, and on a crowd it aliases: the pattern crawls from frame to frame and reads as the picture
 * shaking, even though the timing is even. Four reads instead of one is nothing next to decoding.
 *
 * The chroma planes are already half size and so is the output, so one output pixel is one chroma
 * sample; halving that index again stretched the colour of the top-left quarter over the whole
 * picture and turned the grass pink. The arithmetic is BT.601 in fixed point — jcodec hands
 * samples back already biased by -128, which is why luma is `+ 128` and the chroma terms are
 * used exactly as they arrive.
 */
private fun Picture.writeRgba(out: ByteArray, width: Int, height: Int) {
    val lumaStride = getPlaneWidth(0)
    val chromaStride = getPlaneWidth(1)
    val luma = getPlaneData(0)
    val blueChroma = getPlaneData(1)
    val redChroma = getPlaneData(2)

    var at = 0
    for (row in 0 until height) {
        val topRow = row * Step * lumaStride
        val bottomRow = topRow + lumaStride
        val chromaRow = row * chromaStride
        for (column in 0 until width) {
            val left = column * Step
            val averaged = (
                luma[topRow + left] + luma[topRow + left + 1] +
                    luma[bottomRow + left] + luma[bottomRow + left + 1]
                ) / Samples
            val y = (averaged + Bias) shl FixedShift
            val u = blueChroma[chromaRow + column].toInt()
            val v = redChroma[chromaRow + column].toInt()

            out[at] = clamp(y + RedV * v)
            out[at + 1] = clamp(y - GreenU * u - GreenV * v)
            out[at + 2] = clamp(y + BlueU * u)
            out[at + 3] = Opaque
            at += BytesPerPixel
        }
    }
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

/** The luma samples folded into one output pixel. */
private const val Samples = 4
private const val BytesPerPixel = 4
private const val Bias = 128
private const val Opaque: Byte = -1
private const val MaxSample = 255
private const val FixedShift = 10
private const val RedV = 1436
private const val GreenU = 352
private const val GreenV = 731
private const val BlueU = 1815
