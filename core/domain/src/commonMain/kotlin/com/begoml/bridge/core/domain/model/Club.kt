package com.begoml.bridge.core.domain.model

/**
 * The club's own colours, as the feed states them.
 *
 * Reading them rather than hardcoding a hex value is what lets a fork of this app follow another
 * club without touching the theme.
 */
data class ClubColours(
    val primary: String?,
    val secondary: String?,
    val tertiary: String?,
)

data class ClubMedia(
    val badgeUrl: String?,
    val logoUrl: String?,
    val bannerUrl: String?,
    val fanartUrls: List<String>,
)

data class ClubLinks(
    val website: String?,
    val youtube: String?,
    val twitter: String?,
    val instagram: String?,
)

data class ClubDetails(
    val nicknames: List<String>,
    val colours: ClubColours,
    val links: ClubLinks,
    val venueId: String?,
)

data class Venue(
    val id: String,
    val name: String,
    val description: String?,
    val capacity: Int?,
    val openedYear: Int?,
    val location: String?,
    val thumbUrl: String?,
    val fanartUrl: String?,
    val map: String?,
    val website: String?,
)
