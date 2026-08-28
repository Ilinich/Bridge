package com.begoml.bridge.feature.matches.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.begoml.bridge.foundation.tessera.collectUiState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.begoml.bridge.uikit.LocalScreenPadding
import com.begoml.bridge.uikit.component.BridgeBackButton
import com.begoml.bridge.uikit.component.BridgeTopBar
import com.begoml.bridge.uikit.component.GlassPanel
import com.begoml.bridge.uikit.component.TeamMonogram
import com.begoml.bridge.uikit.glass.GlassBackdrop
import com.begoml.bridge.uikit.glass.GlassScope
import com.begoml.bridge.uikit.shader.ClubBackgroundShader
import com.begoml.bridge.uikit.shader.rememberAnimatedShader
import com.begoml.bridge.uikit.shader.shaded
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.FigureStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import com.begoml.bridge.uikit.theme.LabelStyle

/** The side gutter of this screen. */
private val ScreenGutter = 14.dp

@Composable
internal fun MatchDetailScreen(viewModel: MatchDetailViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.collectUiState()
    val shader = rememberAnimatedShader(ClubBackgroundShader)
    val contentPadding = LocalScreenPadding.current

    GlassBackdrop(
        modifier = modifier.fillMaxSize(),
        backdrop = { Box(Modifier.fillMaxSize().shaded(shader)) },
    ) {
        val glass = this
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val labels = state.labels ?: return@Column
            BridgeTopBar(
                title = labels.title,
                leading = {
                    with(glass) {
                        BridgeBackButton(
                            onClick = viewModel::onBack,
                            contentDescription = labels.back,
                        )
                    }
                },
            )

            val current = state.match
            if (current == null) {
                Text(
                    text = labels.notFound,
                    color = BridgeColors.TextMuted,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    textAlign = TextAlign.Center,
                )
                return@Column
            }

            with(glass) { FixtureCard(match = current, labels = labels) }
        }
    }
}

@Composable
private fun GlassScope.FixtureCard(match: MatchDetailUi, labels: MatchDetailLabels) {
    GlassPanel(modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenGutter)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Side(name = match.homeName, code = match.homeCode)
                Text(
                    text = match.scoreline,
                    style = FigureStyle.copy(
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    ),
                    color = BridgeColors.TextPrimary,
                )
                Side(name = match.awayName, code = match.awayCode)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                MatchFacts(match = match, labels = labels)
                Text(
                    text = labels.kickoff,
                    style = LabelStyle,
                    color = BridgeColors.TextMuted,
                )
                Text(
                    text = match.kickoff,
                    style = MaterialTheme.typography.titleSmall,
                    color = BridgeColors.TextPrimary,
                )
            }
        }
    }
}

/** Round, which side we are on, and how it ended — everything the calendar feed actually carries. */
@Composable
private fun MatchFacts(match: MatchDetailUi, labels: MatchDetailLabels) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Chip(text = match.round)
        match.side?.let { side ->
            Chip(text = if (side == MatchSide.Home) labels.homeLabel else labels.awayLabel)
        }
        match.outcome?.let { outcome ->
            Chip(
                text = when (outcome) {
                    MatchOutcome.Win -> labels.win
                    MatchOutcome.Draw -> labels.draw
                    MatchOutcome.Loss -> labels.loss
                },
                color = when (outcome) {
                    MatchOutcome.Win -> BridgeColors.Win
                    MatchOutcome.Draw -> BridgeColors.Draw
                    MatchOutcome.Loss -> BridgeColors.Loss
                },
            )
        }
    }
}

@Composable
private fun Chip(text: String, color: Color = BridgeColors.TextMuted) {
    Text(
        text = text,
        style = LabelStyle,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = ChipTintAlpha), CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

private const val ChipTintAlpha = 0.16f

@Composable
private fun Side(name: String, code: String) {
    Column(
        modifier = Modifier.width(92.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        TeamMonogram(code = code, size = 40.dp)
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = BridgeColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
    }
}
