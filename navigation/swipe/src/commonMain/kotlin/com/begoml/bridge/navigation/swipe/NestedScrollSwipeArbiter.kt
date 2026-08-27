package com.begoml.bridge.navigation.swipe

import androidx.compose.ui.geometry.Offset
import kotlin.math.absoluteValue

/**
 * Decides whether a nested-scroll gesture is a rightward swipe-to-dismiss or belongs to the child
 * scrollable.
 *
 * The decision has to be made from what a descendant leaves behind, and that leftover carries no
 * axis information: a list consumes the whole vertical delta, so the vertical leftover of a plainly
 * vertical scroll is zero. Classifying on the leftover alone therefore never sees the vertical
 * component and accumulates the horizontal crumbs every real finger movement produces until they
 * cross the slop — a straight scroll would then dismiss the screen. The full delta the descendant
 * saw, `consumed + available`, is what carries the direction, so that is what is accumulated.
 *
 * The verdict is taken once, when the accumulated distance crosses the touch slop, and holds for
 * the rest of the gesture: a thumb arc that curves rightward at the end of a vertical fling must
 * not re-arm the dismiss mid-flight.
 */
internal class NestedScrollSwipeArbiter(private val touchSlop: Float) {

    private var accumulated = Offset.Zero
    private var verdict = Verdict.Undecided

    private enum class Verdict { Undecided, Horizontal, Rejected }

    fun reset() {
        accumulated = Offset.Zero
        verdict = Verdict.Undecided
    }

    /**
     * Whether this sample hands the gesture to the dismiss.
     *
     * Two independent questions. The axis comes from the full delta, because a descendant that
     * consumes everything leaves no evidence of which way the finger went. Ownership comes from the
     * leftover: while the descendant still has room it consumes everything, and only at its edge
     * does the remainder show up in [available] — that remainder is what belongs to the dismiss.
     */
    fun shouldArm(consumed: Offset, available: Offset): Boolean =
        classify(consumed = consumed, available = available) == Verdict.Horizontal &&
            available.x > 0f

    private fun classify(consumed: Offset, available: Offset): Verdict {
        if (verdict != Verdict.Undecided) return verdict
        accumulated += consumed + available
        if (accumulated.getDistance() < touchSlop) return Verdict.Undecided
        verdict = if (accumulated.x > accumulated.y.absoluteValue) Verdict.Horizontal else Verdict.Rejected
        return verdict
    }
}
