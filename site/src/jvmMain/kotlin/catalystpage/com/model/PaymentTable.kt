package catalystpage.com.model

import dto.PaymentMethod
import dto.PaymentStatus
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Payments : IntIdTable("payments") {
    val order = reference("order_id", Orders)
    val amount = decimal("amount", 10, 2).nullable()
    val paymentMethod = customEnumeration(
        "payment_method",
        "ENUM('Gcash','Bank Transfer','Credit Card','COD')",
        { value -> PaymentMethod.fromDatabase(value as String) },
        { it -> PaymentMethod.toDatabase(it) }
    )
    val referenceNumber = varchar("reference_number", 255).nullable()
    val status = customEnumeration(
        name = "status",
        sql = "ENUM('PENDING','APPROVED','REJECTED')",
        fromDb = { value -> PaymentStatus.fromDatabase(value as String) },
        toDb = { it -> PaymentStatus.toDatabase(it) }
    ).default(PaymentStatus.PENDING)
    val proofImage = text("proof_image").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val statusUpdatedAt = timestamp("status_updated_at").nullable()
}