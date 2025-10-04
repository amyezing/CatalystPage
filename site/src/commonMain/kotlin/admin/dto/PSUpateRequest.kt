package admin.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaymentStatusUpdateRequest(
    val orderId: Int,
    val status: String
)