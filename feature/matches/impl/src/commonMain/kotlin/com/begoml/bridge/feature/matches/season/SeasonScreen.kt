package com.begoml.bridge.feature.matches.season

import kotlinx.collections.immutable.ImmutableList
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.begoml.bridge.foundation.tessera.collectUiState
import com.begoml.bridge.uikit.LocalScreenPadding
import com.begoml.bridge.uikit.component.LoadableContent
import com.begoml.bridge.uikit.component.OfflineNotice
import com.begoml.bridge.uikit.component.TeamMonogram
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.FigureStyle
import com.begoml.bridge.uikit.theme.LabelStyle

@Composable
internal fun SeasonScreen(viewModel: SeasonViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.collectUiState()
    val contentPadding = LocalScreenPadding.current

    LoadableContent(
        isLoading = state.isLoading && state.rounds.isEmpty(),
        error = state.error.takeIf { state.rounds.isEmpty() },
        onRetry = viewModel::retry,
        modifier = modifier.fillMaxSize().background(BridgeColors.Ground),
    ) {
        if (state.rounds.isEmpty()) return@LoadableContent

        val pagerState = rememberPagerState(
            initialPage = state.initialRoundIndex,
            pageCount = { state.rounds.size },
        )
        val pillsState = rememberLazyListState()

        LaunchedEffect(pagerState.settledPage) {
            pillsState.animateScrollToItem(pagerState.settledPage)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer(Modifier.height(contentPadding.calculateTopPadding()))
            if (state.isOffline) OfflineNotice(modifier = Modifier.padding(horizontal = 16.dp))
            val scope = rememberCoroutineScope()
            RoundPills(
                rounds = state.rounds,
                selectedIndex = pagerState.currentPage,
                onSelect = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
                state = pillsState,
            )
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                RoundPage(
                    round = state.rounds[page],
                    onMatchClick = viewModel::onMatchClick,
                    bottomPadding = contentPadding.calculateBottomPadding(),
                )
            }
        }
    }
}

@Composable
private fun RoundPills(
    rounds: ImmutableList<SeasonRoundUi>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    state: LazyListState,
) {
    LazyRow(
        state = state,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        itemsIndexed(
            items = rounds,
            key = { _, round -> round.number },
            contentType = { _, _ -> "round-pill" },
        ) { index, round ->
            val selected = index == selectedIndex
            Text(
                text = round.title,
                style = LabelStyle,
                color = if (selected) BridgeColors.TextPrimary else BridgeColors.TextMuted,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (selected) BridgeColors.Club else BridgeColors.Surface)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 11.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun RoundPage(
    round: SeasonRoundUi,
    onMatchClick: (String) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 14.dp,
            end = 14.dp,
            top = 4.dp,
            bottom = bottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = round.matches,
            key = { match -> match.id },
            contentType = { "fixture" },
        ) { match ->
            FixtureRow(match = match, onClick = { onMatchClick(match.id) })
        }
    }
}

@Composable
private fun FixtureRow(match: FixtureRowUi, onClick: () -> Unit) {
    val highlighted = match.highlighted
    val background = remember(highlighted) {
        if (highlighted) {
            Brush.horizontalGradient(
                listOf(BridgeColors.Club.copy(alpha = 0.62f), BridgeColors.Surface),
            )
        } else {
            Brush.horizontalGradient(listOf(BridgeColors.Surface, BridgeColors.Surface))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TeamMonogram(code = match.homeCode, size = 22.dp, highlighted = highlighted)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = match.teams,
                style = MaterialTheme.typography.labelLarge,
                color = BridgeColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = match.day,
                style = LabelStyle,
                color = BridgeColors.TextMuted,
            )
        }
        Text(
            text = match.trailing,
            style = FigureStyle,
            color = if (match.hasScore) BridgeColors.TextPrimary else BridgeColors.TextMuted,
        )
    }
}
