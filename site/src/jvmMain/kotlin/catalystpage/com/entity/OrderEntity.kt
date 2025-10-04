package catalystpage.com.entity

import catalystpage.com.model.OrderItems
import catalystpage.com.model.Orders
import dto.OrderDTO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class OrderEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<OrderEntity>(Orders)

    var firebaseUid by Orders.firebaseUid
    var totalPrice by Orders.totalPrice
    var status by Orders.status
    var createdAt by Orders.createdAt

}

fun OrderEntity.toDTO(): OrderDTO = OrderDTO(
    id = this.id.value,
    firebaseUid = this.firebaseUid, // Extract the string
    totalPrice = this.totalPrice.toDouble(),
    status = this.status.name,
    createdAt = this.createdAt.toString()
)

class OrderItemEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<OrderItemEntity>(OrderItems)

    var order by OrderEntity referencedOn OrderItems.order
    var productVariantId by OrderItems.productVariant
    var quantity by OrderItems.quantity
    var price by OrderItems.price
}