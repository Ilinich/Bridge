package com.begoml.bridge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.begoml.bridge.uikit.shader.ClubBackgroundShader
import com.begoml.bridge.uikit.shader.isRuntimeShaderAvailable
import com.begoml.bridge.uikit.shader.rememberAnimatedShaderBrush

private val PanelColor = Color(0xCC04101F)
private val TextColor = Color(0xFFEAF1FA)
private val MutedColor = Color(0xFF8AA1BC)

private const val NanosPerMilli = 1_000_000.0
private const val FramesPerSample = 60

@Composable
fun App() {
    MaterialTheme {
        val shaderEnabled = remember { mutableStateOf(true) }
        val brush = rememberAnimatedShaderBrush(ClubBackgroundShader, enabled = shaderEnabled.value)
        val frameMillis = rememberAverageFrameMillis(shaderEnabled.value)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .safeContentPadding()
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(PanelColor, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Shader spike",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextColor,
                )
                SpikeRow(label = "Runtime shader available", value = isRuntimeShaderAvailable().toString())
                SpikeRow(label = "Path", value = if (shaderEnabled.value) "runtime shader" else "fallback gradient")
                SpikeRow(label = "Frame time", value = "${frameMillis.trimTo(2)} ms")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Shader on", color = MutedColor)
                    Switch(
                        checked = shaderEnabled.value,
                        onCheckedChange = { shaderEnabled.value = it },
                    )
                }
            }
        }
    }
}

@Composable
private fun SpikeRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = MutedColor)
        Text(text = value, color = TextColor, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun rememberAverageFrameMillis(restartKey: Any): Float {
    val average = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(restartKey) {
        var previousNanos = 0L
        var accumulated = 0.0
        var counted = 0
        while (true) {
            withFrameNanos { nanos ->
                if (previousNanos != 0L) {
                    accumulated += (nanos - previousNanos) / NanosPerMilli
                    counted++
                    if (counted == FramesPerSample) {
                        average.floatValue = (accumulated / counted).toFloat()
                        accumulated = 0.0
                        counted = 0
                    }
                }
                previousNanos = nanos
            }
        }
    }
    return average.floatValue
}

private fun Float.trimTo(decimals: Int): String {
    val text = toString()
    val dot = text.indexOf('.')
    if (dot < 0) return text
    return text.substring(0, minOf(text.length, dot + decimals + 1))
}
