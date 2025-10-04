package model

import kotlinx.serialization.Serializable

@Serializable
data class RecycleResponse(
    val id: Int,
    val status: String
)

@Serializable
data class CommunityLifetimeResponse(val lifetimeTotal: Int)