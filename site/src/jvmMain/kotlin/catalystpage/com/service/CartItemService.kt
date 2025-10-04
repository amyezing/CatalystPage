package catalystpage.com.service

import catalystpage.com.entity.CartItemEntity
import catalystpage.com.model.CartItems
import catalystpage.com.model.toDTO
import dto.CartItemDTO
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class CartItemService {

    fun getCartItemsByUser(
        firebaseUid: String,
        page: Int,
        limit: Int,
        productVariantId: Int? = null
    ): List<CartItemDTO> = transaction {
        val conditions = mutableListOf<Op<Boolean>>(
            CartItems.firebaseUid eq firebaseUid
        )

        productVariantId?.let {
            conditions += (CartItems.productVariantId eq it)
        }

        // Combine conditions using AND
        val finalCondition = conditions.reduce { acc, condition -> acc and condition }

        CartItemEntity.find { finalCondition }
            .limit(limit, offset = ((page - 1) * limit).toLong())
            .map { it.toDTO() }
    }

    fun addToCart(item: CartItemDTO): CartItemDTO = transaction {
        CartItemEntity.new {
            this.firebaseUid = item.firebaseUid
            this.quantity = item.quantity
            this.productVariantId = item.productVariantId
        }.toDTO()
    }

    fun updateQuantity(id: Int, quantity: Int): Boolean = transaction {
        CartItemEntity.findById(id)?.let {
            it.quantity = quantity
            true
        } ?: false
    }

    fun deleteCartItem(id: Int): Boolean = transaction {
        CartItemEntity.findById(id)?.let {
            it.delete()
            true
        } ?: false
    }

    fun deleteAllForUser(firebaseUid: String): Int = transaction {
        val items = CartItemEntity.find { CartItems.firebaseUid eq firebaseUid }
        val count = items.count().toInt() // 👈 convert Long to Int
        items.forEach { it.delete() }
        count
    }

}