package catalystpage.com.entity.admin

import catalystpage.com.model.admin.table.AdminOrderSummary
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID


class AdminOrderSummaryEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AdminOrderSummaryEntity>(AdminOrderSummary)

    var orderId by AdminOrderSummary.orderId
    var usersName by AdminOrderSummary.usersName
    var usersEmail by AdminOrderSummary.usersEmail
    var totalPrice by AdminOrderSummary.totalPrice
    var status by AdminOrderSummary.status
    var createdAt by AdminOrderSummary.createdAt
    var address by AdminOrderSummary.address
}