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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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

    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val dismissThreshold = screenWidth * sensitivity.distanceFraction
    val velocityDismissThreshold = with(density) { sensitivity.velocityDp.dp.toPx() }
    val velocityTracker = remember { VelocityTracker() }

    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isNestedScrollDragging by remember { mutableStateOf(false) }
    var isDismissed by remember { mutableStateOf(false) }
    var animationJob by remember { mutableStateOf<Job?>(null) }

    fun launchSettleAnimation(targetOffset: Float, velocity: Float) {
        animationJob?.cancel()
        animationJob = scope.launch {
            offsetX.snapTo(targetOffset)
            if (targetOffset > dismissThreshold || velocity > velocityDismissThreshold) {
                offsetX.animateTo(screenWidth, tween(SETTLE_TWEEN_MS))
                isDismissed = true
                onDismiss()
            } else {
                offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }
        }
    }

    val isSwiping by remember {
        derivedStateOf {
            val offset = if (isDragging || isNestedScrollDragging) dragOffset else offsetX.value
            offset > 0f
        }
    }

    val nestedScrollConnection = remember {
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
                if (source != NestedScrollSource.UserInput || available.x <= 0f) return Offset.Zero
                if (isDragging) return Offset.Zero
                if (!isNestedScrollDragging) {
                    isNestedScrollDragging = true
                    dragOffset = 0f
                }
                dragOffset = (dragOffset + available.x).coerceAtLeast(0f)
                return Offset(available.x, 0f)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
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
    var hasForegroundSnapshot by remember { mutableStateOf(false) }
    var foregroundSnapshotInvalid by remember { mutableStateOf(true) }
    var hasBackgroundSnapshot by remember { mutableStateOf(false) }
    var isTouchDown by remember { mutableStateOf(false) }

    LaunchedEffect(isTouchDown) { if (isTouchDown) foregroundSnapshotInvalid = true }

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

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().graphicsLayer {
                val offset = if (isDragging || isNestedScrollDragging) dragOffset else offsetX.value
                val progress = (offset / screenWidth).coerceIn(0f, 1f)
                translationX = -(screenWidth / BACKGROUND_PARALLAX_DIVISOR) * (1f - progress)
            },
        ) {
            if (shouldComposeBackground) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .layout { measurable, constraints ->
                            if (isFullyCoveredByForeground) {
                                layout(constraints.maxWidth, constraints.maxHeight) {}
                            } else {
                                val placeable = measurable.measure(constraints)
                                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                            }
                        }
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
                            progress >= CORNER_PROGRESS_FULL -> shapeMax
                            progress >= CORNER_PROGRESS_75 -> shape75
                            progress >= CORNER_PROGRESS_50 -> shape50
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
                    when {
                        isSwiping && hasForegroundSnapshot -> drawLayer(foregroundLayer)
                        !isSwiping && (foregroundSnapshotInvalid || isTouchDown) -> {
                            foregroundLayer.record(size = IntSize(size.width.toInt(), size.height.toInt())) {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(foregroundLayer)
                            if (!hasForegroundSnapshot) hasForegroundSnapshot = true
                            if (foregroundSnapshotInvalid) foregroundSnapshotInvalid = false
                        }
                        !isSwiping -> drawContent()
                        else -> drawContent()
                    }
                },
            ) {
                CompositionLocalProvider(LocalSwipeToDismissActive provides isSwiping) {
                    foregroundContent()
                }
            }
        }
    }
}

private const val SETTLE_TWEEN_MS = 200
private const val BACKGROUND_PARALLAX_DIVISOR = 3f
private const val CORNER_PROGRESS_FULL = 0.5f
private const val CORNER_PROGRESS_75 = 0.35f
private const val CORNER_PROGRESS_50 = 0.20f
