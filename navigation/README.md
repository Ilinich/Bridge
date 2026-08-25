# navigation

Navigation 3 routes, per-tab back stacks and a swipe-to-dismiss gesture, all in common code.

## Back stacks

Each tab owns its own stack. Switching tabs preserves where the user was; selecting the tab
already shown returns it to its root, which is what a tab bar is expected to do.

```kotlin
val backStack = rememberTabbedBackStack()

BridgeNavDisplay(
    backStack = backStack.current,
    onBack = backStack::pop,
) { key ->
    when (key) {
        is Route.Squad -> NavEntry(key, metadata = swipeBackMetadata()) { SquadScreen(...) }
        else -> error("unmapped route")
    }
}
```

## Motion

Pushes slide in from 12% of the width and fade over 260 ms; the departing screen leaves at −6%.
Tab switches cross-fade instead of sliding: tabs are not ordered in space, and a slide would claim
they were.

A swipe from the left edge drags the top screen with the finger, revealing the previous one scaled
and dimmed underneath. Release past 40% of the width — or fast enough — completes the pop;
anything less springs back. The screen underneath is held below RESUMED while it is only being
previewed, so it does not start work it may never need.

Opt a screen in with `swipeBackMetadata()`. A screen that owns a horizontal gesture of its own —
a pager — must not opt in, or the two will fight over the same drag.
