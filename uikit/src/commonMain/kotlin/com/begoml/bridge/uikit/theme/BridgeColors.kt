package com.begoml.bridge.uikit.theme

import androidx.compose.ui.graphics.Color

/**
 * One accent, and neutrals biased towards it.
 *
 * The club blue is the only saturated colour in the app; everything else is a navy neutral, so the
 * accent never has to compete for attention. Loss doubles as the error colour; there is no win
 * or draw colour, because no screen states an outcome in colour yet.
 */
object BridgeColors {
    val Club = Color(0xFF034694)
    val ClubBright = Color(0xFF3E86E8)

    val Ground = Color(0xFF04101F)
    val Surface = Color(0xFF0C1E33)
    val Line = Color(0xFF16304C)

    val TextPrimary = Color(0xFFEAF1FA)
    val TextMuted = Color(0xFF8AA1BC)

    val Loss = Color(0xFFE0574F)
}
