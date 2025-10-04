package dto

import kotlinx.serialization.Serializable

@Serializable
data class ShippingDetailsDTO(
    val id: Int? = null,
    val orderId: Int,
    val address: String,
    val courier: String? = null,
    val trackingNumber: String? = null,
    val status: ShippingStatus = ShippingStatus.Pending,
    val shippedAt: String? = null,
    val deliveredAt: String? = null
)

@Serializable
enum class ShippingStatus {
    Pending, Shipped, Delivered, Cancelled
}