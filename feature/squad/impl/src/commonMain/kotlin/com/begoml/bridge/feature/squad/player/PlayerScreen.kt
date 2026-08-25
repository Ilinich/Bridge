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
import bridge.feature.squad.impl.generated.resources.Res
import bridge.feature.squad.impl.generated.resources.squad_country
import bridge.feature.squad.impl.generated.resources.squad_height
import bridge.feature.squad.impl.generated.resources.squad_not_found
import bridge.feature.squad.impl.generated.resources.squad_number
import bridge.feature.squad.impl.generated.resources.squad_position
import bridge.feature.squad.impl.generated.resources.squad_title
import com.begoml.bridge.core.data.model.Player
import com.begoml.bridge.feature.squad.grid.SquadDelegate
import com.begoml.bridge.foundation.tessera.collectUiState
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
import org.jetbrains.compose.resources.stringResource
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
fun PlayerScreen(
    delegate: SquadDelegate,
    initialPlayerId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val squadState by delegate.collectUiState()
    val players = squadState.players
    val brush = rememberAnimatedShaderBrush(ClubBackgroundShader)
    val contentPadding = LocalScreenPadding.current

    GlassBackdrop(
        modifier = modifier.fillMaxSize(),
        backdrop = { Box(Modifier.fillMaxSize().background(brush)) },
    ) {
        val glass = this
        if (players.isEmpty()) {
            Text(
                text = stringResource(Res.string.squad_not_found),
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
            PlayerPage(glass = glass, player = players[page], offset = offset)
        }

        Column(modifier = Modifier.fillMaxWidth().padding(top = contentPadding.calculateTopPadding())) {
            BridgeTopBar(
                title = players.getOrNull(pagerState.currentPage)?.name
                    ?: stringResource(Res.string.squad_title),
                leading = {
                    with(glass) {
                        BridgeBackButton(
                            onClick = onBack,
                            contentDescription = stringResource(Res.string.squad_title),
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
private fun PlayerPage(glass: GlassScope, player: Player, offset: Float) {
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
                PlayerFacts(player = player)
            }
        }
    }
}

@Composable
private fun PlayerFacts(player: Player) {
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
            Fact(stringResource(Res.string.squad_number), player.shirtNumber, Modifier.weight(1f))
            Fact(stringResource(Res.string.squad_position), player.position, Modifier.weight(1.6f))
            Fact(stringResource(Res.string.squad_country), player.nationality, Modifier.weight(1.2f))
            Fact(stringResource(Res.string.squad_height), player.height, Modifier.weight(1f))
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
