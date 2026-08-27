package com.begoml.bridge.foundation.resource

import kotlin.time.Clock
import kotlin.time.Instant

/** A clock a test moves by hand, so an age test states the age instead of waiting for it. */
class MovableClock(var millis: Long = 0L) : Clock {
    override fun now(): Instant = Instant.fromEpochMilliseconds(millis)
}
