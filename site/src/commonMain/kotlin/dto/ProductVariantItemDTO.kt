package dto

import kotlinx.serialization.Serializable


@Serializable
data class ProductVariantItemDTO(
    val productVariantId: Int,
    val quantity: Int,
    val productName: String,
    val size: String,
    val imageUrl: String?
)