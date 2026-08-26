package com.begoml.bridge.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

const val BridgePackage = "com.begoml.bridge"

private const val TabWaitMillis = 5_000L
private const val ContentWaitMillis = 10_000L
private const val ScrollSteps = 12
private const val SwipeStartFraction = 0.75
private const val SwipeEndFraction = 0.30

/**
 * Tabs are found by their accessible name because the bar draws icons only.
 *
 * The label is gone from the screen but not from the semantics tree, which is the one handle that
 * survives a change of icon set.
 */
enum class BenchmarkTab(val label: String) {
    Matchday("Matchday"),
    Season("Season"),
    Squad("Squad"),
    Club("Club"),
}

fun MacrobenchmarkScope.openTab(tab: BenchmarkTab) {
    val selector = By.desc(tab.label)
    device.wait(Until.hasObject(selector), TabWaitMillis)
    device.findObject(selector)?.click()
    device.waitForIdle()
}

/**
 * Waits for content rather than for idleness.
 *
 * The squad arrives from disk and then from the network, so an idle device is not the same as a
 * populated screen; measuring the scroll before the rows exist would time an empty list.
 */
fun MacrobenchmarkScope.awaitScrollableContent(): Boolean {
    val scrollable = By.scrollable(true)
    return device.wait(Until.hasObject(scrollable), ContentWaitMillis) != null
}

/**
 * Scrolls by coordinates rather than by driving the list node.
 *
 * A UiObject2 is a handle to one accessibility node, and the list is rebuilt when its data lands:
 * the handle then goes stale between finding it and scrolling it, which fails the run. A swipe is
 * addressed to the screen and cannot go stale.
 */
fun MacrobenchmarkScope.scrollDownAndBack() {
    val x = device.displayWidth / 2
    val bottom = (device.displayHeight * SwipeStartFraction).toInt()
    val top = (device.displayHeight * SwipeEndFraction).toInt()

    device.swipe(x, bottom, x, top, ScrollSteps)
    device.waitForIdle()
    device.swipe(x, top, x, bottom, ScrollSteps)
    device.waitForIdle()
}
