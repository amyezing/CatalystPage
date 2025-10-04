package dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderDTO(
    val id: Int,
    val firebaseUid: String,
    val totalPrice: Double,
    val status: String,
    val createdAt: String
)