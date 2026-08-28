package com.begoml.bridge.feature.matches.matchday

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.begoml.bridge.uikit.component.BadgeImage
import com.begoml.bridge.uikit.component.GlassPanel
import com.begoml.bridge.uikit.glass.GlassScope
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.FigureStyle
import com.begoml.bridge.uikit.theme.LabelStyle

/**
 * The fixture, its countdown and the two badges.
 *
 * Split from the screen because it is the only part that redraws every second: the clock reaches
 * it and stops there.
 */
@Composable
internal fun GlassScope.HeroCard(state: MatchdayUiState, labels: MatchdayLabels, nowMillis: Long) {
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
