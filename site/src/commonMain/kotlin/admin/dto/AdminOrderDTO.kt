package admin.dto

import kotlinx.serialization.Serializable

@Serializable
data class AdminOrderDTO(
    val id: Int,
    val firebaseUid: String,
    val userEmail: String,
    val totalPrice: Double,
    val status: String,
    val createdAt: String,
    val address: String? = null
)