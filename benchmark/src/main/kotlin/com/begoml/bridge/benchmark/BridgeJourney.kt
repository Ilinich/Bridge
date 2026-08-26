package com.begoml.bridge.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

const val BridgePackage = "com.begoml.bridge"

private const val TabWaitMillis = 5_000L
private const val ContentWaitMillis = 10_000L
private const val ScrollSteps = 12

/**
 * Tabs are found by their accessible name because the bar draws icons only.
 *
 * The label is gone from the screen but not from the semantics tree, which is the one handle that
 * survives a change of icon set.
 */
enum class BridgeTab(val label: String) {
    Matchday("Matchday"),
    Season("Season"),
    Squad("Squad"),
    Club("Club"),
}

fun MacrobenchmarkScope.openTab(tab: BridgeTab) {
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

fun MacrobenchmarkScope.scrollDownAndBack() {
    val list = device.findObject(By.scrollable(true)) ?: return
    list.setGestureMargin(device.displayWidth / 5)
    list.scroll(Direction.DOWN, 1f, ScrollSteps)
    device.waitForIdle()
    list.scroll(Direction.UP, 1f, ScrollSteps)
    device.waitForIdle()
}
