package model

import kotlinx.serialization.Serializable

@Serializable
data class PendingRecyclingDTO(
    val id: Int,
    val userId: Int,
    val userName: String?, // nullable if user.name is null
    val bottles: Int,
    val zoneId: Int? = null,
    val monthYear: String,
    val createdAt: String
)