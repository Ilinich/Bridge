package com.begoml.bridge.feature.club

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bridge.feature.club.generated.resources.Res
import bridge.feature.club.generated.resources.club_about
import bridge.feature.club.generated.resources.club_capacity
import bridge.feature.club.generated.resources.club_colours
import bridge.feature.club.generated.resources.club_founded
import bridge.feature.club.generated.resources.club_ground
import bridge.feature.club.generated.resources.club_instagram
import bridge.feature.club.generated.resources.club_links
import bridge.feature.club.generated.resources.club_location
import bridge.feature.club.generated.resources.club_opened
import bridge.feature.club.generated.resources.club_twitter
import bridge.feature.club.generated.resources.club_website
import bridge.feature.club.generated.resources.club_youtube
import com.begoml.bridge.core.data.model.Club
import com.begoml.bridge.core.data.model.Venue
import com.begoml.bridge.foundation.tessera.collectUiState
import com.begoml.bridge.uikit.LocalScreenPadding
import com.begoml.bridge.uikit.component.BackdropImage
import com.begoml.bridge.uikit.component.BadgeImage
import com.begoml.bridge.uikit.component.GlassPanel
import com.begoml.bridge.uikit.component.LoadableContent
import com.begoml.bridge.uikit.glass.GlassBackdrop
import com.begoml.bridge.uikit.glass.GlassScope
import com.begoml.bridge.uikit.rememberUrlOpener
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.LabelStyle
import org.jetbrains.compose.resources.stringResource

@Composable
fun ClubScreen(delegate: ClubDelegate, modifier: Modifier = Modifier) {
    val state by delegate.collectUiState()
    val contentPadding = LocalScreenPadding.current

    GlassBackdrop(
        modifier = modifier.fillMaxSize(),
        backdrop = { BackdropImage(url = state.club?.media?.fanartUrls?.lastOrNull()) },
    ) {
        val glass = this
        LoadableContent(
            isLoading = state.isLoading && state.club == null,
            error = state.error.takeIf { state.club == null },
            onRetry = delegate::retry,
        ) {
            val club = state.club ?: return@LoadableContent
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                with(glass) { Header(club = club) }
                club.description?.let { Section(title = stringResource(Res.string.club_about)) { Prose(it) } }
                state.venue?.let { GroundSection(venue = it) }
                LinksSection(club = club)
            }
        }
    }
}

@Composable
private fun GlassScope.Header(club: Club) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            BadgeImage(url = club.media.badgeUrl, code = club.code, size = 68.dp)
            Text(
                text = club.name,
                style = MaterialTheme.typography.headlineSmall,
                color = BridgeColors.TextPrimary,
            )
            if (club.details.nicknames.isNotEmpty()) {
                Text(
                    text = club.details.nicknames.joinToString(" · "),
                    style = LabelStyle,
                    color = BridgeColors.TextMuted,
                    textAlign = TextAlign.Center,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth(),
            ) {
                club.foundedYear?.let {
                    Fact(stringResource(Res.string.club_founded), it.toString())
                }
                ColourStrip(club = club)
            }
        }
    }
}

@Composable
private fun ColourStrip(club: Club) {
    val colours = listOfNotNull(
        club.details.colours.primary,
        club.details.colours.secondary,
        club.details.colours.tertiary,
    ).mapNotNull(::parseHexColour)
    if (colours.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(Res.string.club_colours),
            style = LabelStyle,
            color = BridgeColors.TextMuted,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            colours.forEach { colour ->
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(colour)
                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                )
            }
        }
    }
}

@Composable
private fun GroundSection(venue: Venue) {
    Section(title = stringResource(Res.string.club_ground)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            venue.thumbUrl?.let { url ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp)),
                ) {
                    BackdropImage(url = url)
                }
            }
            Text(
                text = venue.name,
                style = MaterialTheme.typography.titleMedium,
                color = BridgeColors.TextPrimary,
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                venue.capacity?.let {
                    Fact(stringResource(Res.string.club_capacity), it.grouped(), Modifier.weight(1f))
                }
                venue.openedYear?.let {
                    Fact(stringResource(Res.string.club_opened), it.toString(), Modifier.weight(1f))
                }
                venue.location?.let {
                    Fact(stringResource(Res.string.club_location), it, Modifier.weight(1.6f))
                }
            }
            venue.description?.let { Prose(it) }
        }
    }
}

@Composable
private fun LinksSection(club: Club) {
    val opener = rememberUrlOpener()
    val links = listOfNotNull(
        club.details.links.website?.let { stringResource(Res.string.club_website) to it },
        club.details.links.youtube?.let { stringResource(Res.string.club_youtube) to it },
        club.details.links.twitter?.let { stringResource(Res.string.club_twitter) to it },
        club.details.links.instagram?.let { stringResource(Res.string.club_instagram) to it },
    )
    if (links.isEmpty()) return

    Section(title = stringResource(Res.string.club_links)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            links.forEach { (label, url) ->
                Text(
                    text = label,
                    style = LabelStyle,
                    color = BridgeColors.TextPrimary,
                    modifier = Modifier
                        .background(BridgeColors.Club, CircleShape)
                        .clickable { opener.open(url) }
                        .padding(horizontal = 13.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(text = title, style = LabelStyle, color = BridgeColors.TextMuted)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BridgeColors.Surface, RoundedCornerShape(12.dp))
                .padding(12.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun Prose(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = BridgeColors.TextMuted,
    )
}

@Composable
private fun Fact(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, style = LabelStyle, color = BridgeColors.TextMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = BridgeColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun Int.grouped(): String {
    val text = toString()
    if (text.length <= 3) return text
    return text.reversed().chunked(3).joinToString(" ").reversed()
}

/** The feed writes colours as `#RRGGBB`; anything else is skipped rather than guessed at. */
private fun parseHexColour(raw: String): Color? {
    val hex = raw.removePrefix("#").takeIf { it.length == 6 } ?: return null
    val value = hex.toLongOrNull(radix = 16) ?: return null
    return Color(0xFF000000 or value)
}
