package com.begoml.bridge.uikit.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.begoml.bridge.uikit.glass.GlassScope
import com.begoml.bridge.uikit.theme.BridgeColors
import com.begoml.bridge.uikit.theme.LabelStyle

private val BarHeight = 46.dp
private val BarInset = 18.dp
private val BarBottomInset = 12.dp

/** Bars sit over moving content, so they carry far less tint than a panel does. */
private const val BarTintAlpha = 0.26f
private val TabIconSize = 21.dp

/**
 * The floating tab capsule.
 *
 * It is declared inside [GlassScope] because it frosts what scrolls beneath it, and that is only
 * correct when it is a sibling of the backdrop rather than a child of it.
 */
@Composable
fun GlassScope.BridgeTabBar(
    tabs: List<BridgeTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(start = BarInset, end = BarInset, bottom = BarBottomInset)
            .fillMaxWidth()
            .height(BarHeight)
            .then(
                Modifier.glass(
                    shape = CircleShape,
                    tint = BridgeColors.Ground.copy(alpha = BarTintAlpha),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { index, tab ->
            TabItem(
                tab = tab,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TabItem(
    tab: BridgeTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint by animateColorAsState(
        targetValue = if (selected) BridgeColors.ClubBright else BridgeColors.TextMuted,
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(BarHeight - 8.dp)
            .clip(CircleShape)
            // The label is gone from the screen but not from the tab: it stays the accessible
            // name, so a screen reader still announces which tab this is.
            .clickable(onClickLabel = tab.label, onClick = onClick)
            .semantics { contentDescription = tab.label },
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BridgeIconGlyph(icon = tab.icon, tint = tint, glyphSize = TabIconSize)
    }
}

/** A round glass disc carrying a back chevron. */
@Composable
fun GlassScope.BridgeBackButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(30.dp)
            .then(Modifier.glass(shape = CircleShape))
            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
            .clickable(onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BackChevron(tint = BridgeColors.TextPrimary)
    }
}

@Composable
fun BridgeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    leading: @Composable () -> Unit = {},
) {
    Row(
        // A minimum, not a fixed height: at a large font scale a fixed bar clips its own title.
        modifier = modifier.fillMaxWidth().heightIn(min = 44.dp).padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = BridgeColors.TextPrimary,
        )
    }
}

@Composable
private fun BackChevron(tint: Color) {
    Canvas(modifier = Modifier.size(12.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.66f, size.height * 0.12f)
            lineTo(size.width * 0.30f, size.height * 0.5f)
            lineTo(size.width * 0.66f, size.height * 0.88f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = size.width * 0.16f),
        )
    }
}

/** A rounded panel for content that sits on the glass. */
@Composable
fun GlassScope.GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .then(Modifier.glass(shape = RoundedCornerShape(16.dp)))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        content()
    }
}
