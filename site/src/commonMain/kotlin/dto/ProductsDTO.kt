package dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDTO(
    val id: Int,
    val name: String,
    val description: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,  // You can convert to `Instant` if needed

    val type: ProductType = ProductType.SINGLE,
    val price: Double? = null,
    val variants: List<ProductVariantDTO> = emptyList(),
    val labels: List<LabelDTO> = emptyList(),
    val isAvailable: Boolean = true
)

@Serializable
enum class ProductType {
    @SerialName("single") SINGLE,
    @SerialName("bundle") BUNDLE
}
