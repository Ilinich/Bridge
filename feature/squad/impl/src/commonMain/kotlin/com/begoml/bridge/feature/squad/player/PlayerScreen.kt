package com.begoml.bridge.feature.squad.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.begoml.bridge.core.data.model.Player
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.begoml.bridge.feature.squad.PlayerLabels
import com.begoml.bridge.feature.squad.PlayerViewModel
import com.begoml.bridge.uikit.LocalScreenPadding
import com.begoml.bridge.uikit.component.BridgeBackButton
import com.begoml.bridge.uikit.component.BridgeTopBar
import com.begoml.bridge.uikit.component.CutoutImage
import com.begoml.bridge.uikit.component.GlassPanel
import com.begoml.bridge.uikit.glass.GlassBackdrop
import com.begoml.bridge.uikit.glass.GlassScope
import com.begoml.bridge.uikit.shader.ClubBackgroundShader
import com.begoml.bridge.uikit.shader.rememberAnimatedShaderBrush
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.FigureStyle
import com.begoml.bridge.uikit.theme.LabelStyle
import kotlin.math.absoluteValue

private const val CutoutParallaxDp = 40f
private const val EdgeFadeStrength = 0.4f

/**
 * The player card, paged sideways.
 *
 * The parallax is driven by the pager's own offset rather than by the device's tilt: tilt needs
 * sensors on both platforms, behaves differently in a simulator, and does not show up in a
 * screenshot. Everything that moves does so through `graphicsLayer`, which stays on the render
 * thread instead of asking for a relayout each frame.
 *
 * This screen owns a horizontal gesture, so it must never opt into swipe-to-dismiss — the two
 * would fight over the same drag.
 */
@Composable
internal fun PlayerScreen(
    viewModel: PlayerViewModel,
    initialPlayerId: String,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val players = state.squad.players
    val labels = state.labels
    val brush = rememberAnimatedShaderBrush(ClubBackgroundShader)
    val contentPadding = LocalScreenPadding.current

    GlassBackdrop(
        modifier = modifier.fillMaxSize(),
        backdrop = { Box(Modifier.fillMaxSize().background(brush)) },
    ) {
        val glass = this
        if (labels == null) return@GlassBackdrop
        if (players.isEmpty()) {
            Text(
                text = labels.notFound,
                color = BridgeColors.TextMuted,
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                textAlign = TextAlign.Center,
            )
            return@GlassBackdrop
        }

        val startIndex = players.indexOfFirst { it.id == initialPlayerId }.coerceAtLeast(0)
        val pagerState = rememberPagerState(initialPage = startIndex) { players.size }

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val offset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            PlayerPage(glass = glass, player = players[page], labels = labels, offset = offset)
        }

        Column(modifier = Modifier.fillMaxWidth().padding(top = contentPadding.calculateTopPadding())) {
            BridgeTopBar(
                title = players.getOrNull(pagerState.currentPage)?.name ?: labels.title,
                leading = {
                    with(glass) {
                        BridgeBackButton(
                            onClick = viewModel::onBack,
                            contentDescription = labels.back,
                        )
                    }
                },
            )
        }

        PageDots(
            count = players.size,
            selected = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = contentPadding.calculateBottomPadding() + 10.dp),
        )
    }
}

@Composable
private fun PlayerPage(glass: GlassScope, player: Player, labels: PlayerLabels, offset: Float) {
    val contentPadding = LocalScreenPadding.current
    Box(modifier = Modifier.fillMaxSize()) {
        CutoutImage(
            url = player.cutoutUrl,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp, bottom = 190.dp)
                .graphicsLayer {
                    translationX = offset * CutoutParallaxDp * density
                    alpha = 1f - offset.absoluteValue * EdgeFadeStrength
                },
        )

        with(glass) {
            GlassPanel(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = 14.dp,
                        end = 14.dp,
                        bottom = contentPadding.calculateBottomPadding() + 34.dp,
                    ),
            ) {
                PlayerFacts(player = player, labels = labels)
            }
        }
    }
}

@Composable
private fun PlayerFacts(player: Player, labels: PlayerLabels) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text(
            text = player.name,
            style = MaterialTheme.typography.titleLarge,
            color = BridgeColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Fact(labels.number, player.shirtNumber, Modifier.weight(1f))
            Fact(labels.position, player.position, Modifier.weight(1.6f))
            Fact(labels.country, player.nationality, Modifier.weight(1.2f))
            Fact(labels.height, player.height, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Fact(label: String, value: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, style = LabelStyle, color = BridgeColors.TextMuted)
        Text(
            text = value.orEmpty(),
            style = MaterialTheme.typography.labelLarge,
            color = BridgeColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PageDots(count: Int, selected: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val active = index == selected
            Box(
                modifier = Modifier
                    .size(width = if (active) 14.dp else 5.dp, height = 5.dp)
                    .clip(CircleShape)
                    .background(if (active) BridgeColors.ClubBright else BridgeColors.Line),
            )
        }
    }
}
