package com.begoml.bridge.feature.club

import com.begoml.bridge.core.domain.model.Club
import com.begoml.bridge.core.domain.model.Loadable
import com.begoml.bridge.core.domain.model.Venue
import com.begoml.bridge.core.domain.model.FollowedClub
import com.begoml.bridge.core.domain.repository.ClubRepository
import com.begoml.bridge.foundation.tessera.UiStateDelegate
import com.begoml.bridge.foundation.tessera.UiStateDelegateImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class ClubState(
    val club: Club? = null,
    val venue: Venue? = null,
    val isLoading: Boolean = true,
    val error: Throwable? = null,
)

sealed interface ClubEvent

/**
 * The club profile and its ground, gathered from two requests.
 *
 * The ground is a separate lookup that only becomes possible once the club record says which
 * ground to ask about, so the two are collected independently rather than combined — the profile
 * must render without waiting for the second request to come back.
 */
class ClubDelegate(
    private val scope: CoroutineScope,
    private val repository: ClubRepository,
    private val club: FollowedClub,
) : UiStateDelegate<ClubState, ClubEvent> by UiStateDelegateImpl(ClubState()) {

    /** A forced refresh holds the syncer's key mutex across the network, so only one may run. */
    private var refreshJob: Job? = null

    init {
        observe()
    }

    fun retry() {
        if (refreshJob?.isActive == true) return
        refreshJob = scope.launch { repository.refresh(club.id) }
    }

    private fun observe() {
        scope.launch {
            repository.club(club.id).collect { loadable ->
                updateUiState { state ->
                    when (loadable) {
                        is Loadable.Loading -> state.copy(isLoading = true)
                        is Loadable.Failed -> state.copy(isLoading = false, error = loadable.error)
                        is Loadable.Content -> state.copy(
                            club = loadable.value,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
            }
        }
        scope.launch {
            repository.venue(club.id).collect { loadable ->
                if (loadable is Loadable.Content) {
                    updateUiState { state -> state.copy(venue = loadable.value) }
                }
            }
        }
    }
}
