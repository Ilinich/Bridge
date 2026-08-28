package com.begoml.bridge.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Records the classes and methods the app touches on the paths a user actually takes.
 *
 * The journey deliberately goes past startup: a profile that only covers the first frame leaves
 * every other screen to be interpreted on first visit, which is where the stutter shows.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = BridgePackage) {
        pressHome()
        startActivityAndWait()
        awaitScrollableContent()

        BenchmarkTab.entries.forEach { tab ->
            openTab(tab)
            if (awaitScrollableContent()) scrollDownAndBack()
        }
    }
}
