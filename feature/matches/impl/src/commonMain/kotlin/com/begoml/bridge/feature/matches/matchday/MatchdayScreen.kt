package com.begoml.bridge.feature.matches.matchday

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.begoml.bridge.foundation.tessera.collectUiState
import com.begoml.bridge.uikit.LocalScreenPadding
import com.begoml.bridge.uikit.component.BackdropImage
import com.begoml.bridge.uikit.component.BadgeImage
import com.begoml.bridge.uikit.component.GlassPanel
import com.begoml.bridge.uikit.component.FollowStar
import com.begoml.bridge.uikit.component.LoadableContent
import com.begoml.bridge.uikit.glass.GlassBackdrop
import com.begoml.bridge.uikit.glass.GlassScope
import com.begoml.bridge.uikit.component.OfflineNotice
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.FigureStyle
import com.begoml.bridge.uikit.theme.LabelStyle
import kotlinx.collections.immutable.ImmutableList

/** Vertical breathing room for the whole column; the window inset alone sits it flush. */
private val ScreenVerticalPadding = 16.dp

@Composable
internal fun MatchdayScreen(viewModel: MatchdayViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.collectUiState()
    val nowMillis by viewModel.ticker.collectAsStateWithLifecycle(viewModel.nowMillis())
    val contentPadding = LocalScreenPadding.current

    GlassBackdrop(
        modifier = modifier.fillMaxSize(),
        backdrop = { BackdropImage(url = state.backdropUrl) },
    ) {
        val glass = this
        LoadableContent(
            isLoading = state.isLoading &&
                state.nextMatch == null && !state.hasClub,
            error = state.error.takeIf { !state.hasClub && state.nextMatch == null },
            onRetry = viewModel::retry,
        ) {
            val labels = viewModel.labels
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

@Composable
private fun GlassScope.HeroCard(state: MatchdayUiState, labels: MatchdayLabels, nowMillis: Long) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = state.nextMatch?.competition ?: labels.nextMatch,
                    style = LabelStyle,
                    color = BridgeColors.TextMuted,
                )
                state.nextMatch?.venue?.let {
                    Text(text = it, style = LabelStyle, color = BridgeColors.TextMuted)
                }
            }

            val match = state.nextMatch
            if (match == null) {
                Text(
                    text = when {
                        state.nextMatchFailed -> labels.fixtureFailed
                        state.nextMatchLoaded -> labels.noFixture
                        else -> labels.loadingFixture
                    },
                    color = BridgeColors.TextMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            } else {
                Versus(match = match, versus = labels.versus)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = match.kickoffText,
                        style = MaterialTheme.typography.titleSmall,
                        color = BridgeColors.TextPrimary,
                    )
                    Text(
                        text = labels.kickoffLocal,
                        style = LabelStyle,
                        color = BridgeColors.TextMuted,
                    )
                }
                CountdownRow(
                    labels = labels,
                    countdown = Countdown.between(
                        nowMillis = nowMillis,
                        kickoffMillis = match.kickoffMillis,
                    ),
                )
            }
        }
    }
}

@Composable
private fun Versus(match: NextMatchUi, versus: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TeamColumn(name = match.homeName, code = match.homeCode, badge = match.homeBadgeUrl)
        Text(
            text = versus,
            style = FigureStyle,
            color = BridgeColors.TextMuted,
        )
        TeamColumn(name = match.awayName, code = match.awayCode, badge = match.awayBadgeUrl)
    }
}

@Composable
private fun TeamColumn(name: String, code: String, badge: String?) {
    Column(
        modifier = Modifier.width(84.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BadgeImage(url = badge, code = code, size = 36.dp, highlighted = badge == null)
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = BridgeColors.TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CountdownRow(labels: MatchdayLabels, countdown: Countdown) {
    if (countdown.hasStarted) {
        Text(
            text = labels.kickoffNow,
            style = FigureStyle,
            color = BridgeColors.ClubBright,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        CountdownCell(countdown.days, labels.days)
        CountdownCell(countdown.hours, labels.hours)
        CountdownCell(countdown.minutes, labels.minutes)
        CountdownCell(countdown.seconds, labels.seconds)
    }
}

@Composable
private fun CountdownCell(value: Long, unit: String) {
    Column(
        modifier = Modifier
            .widthIn(min = 42.dp)
            .background(BridgeColors.Club.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = value.pad2(), style = FigureStyle, color = BridgeColors.TextPrimary)
        Text(text = unit, style = LabelStyle, color = BridgeColors.TextMuted, maxLines = 1)
    }
}

/** Followed players, written on the squad screen and only read here. */
@Composable
private fun FollowingSection(
    players: ImmutableList<FollowedPlayerUi>,
    labels: MatchdayLabels,
    onPlayerClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(text = labels.following, style = LabelStyle, color = BridgeColors.TextMuted)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            players.forEach { player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPlayerClick(player.id) }
                        .background(BridgeColors.Surface)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    FollowStar(followed = true)
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = BridgeColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentSection(recent: RecentMatchUi, labels: MatchdayLabels) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = labels.recent,
            style = LabelStyle,
            color = BridgeColors.TextMuted,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BridgeColors.Surface, RoundedCornerShape(12.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BadgeImage(url = recent.awayBadgeUrl, code = recent.awayCode, size = 22.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recent.teams,
                    style = MaterialTheme.typography.labelLarge,
                    color = BridgeColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(text = recent.competition, style = LabelStyle, color = BridgeColors.TextMuted)
            }
            recent.score?.let {
                Text(
                    text = it,
                    style = FigureStyle,
                    color = BridgeColors.TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun StadiumSection(stadium: StadiumUi, labels: MatchdayLabels, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = labels.stadium,
            style = LabelStyle,
            color = BridgeColors.TextMuted,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BridgeColors.Surface, RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Fact(
                label = labels.arena,
                value = stadium.arena,
                modifier = Modifier.weight(1.7f),
            )
            Fact(
                label = labels.capacity,
                value = stadium.capacity,
                modifier = Modifier.weight(1f),
            )
            Fact(
                label = labels.founded,
                value = stadium.founded,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Fact(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, style = LabelStyle, color = BridgeColors.TextMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = BridgeColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
