package com.begoml.bridge.uikit.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import bridge.uikit.generated.resources.Res
import bridge.uikit.generated.resources.uikit_offline
import com.begoml.bridge.uikit.theme.BridgeColors
import org.jetbrains.compose.resources.stringResource

private val NoticeShape = RoundedCornerShape(10.dp)

/**
 * States that the data on screen may be stale, without hiding it.
 *
 * Losing the network is not a load failure here: every feed is cached, so the screen keeps working
 * and only its freshness is in doubt.
 */
@Composable
fun OfflineNotice(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = BridgeColors.Loss.copy(alpha = 0.16f), shape = NoticeShape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.uikit_offline),
            style = MaterialTheme.typography.labelLarge,
            color = BridgeColors.TextPrimary,
        )
    }
}
