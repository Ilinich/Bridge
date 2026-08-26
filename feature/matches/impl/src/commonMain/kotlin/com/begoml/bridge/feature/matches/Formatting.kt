package com.begoml.bridge.feature.matches

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private val MonthNames = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

/**
 * Renders a kick-off in the reader's own zone.
 *
 * Month names are spelled out here rather than concatenated from parts elsewhere, so adding a
 * second language means translating this table and nothing in the screens.
 */
fun Instant.formatKickoff(): String {
    val local = toLocalDateTime(TimeZone.currentSystemDefault())
    val month = MonthNames.getOrElse(local.month.ordinal) { "" }
    val hour = if (local.hour < 10) "0${local.hour}" else "${local.hour}"
    val minute = if (local.minute < 10) "0${local.minute}" else "${local.minute}"
    return "$month ${local.day} · $hour:$minute"
}

fun Instant.formatDay(): String {
    val local = toLocalDateTime(TimeZone.currentSystemDefault())
    val month = MonthNames.getOrElse(local.month.ordinal) { "" }
    return "${local.day} $month"
}

fun Instant.formatTime(): String {
    val local = toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = if (local.hour < 10) "0${local.hour}" else "${local.hour}"
    val minute = if (local.minute < 10) "0${local.minute}" else "${local.minute}"
    return "$hour:$minute"
}
