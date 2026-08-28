package com.begoml.bridge.uikit.shader

import android.os.Build

internal actual fun isRuntimeShaderSupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
