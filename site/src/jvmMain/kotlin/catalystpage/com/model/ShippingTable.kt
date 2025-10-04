package catalystpage.com.model

import dto.ShippingStatus
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object ShippingDetails : IntIdTable("shipping_details") {
    val order = reference("order_id", Orders)  // This is of type Column<EntityID<Int>>
    val address = text("address")
    val courier = varchar("courier", 255).nullable()
    val trackingNumber = varchar("tracking_number", 255).nullable()
    val status = enumerationByName("status", 20, ShippingStatus::class).default(ShippingStatus.Pending)
    val shippedAt = datetime("shipped_at").nullable()
    val deliveredAt = datetime("delivered_at").nullable()
}