package com.begoml.bridge.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.LabelStyle

/**
 * A club's three-letter code on a disc.
 *
 * It stands in for a crest everywhere the API does not hand one out per fixture. That is not only
 * a data limitation: no club artwork is stored in this repository, so a monogram is the honest
 * default and a real badge is the exception.
 */
@Composable
fun TeamMonogram(
    code: String,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    highlighted: Boolean = false,
) {
    val background = if (highlighted) BridgeColors.Club else BridgeColors.Surface
    val border = if (highlighted) Color.White.copy(alpha = 0.35f) else BridgeColors.Line

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .border(1.dp, border, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = code,
            style = LabelStyle.copy(fontSize = (size.value * 0.28f).sp, letterSpacing = 0.4.sp),
            color = BridgeColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
    }
}
