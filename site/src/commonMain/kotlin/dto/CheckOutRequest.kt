package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckoutRequest(
    @SerialName("firebase_uid")
    val firebaseUid: String,

    @SerialName("items")
    val items: List<CartItemDTO>,

    @SerialName("address")
    val address: String,

    @SerialName("courier")
    val courier: String? = null

)