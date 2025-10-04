package catalystpage.com.model.admin.table

import model.OrderStatus
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object AdminOrderSummary : IntIdTable("admin_order_summary") {
    val orderId = integer("order_id")
    val usersName = varchar("users_name", 100).nullable()
    val usersEmail = varchar("users_email", 255).nullable()
    val totalPrice = decimal("total_price", 10, 2)
    val status = enumerationByName("status", 20, OrderStatus::class).nullable()
    val createdAt = timestamp("created_at").nullable()
    val address = text("shipping_address").nullable()
}

