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

A screen that owns a horizontal gesture of its own — a pager — may still opt in, but it has to say
when the dismiss is allowed: `Modifier.consumeHorizontalSwipeToDismissWhenNotAtStart(state)` lets
the screen go only when the gesture began with the pager at its first page, deciding once per
gesture. Without it the two fight over the same drag.

**The pop must not be animated by the display.** A swipe-back entry carries
`EnterTransition.None` / `ExitTransition.None` for its pop transition, because the gesture already
drives the outgoing screen. Leave the display's own pop in place and the previous screen is drawn
twice, a fraction of a width apart, for the length of the commit.

## What is deliberately not here

**Keyboard handling.** A gesture that starts while the IME is up should dismiss the keyboard
rather than the screen, and a nested-scroll gesture should do the same. This app has no text
input, so the branches would be code no test and no hand could reach.

**Trace sections.** The gesture is a natural thing to instrument, but a systrace marker is only
worth its noise next to a profiling workflow that reads it. There is none here.
