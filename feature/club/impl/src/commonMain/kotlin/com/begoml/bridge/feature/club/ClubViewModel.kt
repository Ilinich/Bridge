package com.begoml.bridge.feature.club

import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import com.begoml.bridge.foundation.logger.Logger
import com.begoml.bridge.foundation.coroutines.safeLaunch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.begoml.bridge.core.domain.model.Club
import com.begoml.bridge.core.domain.model.Venue
import com.begoml.bridge.core.domain.model.FollowedClub
import com.begoml.bridge.core.domain.repository.ClubRepository
import com.begoml.bridge.foundation.resource.Loadable
import com.begoml.bridge.foundation.format.groupedThousands
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import com.begoml.bridge.core.analytics.Analytics
import com.begoml.bridge.feature.club.analytics.VideoStarted
import com.begoml.bridge.foundation.tessera.UiStateDelegate
import com.begoml.bridge.foundation.tessera.UiStateDelegateImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val Tag = "Club"

/** Every fixed word on the club screen, resolved once away from the composition. */
/**
 * Every fixed word on this screen — as descriptions, not as words.
 *
 * A [StringDesc] names which string is meant; the platform that draws it decides what that reads
 * like, because only it knows the device's locale. Nothing is resolved here, so nothing has to be
 * awaited before the first frame.
 */
data class ClubLabels(
    val about: StringDesc = ClubStrings.strings.club_about.desc(),
    val media: StringDesc = ClubStrings.strings.club_media.desc(),
    val ground: StringDesc = ClubStrings.strings.club_ground.desc(),
    val links: StringDesc = ClubStrings.strings.club_links.desc(),
    val founded: StringDesc = ClubStrings.strings.club_founded.desc(),
    val colours: StringDesc = ClubStrings.strings.club_colours.desc(),
    val capacity: StringDesc = ClubStrings.strings.club_capacity.desc(),
    val opened: StringDesc = ClubStrings.strings.club_opened.desc(),
    val location: StringDesc = ClubStrings.strings.club_location.desc(),
    val website: StringDesc = ClubStrings.strings.club_website.desc(),
    val youtube: StringDesc = ClubStrings.strings.club_youtube.desc(),
    val twitter: StringDesc = ClubStrings.strings.club_twitter.desc(),
    val instagram: StringDesc = ClubStrings.strings.club_instagram.desc(),
)

/**
 * The club as the screen draws it: figures already formatted, colours already parsed.
 *
 * The prose is `summary` rather than `description` because Swift already has that name — every
 * object inherits `description` from NSObject, and a Kotlin field of that name is shadowed by it.
 */
data class ClubUi(
    val name: String,
    val code: String,
    val badgeUrl: String?,
    val backdropUrl: String?,
    val nicknames: String?,
    val founded: String?,
    val summary: String?,
    val colours: ImmutableList<String>,
    val links: ImmutableList<ClubLinkUi>,
)

data class ClubLinkUi(val label: StringDesc, val url: String)

data class GroundUi(
    val name: String,
    val thumbUrl: String?,
    val capacity: String?,
    val opened: String?,
    val location: String?,
    val summary: String?,
)

data class ClubUiState(
    val labels: ClubLabels = ClubLabels(),
    val club: ClubUi? = null,
    val ground: GroundUi? = null,
    val isLoading: Boolean = true,
    val error: Throwable? = null,
)

/**
 * Public because a platform UI is the thing that reads it: on iOS the screen is Swift,
 * and a Swift view cannot see an internal Kotlin class.
 */
class ClubViewModel(
    scope: CoroutineScope,
    private val repository: ClubRepository,
    private val club: FollowedClub,
    private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger,
    private val analytics: Analytics,
) : ViewModel(scope),
    UiStateDelegate<ClubUiState> by UiStateDelegateImpl(ClubUiState()) {

    private var refreshJob: Job? = null

    init {
        viewModelScope.safeLaunch(dispatcher = ioDispatcher, logger = logger, tag = Tag) {
            // Two requests rather than one combined source: the ground can only be asked about
            // once the club record says which ground it is, and the profile must render without
            // waiting for that second answer.
            launch {
                repository.club(club.id).collect { loadable ->
                    val mapped = (loadable as? Loadable.Content)
                        ?.let { it.value.toUi() }
                    updateUiState { state ->
                        state.copy(
                            club = mapped ?: state.club,
                            isLoading = loadable is Loadable.Loading,
                            error = (loadable as? Loadable.Failed)?.error,
                        )
                    }
                }
            }
            launch {
                repository.venue(club.id).collect { loadable ->
                    val ground = (loadable as? Loadable.Content)
                        ?.let { it.value.toUi() } ?: return@collect
                    updateUiState { state -> state.copy(ground = ground) }
                }
            }
        }
    }


    fun onVideoStarted() {
        analytics.track(VideoStarted(source = "club_media"))
    }

    /** A forced refresh holds the syncer's key mutex across the network, so only one may run. */
    fun retry() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.safeLaunch(
            dispatcher = ioDispatcher,
            logger = logger,
            tag = Tag,
        ) { repository.refresh(club.id) }
    }

    private fun Club.toUi(labels: ClubLabels = ClubLabels()) = ClubUi(
        name = name,
        code = code,
        badgeUrl = media.badgeUrl,
        backdropUrl = media.fanartUrls.lastOrNull(),
        nicknames = details.nicknames.takeIf { it.isNotEmpty() }?.joinToString(" · "),
        founded = foundedYear?.toString(),
        summary = description,
        colours = listOfNotNull(
            details.colours.primary,
            details.colours.secondary,
            details.colours.tertiary,
        ).toImmutableList(),
        links = listOfNotNull(
            details.links.website?.let { ClubLinkUi(labels.website, it) },
            details.links.youtube?.let { ClubLinkUi(labels.youtube, it) },
            details.links.twitter?.let { ClubLinkUi(labels.twitter, it) },
            details.links.instagram?.let { ClubLinkUi(labels.instagram, it) },
        ).toImmutableList(),
    )

    private fun Venue.toUi() = GroundUi(
        name = name,
        thumbUrl = thumbUrl,
        capacity = capacity?.groupedThousands(),
        opened = openedYear?.toString(),
        location = location,
        summary = description,
    )

}
