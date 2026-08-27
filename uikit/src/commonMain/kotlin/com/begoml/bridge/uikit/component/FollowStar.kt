package com.begoml.bridge.uikit.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.begoml.bridge.uikit.theme.BridgeColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val DefaultStarSize = 18.dp
private const val Points = 5
private const val InnerRadiusFraction = 0.42f

/** Marks a followed player where there is nothing to tap — a grid card, a summary row. */
@Composable
fun FollowStar(
    followed: Boolean,
    modifier: Modifier = Modifier,
    starSize: Dp = DefaultStarSize,
) {
    Canvas(modifier = modifier.size(starSize)) { drawStar(followed) }
}

/** The same mark, as the app's only control that writes anything. */
@Composable
fun FollowButton(
    followed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 34.dp,
) {
    Canvas(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .size(starSize),
    ) {
        drawStar(followed = followed, scale = 0.62f)
    }
}

/**
 * A five-pointed star, alternating outer and inner radius.
 *
 * The first vertex is pulled a quarter turn back so a point sits at the top; without it the star
 * stands on a point and reads as rotated.
 */
private fun DrawScope.drawStar(followed: Boolean, scale: Float = 1f) {
    val radius = size.minDimension / 2f * scale
    val centre = Offset(size.width / 2f, size.height / 2f)
    val step = PI / Points
    val path = Path()

    repeat(Points * 2) { index ->
        val distance = if (index % 2 == 0) radius else radius * InnerRadiusFraction
        val angle = index * step - PI / 2
        val point = Offset(
            x = centre.x + (cos(angle) * distance).toFloat(),
            y = centre.y + (sin(angle) * distance).toFloat(),
        )
        if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()

    if (followed) {
        drawPath(path = path, color = BridgeColors.ClubBright)
    } else {
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.55f),
            style = Stroke(width = radius * 0.16f),
        )
    }
}
