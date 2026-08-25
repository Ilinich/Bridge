package com.begoml.bridge.uikit.video

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.seekToTime
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView

private const val LoopNotification = "AVPlayerItemDidPlayToEndTimeNotification"

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoSurface(url: String, modifier: Modifier) {
    val player = remember(url) { AVPlayer(uRL = NSURL(string = url)) }

    DisposableEffect(player) {
        // AVPlayer has no repeat mode; the end-of-item notification is the loop.
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = LoopNotification,
            `object` = null,
            queue = null,
        ) { _ -> player.seekToTime(CMTimeMake(value = 0, timescale = 1)) }

        onDispose {
            player.pause()
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }

    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    LaunchedEffect(lifecycleState) {
        if (lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) player.play() else player.pause()
    }

    UIKitView(
        modifier = modifier,
        factory = {
            val container = UIView()
            val layer = AVPlayerLayer()
            layer.player = player
            layer.videoGravity = AVLayerVideoGravityResizeAspectFill
            container.layer.addSublayer(layer)
            container
        },
        update = { container ->
            // The layer does not follow its container's bounds on its own, and an implicit
            // animation on every resize would make it lag behind the layout by a frame.
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            (container.layer.sublayers?.firstOrNull() as? AVPlayerLayer)?.frame = container.bounds
            CATransaction.commit()
        },
    )
}
