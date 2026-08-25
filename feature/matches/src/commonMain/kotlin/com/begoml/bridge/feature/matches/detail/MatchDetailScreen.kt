package com.begoml.bridge.feature.matches.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bridge.feature.matches.generated.resources.Res
import bridge.feature.matches.generated.resources.fixture_score
import bridge.feature.matches.generated.resources.fixture_versus
import bridge.feature.matches.generated.resources.match_kickoff
import bridge.feature.matches.generated.resources.match_not_found
import bridge.feature.matches.generated.resources.match_title
import com.begoml.bridge.core.data.model.SeasonMatch
import com.begoml.bridge.feature.matches.formatKickoff
import com.begoml.bridge.uikit.component.BridgeBackButton
import com.begoml.bridge.uikit.component.BridgeTopBar
import com.begoml.bridge.uikit.component.GlassPanel
import com.begoml.bridge.uikit.component.TeamMonogram
import com.begoml.bridge.uikit.glass.GlassBackdrop
import com.begoml.bridge.uikit.glass.GlassScope
import com.begoml.bridge.uikit.shader.ClubBackgroundShader
import com.begoml.bridge.uikit.shader.rememberAnimatedShaderBrush
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.FigureStyle
import com.begoml.bridge.uikit.theme.LabelStyle
import org.jetbrains.compose.resources.stringResource

@Composable
fun MatchDetailScreen(
    match: SeasonMatch?,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brush = rememberAnimatedShaderBrush(ClubBackgroundShader)

    GlassBackdrop(
        modifier = modifier.fillMaxSize(),
        backdrop = { Box(Modifier.fillMaxSize().background(brush)) },
    ) {
        val glass = this
        Column(
            modifier = Modifier.fillMaxSize().safeContentPadding().padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BridgeTopBar(
                title = stringResource(Res.string.match_title),
                leading = {
                    with(glass) {
                        BridgeBackButton(
                            onClick = onBack,
                            contentDescription = stringResource(Res.string.match_title),
                        )
                    }
                },
            )

            if (match == null) {
                Text(
                    text = stringResource(Res.string.match_not_found),
                    color = BridgeColors.TextMuted,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    textAlign = TextAlign.Center,
                )
                return@Column
            }

            with(glass) { FixtureCard(match = match) }
        }
    }
}

@Composable
private fun GlassScope.FixtureCard(match: SeasonMatch) {
    GlassPanel(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
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
                Side(name = match.home.name, code = match.home.code)
                Text(
                    text = match.score
                        ?.let { stringResource(Res.string.fixture_score, it.home, it.away) }
                        ?: stringResource(Res.string.fixture_versus),
                    style = FigureStyle.copy(
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    ),
                    color = BridgeColors.TextPrimary,
                )
                Side(name = match.away.name, code = match.away.code)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = stringResource(Res.string.match_kickoff),
                    style = LabelStyle,
                    color = BridgeColors.TextMuted,
                )
                Text(
                    text = match.kickoff.formatKickoff(),
                    style = MaterialTheme.typography.titleSmall,
                    color = BridgeColors.TextPrimary,
                )
            }
        }
    }
}

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
