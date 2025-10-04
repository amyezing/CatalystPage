package model

import kotlinx.serialization.Serializable

@Serializable
data class CommunityTotalResponse(
    val monthYear: String,
    val totalBottles: Int
)