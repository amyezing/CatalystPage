package catalystpage.com.model

import model.OrderStatus
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Orders : IntIdTable("orders") {
    val firebaseUid = varchar("firebase_uid", 128)
    val totalPrice = decimal("total_price", 10, 2)
    val status = enumerationByName("status", 50, OrderStatus::class).default(OrderStatus.Pending)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
}