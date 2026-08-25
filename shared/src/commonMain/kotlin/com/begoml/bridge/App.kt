package com.begoml.bridge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val Ground = Color(0xFF04101F)
private val Accent = Color(0xFF3E86E8)

@Composable
fun App() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Ground),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Bridge",
                style = MaterialTheme.typography.headlineMedium,
                color = Accent,
            )
        }
    }
}
