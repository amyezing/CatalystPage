package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductVariantDTO(
    @SerialName("id")val id: Int,

    @SerialName("product_id")
    val productId: Int,

    @SerialName("quantity") val quantity: Int,
    val packSize: String? = null,
    @SerialName("price") val price: Double,
    @SerialName("stock") val stock: Int,
    @SerialName("size") val size: String? = null,
    val isAvailable: Boolean = true
)
