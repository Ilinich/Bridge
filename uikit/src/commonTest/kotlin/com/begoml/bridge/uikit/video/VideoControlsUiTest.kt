package com.begoml.bridge.uikit.video

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakePlayback(
    durationMillis: Long = 0L,
    positionMillis: Long = 0L,
) : VideoPlayback {

    override var isPlaying by mutableStateOf(false)
    override var isBuffering by mutableStateOf(false)
    override var positionMillis by mutableLongStateOf(positionMillis)
    override var durationMillis by mutableLongStateOf(durationMillis)
    override var isMuted by mutableStateOf(true)

    var seekedTo: Long? = null

    override fun play() {
        isPlaying = true
    }

    override fun pause() {
        isPlaying = false
    }

    override fun seekTo(millis: Long) {
        seekedTo = millis
        positionMillis = millis
    }

    override fun mute(muted: Boolean) {
        isMuted = muted
    }
}

@OptIn(ExperimentalTestApi::class)
class VideoControlsUiTest {

    @Test
    fun showsElapsedAndTotalAsClockText() = runComposeUiTest {
        val playback = FakePlayback(durationMillis = 125_000L, positionMillis = 64_000L)
        setContent { VideoControls(playback = playback) }

        onNodeWithTag(VideoControlsTags.Position).assertIsDisplayed()
        onNodeWithText("1:04").assertIsDisplayed()
        onNodeWithText("2:05").assertIsDisplayed()
    }

    @Test
    fun padsSecondsBelowTen() = runComposeUiTest {
        setContent {
            VideoControls(playback = FakePlayback(durationMillis = 9_000L, positionMillis = 5_000L))
        }

        onNodeWithText("0:05").assertIsDisplayed()
        onNodeWithText("0:09").assertIsDisplayed()
    }

    @Test
    fun transportTogglesBetweenPlayAndPause() = runComposeUiTest {
        val playback = FakePlayback(durationMillis = 10_000L)
        setContent { VideoControls(playback = playback) }

        onNodeWithTag(VideoControlsTags.Transport).performClick()
        assertTrue(playback.isPlaying)

        onNodeWithTag(VideoControlsTags.Transport).performClick()
        assertFalse(playback.isPlaying)
    }

    @Test
    fun muteButtonFlipsTheFlag() = runComposeUiTest {
        val playback = FakePlayback(durationMillis = 10_000L)
        setContent { VideoControls(playback = playback) }

        onNodeWithTag(VideoControlsTags.Mute).performClick()
        assertFalse(playback.isMuted)
    }

    /**
     * A clip whose header has not been parsed reports zero, which is not the same as an empty clip:
     * the scrubber must refuse the drag rather than divide by it.
     */
    @Test
    fun scrubberIsDisabledUntilDurationIsKnown() = runComposeUiTest {
        val playback = FakePlayback(durationMillis = 0L)
        setContent { VideoControls(playback = playback) }

        onNodeWithTag(VideoControlsTags.Scrubber).assertIsNotEnabled()
        onNodeWithTag(VideoControlsTags.Position).assertTextEquals("0:00")
        onNodeWithTag(VideoControlsTags.Duration).assertTextEquals("0:00")
        assertEquals(null, playback.seekedTo)
    }
}
