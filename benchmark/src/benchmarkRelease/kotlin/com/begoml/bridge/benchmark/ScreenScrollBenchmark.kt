package com.begoml.bridge.benchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ScrollIterations = 8

/** Frame timings for the screens a user scrolls, each opened warm so startup is not measured. */
@RunWith(AndroidJUnit4::class)
class ScreenScrollBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun squad() = scrollTab(BridgeTab.Squad)

    @Test
    fun season() = scrollTab(BridgeTab.Season)

    @Test
    fun club() = scrollTab(BridgeTab.Club)

    private fun scrollTab(tab: BridgeTab) = rule.measureRepeated(
        packageName = BridgePackage,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require,
        ),
        iterations = ScrollIterations,
        startupMode = StartupMode.WARM,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            openTab(tab)
            awaitScrollableContent()
        },
    ) {
        scrollDownAndBack()
    }
}
