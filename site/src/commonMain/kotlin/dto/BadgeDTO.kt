package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BadgeDTO(
    val id: String,
    val name: String,
    val description: String = "",
    @SerialName("icon_url") val iconUrl: String = "",
    val reward: String = "",
    @SerialName("eco_points") val ecoPoints: Int = 0,
    val unlocked: Boolean = false,
    @SerialName("unlocked_at") val unlockedAt: String? = null
)