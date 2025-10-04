package dto

data class CartItemUI (
    val id: Int,
    val quantity: Int,
    val title: String,  // 🧠 Unified field
    val imageUrl: String,
    val packSize: Int,
    val packPrice: Int,
    val productVariantId: Int? = null,
    val isProduct: Boolean = false
)



