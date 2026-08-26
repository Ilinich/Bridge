package com.begoml.bridge.feature.squad.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.begoml.bridge.core.data.model.Player
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.begoml.bridge.feature.squad.SquadViewModel
import com.begoml.bridge.uikit.LocalScreenPadding
import com.begoml.bridge.uikit.component.CutoutImage
import com.begoml.bridge.uikit.component.LoadableContent
import com.begoml.bridge.uikit.shader.ClubBackgroundShader
import com.begoml.bridge.uikit.shader.rememberAnimatedShaderBrush
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.FigureStyle
import com.begoml.bridge.uikit.theme.LabelStyle

private const val GridColumns = 2

@Composable
internal fun SquadScreen(viewModel: SquadViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val contentPadding = LocalScreenPadding.current

    LoadableContent(
        isLoading = state.isLoading && state.players.isEmpty(),
        error = state.error.takeIf { state.players.isEmpty() },
        onRetry = viewModel::retry,
        modifier = modifier.fillMaxSize().background(BridgeColors.Ground),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(GridColumns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 14.dp,
                end = 14.dp,
                top = contentPadding.calculateTopPadding() + 4.dp,
                bottom = contentPadding.calculateBottomPadding(),
            ),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            items(
                items = state.players,
                key = { player -> player.id },
                contentType = { "player-card" },
            ) { player ->
                PlayerCard(player = player, onClick = { viewModel.onPlayerClick(player.id) })
            }
        }
    }
}

@Composable
private fun PlayerCard(player: Player, onClick: () -> Unit) {
    val brush = rememberAnimatedShaderBrush(ClubBackgroundShader)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(brush)
            .clickable(onClick = onClick),
    ) {
        player.shirtNumber?.let { number ->
            Text(
                text = number,
                style = FigureStyle.copy(fontSize = MaterialTheme.typography.headlineLarge.fontSize),
                color = BridgeColors.TextPrimary.copy(alpha = 0.12f),
                modifier = Modifier.align(Alignment.TopEnd).padding(horizontal = 8.dp),
            )
        }
        CutoutImage(
            url = player.cutoutUrl,
            modifier = Modifier.fillMaxSize().padding(top = 10.dp, bottom = 46.dp),
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.labelLarge,
                color = BridgeColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            player.position?.let {
                Text(
                    text = it,
                    style = LabelStyle,
                    color = BridgeColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
