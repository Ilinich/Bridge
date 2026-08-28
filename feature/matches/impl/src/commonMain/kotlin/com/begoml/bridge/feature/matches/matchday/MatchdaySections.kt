package com.begoml.bridge.feature.matches.matchday

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.begoml.bridge.uikit.component.BadgeImage
import com.begoml.bridge.uikit.component.FollowStar
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.FigureStyle
import com.begoml.bridge.uikit.theme.LabelStyle
import kotlinx.collections.immutable.ImmutableList

/** Followed players, written on the squad screen and only read here. */
@Composable
internal fun FollowingSection(
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
internal fun RecentSection(recent: RecentMatchUi, labels: MatchdayLabels) {
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
internal fun StadiumSection(stadium: StadiumUi, labels: MatchdayLabels, onClick: () -> Unit) {
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
