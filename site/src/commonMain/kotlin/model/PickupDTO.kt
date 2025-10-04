package model

import kotlinx.serialization.Serializable

@Serializable
data class PickupDTO(
    val id: Int? = null,
    val orderId: Int,
    val phoneNumber: String,
    val status: PickupStatus = PickupStatus.Pending,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
enum class PickupStatus {
    Pending, Ready, PickedUp, Cancelled
}