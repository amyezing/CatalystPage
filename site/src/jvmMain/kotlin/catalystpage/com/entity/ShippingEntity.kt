package catalystpage.com.entity

import catalystpage.com.model.ShippingDetails
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ShippingDetailsEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ShippingDetailsEntity>(ShippingDetails)

    var order by OrderEntity referencedOn ShippingDetails.order
    var address by ShippingDetails.address
    var courier by ShippingDetails.courier
    var trackingNumber by ShippingDetails.trackingNumber
    var status by ShippingDetails.status
    var shippedAt by ShippingDetails.shippedAt
    var deliveredAt by ShippingDetails.deliveredAt
}

