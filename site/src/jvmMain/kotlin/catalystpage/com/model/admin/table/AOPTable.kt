package catalystpage.com.model.admin.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.timestamp

object AdminPaymentSummary : Table("admin_payment_summary") {
    val orderId = integer("order_id")
    val usersName = varchar("users_name", 100).nullable()
    val amount = decimal("amount", 10, 2)
    val paymentMethod = varchar("payment_method", 50)
    val proofImage = text("proof_image").nullable()
    val status = varchar("status", 20).nullable()
    val paymentDate = datetime("payment_date").nullable()
    val statusUpdatedAt = timestamp("status_updated_at").nullable()
}