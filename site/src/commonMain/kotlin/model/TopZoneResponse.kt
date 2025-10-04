package model

import kotlinx.serialization.Serializable

@Serializable
data class TopZoneResponse(
    val zoneName: String,
    val bottles: Int
)