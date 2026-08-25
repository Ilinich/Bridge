package com.begoml.bridge.feature.matches.matchday

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bridge.feature.matches.generated.resources.Res
import bridge.feature.matches.generated.resources.fixture_score
import bridge.feature.matches.generated.resources.fixture_teams
import bridge.feature.matches.generated.resources.fixture_versus
import bridge.feature.matches.generated.resources.matchday_arena
import bridge.feature.matches.generated.resources.matchday_capacity
import bridge.feature.matches.generated.resources.matchday_days
import bridge.feature.matches.generated.resources.matchday_founded
import bridge.feature.matches.generated.resources.matchday_hours
import bridge.feature.matches.generated.resources.matchday_kickoff_local
import bridge.feature.matches.generated.resources.matchday_kickoff_now
import bridge.feature.matches.generated.resources.matchday_minutes
import bridge.feature.matches.generated.resources.matchday_next_match
import bridge.feature.matches.generated.resources.matchday_no_fixture
import bridge.feature.matches.generated.resources.matchday_recent
import bridge.feature.matches.generated.resources.matchday_seconds
import bridge.feature.matches.generated.resources.matchday_stadium
import com.begoml.bridge.core.data.model.Club
import com.begoml.bridge.core.data.model.Match
import com.begoml.bridge.feature.matches.formatKickoff
import com.begoml.bridge.feature.matches.groupedThousands
import com.begoml.bridge.foundation.tessera.collectState
import com.begoml.bridge.uikit.LocalScreenPadding
import com.begoml.bridge.uikit.component.BackdropImage
import com.begoml.bridge.uikit.component.BackdropVideo
import com.begoml.bridge.uikit.component.BadgeImage
import com.begoml.bridge.uikit.component.GlassPanel
import com.begoml.bridge.uikit.component.LoadableContent
import com.begoml.bridge.uikit.glass.GlassBackdrop
import com.begoml.bridge.uikit.glass.GlassScope
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.FigureStyle
import com.begoml.bridge.uikit.theme.LabelStyle
import org.jetbrains.compose.resources.stringResource

/** The hero card needs air under the status bar; the window inset alone sits it flush. */
private val HeroTopInset = 16.dp

@Composable
fun MatchdayScreen(feature: MatchdayFeature, modifier: Modifier = Modifier) {
    val state by feature.collectState()
    val contentPadding = LocalScreenPadding.current

    GlassBackdrop(
        modifier = modifier.fillMaxSize(),
        backdrop = { StadiumBackdrop(club = state.club) },
    ) {
        val glass = this
        LoadableContent(
            isLoading = state.isLoading && state.nextMatch == null && state.club == null,
            error = state.error.takeIf { state.club == null && state.nextMatch == null },
            onRetry = { feature.dispatchAction(MatchdayAction.Retry) },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
                    .padding(horizontal = 14.dp, vertical = HeroTopInset),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                with(glass) { HeroCard(state = state) }
                state.lastResult?.let { RecentSection(match = it) }
                state.club?.let { StadiumSection(club = it) }
            }
        }
    }
}

/**
 * The clip that plays behind this screen, or null for the photograph alone.
 *
 * Null today, and the reason was found by looking rather than guessing: the feed carries no video,
 * and every keyless stock clip is narrative footage. Tried on device, a film clip stays
 * recognisable at any opacity — a character's face ends up behind the fixture — while the club's
 * own photograph was carrying the screen. Point this at a football loop and the backdrop becomes
 * video; nothing else has to change.
 */
private val AmbientClipUrl: String? = null

@Composable
private fun StadiumBackdrop(club: Club?) {
    val poster = club?.media?.fanartUrls?.firstOrNull()
    val clip = AmbientClipUrl

    if (clip == null) {
        BackdropImage(url = poster)
    } else {
        BackdropVideo(videoUrl = clip, posterUrl = poster)
    }
}

@Composable
private fun GlassScope.HeroCard(state: MatchdayState) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = state.nextMatch?.competition
                        ?: stringResource(Res.string.matchday_next_match),
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
                    text = stringResource(Res.string.matchday_no_fixture),
                    color = BridgeColors.TextMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            } else {
                Versus(match = match)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = match.kickoff.formatKickoff(),
                        style = MaterialTheme.typography.titleSmall,
                        color = BridgeColors.TextPrimary,
                    )
                    Text(
                        text = stringResource(Res.string.matchday_kickoff_local),
                        style = LabelStyle,
                        color = BridgeColors.TextMuted,
                    )
                }
                CountdownRow(
                    countdown = Countdown.between(
                        nowMillis = state.nowMillis,
                        kickoffMillis = match.kickoff.toEpochMilliseconds(),
                    ),
                )
            }
        }
    }
}

@Composable
private fun Versus(match: Match) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TeamColumn(name = match.home.name, code = match.home.code, badge = match.home.badgeUrl)
        Text(
            text = stringResource(Res.string.fixture_versus),
            style = FigureStyle,
            color = BridgeColors.TextMuted,
        )
        TeamColumn(name = match.away.name, code = match.away.code, badge = match.away.badgeUrl)
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
private fun CountdownRow(countdown: Countdown) {
    if (countdown.hasStarted) {
        Text(
            text = stringResource(Res.string.matchday_kickoff_now),
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
        CountdownCell(countdown.days, stringResource(Res.string.matchday_days))
        CountdownCell(countdown.hours, stringResource(Res.string.matchday_hours))
        CountdownCell(countdown.minutes, stringResource(Res.string.matchday_minutes))
        CountdownCell(countdown.seconds, stringResource(Res.string.matchday_seconds))
    }
}

@Composable
private fun CountdownCell(value: Long, unit: String) {
    Column(
        modifier = Modifier
            .width(42.dp)
            .background(BridgeColors.Club.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = value.pad2(), style = FigureStyle, color = BridgeColors.TextPrimary)
        Text(text = unit, style = LabelStyle, color = BridgeColors.TextMuted)
    }
}

@Composable
private fun RecentSection(match: Match) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = stringResource(Res.string.matchday_recent),
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
            BadgeImage(url = match.away.badgeUrl, code = match.away.code, size = 22.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.fixture_teams, match.home.name, match.away.name),
                    style = MaterialTheme.typography.labelLarge,
                    color = BridgeColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(text = match.competition, style = LabelStyle, color = BridgeColors.TextMuted)
            }
            match.score?.let {
                Text(
                    text = stringResource(Res.string.fixture_score, it.home, it.away),
                    style = FigureStyle,
                    color = BridgeColors.TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun StadiumSection(club: Club) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = stringResource(Res.string.matchday_stadium),
            style = LabelStyle,
            color = BridgeColors.TextMuted,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BridgeColors.Surface, RoundedCornerShape(12.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Fact(
                label = stringResource(Res.string.matchday_arena),
                value = club.stadium.orEmpty(),
                modifier = Modifier.weight(1.7f),
            )
            Fact(
                label = stringResource(Res.string.matchday_capacity),
                value = club.stadiumCapacity?.groupedThousands().orEmpty(),
                modifier = Modifier.weight(1f),
            )
            Fact(
                label = stringResource(Res.string.matchday_founded),
                value = club.foundedYear?.toString().orEmpty(),
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
