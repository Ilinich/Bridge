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
import bridge.feature.club.impl.generated.resources.Res
import com.begoml.bridge.core.data.model.Club
import com.begoml.bridge.core.data.model.Venue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.begoml.bridge.uikit.LocalScreenPadding
import com.begoml.bridge.uikit.groupedThousands
import com.begoml.bridge.uikit.component.BackdropImage
import com.begoml.bridge.uikit.component.BadgeImage
import com.begoml.bridge.uikit.component.LoadableContent
import com.begoml.bridge.uikit.glass.EdgeFadeOverhang
import com.begoml.bridge.uikit.glass.ScrollEdge
import com.begoml.bridge.uikit.glass.EdgeFadeOverhang
import com.begoml.bridge.uikit.glass.ScrollEdgeFade
import com.begoml.bridge.uikit.component.ShaderPanel
import com.begoml.bridge.uikit.shader.FloodlightShader
import com.begoml.bridge.uikit.rememberUrlOpener
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.LabelStyle
import com.begoml.bridge.uikit.video.VideoControls
import com.begoml.bridge.uikit.video.VideoSurface
import com.begoml.bridge.uikit.video.rememberVideoPlayback

/**
 * The clip the media section plays.
 *
 * It is the only video on the screen. An earlier build also ran it blended into the backdrop,
 * which meant two decoders for one clip and made the player look like a duplicate of the wallpaper
 * rather than the thing the user drives.
 */
private const val ClipUrl =
    "https://videos.pexels.com/video-files/2611250/2611250-hd_1920_1080_30fps.mp4"

@Composable
internal fun ClubScreen(viewModel: ClubViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val contentPadding = LocalScreenPadding.current

    Box(modifier = modifier.fillMaxSize()) {
        BackdropImage(url = state.club?.media?.fanartUrls?.lastOrNull())

        LoadableContent(
            isLoading = (state.isLoading || state.labels == null) && state.club == null,
            error = state.error.takeIf { state.club == null },
            onRetry = viewModel::retry,
        ) {
            val club = state.club ?: return@LoadableContent
            val labels = state.labels ?: return@LoadableContent
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Header(club = club, labels = labels)
                MediaSection(title = labels.media)
                club.description?.let { Section(title = labels.about) { Prose(it) } }
                state.venue?.let { GroundSection(venue = it, labels = labels) }
                LinksSection(club = club, labels = labels)
            }
        }

        ScrollEdgeFade(
            edge = ScrollEdge.Top,
            height = contentPadding.calculateTopPadding() + EdgeFadeOverhang,
        )
        ScrollEdgeFade(
            edge = ScrollEdge.Bottom,
            height = contentPadding.calculateBottomPadding() + EdgeFadeOverhang,
        )
    }
}


@Composable
private fun Header(club: Club, labels: ClubLabels) {
    ShaderPanel(spec = FloodlightShader, modifier = Modifier.fillMaxWidth()) {
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
                    Fact(labels.founded, it.toString())
                }
                ColourStrip(club = club, label = labels.colours)
            }
        }
    }
}

@Composable
private fun ColourStrip(club: Club, label: String) {
    val colours = listOfNotNull(
        club.details.colours.primary,
        club.details.colours.secondary,
        club.details.colours.tertiary,
    ).mapNotNull(::parseHexColour)
    if (colours.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
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

/**
 * The clip with its transport controls.
 *
 * It does not auto-play and it does not loop: a backdrop should start on its own, but a player the
 * user is meant to drive should be waiting for them, and a clip that silently restarts makes the
 * position readout look broken.
 */
@Composable
private fun MediaSection(title: String) {
    val playback = rememberVideoPlayback(
        url = ClipUrl,
        autoPlay = false,
        loop = false,
        muted = true,
    )

    Section(title = title) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BridgeColors.Ground),
            ) {
                VideoSurface(playback = playback, modifier = Modifier.fillMaxSize())
            }
            VideoControls(playback = playback)
        }
    }
}

@Composable
private fun GroundSection(venue: Venue, labels: ClubLabels) {
    Section(title = labels.ground) {
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
                    Fact(labels.capacity, it.groupedThousands(), Modifier.weight(1f))
                }
                venue.openedYear?.let {
                    Fact(labels.opened, it.toString(), Modifier.weight(1f))
                }
                venue.location?.let {
                    Fact(labels.location, it, Modifier.weight(1.6f))
                }
            }
            venue.description?.let { Prose(it) }
        }
    }
}

@Composable
private fun LinksSection(club: Club, labels: ClubLabels) {
    val opener = rememberUrlOpener()
    val links = listOfNotNull(
        club.details.links.website?.let { labels.website to it },
        club.details.links.youtube?.let { labels.youtube to it },
        club.details.links.twitter?.let { labels.twitter to it },
        club.details.links.instagram?.let { labels.instagram to it },
    )
    if (links.isEmpty()) return

    Section(title = labels.links) {
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

/** The feed writes colours as `#RRGGBB`; anything else is skipped rather than guessed at. */
private fun parseHexColour(raw: String): Color? {
    val hex = raw.removePrefix("#").takeIf { it.length == 6 } ?: return null
    val value = hex.toLongOrNull(radix = 16) ?: return null
    return Color(0xFF000000 or value)
}
