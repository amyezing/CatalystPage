package catalystpage.com.entity.admin

import catalystpage.com.model.admin.table.AdminShippingSummaryTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class AdminShippingSummaryEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AdminShippingSummaryEntity>(AdminShippingSummaryTable)

    var orderId by AdminShippingSummaryTable.orderId
    var courier by AdminShippingSummaryTable.courier
    var scheduledDate by AdminShippingSummaryTable.scheduledDate
    var trackingNumber by AdminShippingSummaryTable.trackingNumber
    var shippingFee by AdminShippingSummaryTable.shippingFee
    var notes by AdminShippingSummaryTable.notes
    var createdAt by AdminShippingSummaryTable.createdAt
    var updatedAt by AdminShippingSummaryTable.updatedAt
    var status by AdminShippingSummaryTable.status
}

