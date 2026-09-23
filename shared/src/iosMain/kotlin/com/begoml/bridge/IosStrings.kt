package com.begoml.bridge

import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

/**
 * The host's own words, handed to Swift as descriptions.
 *
 * The four tabs are named once, in the shared bundle, and both bars read the same keys. Swift
 * cannot call moko's `desc()` itself — a Kotlin extension on a library type is not exported — so
 * the framework offers the descriptions ready-made.
 */
object IosStrings {

    val tabMatchday: StringDesc get() = HostStrings.strings.tab_matchday.desc()

    val tabSeason: StringDesc get() = HostStrings.strings.tab_season.desc()

    val tabSquad: StringDesc get() = HostStrings.strings.tab_squad.desc()

    val tabClub: StringDesc get() = HostStrings.strings.tab_club.desc()
}
