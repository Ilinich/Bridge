package com.begoml.bridge.uikit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.begoml.bridge.uikit.theme.BridgeColors
import org.jetbrains.compose.resources.stringResource
import bridge.uikit.generated.resources.Res
import bridge.uikit.generated.resources.uikit_error_body
import bridge.uikit.generated.resources.uikit_error_title
import bridge.uikit.generated.resources.uikit_retry

/**
 * Loading, failure and content — and nothing else.
 *
 * Emptiness is deliberately **not** one of the states. A short list is a legitimate answer from
 * these feeds, so an empty result renders through [content] like any other; only a genuine load
 * failure reaches [Failure].
 */
@Composable
fun LoadableContent(
    isLoading: Boolean,
    error: Throwable?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    when {
        error != null -> Failure(onRetry = onRetry, modifier = modifier)
        isLoading -> Loading(modifier = modifier)
        else -> content()
    }
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = BridgeColors.ClubBright)
    }
}

@Composable
private fun Failure(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.uikit_error_title),
            color = BridgeColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.uikit_error_body),
            color = BridgeColors.TextMuted,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) { Text(stringResource(Res.string.uikit_retry)) }
    }
}
