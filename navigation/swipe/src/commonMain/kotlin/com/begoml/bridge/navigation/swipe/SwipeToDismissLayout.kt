package com.begoml.bridge.navigation.swipe

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Interactive swipe-to-dismiss (back). Drags the top screen away with the finger: the [foregroundContent]
 * follows the finger (translationX + progressive corner rounding) while the [backgroundContent] — the
 * live previous screen — is revealed with a 1/3 parallax. Commit by distance ([SwipeSensitivity]
 * distanceFraction) or velocity finishes the pop via [onDismiss], else it springs back.
 *
 * With [freezeBackgroundWhileIdle] the background stays composed (state preserved) but skips both
 * measure and draw while fully covered, and a GraphicsLayer snapshot bridges pop transitions. During
 * an active swipe the foreground replays a snapshot so position-aware effects (Haze) don't recompute.
 */
@Composable
@Suppress("LongMethod", "CyclomaticComplexMethod", "LongParameterList")
internal fun SwipeToDismissLayout(
    onDismiss: () -> Unit,
    backgroundContent: @Composable () -> Unit,
    foregroundContent: @Composable () -> Unit,
    freezeBackgroundWhileIdle: Boolean,
    edgeWidthDp: Dp? = null,
    swipeFromAnywhere: Boolean = false,
    sensitivity: SwipeSensitivity = SwipeSensitivity.Default,
    modifier: Modifier = Modifier,
) {
    val swipeSignal = LocalSwipeDismissSignal.current
    val density = LocalDensity.current
    val screenWidth = LocalWindowInfo.current.containerSize.width.toFloat().coerceAtLeast(1f)
    val isPredictiveBackInProgress by rememberPredictiveBackInProgress()
    val predictiveBackActive by rememberUpdatedState(isPredictiveBackInProgress)
    val keyboardController = LocalSoftwareKeyboardController.current

    val maxCornerRadiusDp = remember(screenWidth, density) { with(density) { (screenWidth * 0.05f).toDp().value } }
    val shape25 = remember(maxCornerRadiusDp) { RoundedCornerShape((maxCornerRadiusDp * 0.25f).dp) }
    val shape50 = remember(maxCornerRadiusDp) { RoundedCornerShape((maxCornerRadiusDp * 0.5f).dp) }
    val shape75 = remember(maxCornerRadiusDp) { RoundedCornerShape((maxCornerRadiusDp * 0.75f).dp) }
    val shapeMax = remember(maxCornerRadiusDp) { RoundedCornerShape(maxCornerRadiusDp.dp) }
    val edgePx = remember(edgeWidthDp, density) { edgeWidthDp?.let { with(density) { it.toPx() } } }

    // Lower-bounded: a settle must never carry the foreground left of its home, where the
    // background is still parallaxed and the right edge of the window would show a gap.
    val offsetX = remember { Animatable(0f).apply { updateBounds(lowerBound = 0f) } }
    val scope = rememberCoroutineScope()

    // Kept current rather than captured: launchSettleAnimation lives in a remember that is not
    // re-created on resize, so a screen rotated after the first composition would settle against
    // the previous width and stop short of the edge.
    val currentWidth by rememberUpdatedState(screenWidth)
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val dismissThreshold by rememberUpdatedState(screenWidth * sensitivity.distanceFraction)
    val velocityDismissThreshold by rememberUpdatedState(
        with(density) { sensitivity.velocityDp.dp.toPx() },
    )
    val velocityTracker = remember { VelocityTracker() }

    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isNestedScrollDragging by remember { mutableStateOf(false) }
    var isDismissed by remember { mutableStateOf(false) }
    var isTouchDown by remember { mutableStateOf(false) }
    var foregroundSnapshotInvalid by remember { mutableStateOf(false) }
    var animationJob by remember { mutableStateOf<Job?>(null) }

    fun launchSettleAnimation(targetOffset: Float, velocity: Float) {
        animationJob?.cancel()
        animationJob = scope.launch {
            offsetX.snapTo(targetOffset)
            if (targetOffset > dismissThreshold || velocity > velocityDismissThreshold) {
                offsetX.animateTo(currentWidth, tween(SettleTweenMillis))
                isDismissed = true
                currentOnDismiss()
            } else {
                offsetX.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
                    initialVelocity = velocity,
                )
            }
        }
    }

    val isSwiping by remember {
        derivedStateOf {
            val offset = if (isDragging || isNestedScrollDragging) dragOffset else offsetX.value
            offset > 0f
        }
    }

    val touchSlop = LocalViewConfiguration.current.touchSlop
    val arbiter = remember(touchSlop) { NestedScrollSwipeArbiter(touchSlop) }

    val nestedScrollConnection = remember(arbiter) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!isNestedScrollDragging || source != NestedScrollSource.UserInput) return Offset.Zero
                val newOffset = (dragOffset + available.x).coerceAtLeast(0f)
                val consumed = newOffset - dragOffset
                dragOffset = newOffset
                if (dragOffset == 0f) isNestedScrollDragging = false
                return Offset(consumed, 0f)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val isUserGesture = source == NestedScrollSource.UserInput && !isDragging
                if (!isUserGesture) return Offset.Zero

                if (!isNestedScrollDragging) {
                    val arms = isTouchDown &&
                        arbiter.shouldArm(consumed = consumed, available = available)
                    if (!arms) return Offset.Zero
                    isNestedScrollDragging = true
                    foregroundSnapshotInvalid = true
                    dragOffset = 0f
                }
                // Every sample reaches the arbiter above, including the leftward ones: the
                // verdict is about the direction of the whole gesture, not of one frame.
                return if (available.x <= 0f) {
                    Offset.Zero
                } else {
                    dragOffset = (dragOffset + available.x).coerceAtLeast(0f)
                    Offset(available.x, 0f)
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                arbiter.reset()
                if (!isNestedScrollDragging) return Velocity.Zero
                isNestedScrollDragging = false
                val velocity = available.x
                val capturedOffset = dragOffset
                dragOffset = 0f
                launchSettleAnimation(targetOffset = capturedOffset, velocity = velocity)
                return Velocity(velocity, 0f)
            }
        }
    }

    val isBeingRemoved = LocalNavAnimatedContentScope.current.transition.targetState == EnterExitState.PostExit

    val lifecycleOwner = LocalLifecycleOwner.current
    var isResumed by remember { mutableStateOf(false) }
    var hasBeenResumed by remember { mutableStateOf(false) }
    val backgroundLayer = rememberGraphicsLayer()
    val foregroundLayer = rememberGraphicsLayer()
    var hasBackgroundSnapshot by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> { isResumed = true; hasBeenResumed = true }
                Lifecycle.Event.ON_PAUSE -> isResumed = false
                else -> Unit
            }
        }
        val currentlyResumed = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        isResumed = currentlyResumed
        if (currentlyResumed) hasBeenResumed = true
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val shouldComposeBackground = isResumed && !isDismissed && !isBeingRemoved
    val isInteracting = isDragging || isNestedScrollDragging || isSwiping || isTouchDown
    val isFullyCoveredByForeground = freezeBackgroundWhileIdle && isResumed &&
        !isDismissed && !isBeingRemoved && !isInteracting && hasBackgroundSnapshot

    SideEffect { swipeSignal.isActive = isSwiping }

    DisposableEffect(swipeSignal) {
        onDispose { swipeSignal.isActive = false }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().graphicsLayer {
                val offset = if (isDragging || isNestedScrollDragging) dragOffset else offsetX.value
                val progress = (offset / screenWidth).coerceIn(0f, 1f)
                translationX = -(screenWidth / BackgroundParallaxDivisor) * (1f - progress)
            },
        ) {
            if (shouldComposeBackground) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // Draw is what gets skipped while the foreground covers this; measure and
                        // placement are not. A subtree that skipped measure would be exposed
                        // unplaced on the first frame of a gesture — one frame of a screen missing
                        // whatever its layout decides, which for a bottom-anchored bar is the bar.
                        .drawWithContent {
                            if (isFullyCoveredByForeground) return@drawWithContent
                            if (predictiveBackActive) return@drawWithContent
                            backgroundLayer.record(size = IntSize(size.width.toInt(), size.height.toInt())) {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(backgroundLayer)
                            if (freezeBackgroundWhileIdle && !hasBackgroundSnapshot) hasBackgroundSnapshot = true
                        },
                ) {
                    backgroundContent()
                }
            } else if (hasBeenResumed) {
                Spacer(
                    modifier = Modifier.fillMaxSize().drawBehind {
                        if (!predictiveBackActive) drawLayer(backgroundLayer)
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        isTouchDown = true
                        arbiter.reset()
                        try {
                            do {
                                val event = awaitPointerEvent()
                            } while (event.changes.any { it.pressed })
                        } finally {
                            isTouchDown = false
                        }
                    }
                }
                .nestedScroll(nestedScrollConnection)
                .pointerInput(edgePx, swipeFromAnywhere) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val effectiveEdge = when {
                            swipeFromAnywhere -> Float.MAX_VALUE
                            edgePx != null -> edgePx
                            else -> Float.MAX_VALUE
                        }
                        if (down.position.x > effectiveEdge) return@awaitEachGesture
                        if (predictiveBackActive) return@awaitEachGesture

                        var slopOver = 0f
                        val drag: PointerInputChange = awaitHorizontalTouchSlopOrCancellation(down.id) { change, over ->
                            if (over > 0f) { change.consume(); slopOver = over }
                        } ?: return@awaitEachGesture

                        keyboardController?.hide()
                        animationJob?.cancel()
                        isDragging = true
                        foregroundSnapshotInvalid = true
                        dragOffset = (offsetX.value + slopOver).coerceAtLeast(0f)
                        velocityTracker.resetTracking()
                        velocityTracker.addPosition(drag.uptimeMillis, drag.position)

                        val completed = horizontalDrag(drag.id) { change ->
                            val dx = change.positionChange().x
                            if (dx > 0f || dragOffset > 0f) {
                                change.consume()
                                dragOffset = (dragOffset + dx).coerceAtLeast(0f)
                                velocityTracker.addPosition(change.uptimeMillis, Offset(dragOffset, 0f))
                            }
                        }
                        isDragging = false
                        if (!completed) {
                            launchSettleAnimation(targetOffset = dragOffset, velocity = 0f)
                            return@awaitEachGesture
                        }
                        val velocity = runCatching { velocityTracker.calculateVelocity().x }.getOrDefault(0f)
                        launchSettleAnimation(targetOffset = dragOffset, velocity = velocity)
                    }
                }
                .graphicsLayer {
                    val offset = if (isDragging || isNestedScrollDragging) dragOffset else offsetX.value
                    val progress = (offset / screenWidth).coerceIn(0f, 1f)
                    translationX = offset
                    if (progress > 0.05f) {
                        shape = when {
                            progress >= ShapeMaxAboveProgress -> shapeMax
                            progress >= Shape75AboveProgress -> shape75
                            progress >= Shape50AboveProgress -> shape50
                            else -> shape25
                        }
                        clip = true
                    } else {
                        clip = false
                    }
                },
        ) {
            Box(
                modifier = Modifier.fillMaxSize().drawWithContent {
                    // Recording takes precedence over replaying: a snapshot invalidated in the
                    // same frame a gesture is accepted must be refreshed, not replayed stale for
                    // the whole swipe. Idle content draws live, so an idle screen pays nothing.
                    when {
                        foregroundSnapshotInvalid -> {
                            foregroundLayer.record(size = IntSize(size.width.toInt(), size.height.toInt())) {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(foregroundLayer)
                            foregroundSnapshotInvalid = false
                        }
                        isSwiping -> drawLayer(foregroundLayer)
                        else -> drawContent()
                    }
                },
            ) {
                foregroundContent()
            }
        }
    }
}

private const val SettleTweenMillis = 200
private const val BackgroundParallaxDivisor = 3f
private const val ShapeMaxAboveProgress = 0.5f
private const val Shape75AboveProgress = 0.35f
private const val Shape50AboveProgress = 0.20f
