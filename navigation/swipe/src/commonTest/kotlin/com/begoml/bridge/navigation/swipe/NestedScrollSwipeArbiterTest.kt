package com.begoml.bridge.navigation.swipe

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val Slop = 10f

class NestedScrollSwipeArbiterTest {

    @Test
    fun `a vertical scroll never arms the dismiss`() {
        val arbiter = NestedScrollSwipeArbiter(Slop)

        val armed = (1..20).any {
            // What a list leaves behind: it ate the vertical delta whole, and the horizontal
            // crumbs of a real thumb are all that reaches here.
            arbiter.shouldArm(consumed = Offset(0f, -30f), available = Offset(0.6f, 0f))
        }

        assertFalse(armed)
    }

    @Test
    fun `a horizontal drag past a list at its edge arms once the slop is crossed`() {
        val arbiter = NestedScrollSwipeArbiter(Slop)

        assertFalse(arbiter.shouldArm(consumed = Offset.Zero, available = Offset(4f, 0f)))
        assertTrue(arbiter.shouldArm(consumed = Offset.Zero, available = Offset(9f, 0f)))
    }

    @Test
    fun `a verdict taken against the dismiss survives a rightward tail`() {
        val arbiter = NestedScrollSwipeArbiter(Slop)

        arbiter.shouldArm(consumed = Offset(0f, -40f), available = Offset.Zero)
        val armedLater = arbiter.shouldArm(consumed = Offset.Zero, available = Offset(120f, 0f))

        assertFalse(armedLater)
    }

    @Test
    fun `reset lets the next gesture decide again`() {
        val arbiter = NestedScrollSwipeArbiter(Slop)
        arbiter.shouldArm(consumed = Offset(0f, -40f), available = Offset.Zero)

        arbiter.reset()

        assertTrue(arbiter.shouldArm(consumed = Offset.Zero, available = Offset(40f, 0f)))
    }
}
