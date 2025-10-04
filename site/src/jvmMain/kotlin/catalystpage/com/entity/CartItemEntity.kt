package catalystpage.com.entity

import catalystpage.com.model.CartItems
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CartItemEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CartItemEntity>(CartItems)

    var firebaseUid by CartItems.firebaseUid
    var quantity by CartItems.quantity
    var productVariantId by CartItems.productVariantId
}