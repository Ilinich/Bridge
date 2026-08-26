package com.begoml.bridge.uikit.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.begoml.bridge.uikit.theme.BridgeColors
import androidx.compose.ui.viewinterop.UIKitView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.delay
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.seekToTime
import platform.AVFoundation.setVolume
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.QuartzCore.CATransaction
import platform.CoreGraphics.CGRectZero
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIColor
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
private fun Color.toUIColor(): UIColor = UIColor(
    red = red.toDouble(),
    green = green.toDouble(),
    blue = blue.toDouble(),
    alpha = alpha.toDouble(),
)

private const val LoopNotification = "AVPlayerItemDidPlayToEndTimeNotification"
private const val PositionPollMillis = 200L
private const val SeekTimescale = 600
private const val MillisPerSecond = 1000.0

@OptIn(ExperimentalForeignApi::class)
private class AvPlayback(val player: AVPlayer, private val loop: Boolean) :
    VideoPlayback,
    RememberObserver {

    override var isPlaying by mutableStateOf(false)
        private set

    override var isBuffering by mutableStateOf(true)
        private set

    override var positionMillis by mutableLongStateOf(0L)
        private set

    override var durationMillis by mutableLongStateOf(0L)
        private set

    override var isMuted by mutableStateOf(true)
        private set

    override fun play() {
        player.play()
        isPlaying = true
    }

    override fun pause() {
        player.pause()
        isPlaying = false
    }

    override fun seekTo(millis: Long) {
        player.seekToTime(CMTimeMakeWithSeconds(millis / MillisPerSecond, SeekTimescale))
        positionMillis = millis
    }

    override fun mute(muted: Boolean) {
        player.setVolume(if (muted) 0f else 1f)
        isMuted = muted
    }

    fun onReachedEnd() {
        player.seekToTime(CMTimeMake(value = 0, timescale = 1))
        if (loop) player.play() else pause()
    }

    override fun onRemembered() = Unit

    /**
     * The player is a resource the composition owns, so it stops on both exits.
     *
     * onAbandoned is the one that matters: a remembered value whose composition is discarded before
     * it is applied never reaches a DisposableEffect.
     */
    override fun onForgotten() = pause()

    override fun onAbandoned() = pause()

    fun poll() {
        positionMillis = CMTimeGetSeconds(player.currentTime()).toMillis()
        val reported = player.currentItem?.duration?.let { CMTimeGetSeconds(it) }?.toMillis() ?: 0L
        if (reported > 0L) {
            durationMillis = reported
            isBuffering = false
        }
    }

    private fun Double.toMillis(): Long = if (isNaN() || this < 0.0) 0L else (this * MillisPerSecond).toLong()
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberVideoPlayback(
    url: String,
    autoPlay: Boolean,
    loop: Boolean,
    muted: Boolean,
): VideoPlayback {
    val playback = remember(url, loop, muted) {
        AvPlayback(AVPlayer(uRL = NSURL(string = url)), loop).apply { mute(muted) }
    }

    DisposableEffect(playback) {
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = LoopNotification,
            `object` = null,
            queue = null,
        ) { _ -> playback.onReachedEnd() }

        onDispose {
            playback.pause()
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }

    LifecycleBoundPlayback(playback = playback, autoPlay = autoPlay)

    // Gated on isPlaying, as on Android: a paused clip has no position to report, and the timer
    // would otherwise wake five times a second for the life of the screen.
    LaunchedEffect(playback, playback.isPlaying) {
        playback.poll()
        while (playback.isPlaying) {
            delay(PositionPollMillis)
            playback.poll()
        }
    }

    return playback
}

/**
 * Pauses on background and resumes only what the user had running.
 *
 * Resuming unconditionally would restart a clip the user deliberately paused before leaving, so
 * the intent is captured on the way out rather than inferred on the way back.
 */
@Composable
private fun LifecycleBoundPlayback(playback: VideoPlayback, autoPlay: Boolean) {
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    var resumeOnReturn by remember { mutableStateOf(autoPlay) }

    LaunchedEffect(lifecycleState) {
        if (lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) {
            if (resumeOnReturn) playback.play()
        } else {
            resumeOnReturn = playback.isPlaying
            playback.pause()
        }
    }
}

/**
 * Hosts the player layer and keeps it the size of the view.
 *
 * A sublayer does not follow its host's bounds, and Compose gives interop views no resize
 * callback, so the frame has to be set where UIKit actually reports layout. Setting it from the
 * composition instead leaves the layer at zero size and the interop view shows through as a blank
 * rectangle. Implicit animation is disabled so the layer cannot lag a frame behind the layout.
 */
@OptIn(ExperimentalForeignApi::class)
private class PlayerContainerView : UIView(frame = CGRectZero.readValue()) {

    val playerLayer = AVPlayerLayer()

    init {
        // Opaque and dark, not clear: until the first frame decodes there is nothing in the layer,
        // and a transparent interop view shows through as white for as long as that takes.
        backgroundColor = BridgeColors.Ground.toUIColor()
        playerLayer.backgroundColor = BridgeColors.Ground.toUIColor().CGColor
        playerLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        layer.addSublayer(playerLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        CATransaction.begin()
        CATransaction.setValue(true, kCATransactionDisableActions)
        playerLayer.setFrame(bounds)
        CATransaction.commit()
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoSurface(playback: VideoPlayback, modifier: Modifier) {
    val avPlayback = playback as AvPlayback

    UIKitView(
        modifier = modifier,
        factory = { PlayerContainerView().apply { playerLayer.player = avPlayback.player } },
        update = { container -> container.setNeedsLayout() },
    )
}
