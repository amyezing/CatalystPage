package model

import dto.ShippingStatus
import kotlinx.serialization.Serializable

@Serializable
data class UpdateShippingStatusRequest(
    val orderId: Int,
    val status: ShippingStatus,
    val trackingNumber: String? = null,
    val courier: String? = null
)