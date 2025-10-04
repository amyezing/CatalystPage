package admin.dto

import kotlinx.serialization.Serializable

@Serializable
data class AdminPaymentSummaryDTO(
    val orderId: Int,
    val usersName: String?,
    val amount: Double,
    val paymentMethod: String,
    val proofImage: String?,
    val status: String,
    val paymentDate: String,
    val statusUpdatedAt: String?
)