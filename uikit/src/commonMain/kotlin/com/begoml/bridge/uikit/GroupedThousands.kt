package com.begoml.bridge.uikit

/** Splits a number into thousands with a thin space, the way scores and capacities are read. */
fun Int.groupedThousands(): String {
    val text = toString()
    if (text.length <= GroupSize) return text
    return text.reversed().chunked(GroupSize).joinToString(" ").reversed()
}

private const val GroupSize = 3
