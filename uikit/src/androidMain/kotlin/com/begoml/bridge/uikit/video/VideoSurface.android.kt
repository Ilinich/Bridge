package com.begoml.bridge.uikit.video

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

private const val PositionPollMillis = 200L

private class ExoPlayback(val player: ExoPlayer) : VideoPlayback, Player.Listener, RememberObserver {

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
        player.playWhenReady = true
    }

    override fun pause() {
        player.playWhenReady = false
    }

    override fun seekTo(millis: Long) {
        player.seekTo(millis)
        positionMillis = millis
    }

    override fun mute(muted: Boolean) {
        player.volume = if (muted) 0f else 1f
        isMuted = muted
    }

    override fun onIsPlayingChanged(playing: Boolean) {
        isPlaying = playing
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        isBuffering = playbackState == Player.STATE_BUFFERING
        readDuration()
        if (playbackState == Player.STATE_ENDED) rewind()
    }

    /**
     * Returns a finished clip to its first frame.
     *
     * ExoPlayer stops on the last frame and leaves the position at the duration, so without this
     * the readout would sit at the end and the next tap on play would resume nothing. AVPlayer
     * needs the same nudge, so both platforms end a clip the same way.
     */
    private fun rewind() {
        player.playWhenReady = false
        player.seekTo(0L)
        positionMillis = 0L
    }

    fun poll() {
        positionMillis = player.currentPosition
        readDuration()
    }

    override fun onRemembered() = Unit

    /**
     * A decoder is a resource the composition owns, so it is released on both exits.
     *
     * onAbandoned is the one that matters: a remembered value whose composition is discarded before
     * it is applied never reaches a DisposableEffect, and the player would have leaked with its
     * decoder and its socket.
     */
    override fun onForgotten() = release()

    override fun onAbandoned() = release()

    private fun release() {
        player.removeListener(this)
        player.release()
    }

    private fun readDuration() {
        val reported = player.duration
        if (reported > 0L) durationMillis = reported
    }
}

/**
 * Pauses on background and resumes only what the user had running.
 *
 * Resuming unconditionally would restart a clip the user deliberately paused before leaving, so
 * the intent is captured on the way out rather than inferred on the way back.
 */
@Composable
private fun LifecycleBoundPlayback(playback: ExoPlayback, autoPlay: Boolean) {
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

@OptIn(UnstableApi::class)
@Composable
actual fun rememberVideoPlayback(
    url: String,
    autoPlay: Boolean,
    loop: Boolean,
    muted: Boolean,
): VideoPlayback {
    val context = LocalContext.current
    val playback = remember(url, loop, muted) {
        val player = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = if (loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
            volume = if (muted) 0f else 1f
            prepare()
        }
        ExoPlayback(player).also { player.addListener(it) }
    }

    LifecycleBoundPlayback(playback = playback, autoPlay = autoPlay)

    LaunchedEffect(playback, playback.isPlaying) {
        while (playback.isPlaying) {
            playback.poll()
            delay(PositionPollMillis)
        }
    }

    return playback
}

@Composable
actual fun VideoSurface(playback: VideoPlayback, modifier: Modifier) {
    val exoPlayback = playback as ExoPlayback

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                player = exoPlayback.player
            }
        },
        onRelease = { view -> view.player = null },
    )
}
