package com.begoml.bridge.core.data.remote.openfootball

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class SeasonEnvelope(
    @SerialName("name") val name: String? = null,
    @SerialName("matches") val matches: List<SeasonMatchDto> = emptyList(),
)

@Serializable
internal class SeasonMatchDto(
    @SerialName("round") val round: String? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("time") val time: String? = null,
    @SerialName("team1") val homeTeam: String? = null,
    @SerialName("team2") val awayTeam: String? = null,
    @Serializable(with = FullTimeScoreSerializer::class)
    @SerialName("score") val score: List<Int>? = null,
)
