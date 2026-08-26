# navigation:swipe

Swipe-to-dismiss for Navigation 3, as a Scene strategy.

It is a separate module so that the boundary is enforced rather than intended: this package knows
about Compose, lifecycle and Navigation 3, and **nothing about this app**. No route type, no
router, no tab. That is checkable — its whole import surface is framework packages — and a module
boundary is what keeps it that way as the app grows.

## What it does

A drag from the left edge moves the top entry with the finger, revealing the previous one scaled
and dimmed. Release past the threshold pops; anything less springs back.

```kotlin
NavDisplay(
    backStack = stack,
    sceneStrategies = listOf(SwipeToDismissSceneStrategy()),
    entryProvider = provider,
)

// opt a destination in
NavEntry(key, metadata = SwipeToDismissSceneStrategy.enabled()) { … }
```

## Two details that are not obvious

**The screen underneath is capped below RESUMED while it is only being previewed.**
`CappedLifecycleOwner` wraps it, so a screen the user may never actually return to does not start
work on the way past.

**The background is frozen while the gesture is idle.** Redrawing a screen nobody is dragging
costs frames for nothing; `FreezeBackgroundWhileIdle` holds the last frame until the drag starts.

A screen that owns a horizontal gesture of its own — a pager — must not opt in, or the two will
fight over the same drag.
