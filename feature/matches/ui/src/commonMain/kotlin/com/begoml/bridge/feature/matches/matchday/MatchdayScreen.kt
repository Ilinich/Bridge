package com.begoml.bridge.feature.matches.matchday

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.begoml.bridge.foundation.tessera.collectUiState
import com.begoml.bridge.uikit.LocalScreenPadding
import com.begoml.bridge.uikit.component.BackdropImage
import com.begoml.bridge.uikit.component.LoadableContent
import com.begoml.bridge.uikit.glass.GlassBackdrop
import com.begoml.bridge.uikit.component.OfflineNotice

/** Vertical breathing room for the whole column; the window inset alone sits it flush. */
private val ScreenVerticalPadding = 16.dp

@Composable
internal fun MatchdayScreen(viewModel: MatchdayViewModel, modifier: Modifier = Modifier) {
    val uiState = viewModel.collectUiState()
    val nowMillis by viewModel.ticker.collectAsStateWithLifecycle(viewModel.nowMillis())
    val contentPadding = LocalScreenPadding.current

    // Every read happens in the scope that draws the thing being read: the backdrop follows one
    // url, the content follows the rest, and neither recomposes for the other's changes.
    GlassBackdrop(
        modifier = modifier.fillMaxSize(),
        backdrop = { BackdropImage(url = uiState.value.backdropUrl) },
    ) {
        val glass = this
        val state = uiState.value
        LoadableContent(
            isLoading = state.isLoading &&
                state.nextMatch == null && !state.hasClub,
            error = state.error.takeIf { !state.hasClub && state.nextMatch == null },
            onRetry = viewModel::retry,
        ) {
            val labels = state.labels
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
                    .padding(horizontal = 14.dp, vertical = ScreenVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.isOffline) OfflineNotice()
                with(glass) { HeroCard(state = state, labels = labels, nowMillis = nowMillis) }
                if (state.following.isNotEmpty()) {
                    FollowingSection(
                        players = state.following,
                        labels = labels,
                        onPlayerClick = viewModel::onFollowedPlayerClick,
                    )
                }
                state.recent?.let { RecentSection(recent = it, labels = labels) }
                state.stadium?.let {
                    StadiumSection(
                        stadium = it,
                        labels = labels,
                        onClick = viewModel::onStadiumClick,
                    )
                }
            }
        }
    }
}
