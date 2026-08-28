package com.begoml.bridge.benchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val StartupIterations = 10

/**
 * Cold start, measured twice.
 *
 * The pair is the point: [none] is the floor with nothing pre-compiled and [baselineProfile] is
 * what ships. A profile that does not move the gap between them is not earning its place.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun none() = startup(CompilationMode.None())

    @Test
    fun baselineProfile() =
        startup(CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require))

    private fun startup(mode: CompilationMode) = rule.measureRepeated(
        packageName = BridgePackage,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = mode,
        iterations = StartupIterations,
        startupMode = StartupMode.COLD,
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait()
    }
}
