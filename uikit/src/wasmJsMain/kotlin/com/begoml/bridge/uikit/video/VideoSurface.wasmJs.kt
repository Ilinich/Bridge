package com.begoml.bridge.uikit.video

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import com.begoml.bridge.uikit.LocalScreenPadding
import kotlinx.browser.document
import org.w3c.dom.HTMLVideoElement

private const val MillisPerSecond = 1000.0

/**
 * The clip as a real `<video>` element, laid over the canvas Compose draws into.
 *
 * There is no other way: the browser will not hand a cross-origin video's pixels to WebGL, so a
 * frame cannot be composited into the scene — the element has to sit on top of it. What Compose
 * owns is its geometry: the surface reports where it was laid out and the element is moved there,
 * which keeps it in step with scrolling and with the window being resized.
 *
 * It is clipped to the page's own content area so that a clip scrolled towards the tab bar
 * disappears under it rather than over it. The bar frosts what is behind it, and an element
 * outside the canvas is not something it can frost.
 */
private class HtmlPlayback(
    private val element: HTMLVideoElement,
    private val loop: Boolean,
) : VideoPlayback, RememberObserver {

    override var isPlaying by mutableStateOf(false)
        private set

    override var isBuffering by mutableStateOf(false)
        private set

    override var positionMillis by mutableLongStateOf(0L)
        private set

    override var durationMillis by mutableLongStateOf(0L)
        private set

    override var isMuted by mutableStateOf(element.muted)
        private set

    fun attach() {
        element.onplay = { isPlaying = true }
        element.onpause = { isPlaying = false }
        element.onwaiting = { isBuffering = true }
        element.onplaying = { isBuffering = false }
        element.ontimeupdate = { positionMillis = (element.currentTime * MillisPerSecond).toLong() }
        element.ondurationchange = { readDuration() }
        element.onended = { if (!loop) rewind() }
        readDuration()
    }

    /** Where the element has to be, in the page's own pixels rather than the canvas's. */
    fun placeAt(left: Double, top: Double, width: Double, height: Double, clip: String) {
        with(element.style) {
            setProperty("left", "${left}px")
            setProperty("top", "${top}px")
            setProperty("width", "${width}px")
            setProperty("height", "${height}px")
            setProperty("clip-path", clip)
        }
    }

    override fun play() {
        element.play()
    }

    override fun pause() {
        element.pause()
    }

    override fun seekTo(millis: Long) {
        element.currentTime = millis / MillisPerSecond
        positionMillis = millis
    }

    override fun mute(muted: Boolean) {
        element.muted = muted
        isMuted = muted
    }

    /**
     * Returns a finished clip to its first frame, the way the other platforms do: left at the end,
     * the readout sits at the duration and the next tap on play resumes nothing.
     */
    private fun rewind() {
        element.pause()
        element.currentTime = 0.0
        positionMillis = 0L
    }

    private fun readDuration() {
        val seconds = element.duration
        if (seconds.isFinite() && seconds > 0.0) {
            durationMillis = (seconds * MillisPerSecond).toLong()
        }
    }

    override fun onRemembered() = Unit

    /**
     * The element is the composition's, so it goes on both exits. `onAbandoned` is the one that
     * matters: a remembered value whose composition is discarded never reaches a DisposableEffect,
     * and the element would stay in the page decoding a clip nobody can see.
     */
    override fun onForgotten() = release()

    override fun onAbandoned() = release()

    private fun release() {
        element.pause()
        element.removeAttribute("src")
        element.load()
        element.remove()
    }
}

@Composable
actual fun rememberVideoPlayback(
    url: String,
    autoPlay: Boolean,
    loop: Boolean,
    muted: Boolean,
): VideoPlayback {
    val playback = remember(url) {
        val element = document.createElement("video") as HTMLVideoElement
        element.src = url
        element.muted = muted
        element.loop = loop
        element.autoplay = autoPlay
        element.setAttribute("playsinline", "")
        // The clip's host allows cross-origin reads, and asking for them keeps the element out of
        // the "tainted" class — which is what lets a screenshot of this page contain the video.
        element.crossOrigin = "anonymous"
        element.setAttribute("preload", "metadata")
        with(element.style) {
            setProperty("position", "fixed")
            setProperty("object-fit", "cover")
            setProperty("border-radius", "12px")
            // The element is scenery, not a control: the transport below it is the control, and a
            // second set of buttons from the browser would be a second answer to the same question.
            setProperty("pointer-events", "none")
            setProperty("background", "transparent")
        }
        document.body?.appendChild(element)
        HtmlPlayback(element, loop).also { it.attach() }
    }

    return playback
}

@Composable
actual fun VideoSurface(playback: VideoPlayback, modifier: Modifier) {
    val html = playback as HtmlPlayback
    val density = LocalDensity.current.density
    val window = LocalWindowInfo.current.containerSize
    val padding = LocalScreenPadding.current
    val topInset = with(LocalDensity.current) { padding.calculateTopPadding().toPx() }
    val bottomInset = with(LocalDensity.current) { padding.calculateBottomPadding().toPx() }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInWindow()
            val above = ((topInset - bounds.top).coerceAtLeast(0f) / density).toDouble()
            val below = ((bounds.bottom - (window.height - bottomInset)).coerceAtLeast(0f) / density)
                .toDouble()
            html.placeAt(
                left = (bounds.left / density).toDouble(),
                top = (bounds.top / density).toDouble(),
                width = (bounds.width / density).toDouble(),
                height = (bounds.height / density).toDouble(),
                clip = "inset(${above}px 0px ${below}px 0px)",
            )
        },
    )
}
