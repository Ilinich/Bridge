package com.begoml.bridge.feature.matches.season

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bridge.feature.matches.impl.generated.resources.Res
import bridge.feature.matches.impl.generated.resources.fixture_score
import bridge.feature.matches.impl.generated.resources.fixture_teams
import bridge.feature.matches.impl.generated.resources.season_round
import com.begoml.bridge.core.data.model.SeasonMatch
import com.begoml.bridge.core.data.model.SeasonRound
import com.begoml.bridge.feature.matches.formatDay
import com.begoml.bridge.feature.matches.formatTime
import com.begoml.bridge.foundation.tessera.collectState
import com.begoml.bridge.uikit.LocalScreenPadding
import com.begoml.bridge.uikit.component.LoadableContent
import com.begoml.bridge.uikit.component.TeamMonogram
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.FigureStyle
import com.begoml.bridge.uikit.theme.LabelStyle
import org.jetbrains.compose.resources.stringResource

@Composable
fun SeasonScreen(
    feature: SeasonFeature,
    onMatchClick: (SeasonMatch) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by feature.collectState()
    val contentPadding = LocalScreenPadding.current

    LoadableContent(
        isLoading = state.isLoading && state.rounds.isEmpty(),
        error = state.error.takeIf { state.rounds.isEmpty() },
        onRetry = { feature.dispatchAction(SeasonAction.Retry) },
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
            RoundPills(
                rounds = state.rounds,
                selectedIndex = pagerState.currentPage,
                state = pillsState,
            )
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                RoundPage(
                    round = state.rounds[page],
                    isOurs = { index -> feature.isOurs(state.rounds[page], index) },
                    onMatchClick = onMatchClick,
                    bottomPadding = contentPadding.calculateBottomPadding(),
                )
            }
        }
    }
}

@Composable
private fun RoundPills(
    rounds: List<SeasonRound>,
    selectedIndex: Int,
    state: androidx.compose.foundation.lazy.LazyListState,
) {
    LazyRow(
        state = state,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        items(items = rounds, key = { it.number }, contentType = { "round-pill" }) { round ->
            val selected = rounds.indexOf(round) == selectedIndex
            Text(
                text = stringResource(Res.string.season_round, round.number),
                style = LabelStyle,
                color = if (selected) BridgeColors.TextPrimary else BridgeColors.TextMuted,
                modifier = Modifier
                    .background(
                        color = if (selected) BridgeColors.Club else BridgeColors.Surface,
                        shape = CircleShape,
                    )
                    .padding(horizontal = 11.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun RoundPage(
    round: SeasonRound,
    isOurs: (Int) -> Boolean,
    onMatchClick: (SeasonMatch) -> Unit,
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
            val index = round.matches.indexOf(match)
            FixtureRow(
                match = match,
                highlighted = isOurs(index),
                onClick = { onMatchClick(match) },
            )
        }
    }
}

@Composable
private fun FixtureRow(match: SeasonMatch, highlighted: Boolean, onClick: () -> Unit) {
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
        TeamMonogram(code = match.home.code, size = 22.dp, highlighted = highlighted)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.fixture_teams, match.home.name, match.away.name),
                style = MaterialTheme.typography.labelLarge,
                color = BridgeColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = match.kickoff.formatDay(),
                style = LabelStyle,
                color = BridgeColors.TextMuted,
            )
        }
        val score = match.score
        Text(
            text = score
                ?.let { stringResource(Res.string.fixture_score, it.home, it.away) }
                ?: match.kickoff.formatTime(),
            style = FigureStyle,
            color = if (score != null) BridgeColors.TextPrimary else BridgeColors.TextMuted,
        )
    }
}
