package com.begoml.bridge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import bridge.shared.generated.resources.Res
import bridge.shared.generated.resources.app_name
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.BridgeTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun App() {
    BridgeTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(BridgeColors.Ground),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = BridgeColors.ClubBright,
            )
        }
    }
}
