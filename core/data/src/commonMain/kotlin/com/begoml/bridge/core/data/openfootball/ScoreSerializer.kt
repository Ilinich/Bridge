package com.begoml.bridge.core.data.openfootball

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

private const val ScorePairSize = 2

/**
 * Reads the two shapes `score` arrives in.
 *
 * The feed carries both `{"ft": [4, 2], "ht": [1, 0]}` and a bare `[0, 0]` for the same field, so
 * a single generated deserialiser cannot cover it. Anything else — absent, null, a half-written
 * pair — reads as no score rather than as zero-zero, because "not played yet" and "goalless draw"
 * must not collapse into the same value.
 */
internal object FullTimeScoreSerializer : KSerializer<List<Int>?> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FullTimeScore")

    override fun deserialize(decoder: Decoder): List<Int>? {
        val jsonDecoder = decoder as? JsonDecoder ?: return null
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonArray -> element.toScore()
            is JsonObject -> (element["ft"] as? JsonArray)?.toScore()
            else -> null
        }
    }

    override fun serialize(encoder: Encoder, value: List<Int>?) {
        error("Bridge only reads this feed")
    }

    private fun JsonArray.toScore(): List<Int>? {
        val goals = mapNotNull { (it as? JsonPrimitive)?.intOrNull }
        return goals.takeIf { it.size == ScorePairSize }
    }
}
