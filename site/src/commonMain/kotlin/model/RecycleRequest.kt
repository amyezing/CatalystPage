package model

import kotlinx.serialization.Serializable

@Serializable
data class RecycleRequest(
    val userId: Int,
    val bottles: Int,
    val zoneId: Int? = null // now optional
)