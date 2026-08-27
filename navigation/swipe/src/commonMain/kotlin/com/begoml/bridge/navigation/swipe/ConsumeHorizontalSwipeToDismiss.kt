package com.begoml.bridge.navigation.swipe

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity

/**
 * Swallows the horizontal leftover a child scrollable hands upwards, so it never reaches
 * [SwipeToDismissLayout]. For a screen where horizontal scrolling is the point and a dismiss by
 * overscroll would be an accident.
 */
@Composable
fun Modifier.consumeHorizontalSwipeToDismiss(): Modifier {
    val connection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset = Offset(available.x, 0f)

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
                Velocity(available.x, 0f)
        }
    }
    return this.nestedScroll(connection)
}

/**
 * The same, but renders the leftover as an edge overscroll instead of dropping it: reaching the
 * end should look like bumping into a wall rather than like a dead stop.
 *
 * Plain consumption cannot produce that animation. Compose dispatches the leftover to parents
 * *inside* `OverscrollEffect.applyToScroll`, and the child's own edge effect receives only
 * `delta - consumedByEveryone`, so whatever is taken here is exactly what the child's overscroll
 * loses. The leftover is therefore taken and fed to an effect owned by this modifier. Because that
 * effect lives outside the child, the child no longer relaxes it when the gesture reverses — a
 * reversed drag is consumed by the child and never reaches `onPostScroll` — so [onPreScroll]
 * offers the incoming delta to the effect first while it is showing.
 *
 * The decorated node must have a non-empty size, or the effect skips rendering.
 */
@Composable
fun Modifier.consumeHorizontalSwipeToDismissWithOverscroll(): Modifier {
    val effect = rememberOverscrollEffect()
    val connection = remember(effect) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (effect == null || !effect.isInProgress) return Offset.Zero
                if (source != NestedScrollSource.UserInput || available.x == 0f) return Offset.Zero
                var passThrough = Offset.Zero
                effect.applyToScroll(Offset(available.x, 0f), source) { remaining ->
                    passThrough = remaining
                    remaining
                }
                return Offset(available.x - passThrough.x, 0f)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput || available.x == 0f) return Offset.Zero
                val leftover = Offset(available.x, 0f)
                effect?.applyToScroll(leftover, source) { Offset.Zero }
                return leftover
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val leftover = Velocity(available.x, 0f)
                effect?.applyToFling(leftover) { Velocity.Zero }
                return leftover
            }
        }
    }
    return this
        .nestedScroll(connection)
        .overscroll(effect)
}

/**
 * Blocks the dismiss only when the gesture started away from the beginning of the list.
 *
 * This is the platform behaviour a horizontal pager imitates: begin the swipe with the list
 * already at its start and the screen goes; begin it in the middle and the same swipe only scrolls
 * the list, leaving the screen where it is.
 *
 * The verdict is taken on the first user event of a gesture and held: scrolling all the way back
 * to the start mid-gesture must not unlock the dismiss under the finger.
 */
@Composable
fun Modifier.consumeHorizontalSwipeToDismissWhenNotAtStart(state: ScrollableState): Modifier {
    val connection = remember(state) {
        object : NestedScrollConnection {

            private var blockForThisGesture: Boolean? = null

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val isFirstSample = source == NestedScrollSource.UserInput &&
                    available.x != 0f &&
                    blockForThisGesture == null
                if (isFirstSample) blockForThisGesture = state.canScrollBackward
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset = when {
                source != NestedScrollSource.UserInput -> Offset.Zero
                blockForThisGesture == true -> Offset(available.x, 0f)
                else -> Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val block = blockForThisGesture == true
                blockForThisGesture = null
                return if (block) Velocity(available.x, 0f) else Velocity.Zero
            }
        }
    }
    return this.nestedScroll(connection)
}
