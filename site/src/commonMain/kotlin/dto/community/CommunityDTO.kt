package dto.community
import kotlinx.serialization.Serializable

@Serializable
data class ZoneDTO(
    val id: Int,
    val name: String,
    val description: String?
)

@Serializable
data class ZoneCityDTO(
    val id: Int,
    val zoneId: Int,
    val cityName: String
)

@Serializable
data class ZoneProgressDTO(
    val zoneId: Int,
    val monthYear: String,
    val totalBottles: Int
)

@Serializable
data class CommunityProgressDTO(
    val monthYear: String,
    val totalBottles: Int,

)

@Serializable
data class UserRecyclingDTO(
    val id: Int?,
    val userId: Int,
    val bottles: Int,
    val zoneId: Int?,
    val monthYear: String,
    val createdAt: String?
)

@Serializable
data class CommunityRewardDTO(
    val id: Int?,
    val userId: Int,
    val rewardType: String,
    val rewardValue: Int? = null,
    val note: String? = null,
    val monthYear: String
)

enum class RewardType {
    PriorityTester,
    FlavorRoyalty,
    EcoBoost
}

enum class RecyclingStatus {
    PENDING, CONFIRMED, REJECTED
}
