package com.begoml.bridge.core.domain.model

/**
 * The club this build follows.
 *
 * It is a value in the graph rather than a constant baked into the repositories, so that asking
 * for a club stays a request with a subject: the repositories answer about whichever club they are
 * asked about, and the one this app happens to follow is decided in a single place at the top. A
 * fork changes this value; nothing under it changes at all.
 */
data class FollowedClub(val id: String, val name: String)
