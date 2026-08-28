package com.begoml.bridge.navigation.swipe

import android.os.Build
import android.view.RoundedCorner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp

@Composable
actual fun deviceCornerRadius(): Dp? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val density = LocalDensity.current
    val insets = LocalView.current.rootWindowInsets
    return remember(insets, density) {
        insets?.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
            ?.radius
            ?.let { with(density) { it.toDp() } }
    }
}
