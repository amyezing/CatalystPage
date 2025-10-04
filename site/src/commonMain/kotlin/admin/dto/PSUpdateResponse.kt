package admin.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaymentStatusUpdateResponse(
    val success: Boolean,
    val message: String
)