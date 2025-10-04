package model

import kotlinx.serialization.Serializable

@Serializable
data class CreatePickupRequest(
    val orderId: Int,
    val phoneNumber: String
)

@Serializable
data class UpdatePickupStatusRequest(
    val pickupId: Int,
    val status: PickupStatus
)