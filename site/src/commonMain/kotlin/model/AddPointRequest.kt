package model

import kotlinx.serialization.Serializable

@Serializable
data class AddPointsRequest(val points: Int, val reason: String? = null)