package dto

import kotlinx.serialization.Serializable

@Serializable
data class LowStockItemDTO(
    val itemId: Int,
    val itemName: String,
    val size: String,
    val quantity: Int,
    val stock: Int,
    val itemType: String
)