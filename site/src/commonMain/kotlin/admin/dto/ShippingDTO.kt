package admin.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShippingDTO(
    val id: Int,
    val orderId: Int,
    val courier: String?,
    val scheduledDate: String?,
    val trackingNumber: String?,
    val shippingFee: Double,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String,
    val status: ShippingSummary


)
@Serializable
enum class ShippingSummary {
    @SerialName("pending") pending,
   @SerialName("ready" )ready
}