package com.begoml.bridge.uikit.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun BridgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = BridgeColors.ClubBright,
            onPrimary = BridgeColors.TextPrimary,
            primaryContainer = BridgeColors.Club,
            onPrimaryContainer = BridgeColors.TextPrimary,
            background = BridgeColors.Ground,
            onBackground = BridgeColors.TextPrimary,
            surface = BridgeColors.Surface,
            onSurface = BridgeColors.TextPrimary,
            outline = BridgeColors.Line,
            error = BridgeColors.Loss,
        ),
        typography = BridgeTypography,
        content = content,
    )
}
