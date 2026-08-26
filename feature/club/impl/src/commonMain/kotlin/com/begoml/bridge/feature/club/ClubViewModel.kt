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
import com.begoml.bridge.core.data.model.Club
import com.begoml.bridge.core.data.model.Venue
import com.begoml.bridge.core.data.repository.ClubRepository
import com.begoml.bridge.foundation.analytics.Analytics
import com.begoml.bridge.foundation.analytics.AnalyticsEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

data class ClubUiState(
    val club: Club? = null,
    val venue: Venue? = null,
    /**
     * Null until the labels have been read.
     *
     * The screen treats that as part of loading rather than rendering blank headings for a frame:
     * resolving strings off the composition means they are not available synchronously.
     */
    val labels: ClubLabels? = null,
    val isLoading: Boolean = true,
    val error: Throwable? = null,
)

/** How long a state flow outlives its last collector, so a rotation does not refetch. */
private const val SubscriptionTimeoutMillis = 5_000L

internal class ClubViewModel(
    repository: ClubRepository,
    private val ioDispatcher: CoroutineDispatcher,
    private val analytics: Analytics,
) : ViewModel() {

    private val delegate = ClubDelegate(scope = viewModelScope, repository = repository)
    private val labels = MutableStateFlow<ClubLabels?>(null)

    val state: StateFlow<ClubUiState> =
        combine(delegate.uiStateFlow, labels) { content, resolved ->
            ClubUiState(
                club = content.club,
                venue = content.venue,
                labels = resolved,
                isLoading = content.isLoading || resolved == null,
                error = content.error,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SubscriptionTimeoutMillis), ClubUiState())

    init {
        viewModelScope.launch { labels.value = withContext(ioDispatcher) { readLabels() } }
    }

    fun onVideoStarted() {
        analytics.track(AnalyticsEvent.VideoStarted(source = "club_media"))
    }

    fun retry() {
        delegate.retry()
    }

    private suspend fun readLabels() = ClubLabels(
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
}
