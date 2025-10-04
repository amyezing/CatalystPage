package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserEcoPointsDTO(
    @SerialName("user_id") val userId: Int,
    val points: Int,
    val updatedAt: String
)