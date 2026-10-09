package com.begoml.bridge.shots

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import com.begoml.bridge.App
import com.begoml.bridge.di.startBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
import java.io.File

/**
 * Renders the desktop window off screen, one tab at a time.
 *
 * A Compose UI test cannot do this: the screens carry a runtime shader whose clock never stops, so
 * `waitForIdle` never returns. Here the frame loop belongs to the caller, which also means real
 * seconds pass between frames and the feeds have time to answer.
 *
 * Every call into the scene hops to the AWT thread, and the waiting happens off it: that thread is
 * what `Dispatchers.Main` dispatches to on the desktop, so blocking it would stop the very state
 * updates these frames exist to show.
 */
@Suppress("MagicNumber")
fun main(args: Array<String>) = runBlocking {
    startBridge()

    val width = args.getOrNull(0)?.toIntOrNull() ?: PhoneWidth
    val suffix = args.getOrNull(1).orEmpty()

    val scene = withContext(Dispatchers.Swing) {
        ImageComposeScene(
            width = (width * Scale).toInt(),
            height = (Height * Scale).toInt(),
            density = Density(Scale),
        ) { App() }
    }
    try {
        settle(scene, frames = 240)
        shoot(scene, "matchday$suffix")

        TabNames.forEachIndexed { index, name ->
            withContext(Dispatchers.Swing) {
                val at = Offset(tabCentre(width, index + 1) * Scale, TabCentreY * Scale)
                scene.sendPointerEvent(PointerEventType.Press, at)
                scene.sendPointerEvent(PointerEventType.Release, at)
            }
            settle(scene, frames = 180)
            shoot(scene, "$name$suffix")
        }
    } finally {
        withContext(Dispatchers.Swing) { scene.close() }
    }
}

/** The capsule is capped and centred, so a tab's centre follows from the window's width alone. */
private fun tabCentre(windowWidth: Int, tab: Int): Float {
    val barLeft = (windowWidth - BarWidth) / 2f
    return barLeft + BarWidth / TabCount * (tab + 0.5f)
}

// Both helpers suspend through `withContext`, which detekt cannot see: this module's detekt task
// runs without a classpath, so the rule judges the modifier unused.
@Suppress("RedundantSuspendModifier")
private suspend fun settle(scene: ImageComposeScene, frames: Int) {
    repeat(frames) { frame ->
        withContext(Dispatchers.Swing) { scene.render(frame * FrameNanos) }
        delay(FrameMillis)
    }
}

@Suppress("RedundantSuspendModifier")
private suspend fun shoot(scene: ImageComposeScene, name: String) {
    val bytes = withContext(Dispatchers.Swing) {
        scene.render().encodeToData(EncodedImageFormat.PNG)?.bytes
    } ?: error("could not encode $name")
    val file = File(OutputDirectory, "$name.png")
    file.parentFile.mkdirs()
    file.writeBytes(bytes)
    println("SHOT ${file.path}")
}

/** The window's own points; the scene is sized in pixels, which is why these are scaled. */
private val OutputDirectory = File("build/shots")
private const val PhoneWidth = 420
private const val Height = 880
private const val Scale = 2f
private const val FrameMillis = 16L
private const val FrameNanos = 16_000_000L

private val TabNames = listOf("season", "squad", "club")
private const val TabCount = 4f
private const val BarWidth = 420f
private const val TabCentreY = 835f
