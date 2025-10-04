package admin.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateStockRequest(
    val itemId: Int,
    val newStock: Int,
    val itemType: String // "PRODUCT" or "BUNDLE"
)
