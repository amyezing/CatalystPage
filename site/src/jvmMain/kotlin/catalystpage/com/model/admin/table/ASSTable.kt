package catalystpage.com.model.admin.table

import admin.dto.ShippingDTO
import admin.dto.ShippingSummary
import catalystpage.com.entity.admin.AdminShippingSummaryEntity
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object AdminShippingSummaryTable : IntIdTable("admin_shipping_summary") {
    val orderId = integer("order_id") // FK to orders.id, not the view
    val courier = varchar("courier", 100).nullable()
    val scheduledDate = date("scheduled_date").nullable()
    val trackingNumber = varchar("tracking_number", 255).nullable()
    val shippingFee = decimal("shipping_fee", 10, 2)
    val notes = text("notes").nullable()
    val createdAt = datetime("created_at").nullable()
    val updatedAt = datetime("updated_at").nullable()
    val status = enumerationByName("status", 20, ShippingSummary::class)
        .default(ShippingSummary.pending)
}
fun AdminShippingSummaryEntity.toDTO() = ShippingDTO(
    id = id.value,
    orderId = orderId,
    courier = courier,
    scheduledDate = scheduledDate?.toString(),
    trackingNumber = trackingNumber,
    shippingFee = shippingFee.toDouble(),
    notes = notes,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    status = status
)