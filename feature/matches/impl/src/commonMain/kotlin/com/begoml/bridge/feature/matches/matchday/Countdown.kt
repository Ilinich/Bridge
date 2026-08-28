package com.begoml.bridge.feature.matches.matchday

private const val MillisPerSecond = 1000L
private const val SecondsPerMinute = 60L
private const val MinutesPerHour = 60L
private const val HoursPerDay = 24L

data class Countdown(val days: Long, val hours: Long, val minutes: Long, val seconds: Long) {

    val hasStarted: Boolean get() = days == 0L && hours == 0L && minutes == 0L && seconds == 0L

    companion object {

        fun between(nowMillis: Long, kickoffMillis: Long): Countdown {
            val remaining = ((kickoffMillis - nowMillis) / MillisPerSecond).coerceAtLeast(0L)
            return Countdown(
                days = remaining / (SecondsPerMinute * MinutesPerHour * HoursPerDay),
                hours = remaining / (SecondsPerMinute * MinutesPerHour) % HoursPerDay,
                minutes = remaining / SecondsPerMinute % MinutesPerHour,
                seconds = remaining % SecondsPerMinute,
            )
        }
    }
}

fun Long.pad2(): String = if (this < 10) "0$this" else toString()
