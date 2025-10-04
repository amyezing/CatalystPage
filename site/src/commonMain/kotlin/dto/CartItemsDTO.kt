package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartItemDTO(
    val id: Int? = null,

    @SerialName("firebase_uid")
    val firebaseUid: String,
    val quantity: Int = 1,
    @SerialName("product_variant_id")
    val productVariantId: Int? = null,
    val productName: String? = null,
    val price : Double? = null,
    val packSize: String? = null

)