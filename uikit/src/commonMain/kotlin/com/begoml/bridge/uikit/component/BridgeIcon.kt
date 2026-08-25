package com.begoml.bridge.uikit.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp

/**
 * The three tab glyphs, drawn rather than imported.
 *
 * Three shapes do not justify an icon dependency, and drawing them keeps the stroke weight
 * consistent with the rest of the kit.
 */
enum class BridgeIcon { Matchday, Season, Squad }

@Composable
fun BridgeIconGlyph(icon: BridgeIcon, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val stroke = Stroke(width = size.minDimension * 0.09f)
        when (icon) {
            BridgeIcon.Matchday -> drawShield(tint, stroke)
            BridgeIcon.Season -> drawCalendar(tint, stroke)
            BridgeIcon.Squad -> drawPeople(tint, stroke)
        }
    }
}

private fun DrawScope.drawShield(tint: Color, stroke: Stroke) {
    val path = Path().apply {
        val width = size.width
        val height = size.height
        moveTo(width * 0.5f, height * 0.08f)
        lineTo(width * 0.88f, height * 0.24f)
        lineTo(width * 0.88f, height * 0.55f)
        cubicTo(
            width * 0.88f, height * 0.80f,
            width * 0.70f, height * 0.90f,
            width * 0.5f, height * 0.95f,
        )
        cubicTo(
            width * 0.30f, height * 0.90f,
            width * 0.12f, height * 0.80f,
            width * 0.12f, height * 0.55f,
        )
        lineTo(width * 0.12f, height * 0.24f)
        close()
    }
    drawPath(path, tint, style = stroke)
}

private fun DrawScope.drawCalendar(tint: Color, stroke: Stroke) {
    val inset = size.width * 0.12f
    drawRoundRect(
        color = tint,
        topLeft = Offset(inset, size.height * 0.22f),
        size = Size(size.width - inset * 2, size.height * 0.66f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * 0.12f),
        style = stroke,
    )
    drawLine(
        color = tint,
        start = Offset(inset, size.height * 0.42f),
        end = Offset(size.width - inset, size.height * 0.42f),
        strokeWidth = stroke.width,
    )
    listOf(0.34f, 0.66f).forEach { fraction ->
        drawLine(
            color = tint,
            start = Offset(size.width * fraction, size.height * 0.08f),
            end = Offset(size.width * fraction, size.height * 0.30f),
            strokeWidth = stroke.width,
        )
    }
}

private fun DrawScope.drawPeople(tint: Color, stroke: Stroke) {
    drawCircle(
        color = tint,
        radius = size.width * 0.17f,
        center = Offset(size.width * 0.38f, size.height * 0.32f),
        style = stroke,
    )
    val body = Path().apply {
        moveTo(size.width * 0.08f, size.height * 0.92f)
        cubicTo(
            size.width * 0.08f, size.height * 0.62f,
            size.width * 0.68f, size.height * 0.62f,
            size.width * 0.68f, size.height * 0.92f,
        )
    }
    drawPath(body, tint, style = stroke)
    drawCircle(
        color = tint,
        radius = size.width * 0.13f,
        center = Offset(size.width * 0.76f, size.height * 0.34f),
        style = stroke,
    )
    val secondBody = Path().apply {
        moveTo(size.width * 0.72f, size.height * 0.62f)
        cubicTo(
            size.width * 0.94f, size.height * 0.66f,
            size.width * 0.94f, size.height * 0.78f,
            size.width * 0.94f, size.height * 0.92f,
        )
    }
    drawPath(secondBody, tint, style = stroke)
}
