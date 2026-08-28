package com.begoml.bridge.feature.club

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bridge.feature.club.impl.generated.resources.Res
import bridge.feature.club.impl.generated.resources.club_about
import bridge.feature.club.impl.generated.resources.club_capacity
import bridge.feature.club.impl.generated.resources.club_colours
import bridge.feature.club.impl.generated.resources.club_founded
import bridge.feature.club.impl.generated.resources.club_ground
import bridge.feature.club.impl.generated.resources.club_instagram
import bridge.feature.club.impl.generated.resources.club_links
import bridge.feature.club.impl.generated.resources.club_location
import bridge.feature.club.impl.generated.resources.club_media
import bridge.feature.club.impl.generated.resources.club_opened
import bridge.feature.club.impl.generated.resources.club_twitter
import bridge.feature.club.impl.generated.resources.club_website
import bridge.feature.club.impl.generated.resources.club_youtube
import com.begoml.bridge.core.domain.model.Club
import com.begoml.bridge.core.domain.model.Venue
import com.begoml.bridge.core.domain.model.FollowedClub
import com.begoml.bridge.core.domain.repository.ClubRepository
import com.begoml.bridge.foundation.resource.Loadable
import com.begoml.bridge.uikit.groupedThousands
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
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString

/** Every fixed word on the club screen, resolved once away from the composition. */
data class ClubLabels(
    val about: String,
    val media: String,
    val ground: String,
    val links: String,
    val founded: String,
    val colours: String,
    val capacity: String,
    val opened: String,
    val location: String,
    val website: String,
    val youtube: String,
    val twitter: String,
    val instagram: String,
)

/** The club as the screen draws it: figures already formatted, colours already parsed. */
data class ClubUi(
    val name: String,
    val code: String,
    val badgeUrl: String?,
    val backdropUrl: String?,
    val nicknames: String?,
    val founded: String?,
    val description: String?,
    val colours: ImmutableList<String>,
    val links: ImmutableList<ClubLinkUi>,
)

data class ClubLinkUi(val label: String, val url: String)

data class GroundUi(
    val name: String,
    val thumbUrl: String?,
    val capacity: String?,
    val opened: String?,
    val location: String?,
    val description: String?,
)

data class ClubUiState(
    val club: ClubUi? = null,
    val ground: GroundUi? = null,
    val isLoading: Boolean = true,
    val error: Throwable? = null,
)

internal class ClubViewModel(
    scope: CoroutineScope,
    private val repository: ClubRepository,
    private val club: FollowedClub,
    val labels: ClubLabels,
    private val ioDispatcher: CoroutineDispatcher,
    private val analytics: Analytics,
) : ViewModel(scope),
    UiStateDelegate<ClubUiState> by UiStateDelegateImpl(ClubUiState()) {

    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            // Two requests rather than one combined source: the ground can only be asked about
            // once the club record says which ground it is, and the profile must render without
            // waiting for that second answer.
            launch {
                repository.club(club.id).collect { loadable ->
                    val mapped = (loadable as? Loadable.Content)
                        ?.let { withContext(ioDispatcher) { it.value.toUi(labels) } }
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
                        ?.let { withContext(ioDispatcher) { it.value.toUi() } } ?: return@collect
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
        refreshJob = viewModelScope.launch { repository.refresh(club.id) }
    }

    private fun Club.toUi(labels: ClubLabels) = ClubUi(
        name = name,
        code = code,
        badgeUrl = media.badgeUrl,
        backdropUrl = media.fanartUrls.lastOrNull(),
        nicknames = details.nicknames.takeIf { it.isNotEmpty() }?.joinToString(" · "),
        founded = foundedYear?.toString(),
        description = description,
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
        description = description,
    )

}

/** Read once for the whole run: the words do not change while the app is open. */
suspend fun loadClubLabels() = ClubLabels(
    about = getString(Res.string.club_about),
    media = getString(Res.string.club_media),
    ground = getString(Res.string.club_ground),
    links = getString(Res.string.club_links),
    founded = getString(Res.string.club_founded),
    colours = getString(Res.string.club_colours),
    capacity = getString(Res.string.club_capacity),
    opened = getString(Res.string.club_opened),
    location = getString(Res.string.club_location),
    website = getString(Res.string.club_website),
    youtube = getString(Res.string.club_youtube),
    twitter = getString(Res.string.club_twitter),
    instagram = getString(Res.string.club_instagram),
)
