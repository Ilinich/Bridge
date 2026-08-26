package com.begoml.bridge.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.begoml.bridge.uikit.shader.ShaderSpec
import com.begoml.bridge.uikit.shader.rememberAnimatedShaderBrush

private val PanelShape = RoundedCornerShape(16.dp)

/**
 * A rounded panel lit by a runtime shader.
 *
 * The counterpart to [GlassPanel]: that one frosts what is behind it, this one draws its own
 * surface. A panel cannot do both, because glass only shows a backdrop it does not paint over.
 */
@Composable
fun ShaderPanel(
    spec: ShaderSpec,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val brush = rememberAnimatedShaderBrush(spec)

    Box(
        modifier = modifier
            .clip(PanelShape)
            .background(brush)
            .border(1.dp, Color.White.copy(alpha = 0.14f), PanelShape)
            .padding(14.dp),
    ) {
        content()
    }
}
